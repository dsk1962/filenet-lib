package com.dkgeneric.filenet.content.request;

import com.dkgeneric.filenet.content.model.SearchData;
import com.dkgeneric.filenet.content.model.SearchParameters;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains data to search document information <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class SearchRequest extends BaseRequest {

	/**
	 * Search parameters for FileNet search
	 * 
	 * @param searchParameters the new search parameters
	 * @return Current search parameters
	 */
	private SearchParameters searchParameters = new SearchParameters();

	/**
	 * Search data required to perform FileNet search
	 * 
	 * @param searchData the new search data
	 * @return Current search data
	 */
	private SearchData searchData = new SearchData();
}
