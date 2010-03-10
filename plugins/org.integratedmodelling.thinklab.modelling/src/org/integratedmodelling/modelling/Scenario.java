package org.integratedmodelling.modelling;

import java.util.ArrayList;
import java.util.Map;

import org.integratedmodelling.modelling.corescience.ObservationModel;
import org.integratedmodelling.modelling.interfaces.IModel;

/**
 * A scenario is an identification model containing observables that can be
 * swapped for others in a model. The form allows a simpler specification
 * of dependencies, which are used differently.
 * 
 * @author Ferdinando Villa
 *
 */
public class Scenario extends ObservationModel {

	ArrayList<IModel> models = new ArrayList<IModel>();
	private String description;
	
	public void setDescription(String s) {
		description = s;
	}
	
	public void addModel(IModel model, Map<?,?> metadata) {

		if (metadata != null) {
			// TODO use it
			System.out.println("\nMETADATA! " + metadata + "\n");
		}
		models.add(model);
	}
	
	/**
	 * Add observables that were not defined, substitute those
	 * that were with the incoming ones.
	 * 
	 * @param scenario
	 */
	public void merge(Scenario scenario) {
		// TODO
	}

}