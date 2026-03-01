package com.dkgeneric.filenet.content.request;

import com.dkgeneric.filenet.content.config.P8SearchConfiguration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class contains data to retrieve document information <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class GetDocumentByIdRequest extends BaseRequest {

	/**
	 * FileNet document id like {107DE07F-0000-C315-97F6-28C25358CD22}
	 * 
	 * @param documentId the new document id
	 * @return Current document id
	 */
	private String documentId;

	/**
	 * Property list name. See
	 * {@link P8SearchConfiguration#getPropertiesToInclude(String)}
	 * 
	 * @param propertiesListName the new property list name
	 * @return Current property list name
	 */
	private String propertiesListName;

	/**
	 * Property retrieveLatestVersion. Retrieve latest version for document with this id 
	 * 
	 * @param retrieveLatestVersion the new retrieveLatestVersion
	 * @return Current retrieveLatestVersion
	 */
	private boolean retrieveLatestVersion;
}
