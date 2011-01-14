package org.integratedmodelling.modelling.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.integratedmodelling.corescience.CoreScience;
import org.integratedmodelling.corescience.interfaces.IContext;
import org.integratedmodelling.corescience.interfaces.IExtent;
import org.integratedmodelling.corescience.interfaces.IObservation;
import org.integratedmodelling.corescience.interfaces.internal.Topology;
import org.integratedmodelling.corescience.literals.DistributionValue;
import org.integratedmodelling.corescience.metadata.Metadata;
import org.integratedmodelling.modelling.ModellingPlugin;
import org.integratedmodelling.modelling.ObservationCache;
import org.integratedmodelling.modelling.ObservationFactory;
import org.integratedmodelling.modelling.annotation.ModelAnnotation;
import org.integratedmodelling.modelling.context.Context;
import org.integratedmodelling.modelling.exceptions.ThinklabModelException;
import org.integratedmodelling.modelling.interfaces.IModel;
import org.integratedmodelling.modelling.interfaces.IModelForm;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.Thinklab;
import org.integratedmodelling.thinklab.constraint.Constraint;
import org.integratedmodelling.thinklab.constraint.DefaultConformance;
import org.integratedmodelling.thinklab.constraint.Restriction;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.interfaces.applications.ISession;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;
import org.integratedmodelling.thinklab.interfaces.knowledge.datastructures.IntelligentMap;
import org.integratedmodelling.thinklab.interfaces.query.IConformance;
import org.integratedmodelling.thinklab.interfaces.query.IQueryResult;
import org.integratedmodelling.thinklab.interfaces.storage.IKBox;
import org.integratedmodelling.thinklab.kbox.GroupingQueryResult;
import org.integratedmodelling.thinklab.owlapi.Session;
import org.integratedmodelling.utils.CamelCase;
import org.integratedmodelling.utils.MiscUtilities;
import org.integratedmodelling.utils.Path;
import org.integratedmodelling.utils.Polylist;

import clojure.lang.IFn;

public abstract class DefaultAbstractModel implements IModel {

	protected IModel mediated = null;
	protected ArrayList<IModel> dependents = new ArrayList<IModel>();
	protected ArrayList<IModel> observed = new ArrayList<IModel>();

	/*
	 * not null if this model has been generated by applying a scenario to
	 * another model.
	 */
	protected Scenario scenario = null;
	protected IConcept observable = null;
	protected String observableId = null;

	// this is the defmodel <name>, complete with namespace (slash-separated)
	protected String name = null;

	protected Polylist observableSpecs = null;
	protected Object state = null;
	protected DistributionValue distribution = null;
	protected String id = null;
	protected String description = null;
	protected IFn whenClause = null;
	private LinkedList<Polylist> transformerQueue = new LinkedList<Polylist>();
	protected boolean mediatesExternal;
	private boolean _validated = false;

	/*
	 * if the model was declared entifiable, this is the agent type that will
	 * incarnate the entities that can be produced from an observation of it.
	 */
	private String entityAgent = null;

	/*
	 * Any clause not intercepted by applyClause becomes metadata, which is
	 * communicated to the observation created.
	 */
	protected Metadata metadata = new Metadata();

	protected boolean isOptional = false;

	/*
	 * if scenarios can be applied to this model, the content of editable will
	 * be non-null and they will specify how we can be edited (e.g. a range of
	 * values for a state, or simply "true" for any edit).
	 */
	protected Object editable = null;
	protected String namespace;
	private String localFormalName = null;

	protected boolean isMediating() {
		return mediated != null || mediatesExternal;
	}

	public String getObservableId() {
		return observableId;
	}

	public void setMetadata(String kw, Object value) {
		metadata.put(kw.startsWith(":") ? kw.substring(1) : kw, value);
	}

	public void setName(String name) {
		String[] x = name.split("/");
		this.name = name;
		this.namespace = x[0];
		this.id = x[1];
	}

	@Override
	public Collection<IModel> getDependencies() {
		return dependents;
	}

	@Override
	public String getNamespace() {
		return this.namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}

	/**
	 * This one is invoked once before any use is made of the model, and is
	 * supposed to validate all concepts used in the model's definition. In
	 * order to allow facilitated and automated annotation, no model should
	 * perform concept validation at declaration; all validation should be done
	 * within this function.
	 * 
	 * Validation of concepts should be done using annotateConcept() so that
	 * annotation mode will be enabled.
	 * 
	 * @param session
	 *            TODO
	 * 
	 * @throws ThinklabException
	 */
	protected abstract void validateSemantics(ISession session)
			throws ThinklabException;

