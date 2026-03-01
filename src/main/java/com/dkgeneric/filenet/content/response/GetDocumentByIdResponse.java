package com.dkgeneric.filenet.content.response;

import com.dkgeneric.filenet.content.model.P8ContentObject;

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
public class GetDocumentByIdResponse extends BaseResponse {

	/**
	 * Document data object
	 * 
	 * @param p8ContentObject the new document data
	 * @return Current document data
	 */
	private P8ContentObject p8ContentObject;
}
