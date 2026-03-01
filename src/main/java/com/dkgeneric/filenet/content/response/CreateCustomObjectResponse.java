package com.dkgeneric.filenet.content.response;

import com.dkgeneric.filenet.content.model.P8Object;
import com.dkgeneric.filenet.content.model.P8ObjectAuditInfo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains results of create document request <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class CreateCustomObjectResponse extends BaseResponse {

	/**
	 * Document id of created document
	 * 
	 * @param p8DocumentId the new document id
	 * @return Current document id
	 */
	private String p8CustomObjectId;

	/**
	 * Created document
	 * 
	 * @param p8Object the new document
	 * @return Current document
	 */
	private P8Object p8Object;

	/** The audit info. 
	 * 
	 * @param auditInfo information about created document for audit purposes
	 * @return Current information about created document for audit purposes
	 */
	private P8ObjectAuditInfo auditInfo;
}