	protected IConcept annotateConcept(String conceptId, ISession session,
			Object potentialParent) throws ThinklabValidationException {

		IConcept c = KnowledgeManager.get().retrieveConcept(conceptId);

		if (c == null) {

			ModelAnnotation an = (ModelAnnotation) session
					.getVariable(ModellingPlugin.ANNOTATION_UNDERWAY);
			if (an != null) {
				an.addConcept(conceptId, potentialParent == null ? null
						: potentialParent.toString());
			} else {
				throw new ThinklabValidationException(
						"model: concept "
								+ conceptId
								+ " is undefined: please annotate this model or load knowledge");
			}
		}
		return c;
	}

	public void setObservable(Object observableOrModel)
			throws ThinklabException {

		if (observableOrModel instanceof IModel) {
			this.mediated = (IModel) observableOrModel;
			this.observableId = ((DefaultAbstractModel) observableOrModel).observableId;
			this.observableSpecs = ((DefaultAbstractModel) observableOrModel).observableSpecs;
		} else if (observableOrModel instanceof IConcept) {
			this.observable = (IConcept) observableOrModel;
			this.observableSpecs = Polylist.list(this.observable);
			this.observableId = this.observable.toString();
		} else if (observableOrModel instanceof Polylist) {
			this.observableSpecs = (Polylist) observableOrModel;
			this.observableId = this.observableSpecs.first().toString();
		} else {
			this.observableId = observableOrModel.toString();
//			this.observable = KnowledgeManager.get().requireConcept(
//					this.observableId);
//			// this takes care of the self-annotating models with the $ at the
//			// end
//			this.observableId = this.observable.toString();
			this.observableSpecs = Polylist.list(this.observableId);
		}

		id = Path.getLast(observableId.endsWith("$") ? observableId.substring(0, observableId.length()-2) : observableId, ':').toLowerCase();
	}

	@Override
	public void applyClause(String keyword, Object argument)
			throws ThinklabException {

		// System.out.println(this + "processing clause " + keyword + " -> " +
		// argument);

		if (keyword.equals(":context")) {
			Collection<?> c = (Collection<?>) argument;
			for (Object o : c) {
				addDependentModel((IModel) o);
			}
		} else if (keyword.equals(":observed")) {
			Collection<?> c = (Collection<?>) argument;
			for (Object o : c) {
				addObservedModel((IModel) o);
			}
		} else if (keyword.equals(":as")) {
			setLocalId(argument.toString());
		} else if (keyword.equals(":when")) {
			whenClause = (IFn) argument;
		} else if (keyword.equals(":editable")) {
			editable = argument;
		} else if (keyword.equals(":optional")) {
			isOptional = (Boolean) argument;
		} else if (keyword.equals(":required")) {
			isOptional = !((Boolean) argument);
		} else if (keyword.equals(":agent")) {
			entityAgent = argument.toString();
		} else {
			metadata.put(keyword.substring(1), argument);
		}
	}

	public boolean isEntifiable() {
		return entityAgent != null;
	}

	public String getEntityType() {
		return entityAgent;
	}

//	/**
//	 * Pass a precontextualized state and get a model which is exactly like us,
//	 * but will only produce one observation with a predefined state, only valid
//	 * in the context that the state was generated for. Used to build scenarios
//	 * based on precomputed information.
//	 * 
//	 * @param state
//	 * @return
//	 */
//	public IModel getPrecontextualizedModel(IState state) {
//		try {
//			return new PrecontextualizedModelProxy((IModel) this.clone(), state);
//		} catch (CloneNotSupportedException e) {
//			throw new ThinklabRuntimeException(e);
//		}
//	}

	/**
	 * This is called for each model defined for us in a :context clause, after
	 * the dependent has been completely specified.
	 * 
	 * @param model
	 */
	public void addDependentModel(IModel model) {
		// null-tolerant so we can deal with the silly "functional comments" in
		// clojure
		if (model != null)
			dependents.add(model);
	}

	public void addObservedModel(IModel model) {
		// null-tolerant so we can deal with the silly "functional comments" in
		// clojure
		if (model != null)
			observed.add(model);
	}

	/**
	 * If the resulting observation is to be transformed by a transformer obs,
	 * add a transformer definition from defmodel (e.g. :cluster (def)) in the
	 * transformer queue.
	 * 
	 * @param definition
	 */
	public void enqueueTransformer(Polylist definition) {
		transformerQueue.addLast(definition);
	}

