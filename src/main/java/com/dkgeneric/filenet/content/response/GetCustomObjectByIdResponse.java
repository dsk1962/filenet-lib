package com.dkgeneric.filenet.content.response;

import com.dkgeneric.filenet.content.model.P8Object;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains result of get document operation <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class GetCustomObjectByIdResponse extends BaseResponse {

	/**
	 * Custom Object data object
	 * 
	 * @param p8Object the new Custom Object data
	 * @return Current Custom Object data
	 */
	private P8Object p8Object;
}
