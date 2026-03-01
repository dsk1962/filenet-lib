package com.dkgeneric.filenet.content.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Instances of this class represents FileNet object audit information. <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@ToString
public class P8ObjectAuditInfo {

	/**
	 * List of modified FileNet object  properties
	 * 
	 * @param modifiedProperties the new modified properties
	 * @return Current modified properties
	 */
	private List<PropertyAuditInfo> modifiedProperties;
	/**
	 * FileNet object id
	 * 
	 * @param objectId the new object id
	 * @return Current object id
	 */
	private String objectId;
	/**
	 * FileNet object class (symbolic name)
	 * 
	 * @param objectClass the new object class
	 * @return Current  object class
	 */
	private String objectClass;
	/**
	 * Audit event time
	 * 
	 * @param eventTime the new event time
	 * @return Current  event time
	 */
	private Date eventTime;

	/**
	 * Instantiates a new FileNet object audit info. Initializes eventTime with current date (new Date())
	 *
	 */
	public P8ObjectAuditInfo() {
		eventTime = new Date();
	}

	public void addPropertyAuditInfo(PropertyAuditInfo propertyAuditInfo) {
		if (modifiedProperties == null)
			modifiedProperties = new ArrayList<>();
		modifiedProperties.add(propertyAuditInfo);
	}

}
