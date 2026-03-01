package com.dkgeneric.filenet.content.model;

import java.util.ArrayList;
import java.util.List;

import com.filenet.api.core.IndependentlyPersistableObject;

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
public class P8ResultSet {
	/**
	 * List of retrieved FileNet objects
	 * 
	 * @param searchResults the new list of FileNet objects
	 * @return Current list of FileNet objects
	 */
	private List<IndependentlyPersistableObject> searchResults = new ArrayList<>();
	/**
	 * Total page number based on page size in request. For non-pagination request or if no documents were found 1 will be returned
	 * 
	 * @param totalPageNumber the new page number
	 * @return Current total page number
	 */
	private int totalPageNumber = 1;
	/**
	 * Total number of documents in Filenet for this search request. For non-pagination request 1 will be same as {@link #getSearchResults()} size 
	 * 
	 * @param totalDocumentNumber the new Total number of documents
	 * @return Current Total number of documents
	 */
	private int totalDocumentNumber;

	/**
	 * Adds the filenet object to result set.
	 *
	 * @param independentlyPersistableObject the new filenet object
	 */
	public void addIndependentlyPersistableObject(IndependentlyPersistableObject independentlyPersistableObject) {
		if (searchResults == null)
			searchResults = new ArrayList<>();
		searchResults.add(independentlyPersistableObject);
	}
}
