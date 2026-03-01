package com.dkgeneric.filenet.content.request;

import com.dkgeneric.filenet.content.model.P8ContentObject;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@ToString(callSuper = true)

public class CreateDocumentVersionRequest extends CreateDocumentRequest {

	/**
	 * FileNet data required to create document
	 * 
	 * @param p8ContentObject the new document data
	 * @return Current document data
	 */
	private P8ContentObject p8ContentObject;
	
	/**
	 * FileNet object id ( document id or version series id )
	 * 
	 * @param currentVersionId the p8 object id
	 * @return Current p8 object id
	 */
	private String p8ObjectId;
	
	/**
	 * Flag to control version type (MAJOR/MINOR). Default true MAJOR version will be created 
	 * 
	 * @param saveAsMajorVersion the new flag value 
	 * @return Current flag value
	 */
	private boolean saveAsMajorVersion=true;
}
