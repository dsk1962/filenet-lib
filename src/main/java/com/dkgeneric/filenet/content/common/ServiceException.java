package com.dkgeneric.filenet.content.common;

import com.dkgeneric.filenet.content.exceptioncodes.P8ExceptionCodes;
import com.filenet.api.exception.EngineRuntimeException;

import lombok.Getter;

/**
 * Custom exception. New instance may be created as result of data validation
 * process, application logic errors or exceptions thrown from external packages
 * Use {@link ErrorMessages} to get exception detailed information
 */
@Getter
public class ServiceException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -185637399875409190L;

	/**
	 * The exception parameters. Can be used for exception error formatting
	 * 
	 * @return Current exception parameters
	 */
	private final transient Object[] parameters;

	/**
	 * The exception code. Specifies error code, will be used to get error message
	 * details from property file
	 * 
	 * @return Current exception code
	 */
	private final String exceptionCode;

	public ServiceException(EngineRuntimeException ere) {
		this.exceptionCode = P8ExceptionCodes.ENGINE_RUNTIME_EXCEPTION_THROWN.getExceptionCode();
		String code = ere.getExceptionCode() == null ? "None" : ere.getExceptionCode().getErrorId();
		this.parameters = new Object[] { code, ere.getMessage() };
	}

	/**
	 * Instantiates a new service exception.
	 *
	 * @param exceptionCode the exception code
	 */
	public ServiceException(String exceptionCode) {
		this(exceptionCode, (Object) null);
	}

	/**
	 * Instantiates a new service exception.
	 *
	 * @param exceptionCode the exception code
	 * @param parameter     the parameter
	 */
	public ServiceException(String exceptionCode, Object parameter) {
		this.exceptionCode = exceptionCode;
		this.parameters = new Object[] { parameter };
	}

	/**
	 * Instantiates a new service exception.
	 *
	 * @param exceptionCode the exception code
	 * @param parameters    the parameters
	 */
	public ServiceException(String exceptionCode, Object[] parameters) {
		this.exceptionCode = exceptionCode;
		this.parameters = parameters;
	}

	/**
	 * Instantiates a new service exception.
	 *
	 * @param exceptionCode the exception code
	 * @param message     the error message
	 */
	public ServiceException(String exceptionCode, String message) {
		super(message);
		this.exceptionCode = exceptionCode;
		this.parameters = new Object[] { null };
	}
}