	/**
	 * This handles the :as clause. If we don't have one, our id is the
	 * de-camelized name of our observable class.
	 * 
	 * @param id
	 */
	public void setLocalId(String id) {
		this.localFormalName = id;
	}

	protected void validateMediatedModel(IModel model)
			throws ThinklabValidationException {
		if (getObservableClass().equals(model.getObservableClass())) {
			throw new ThinklabValidationException(
					"a model cannot mediate another that observes the same concept: "
							+ model.getObservableClass());
		}
	}

	@Override
	public IConcept getObservableClass() {
		if (observable == null) {
			try {
				observable = KnowledgeManager.get()
						.requireConcept(observableId);
			} catch (Exception e) {
				throw new ThinklabRuntimeException(e);
			}
		}
		return observable;
	}

	@Override
	public boolean isResolved() {
		return state != null || mediated != null || distribution != null;
	}

	/*
	 * Copy the relevant fields when a clone is created before configuration
	 */
	protected void copy(DefaultAbstractModel model) {

		id = model.id;
		namespace = model.namespace;
		localFormalName = model.localFormalName;
		mediated = model.mediated;
		observable = model.observable;
		observableSpecs = model.observableSpecs;
		observableId = model.observableId;
		editable = model.editable;
		metadata = model.metadata;
		name = model.name;
		isOptional = model.isOptional;
		whenClause = model.whenClause;
		state = model.state;
		mediatesExternal = model.mediatesExternal;
		distribution = model.distribution;

	}

	/**
	 * Generate a query that will select the requested observation type and
	 * restrict the observable to the specifications we got for this model. Use
	 * passed conformance table to define the observable constraint. Optionally
	 * add in an extent restriction.
	 * 
	 * @param extentRestriction
	 * @param conformancePolicies
	 * @param session
	 * @param context
	 *            .getTopologies()
	 * @return
	 * @throws ThinklabException
	 */
	public Constraint generateObservableQuery(
			IntelligentMap<IConformance> conformancePolicies, ISession session,
			IContext context) throws ThinklabException {

		Constraint c = new Constraint(
				this.getCompatibleObservationType(session));

		IInstance inst = session.createObject(observableSpecs);
		IConformance conf = conformancePolicies == null ? new DefaultConformance()
				: conformancePolicies.get(inst.getDirectType());

		c = c.restrict(new Restriction(CoreScience.HAS_OBSERVABLE, conf
				.getConstraint(inst)));

		if (context.getExtents().size() > 0) {

			ArrayList<Restriction> er = new ArrayList<Restriction>();
			for (IExtent o : context.getExtents()) {
				Restriction r = o.getConstraint("intersects");
				if (r != null)
					er.add(r);
			}

			if (er.size() > 0) {
				c = c.restrict(er.size() == 1 ? er.get(0) : Restriction.AND(er
						.toArray(new Restriction[er.size()])));
			}
		}

		/*
		 * if we need this, we are mediators even if there was no mediated model
		 * in the specs
		 */
		mediatesExternal = true;

		return c;
	}

	@Override
	public IQueryResult observe(IKBox kbox, ISession session, Object... params)
			throws ThinklabException {

		validateConcepts(session);

		IntelligentMap<IConformance> conformances = null;
		ArrayList<Topology> extents = new ArrayList<Topology>();
		IContext context = null;

		if (params != null)
			for (Object o : params) {
				if (o instanceof IntelligentMap<?>) {
					conformances = (IntelligentMap<IConformance>) o;
				} else if (o instanceof IInstance) {
					// put away all the extents we passed
					IObservation obs = ObservationFactory
							.getObservation((IInstance) o);
					if (obs instanceof Topology) {
						extents.add((Topology) obs);
					}
				} else if (o instanceof Topology) {
					extents.add((Topology) o);
				} else if (o instanceof IContext) {
					context = (IContext) o;
					;
				}
			}

		if (context == null)
			context = Context.getContext(extents);

		return observeInternal(kbox, session, conformances, context, false);

	}

