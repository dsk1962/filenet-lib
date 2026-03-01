package com.dkgeneric.filenet.content.model;

import com.dkgeneric.filenet.content.resources.P8ContentResource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Instances of this class represents FileNet content object audit information. <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)

public class P8ContentObjectAuditInfo extends P8ObjectAuditInfo {
	/**
	 * FileNet content resource
	 * 
	 * @param contentResource the new content resource
	 * @return Current object content resource
	 */
	private P8ContentResource contentResource;
}
