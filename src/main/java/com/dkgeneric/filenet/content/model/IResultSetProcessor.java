package com.dkgeneric.filenet.content.model;

/**
 * The Interface IResultSetProcessor.
 */
public interface IResultSetProcessor {

	/**
	 * Gets the processing result.
	 *
	 * @return the processing result
	 */
	public Object getProcessingResult();

	/**
	 * Process next object.
	 *
	 * @param p8Object the p8 object
	 * @return false, if result set processing should be interrupted
	 * @throws Exception 
	 */
	public boolean processNextObject(P8Object p8Object) throws Exception;
}
