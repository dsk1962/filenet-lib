package com.dkgeneric.filenet.content.response;

import com.dkgeneric.filenet.content.model.P8ObjectAuditInfo;
import com.dkgeneric.filenet.content.resources.P8ContentResource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains results of get document content request <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class GetContentResponse extends BaseResponse {

	/**
	 * Document resource
	 * 
	 * @param p8DocumentResource the new document resource
	 * @return Current document resource
	 */
	private P8ContentResource p8DocumentResource;

	/** The audit info. 
	 * 
	 * @param auditInfo information about document for audit purposes
	 * @return Current information about document for audit purposes
	 */
	private P8ObjectAuditInfo auditInfo;
}
