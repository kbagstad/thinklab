package org.integratedmodelling.modelling.visualization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.integratedmodelling.corescience.Obs;
import org.integratedmodelling.corescience.interfaces.data.IContextualizedState;
import org.integratedmodelling.corescience.interfaces.observation.IObservation;
import org.integratedmodelling.geospace.Geospace;
import org.integratedmodelling.geospace.extents.GridExtent;
import org.integratedmodelling.geospace.implementations.cmodels.RegularRasterModel;
import org.integratedmodelling.geospace.implementations.observations.RasterGrid;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabIOException;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.thinklab.interfaces.knowledge.IInstance;
import org.integratedmodelling.time.implementations.observations.RegularTemporalGrid;

import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;

/**
 * Easily create a NetCDF file with all the data from an observation set, or add data piece by
 * piece.
 * 
 * @author Ferdinando
 *
 */
public class NetCDFArchive {

	RasterGrid space         = null;
	RegularTemporalGrid time = null;
	Map<IConcept,IContextualizedState> variables;
	
	/**
	 * Add a contextualized observation and we do the rest.
	 * @param obs
	 * @throws ThinklabException 
	 */
	public void setObservation(IInstance obs) throws ThinklabException {
		
		IObservation o = Obs.getObservation(obs);
		
		//time  = (RasterGrid) Obs.findObservation(o, TimePlugin.GridObservable());
		space = (RasterGrid) Obs.findObservation(o, Geospace.get().RasterGridObservable());
		variables = Obs.getStateMap(o);
	}
	
	public void setSpaceGrid(RasterGrid grid) {
	}
	
	public void setTimeGrid() {
	}
	
	public void addRasterVariable(String name, String units, double[] data) {
	}
	
	public void write(String filename) throws ThinklabException {
		
		Dimension latDim = null;
		Dimension lonDim = null;
		Dimension timDim = null;
		GridExtent ext   = null;

		ArrayList<Dimension> spdims = new ArrayList<Dimension>();
		
		if (!filename.endsWith(".nc"))
			filename += ".nc";
		
		NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);
		
		/*
		 * add dimensions
		 */
		if (time != null) {
			// unimplemented for now
		}
		
		if (space != null) {
			
			ext = (GridExtent) ((RegularRasterModel)(space.getConceptualModel())).getExtent();
			
			latDim = ncfile.addDimension("lat", ext.getYCells());
			lonDim = ncfile.addDimension("lon", ext.getXCells());
			spdims.add(latDim);
			spdims.add(lonDim);

			/* add latitude and longitude as variables */
			ncfile.addVariable("latitude", DataType.DOUBLE, new Dimension[]{latDim});
			ncfile.addVariableAttribute("latitude", "units", "degrees_north");
			/* add latitude and longitude as variables */
			ncfile.addVariable("longitude", DataType.DOUBLE, new Dimension[]{lonDim});
			ncfile.addVariableAttribute("longitude", "units", "degrees_east");
		}
		
		for (IConcept obs : variables.keySet()) {
			
			// TODO implement the rest
			
			if (spdims.size() == 2) {
				// we have space only
				String varname = obs.getLocalName();
				ncfile.addVariable(varname, DataType.DOUBLE, new Dimension[]{latDim,lonDim});
				// TODO if var is a measurement, add units attribute
			}
		}
		
		/*
		 * create the file before we add variables
		 */
		try {
			ncfile.create();
		} catch (IOException e) {
			throw new ThinklabIOException(e);
		}
		
		/*
		 * TODO write time data
		 */
		
		/*
		 * lat and lon data if any
		 */
		if (space != null) {
			
			ArrayDouble alat = new ArrayDouble.D1(latDim.getLength());
			Index ind1 = alat.getIndex();
			for (int i = 0; i < latDim.getLength(); i++) {
				alat.setDouble(ind1.set(i), ext.getSouth() + ext.getNSResolution() * i);
			}
			
			ArrayDouble alon = new ArrayDouble.D1(lonDim.getLength());
			Index ind2 = alon.getIndex();
			for (int i = 0; i < lonDim.getLength(); i++) {
				alon.setDouble(ind2.set(i), ext.getEast() + ext.getEWResolution() * i);
			}
			
			try {
				ncfile.write("latitude", alat);
				ncfile.write("longitude", alon);
			} catch (Exception e) {
				throw new ThinklabIOException(e);
			}
			
		}
		
		for (IConcept obs : variables.keySet()) {
			
			// TODO implement the rest
			
			if (spdims.size() == 2) {
				
				// we have space only
				String varname = obs.getLocalName();
			
				ArrayDouble data = new ArrayDouble.D2(latDim.getLength(), lonDim.getLength());
				Index ind = data.getIndex();
				double[] dd = variables.get(obs).getDataAsDoubles();
				int i = 0;
				for (int lat = 0; lat < latDim.getLength(); lat++) {
					for (int lon = 0; lon < lonDim.getLength(); lon++) {
						data.setDouble(ind.set(lat,lon), dd[i++]);
					}	
				}
				
				try {
					ncfile.write(varname, data);
				} catch (Exception e) {
					throw new ThinklabIOException(e);
				}
			}
		}
		
		try {
			ncfile.close();
		} catch (IOException e) {
			throw new ThinklabIOException(e);
		}
	}
}
