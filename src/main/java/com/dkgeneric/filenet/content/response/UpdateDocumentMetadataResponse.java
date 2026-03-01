package com.dkgeneric.filenet.content.response;

import java.util.ArrayList;
import java.util.List;

import com.dkgeneric.filenet.content.model.P8ObjectAuditInfo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains results of document update operation <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class UpdateDocumentMetadataResponse extends BaseResponse {

	/**
	 * Total number of updated documents
	 * 
	 * @param numberOfUpdatedDocuments the new number of updated documents
	 * @return Current number of updated documents
	 */
	private int numberOfUpdatedDocuments;

	/** The audit info. 
	 * 
	 * @param auditInfos information about document(s) changes for audit purposes
	 * @return Current information about document(s) changes for audit purposes
	 */
	private List<P8ObjectAuditInfo> auditInfos;

	/**
	 * Adds information about document changes for audit purposes.
	 *
	 * @param info the information about document changes for audit purposes
	 */
	public void addP8ObjectAuditInfo(P8ObjectAuditInfo info) {
		if (auditInfos == null)
			auditInfos = new ArrayList<>();
		auditInfos.add(info);
	}
}
