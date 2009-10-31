/**
 * RegularRasterGridDataSource.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Feb 18, 2008
 *
 * ----------------------------------------------------------------------------------
 * This file is part of ThinklabGeospacePlugin.
 * 
 * ThinklabGeospacePlugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ThinklabGeospacePlugin is distributed in the hope that it will be useful,
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
 * @date      Feb 18, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.geospace.implementations.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.integratedmodelling.corescience.interfaces.cmodel.IConceptualModel;
import org.integratedmodelling.corescience.interfaces.cmodel.IExtent;
import org.integratedmodelling.corescience.interfaces.context.IObservationContext;
import org.integratedmodelling.geospace.Geospace;
import org.integratedmodelling.geospace.coverage.CoverageFactory;
import org.integratedmodelling.geospace.coverage.ICoverage;
import org.integratedmodelling.geospace.coverage.VectorCoverage;
import org.integratedmodelling.geospace.extents.GridExtent;
import org.integratedmodelling.geospace.extents.ShapeExtent;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabIOException;
import org.integratedmodelling.thinklab.exception.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;
import org.integratedmodelling.thinklab.interfaces.knowledge.IRelationship;
import org.integratedmodelling.utils.URLUtils;

/**
 * TODO it's just a verbatim copy of the raster one for now.
 * @author Ferdinando
 *
 */
public class VectorCoverageDataSource extends CoverageDataSource<Object> {

	/**
	 * The conceptual model that defines the data we need to return, saved at handshaking
	 */
	private   IConceptualModel dataCM = null;
	protected ICoverage coverage = null;

	/* same here - these are overall extents that we need to conform to */
	private GridExtent gridExtent;
	private ShapeExtent shapeExtent;
	
	public boolean handshake(IConceptualModel cm,
			IObservationContext observationContext,
			IObservationContext overallContext)
			throws ThinklabException {
		
		dataCM = cm;
		
		IExtent extent = overallContext.getExtent(Geospace.get().SubdividedSpaceObservable());

		/*
		 * See what we have to deal with overall. 
		 */
		if (extent instanceof GridExtent) {
			gridExtent = (GridExtent)extent;			
		} else {
			this.shapeExtent = (ShapeExtent)extent;			
			// communicate the features to the extent, so that we can compute multiplicity and overall shape
			this.shapeExtent.setFeatures(((VectorCoverage)coverage).getFeatures(), coverage.getSourceUrl());
		}
		
		/*
		 * if raster, we may need to adjust the coverage to the extent for CRS, bounding box, and resolution.
		 */
		if (gridExtent != null) {
			
			/*
			 * this will rasterize our vector coverage. If it's a raster, we should then fall back to
			 * raster methods when data are accessed. For the way this is defined, the coverage class
			 * should automatically take care of that.
			 */
			coverage = coverage.requireMatch(gridExtent, true);

			/*
			 * ask for the main extent's activation layer (creating an inactive
			 * default if not there) and AND our active areas with it.
			 */
			defineActivationLayer(
					gridExtent.requireActivationLayer(true), gridExtent);
		}
		
		// if we get to handshaking, we need to load the data
		coverage.loadData();
		
		// whatever happens, we can definitely use indexes here, so return true.
		return true;
	}
	
	public void initialize(IInstance i) throws ThinklabException {

		// these are compulsory
		String sourceURL = null;
		String valueAttr = null;

		// these are only needed if we use an external attribute table
		String dataURL = null;
		String sourceAttr = null;
		String targetAttr = null;
		
		// read requested parameters from properties
		for (IRelationship r : i.getRelationships()) {
			
			if (r.isLiteral()) {
				
				if (r.getProperty().equals(Geospace.COVERAGE_SOURCE_URL)) {
					sourceURL = URLUtils.resolveUrl(
							r.getValue().toString(),
							Geospace.get().getProperties());
				} else if (r.getProperty().equals(Geospace.HAS_SOURCE_LINK_ATTRIBUTE)) {
					sourceAttr = r.getValue().toString();
				} else if (r.getProperty().equals(Geospace.HAS_TARGET_LINK_ATTRIBUTE)) {
					targetAttr = r.getValue().toString();
				} else if (r.getProperty().equals(Geospace.HAS_VALUE_ATTRIBUTE)) {
					valueAttr = r.getValue().toString();
				} else if (r.getProperty().equals(Geospace.HAS_ATTRIBUTE_URL)) {
					dataURL = r.getValue().toString();
				}
			}
		}

		// check data
		if (sourceURL == null || valueAttr == null)
			throw new ThinklabValidationException("vector coverage: specifications are invalid (source url or value attribute missing)");

		if (dataURL != null && ( sourceAttr == null || targetAttr == null))
			throw new ThinklabValidationException("vector coverage: specifications are invalid (no link attributes for external data table)");

		try {

			Properties p = new Properties();
			p.setProperty(CoverageFactory.VALUE_ATTRIBUTE_PROPERTY, valueAttr);
			
			if (dataURL != null) {
				p.setProperty(CoverageFactory.ATTRIBUTE_URL_PROPERTY, dataURL);
				p.setProperty(CoverageFactory.SOURCE_LINK_ATTRIBUTE_PROPERTY, sourceAttr);
				p.setProperty(CoverageFactory.TARGET_LINK_ATTRIBUTE_PROPERTY, targetAttr);
			}
			
			this.coverage = 
				CoverageFactory.requireCoverage(new URL(sourceURL), p);
			
		} catch (MalformedURLException e) {
			throw new ThinklabIOException(e);
		}
		
	}

	public Object getInitialValue() {
		return null;
	}

	public void validate(IInstance i) throws ThinklabException {

		if (coverage != null) {
			
		} else {	
			// TODO we should support inline data
			throw new ThinklabValidationException("vector datasource: no coverage specified");		
		}
	}

	@Override
	public Object getValue(int index, Object[] parameters) {
		
		try {
			return coverage.getSubdivisionValue(
					index, 
					dataCM, 
					gridExtent == null ? shapeExtent : gridExtent);
		} catch (ThinklabValidationException e) {
			throw new ThinklabRuntimeException(e);
		}
	}

	@Override
	public IConcept getValueType() {
		// TODO Auto-generated method stub
		return null;
	}

}
