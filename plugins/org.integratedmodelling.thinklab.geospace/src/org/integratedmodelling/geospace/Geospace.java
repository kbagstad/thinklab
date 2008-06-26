/**
 * GeospacePlugin.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Jan 17, 2008
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
 * @date      Jan 17, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.geospace;

import org.apache.log4j.Logger;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.referencing.CRS;
import org.integratedmodelling.geospace.constructors.ArealLocationConstructor;
import org.integratedmodelling.geospace.constructors.ArealLocationValidator;
import org.integratedmodelling.geospace.constructors.GeospaceValidator;
import org.integratedmodelling.geospace.constructors.RasterDatasourceConstructor;
import org.integratedmodelling.geospace.constructors.RasterGridConstructor;
import org.integratedmodelling.geospace.constructors.SubdividedCoverageModelConstructor;
import org.integratedmodelling.thinklab.KnowledgeManager;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabPluginException;
import org.integratedmodelling.thinklab.interfaces.IConcept;
import org.integratedmodelling.thinklab.interfaces.IInstance;
import org.integratedmodelling.thinklab.plugin.ThinklabPlugin;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Node;

public class Geospace extends ThinklabPlugin  {

	private static IConcept shapeType;
	private static IConcept pointType;
	private static IConcept lineStringType;
	private static IConcept polygonType;
	private static IConcept multiPointType;
	private static IConcept multiLineStringType;
	private static IConcept multiPolygonType;
	private static IInstance areaLocationInstance;
	private static IInstance rasterGridInstance;
	private static IInstance spatialCoverageInstance;
	private static IConcept arealLocationType;
	private static IConcept rasterGridObservable;
	private static IConcept subdividedSpaceObservable;
	private static String hasBoundingBoxPropertyID;
	private static String hasCentroidPropertyID;

	/* log4j logger used for this class. Can be used by other classes through logger()  */
	private static  Logger log = Logger.getLogger(Geospace.class);
	private static IConcept rasterSpaceType;
	static final public String PLUGIN_ID = "org.integratedmodelling.thinklab.geospace";
	
	public static final String X_RANGE_OFFSET = "geospace:hasXRangeOffset";
	public static final String X_RANGE_MAX = "geospace:hasXRangeMax";
	public static final String Y_RANGE_OFFSET = "geospace:hasYRangeOffset";
	public static final String Y_RANGE_MAX = "geospace:hasYRangeMax";
	public static final String LAT_LOWER_BOUND = "geospace:hasLatLowerBound";
	public static final String LON_LOWER_BOUND = "geospace:hasLonLowerBound";
	public static final String LAT_UPPER_BOUND = "geospace:hasLatUpperBound";
	public static final String LON_UPPER_BOUND = "geospace:hasLonUpperBound";
	public static final String CRS_CODE = "geospace:hasCoordinateReferenceSystem";
	public static final String COVERAGE_SOURCE_URL = "geospace:hasSourceURL";
	public static final String RASTER_CONCEPTUAL_MODEL = "geospace:RasterSpatialCoverage";
	public static final String POLYGON_COVERAGE_CONCEPTUAL_MODEL = "geospace:PolygonSpatialCoverage";
	public static final String RASTER_GRID_OBSERVABLE = "geospace:ContinuousRegularSpatialGrid";
	public static final String PREFERRED_CRS_PROPERTY = "geospace.preferred.crs";
	public static final String HAS_VALUE_ATTRIBUTE = "geospace:hasValueAttribute";
	public static final String HAS_SOURCE_LINK_ATTRIBUTE = "geospace:hasSourceLinkAttribute";
	public static final String HAS_TARGET_LINK_ATTRIBUTE = "geospace:hasTargetLinkAttribute";
	public static final String HAS_ATTRIBUTE_URL = "geospace:hasAttributeUrl";
	/*
	 * if not null, we have a preferred crs in the properties, and we solve
	 * all conflicts by translating to it. 
	 */
	CoordinateReferenceSystem preferredCRS = null;
	
	public Geospace() {
		// TODO Auto-generated constructor stub
	}

	
	public static Geospace get() {
		return (Geospace) getPlugin(PLUGIN_ID);
	}

	public static Logger logger() {
		return log;
	}

	@Override
	public void load(KnowledgeManager km) throws ThinklabPluginException {

		
		try {
			
			/*
			 * TODO put all these class names into global strings
			 */
			pointType = km.requireConcept("geospace:Point");
			lineStringType = km.requireConcept("geospace:LineString");
			polygonType = km.requireConcept("geospace:Polygon");
			multiPointType = km.requireConcept("geospace:MultiPoint");
			multiLineStringType = km.requireConcept("geospace:MultiLineString");
			multiPolygonType = km.requireConcept("geospace:MultiPolygon");
			areaLocationInstance = km.requireInstance("geospace:ArealLocationInstance");
			rasterGridInstance = km.requireInstance("geospace:RegularGridInstance");
			spatialCoverageInstance = km.requireInstance("geospace:SpatialCoverageInstance");
			arealLocationType = km.requireConcept("geospace:ArealLocation");
			rasterSpaceType = km.requireConcept(RASTER_CONCEPTUAL_MODEL);
			rasterGridObservable = km.requireConcept(RASTER_GRID_OBSERVABLE);
			subdividedSpaceObservable = km.requireConcept("geospace:SubdividedSpace");
			
			shapeType = km.requireConcept("geospace:SpatialRecord");
			
			hasBoundingBoxPropertyID = "geospace:hasBoundingBox";
			hasCentroidPropertyID = "geospace:hasCentroid";
			
			/* commands */
//			new GISToOPAL().install(km);
//			new Rasterize().install(km);
//			new Vectorize().install(km);
			
		} catch (ThinklabException e) {
			throw new ThinklabPluginException(e);
		}
		
		/*
		 * TODO all these plus the kbox must become extension points and get out of here
		 */
		km.registerLiteralValidator("geospace:SpatialRecord", 
				new GeospaceValidator());
		km.registerLiteralValidator("geospace:ArealLocation", 
				new ArealLocationValidator());
		km.registerInstanceConstructor("geospace:ArealLocation", 
				new ArealLocationConstructor());
		km.registerInstanceConstructor("geospace:PolygonSpatialCoverage",
				new SubdividedCoverageModelConstructor());
		km.registerInstanceConstructor("geospace:RasterSpatialCoverage",
				new SubdividedCoverageModelConstructor());
		km.registerInstanceConstructor("geospace:RasterGrid",
				new RasterGridConstructor());
		km.registerInstanceConstructor("geospace:ExternalRasterDataSource",
				new RasterDatasourceConstructor());

		
		/*
		 * create preferred CRS if one is specified. Highly adviceable to set one if hybrid data
		 * are used.
		 */
		if (getProperties().containsKey(PREFERRED_CRS_PROPERTY)) {
			try {
				preferredCRS = CRS.decode(getProperties().getProperty(PREFERRED_CRS_PROPERTY));
			} catch (Exception e) {
				throw new ThinklabPluginException(e);
			}
		}
	}

	// TODO declare world images in plugin 