	/**
	 * This will be called once before any observation is made, or it can be
	 * called from the outside API to ensure that all concepts are valid.
	 * 
	 * @param session
	 * @throws ThinklabException
	 */
	public void validateConcepts(ISession session) throws ThinklabException {

		if (!_validated) {

			/*
			 * resolve all concepts for the observable
			 */
			if (this.observable == null)
				this.observable = annotateConcept(observableId, session, null);

			if (this.observableSpecs == null)
				this.observableSpecs = Polylist.list(this.observable);

			/*
			 * notify annotation if we are unresolved, so we can find data in
			 * this phase.
			 */
			if (!isResolved() && (this instanceof DefaultStatefulAbstractModel)) {
				ModelAnnotation an = (ModelAnnotation) session
						.getVariable(ModellingPlugin.ANNOTATION_UNDERWAY);
				if (an != null) {
					an.addUnresolvedState(
							this.observableId,
							this.getCompatibleObservationType(session),
							observable == null ? null
									: generateObservableQuery(null, session,
											new Context()));
				}
			}

			validateSemantics(session);

			/*
			 * validate mediated
			 */
			if (mediated != null) {

				((DefaultAbstractModel) mediated).validateConcepts(session);

				/*
				 * TODO FIXME shouldn't we call validateMediated() at this
				 * point? Nothing seems to be calling it anymore.
				 */
				metadata.merge(((DefaultAbstractModel) mediated).metadata);
			}

			/*
			 * validate dependents and observed
			 */
			for (IModel m : dependents)
				((DefaultAbstractModel) m).validateConcepts(session);

			for (IModel m : observed)
				((DefaultAbstractModel) m).validateConcepts(session);

			_validated = true;
		}
	}

	@Override
	public Model train(IKBox kbox, ISession session, Object... params)
			throws ThinklabException {
		// TODO! Needs to observe everything (including the observed) and invoke
		// a virtual
		return null;
	}

	/*
	 * this should be protected, but...
	 */
	public ModelResult observeInternal(IKBox kbox, ISession session,
			IntelligentMap<IConformance> cp, IContext context,
			boolean acceptEmpty) throws ThinklabException {

		ModelResult ret = new ModelResult(this, kbox, session, context);

		/*
		 * if we're resolved, the model result contains all we need to know
		 */
		if (state != null || distribution != null)
			return ret;

		/*
		 * if mediated, realize mediated and add it
		 */
		if (mediated != null) {

			ModelResult res = ((DefaultAbstractModel) mediated)
					.observeInternal(kbox, session, cp, context, acceptEmpty);

			if (res == null || res.getTotalResultCount() == 0) {

				if (acceptEmpty)
					return null;

				throw new ThinklabModelException("model: cannot observe "
						+ ((DefaultAbstractModel) mediated).observable
						+ " in kbox " + kbox);
			}

			ret.addMediatedResult(res);
		}

		/*
		 * query dependencies
		 */
		for (IModel dep : dependents) {

			ModelResult d = ((DefaultAbstractModel) dep).observeInternal(kbox,
					session, cp, context,
					((DefaultAbstractModel) dep).isOptional);

			// can only return null if optional is true for the dependent
			if (d != null)
				ret.addDependentResult(d);
		}

		/*
		 * if no state, dependents and no mediated, we need to find an
		 * observable to mediate
		 */
		if (mediated == null && dependents.size() == 0) {

			ObservationCache cache = ModellingPlugin.get().getCache();

			if (Thinklab.debug(session)) {
				session.print("---  " + getName()
						+ ": looking up observations for: " + observable);
			}

			if (kbox == null && cache == null) {
				if (acceptEmpty)
					return null;
				else
					throw new ThinklabModelException("model: cannot observe "
							+ observable + ": no kbox given");
			}

			// TODO must use the context here, before the "cache" - context
			// should be
			// primed with a cache if it's there
			if (cache != null) {
				//
				// /*
				// * build context from extent array
				// */
				// IObservationContext ctx =
				// ((Context)context).getObservationContext(o);
				//
				// /*
				// * lookup in cache, if existing, return it
				// */
				// Polylist res = cache.getObservation(observable, ctx,
				// (String) session
				// .getVariable(ModelFactory.AUX_VARIABLE_DESC));
				// if (res != null)
				// return new ModelResult(res);
			}

			Constraint query = generateObservableQuery(cp, session, context);

			if (Thinklab.debug(session)) {
				session.print("---  query: " + query);
			}

			IQueryResult rs = new GroupingQueryResult(kbox.query(query,
					new String[] { "dataset" }, 0, -1), "dataset");

			if (Thinklab.debug(session)) {
				session.print("---  result: " + rs);
			}

			if (rs == null || rs.getTotalResultCount() == 0)
				if (acceptEmpty)
					return null;
				else
					throw new ThinklabModelException("model: cannot observe "
							+ observable + " in kbox " + kbox);

			ret.addMediatedResult(rs);
		}

		ret.initialize();

		return ret;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return namespace + "/" + id;
	}

