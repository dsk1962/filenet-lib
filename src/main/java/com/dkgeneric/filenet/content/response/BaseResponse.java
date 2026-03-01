package com.dkgeneric.filenet.content.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO base class for response objects <br>
 * {@link #toString()} implemented through Lombok @ToString.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class BaseResponse {

	/**
	 * Error code
	 * 
	 * @param errorCode the new error code
	 * @return Current property error code
	 */
	private String errorCode;

	/**
	 * Error message
	 * 
	 * @param errorMessage the new error message
	 * @return Current error message
	 */
	private String errorMessage;

	/**
	 * Error type
	 * 
	 * @param errorType the new error type
	 * @return Current error type
	 */
	private int errorType;

	/**
	 * Helper method. Returns true if error code is null
	 *
	 * @return true, if operation completed successfully
	 */
	public boolean success() {
		return errorCode == null;
	}
}
