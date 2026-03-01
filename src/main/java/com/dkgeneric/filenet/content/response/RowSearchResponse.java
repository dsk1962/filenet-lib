package com.dkgeneric.filenet.content.response;

import java.util.List;

import com.dkgeneric.filenet.content.model.P8Properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains results of search request <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class RowSearchResponse extends BaseResponse {

	/**
	 * List of retrieved FileNet objects
	 * 
	 * @param searchResults the new list of FileNet objects
	 * @return Current list of FileNet objects
	 */
	private List<P8Properties> searchResults;
	/**
	 * Total page number based on page size in request. For non-pagination request or if no documents were found 1 will be returned
	 * 
	 * @param totalPageNumber the new page number
	 * @return Current total page number
	 */
	private int totalPageNumber = 1;
	/**
	 * Total number of rows in Filenet for this search request. For non-pagination request 1 will be same as {@link #getSearchResults()} size 
	 * 
	 * @param totalDocumentNumber the new Total number of rows
	 * @return Current Total number of rows
	 */
	private int totalRows;
}
