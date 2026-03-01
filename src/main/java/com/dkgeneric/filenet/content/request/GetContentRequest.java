package com.dkgeneric.filenet.content.request;

import com.dkgeneric.filenet.content.model.SearchData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains data to retrieve document content <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class GetContentRequest extends BaseRequest {

	/**
	 * FileNet document id like {107DE07F-0000-C315-97F6-28C25358CD22}
	 * 
	 * @param documentId the new document id
	 * @return Current document id
	 */
	private String documentId;

	/**
	 * Search object with information how to retrieve document.
	 * 
	 * @param searchData the new search data
	 * @return Current search data
	 */
	private SearchData searchData;
}
