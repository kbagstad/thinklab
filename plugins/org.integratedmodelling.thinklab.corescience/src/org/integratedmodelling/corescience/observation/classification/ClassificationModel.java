/**
 * ClassificationModel.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Apr 3, 2008
 *
 * ----------------------------------------------------------------------------------
 * This file is part of ThinklabCoreSciencePlugin.
 * 
 * ThinklabCoreSciencePlugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ThinklabCoreSciencePlugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the software; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * ----------------------------------------------------------------------------------
 * 
 * @copyright 2008 www.integratedmodelling.org
 * @author    Ferdinando Villa (fvilla@uvm.edu)
 * @date      Apr 3, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.corescience.observation.classification;

import java.util.HashMap;

import org.integratedmodelling.corescience.CoreScience;
import org.integratedmodelling.corescience.interfaces.IConceptualModel;
import org.integratedmodelling.corescience.interfaces.IObservation;
import org.integratedmodelling.corescience.interfaces.IObservationContext;
import org.integratedmodelling.corescience.interfaces.IObservationContextState;
import org.integratedmodelling.corescience.interfaces.IValueAggregator;
import org.integratedmodelling.corescience.interfaces.IValueMediator;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstanceImplementation;
import org.integratedmodelling.thinklab.interfaces.knowledge.IKnowledgeSubject;
import org.integratedmodelling.thinklab.interfaces.knowledge.IRelationship;
import org.integratedmodelling.thinklab.interfaces.literals.IValue;
import org.jscience.mathematics.number.Rational;

public class ClassificationModel implements IConceptualModel, IInstanceImplementation {

	String name;
	
	HashMap<Object, IKnowledgeSubject> mapping = new HashMap<Object, IKnowledgeSubject>();
	
	public IValueAggregator getAggregator(IObservationContext ownContext,
			IObservationContext overallContext) {
		// TODO Auto-generated method stub
		return null;
	}

	public IValueMediator getMediator(IConceptualModel conceptualModel,
			IObservationContext ctx) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	public IConcept getStateType() {
		// the most general concept that subsumes our state
		return null;
	}

	public IValue partition(IValue originalValue, Rational ratio) {
		// TODO Auto-generated method stub
		return null;
	}

	public void validate(IObservation observation)
			throws ThinklabValidationException {
		// TODO Auto-generated method stub

	}

	public IValue validateData(byte b) throws ThinklabValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IValue validateData(int b) throws ThinklabValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IValue validateData(long b) throws ThinklabValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IValue validateData(float b) throws ThinklabValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IValue validateData(double b) throws ThinklabValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IValue validateLiteral(String value,
			IObservationContextState contextState)
			throws ThinklabValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IValue validateValue(IValue value,
			IObservationContextState contextState)
			throws ThinklabValidationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getObjectName() {
		return name;
	}

	public void setObjectName(String name) {
		this.name = name;
	}

	public void initialize(IInstance i) throws ThinklabException {

		for (IRelationship r : i.getRelationships()) {
			
			/* again, for speed */
			if (r.isLiteral()) {
				
				String s = r.getValue().toString();
				
				if (r.getProperty().is(CoreScience.HAS_SOURCE_VALUE_TYPE)) {
					
				} else if (r.getProperty().is(CoreScience.HAS_CONCEPTUAL_SPACE)) {
					
				}

				
			} else if (r.isObject()) {
				
				if (r.getProperty().is(CoreScience.CLASS_MAPPING)) {
					
					IInstance cm = r.getValue().asObjectReference().getObject();
					IKnowledgeSubject kn = null;
					
					String lit = cm.get(CoreScience.HAS_SOURCE_VALUE).toString();
					
					
					
					/* these two are mutually exclusive */
					for (IRelationship rl : cm.getRelationships(CoreScience.HAS_TARGET_CONCEPT)) {
						String c = rl.getValue().toString();
						kn = KnowledgeManager.get().requireConcept(c);
					}
						
					if (kn == null)
						for (IRelationship rl : cm.getRelationships(CoreScience.HAS_TARGET_INSTANCE)) {
							IInstance inst = rl.getValue().asObjectReference().getObject();
						}
										
					if (kn != null) {
						
						/* add mapping */
					}
						 
					
				} 
			}
		}
	}

	public void validate(IInstance i) throws ThinklabException {

		/*
		 * TODO - maybe...
		 *  
		 * The proper validation should ensure that all values are disjoint. Whether
		 * to check it or not (depends on having a reasoner and may be expensive) is
		 * another story. I think such validations should be conditional to an
		 * options in the corescience properties.
		 */
	}

	@Override
	public IConcept getUncertaintyType() {
		return KnowledgeManager.Nothing();
	}

}