//	@Override
//	public void notifyResource(String name, long time, long size)
//			throws ThinklabException {
//
//		if (name.contains("visualization/worldimages")) {
//			GeoImageFactory.get().addWorldImage(this.exportResourceCached(name));
//		}
//
//	}


	public CoordinateReferenceSystem getPreferredCRS() {
		return preferredCRS;
	}
	
	/**
	 * The geotools implementation is unclear and doesn't seem to work, so 
	 * I put this function here and we'll only have to fix it in one place.
	 * 
	 * @param crs
	 * @return
	 * @throws ThinklabPluginException 
	 */
	public static String getCRSIdentifier(CoordinateReferenceSystem crs, boolean useDefault) throws ThinklabPluginException {
		
		if (crs != null) {
			try {
				return CRS.lookupIdentifier(crs, true);
			} catch (FactoryException e) {
				// FIXME when this thing works, just throw the exception
				return crs.getIdentifiers().iterator().next().toString();
				// throw new ThinklabValidationException(e);
			}
		}
		
		return useDefault ? get().getProperties().getProperty(PREFERRED_CRS_PROPERTY) : null;

	}
	

	public static IConcept Point() {
		return pointType;
	}

	public static IConcept LineString() {
		return lineStringType;
	}

	public static IConcept Polygon() {
		return polygonType;
	}

	public static IConcept MultiPoint() {
		return multiPointType;
	}

	public static IConcept MultiLineString() {
		return multiLineStringType;
	}

	public static IConcept MultiPolygon() {
		return multiPolygonType;
	}
	
	public static IConcept Shape() {
		return shapeType;
	}

	public static IInstance absoluteArealLocationInstance() {
		return areaLocationInstance;
	}
	
	public static IInstance absoluteRasterGridInstance() {
		return rasterGridInstance;
	}

	public static IConcept ArealLocation() {
		return arealLocationType;
	}

	public static String hasBoundingBox() {
		return hasBoundingBoxPropertyID;
	}

	public static String hasCentroid() {
		return hasCentroidPropertyID;
	}


	public boolean handlesFormat(String format) {
		// TODO add remaining support formats as necessary
		return 
			format.equals("shp") || 
			format.equals("tif") ||
			format.equals("tiff");
	}



	public Hints getGeotoolsHints() {
		// TODO we need to create appropriate hints at initialization, using the plugin's 
		// properties.
		return GeoTools.getDefaultHints();
	}

	public void notifyConfigurationNode(Node n) {
		// TODO Auto-generated method stub
		
	}

	public static IConcept RasterObservationSpace() {
		return rasterSpaceType;
	}

	public static IConcept RasterGridObservable() {
		return rasterGridObservable;
	}

	public static IConcept SubdividedSpaceObservable() {
		return subdividedSpaceObservable;
	}

	public void setPreferredCRS(CoordinateReferenceSystem crs) {
		preferredCRS = crs;
	}

	public static IInstance absoluteSpatialCoverageInstance() {
		return spatialCoverageInstance;
	}


	@Override
	protected void unload() throws ThinklabException {
		// TODO Auto-generated method stub
		
	}

}