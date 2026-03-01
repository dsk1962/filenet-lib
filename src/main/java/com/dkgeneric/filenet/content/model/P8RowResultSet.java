package com.dkgeneric.filenet.content.model;

import java.util.ArrayList;
import java.util.List;

import com.filenet.api.query.RepositoryRow;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains result set information<br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = { "searchResults" })
public class P8RowResultSet {
	/**
	 * List of retrieved FileNet rows
	 * 
	 * @param searchResults the new list of FileNet rows
	 * @return Current list of FileNet rows
	 */
	private List<RepositoryRow> searchResults = new ArrayList<>();
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
	 * @param totalDocumentNumber the new total number of rows
	 * @return Current total number of rows
	 */
	private int totalRows;

	/**
	 * Adds the filenet repository row to result set.
	 *
	 * @param repositoryRow the new filenet object
	 */
	public void addRepositoryRow(RepositoryRow repositoryRow) {
		if (searchResults == null)
			searchResults = new ArrayList<>();
		searchResults.add(repositoryRow);
	}
}
