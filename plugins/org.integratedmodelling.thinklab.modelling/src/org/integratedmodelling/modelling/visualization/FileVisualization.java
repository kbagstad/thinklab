package org.integratedmodelling.modelling.visualization;

import java.io.File;
import java.util.Properties;

import org.integratedmodelling.corescience.interfaces.IObservationContext;
import org.integratedmodelling.corescience.interfaces.IState;
import org.integratedmodelling.modelling.ModellingPlugin;
import org.integratedmodelling.modelling.interfaces.IVisualization;
import org.integratedmodelling.modelling.storage.FileArchive;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.integratedmodelling.utils.Pair;

/**
 * Uses a FileArchive inside, or it can be initialized from one, so it shares the same
 * file structure. If will build pngs in each state directory to match the context of
 * the states.
 * 
 * @author ferdinando.villa
 *
 */
public class FileVisualization implements IVisualization {

	FileArchive archive = null;
	private IObservationContext context;
	
	/**
	 * largest edge of plot in pixels, used to define the dimensions of any visual 
	 * unless the max viewport is given.
	 */
	private int maxEdgeLength = 800;
	
	/**
	 * if these are set, the plots will be the largest possible for the given viewport.
	 */
	private int maxWidth  = -1;
	private int maxHeight = -1;
	
	public FileVisualization() {
	}
	
	public FileVisualization(IObservationContext context) throws ThinklabException {
		initialize(context);
	}
	
	
	@Override
	public void initialize(IObservationContext context) throws ThinklabException {
		this.context = context;
		if (this.archive == null) {
			this.archive = new FileArchive(context);
		}
		
	}
	
	/**
	 * Use this if you need the visuals to fit a given viewport.
	 * 
	 * @param width
	 * @param height
	 */
	public void setViewPort(int width, int height) {
		
	}

//	@Override
	public String graph(IState concept, int width, int height,
			String destination, Properties properties) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void visualize() throws ThinklabException {
				
		for (IState state : context.getStates()) {
			
			for (String plotType : VisualizationFactory.get().getPlotTypes(state, context)) {
		
				Pair<Integer,Integer> xy = 
					maxHeight < 0 ?
						VisualizationFactory.get().getPlotSize(maxEdgeLength, state, context) :
						VisualizationFactory.get().getPlotSize(maxWidth, maxHeight, state, context);
				
				File dir = archive.getStateDirectory(state.getObservableClass());
				File out = new File(dir + File.separator + plotType);
				VisualizationFactory.get().
					plot(state, context, plotType, xy.getFirst(), xy.getSecond(), out);
			}
		}
		
		ModellingPlugin.get().logger().info(
				"visualization of " + 
				context.getObservation().getObservableClass() + 
				" created in " +
				archive.getDirectory());
				
	}

}