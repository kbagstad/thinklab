package org.integratedmodelling.modelling.storage;

import java.io.File;

import org.integratedmodelling.corescience.context.ObservationContext;
import org.integratedmodelling.corescience.interfaces.IContext;
import org.integratedmodelling.corescience.interfaces.IObservationContext;
import org.integratedmodelling.corescience.interfaces.IState;
import org.integratedmodelling.geospace.coverage.RasterCoverage;
import org.integratedmodelling.modelling.ModellingPlugin;
import org.integratedmodelling.modelling.interfaces.IDataset;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.thinklab.exception.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.exception.ThinklabValidationException;
import org.integratedmodelling.thinklab.interfaces.knowledge.IConcept;
import org.integratedmodelling.utils.MiscUtilities;

public class GISArchive implements IDataset {

	IContext context = null;
	
	/* 
	 * the main directory where the archives live. If not set, chosen from the 
	 * THINKLAB_ARCHIVE_DIR environmental variable, defaulting to the modelling
	 * plugin's data area + "/archive".
	 */
	private File directory;

	@Override
	public String toString() {
		return "[" + getLocation() + "]";
	}
	
	/* 
	 * the name of the file directory under the main dir. If not set it will be set
	 * from the name of the main observable and given a date suffix.
	 */
	private String location = null;
	
	public GISArchive() throws ThinklabException {
		directory = getMainDirectory();
	}
	
	public GISArchive(File directory) throws ThinklabException {
		this.directory = directory;
		this.directory.mkdirs();
	}

	public GISArchive(IContext context, File directory) throws ThinklabException {
		this.directory = directory;
		this.directory.mkdirs();
		setContext(context);
	}

	public GISArchive(IContext context) throws ThinklabException {
		this();
		setContext(context);
	}

	@Override
	public void setContext(IContext context)
			throws ThinklabException {
		
		if (this.context != null) {
			((ObservationContext)(this.context)).mergeStates((IObservationContext) context);
		} else {
			this.context = context;
		}
	}

	@Override
	public IContext getContext()
			throws ThinklabException {
		return this.context;
	}

	/**
	 * Return the name of the folder we are storing our states under.
	 * @return
	 */
	public String getLocation() {
		
		if (location == null) {
			location = 
				((IObservationContext)context).getObservation().getObservableClass().
					toString().replaceAll(":",".").toLowerCase() +
				"." + 
				MiscUtilities.getDateSuffix();
		}
		
		return location;
	}
	
	@Override
	public String persist() throws ThinklabException {
				
		/*
		 * persist all states
		 */
		for (IState state : context.getStates()) {
			
			File outf = getFileForState(state.getObservableClass());
			RasterCoverage cov = new RasterCoverage(context, state);
			try {
				cov.write(outf);	
			} catch (ThinklabValidationException e) {
				ModellingPlugin.get().logger().error("could not write GeoTIFF for state " + state.getObservableClass());
			}
		}
		
		return getLocation();
	}

	@Override
	public void restore(String location) throws ThinklabException {

		/*
		 * load contexts from location
		 */
		
		/*
		 * load all states
		 */
		
	}

	public File getFileForState(IConcept c) {
		
		File ret = new File(
				directory + 
				File.separator + 
				getLocation());
				
		ret.mkdirs();

		ret = new File(
				ret + 
				File.separator +
				c.toString().replaceAll(":","_").toLowerCase() +
				".tif");
		
		
		return ret;
	}
	
	public String getStateRelativePath(IConcept c) {
		
		return
			getLocation() + 
			"/" +
			c.toString().replaceAll(":",".").toLowerCase();
	}
	
	public static File getDefaultDirectory() {
		
		File ret = null;
		String fenv = System.getenv("THINKLAB_ARCHIVE_DIR");
		if (fenv != null) {
			ret = new File(fenv);			
		}
	
		if (ret == null) {
			try {
				ret = 
					new File(
						ModellingPlugin.get().getScratchPath() + 
						File.separator +
						"archive");
			} catch (ThinklabException e) {
				throw new ThinklabRuntimeException(e);
			}			
		}
		ret.mkdirs();
		return ret;
	}
	
	public File getMainDirectory() {
		
		File ret = directory;
		if (ret == null) {
			ret = getDefaultDirectory();
		}
		return ret;
	}

	public File getDirectory() {
		return new File(directory + File.separator + getLocation());
	}
	
}