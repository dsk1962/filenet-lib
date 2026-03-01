package com.dkgeneric.filenet.content.model;

import java.util.HashMap;
import java.util.Map;

import com.filenet.api.core.Domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Instances of this class represents FileNet property audit information. <br>
 * {@link #toString()} implemented through Lombok @ToString
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DomainSchema {

	private String guid;
	/**
	 * FileNet domain symbolic name.
	 * 
	 * @param symbolicName the new domain symbolic name
	 * @return Current domain symbolic name
	 */
	private String symbolicName;

	/**
	 * FileNet domain text search enabled flag.
	 * 
	 * @param textSearchEnabled the new domain text search enabled flag value
	 * @return Current domain text search enabled flag value
	 */
	private boolean textSearchEnabled;

	/**
	 * Object store map defined in this domain. Key value is object store symbolic name
	 * 
	 * @param objectStores the new object store map
	 * @return Current domain object store map
	 */
	private Map<String, ObjectStoreSchema> objectStores;

	/**
	 * Instantiates a new domain schema.
	 *
	 * @param p8Def the filenet domain object
	 */
	public DomainSchema(Domain p8Def) {
		this.symbolicName = p8Def.get_Name();
		this.guid = p8Def.get_Id().toString();
	}

	/**
	 * Adds the object store schema.
	 *
	 * @param schema the object store schema.
	 */
	public void addObjectStoreSchema(ObjectStoreSchema schema) {
		if (objectStores == null)
			objectStores = new HashMap<>();
		objectStores.put(schema.getSymbolicName(), schema);
	}

	/**
	 * Gets the object store schema.
	 *
	 * @param symbolicName the object store symbolic name
	 * @return the object store schema
	 */
	public ObjectStoreSchema getObjectStoreSchema(String symbolicName) {
		return objectStores == null ? null : objectStores.get(symbolicName);
	}
}
