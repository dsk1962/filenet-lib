package com.dkgeneric.filenet.content.request;

import com.dkgeneric.filenet.content.model.P8Properties;
import com.dkgeneric.filenet.content.model.SearchData;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains data to update document(s) {@link #toString()}
 * implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class UpdateDocumentMetadataRequest extends BaseRequest {

	/**
	 * Document id
	 * 
	 * @param id the id of the document to update
	 * @return Current document id
	 */
	private String id;

	/**
	 * Document class o reindex
	 * 
	 * @param new document class
	 * @return Current value of new document class. If null or empty reindex is not required 
	 */
	private String reindexClass;

	/**
	 * Search data to retrieve documents to update
	 * 
	 * @param searchData the new search data
	 * @return Current search data
	 */
	private SearchData searchData;

	/**
	 * Properties to update
	 * 
	 * @param properties the new properties to update
	 * @return Current properties to update
	 */
	private P8Properties properties = new P8Properties();
}
