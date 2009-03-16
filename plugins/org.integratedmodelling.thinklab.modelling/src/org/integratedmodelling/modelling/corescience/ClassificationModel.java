package org.integratedmodelling.modelling.corescience;

import java.util.Collection;
import java.util.Iterator;

import org.integratedmodelling.modelling.interfaces.IModel;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.interfaces.applications.ISession;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;
import org.integratedmodelling.thinklab.interfaces.storage.IKBox;

public class ClassificationModel implements IModel {

	IModel classified = null;
	IConcept toClassify = null;
	
	public ClassificationModel(Object concOrMod, Collection<?> classdefs) throws ThinklabException {
	
		if (concOrMod instanceof IModel) {
			classified = (IModel) concOrMod;
		} else if (concOrMod instanceof IConcept) {
			toClassify = KnowledgeManager.get().requireConcept(concOrMod.toString());
		} 		
		
		/*
		 * analyze the sequence of classdefs
		 */
		for (Iterator<?> it = classdefs.iterator(); it.hasNext(); ) {
			
			Object classSpec = it.next();
			Object classRet  = it.next();
			
			/*
			 * class def should be a string specification or a list of concepts
			 */
			
			/*
			 * class target should be a number, a concept or an instance 
			 */
			
			/*
			 * define the type of observation we need to build according
			 * to the classification specs
			 */
		}
	}
	
	@Override
	public IInstance buildObservation(IKBox kbox, ISession session)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IConcept getCompatibleObservationType(ISession session) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IConcept getObservable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isResolved() {
		// TODO Auto-generated method stub
		return false;
	}

}
