/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.epic.perleditor.templates.ui;

/**
 * Defines status codes relevant to the PHP UI plug-in. When a 
 * Core exception is thrown, it contain a status object describing
 * the cause of the exception. The status objects originating from the
 * PHP UI plug-in use the codes defined in this interface.
  */
public class EPICStatusConstants {
	
	// Prevent instantiation
	private EPICStatusConstants() {
	}

	/** Status code describing an internal error */
	public static final int INTERNAL_ERROR= 1;
	
	/**
	 * Status constant indicating that an exception occured on
	 * storing or loading templates.
	 */
	public static final int TEMPLATE_IO_EXCEPTION = 2;
	
}
