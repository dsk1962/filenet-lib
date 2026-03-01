package com.dkgeneric.filenet.content.request;

import com.dkgeneric.filenet.content.model.P8Object;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains data to complete document creation <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class CreateCustomObjectRequest extends BaseRequest {

	/**
	 * FileNet data required to create document
	 * 
	 * @param p8ContentObject the new document data
	 * @return Current document data
	 */
	private P8Object p8Object;
}
