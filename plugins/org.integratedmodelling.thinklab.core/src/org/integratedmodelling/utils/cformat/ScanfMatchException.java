/**
 * ScanfMatchException.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Jan 17, 2008
 *
 * ----------------------------------------------------------------------------------
 * This file is part of Thinklab.
 * 
 * Thinklab is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Thinklab is distributed in the hope that it will be useful,
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
 * @author    Ioannis N. Athanasiadis (ioannis@athanasiadis.info)
 * @date      Jan 17, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.utils.cformat;

import java.io.IOException;

/** 
  * Exception class used by the <code>scan</code> methods within
  * ScanfReader when the input does not match the specified format.
  * 
  * @author John E. Lloyd, Fall 2000
  * @see ScanfReader
  */
public class ScanfMatchException extends IOException
{
	/** 
	  * Creates a new ScanfMatchException with the given message. 
	  * 
	  * @param msg Error message
	  * @see ScanfReader
	  */
	public ScanfMatchException (String msg)
	 { super (msg);
	 }
}
