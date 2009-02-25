/**
 * IValueAggregator.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Jan 17, 2008
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
 * @date      Jan 17, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.corescience.interfaces.cmodel;

import org.integratedmodelling.corescience.interfaces.context.IObservationContextState;
import org.integratedmodelling.thinklab.exception.ThinklabException;
import org.jscience.mathematics.number.Rational;

/**
 * Conceptual models create these to enable aggregation of fine-grained dependencies into values to
 * use when computing coarser-grained dependents.
 * 
 * @author Ferdinando Villa
 *
 */
public interface IValueAggregator<T> {

	/**
	 * 
	 * @param value
	 * @param uncertainty
	 * @param contextState
	 * @throws ThinklabException
	 */
	public abstract void addValue(T value, IObservationContextState contextState)
		throws ThinklabException;
	
	/**
	 * 
	 * @return
	 * @throws ThinklabException
	 */
	public abstract T aggregateAndReset() throws ThinklabException;

	/**
	 * Partition the passed value into what makes sense for a new extent which stands in the
	 * passed ratio with the original one. If partitioning makes no sense for this type and
	 * concept, just return the original value.
	 * 
	 * @param originalValue
	 * @param ratio
	 * @return
	 */
	public abstract T partition(T originalValue, Rational ratio);

}