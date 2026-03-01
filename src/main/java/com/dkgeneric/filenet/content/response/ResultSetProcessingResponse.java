package com.dkgeneric.filenet.content.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains results of result set processing <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class ResultSetProcessingResponse extends BaseResponse {
	/**
	 * Result set processing result 
	 * 
	 * @param result the new result
	 * @return Current result 
	 */
	private Object result;
}