	/**
	 * 
	 * @param scenario
	 * @return
	 * @throws ThinklabException
	 */
	@Override
	public IModel applyScenario(Scenario scenario) throws ThinklabException {
		return applyScenarioInternal(scenario, new Session());
	}

	protected IModel applyScenarioInternal(Scenario scenario, Session session)
			throws ThinklabException {

		DefaultAbstractModel ret = null;

		IInstance match = session.createObject(observableSpecs);

		/*
		 * if I am in the scenario, clone the scenario's, not me, unless I am a
		 * Model which requires to be preserved for functionality
		 */
		if (!(this instanceof Model)) {
			for (IModel m : scenario.models) {

				IInstance im = session
						.createObject(((DefaultAbstractModel) m).observableSpecs);
				if (im.isConformant(match, null)) {

					/*
					 * use the return value of a model function that
					 * accepts/rejects/mediates the model, if null don't
					 * substitute and continue.
					 */
					IModel mo = validateSubstitutionModel(m);
					if (mo != null)
						return mo;
				}
			}
		} else {

			/*
			 * Substitute the contingencies. They should be substituted in toto
			 * if the observable matches.
			 * 
			 * FIXME We just should not substitute two different contingencies
			 * with the same model, which is possible right now.
			 */
			for (IModel m : ((Model) this).models) {

				IModel con = ((DefaultAbstractModel) m).applyScenarioInternal(
						scenario, session);
				try {
					if (ret == null)
						ret = (Model) this.clone();
				} catch (CloneNotSupportedException e) {
				}
				((Model) ret).defModel(con == null ? m : con, null);
			}
		}

		/*
		 * clone me if necessary and add the applyScenarios of the dependents as
		 * dependents
		 */
		try {
			if (ret == null)
				ret = (DefaultAbstractModel) this.clone();
		} catch (CloneNotSupportedException e) {
			// yeah, right
		}

		if (mediated != null) {
			IModel dep = ((DefaultAbstractModel) mediated)
					.applyScenarioInternal(scenario, session);
			ret.mediated = (dep == null ? mediated : dep);
		}

		for (IModel d : dependents) {
			IModel dep = ((DefaultAbstractModel) d).applyScenarioInternal(
					scenario, session);
			ret.addDependentModel(dep == null ? d : dep);
		}

		/*
		 * give the sucker our ID so it will work in references, and store the
		 * scenario to constrain queries as needed.
		 */
		if (ret != null) {
			ret.id = id;
			ret.namespace = namespace;
			ret.scenario = scenario;
		}

		return ret;
	}

	/**
	 * This one gets called when a scenario contains a model that has a
	 * conformant observable as us. In that case, the model is passed and we are
	 * expected to return a clone or a wrapper so that it expresses our same
	 * semantics. E.g., any passed measurement must be wrapped so that its units
	 * are the same as ours. If the model is not compatible, return null.
	 * 
	 * @param m
	 * @return
	 */
	protected IModel validateSubstitutionModel(IModel m) {
		return null;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return getConfigurableClone();
	}

	public void dump(PrintStream out) {
		dumpInternal(out, 0);

	}

	protected void dumpInternal(PrintStream out, int i) {
		String prefix = MiscUtilities.spaces(i);
		out.println(prefix + this + " (" + getName() + ")");
		out.println(prefix + this.metadata);

		if (mediated != null) {
			out.println(prefix + "Mediates:");
			((DefaultAbstractModel) mediated).dumpInternal(out, i + 3);
		}

		if (dependents.size() > 0) {
			out.println(prefix + "Depends on:");
			for (IModel m : dependents)
				((DefaultAbstractModel) m).dumpInternal(out, i + 3);

		}

		if (this instanceof Model && ((Model) this).models.size() > 0) {
			out.println(prefix + "Contingent on:");
			for (IModel m : ((Model) this).models)
				((DefaultAbstractModel) m).dumpInternal(out, i + 3);
		}
	}

	public void setMediatedModel(IModel m) {
		this.mediated = m;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof IModel ? getName().equals(
				((IModelForm) obj).getName()) : false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	public String getLocalFormalName() {
		if (localFormalName == null)
			return CamelCase.toLowerCase(getObservableClass().getLocalName(),
					'-');
		return localFormalName;
	}

}