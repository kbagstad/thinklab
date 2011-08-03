package org.integratedmodelling.thinklab.modelling.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IInstance;
import org.integratedmodelling.thinklab.api.listeners.IListener;
import org.integratedmodelling.thinklab.api.modelling.metadata.IMetadata;
import org.integratedmodelling.thinklab.api.modelling.observation.IContext;
import org.integratedmodelling.thinklab.api.modelling.observation.IContextMapper;
import org.integratedmodelling.thinklab.api.modelling.observation.IExtent;
import org.integratedmodelling.thinklab.api.modelling.observation.IObservation;
import org.integratedmodelling.thinklab.api.modelling.observation.IState;
import org.integratedmodelling.thinklab.metadata.Metadata;
import org.integratedmodelling.thinklab.modelling.internal.ListenerSet;
import org.integratedmodelling.thinklab.modelling.internal.NamespaceQualified;

public class Context extends NamespaceQualified implements IContext {

	private ArrayList<IListener> _listeners = new ArrayList<IListener>();
	private Metadata _metadata = new Metadata();	
	private HashMap<IInstance, IState> _states = new HashMap<IInstance, IState>();
	
	public Context(IContext context) {
		// TODO copy every state and extent from the passed one, then initialize
	}

	public Context() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Set<IInstance> getObservables() {
		return _states.keySet();
	}

	@Override
	public IMetadata getMetadata() {
		return _metadata;
	}

	@Override
	public Collection<IExtent> getExtents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMultiplicity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMultiplicity(IConcept concept) throws ThinklabException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IExtent getExtent(IConcept observable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCovered(int index) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IState getState(IConcept observable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean intersects(IContext context) throws ThinklabException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IExtent getTime() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IExtent getSpace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IState> getStates() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public IContext collapse(IConcept dimension) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextMapper mapContext(IObservation observation) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] getDimensionSizes() {
		// TODO Auto-generated method stub
		return null;
	}

	public IConcept getDimension(IConcept c) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<IConcept> getDimensions() {
		return null;
	}

	public IConcept getDimension(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addState(IState s) {
		// TODO Auto-generated method stub
		
	}

	public ListenerSet getListenerSet() {
		return new ListenerSet(_listeners);
	}

	@Override
	public void listen(IListener... listeners) {
		for (IListener l : listeners)
			_listeners.add(l);
	}

	public boolean containsState(IInstance observable) {
		return _states.containsKey(observable);
	}

	@Override
	public IContext intersection(IContext other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContext union(IContext other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(IContext o) throws ThinklabException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean overlaps(IContext o) throws ThinklabException {
		// TODO Auto-generated method stub
		return false;
	}

}
