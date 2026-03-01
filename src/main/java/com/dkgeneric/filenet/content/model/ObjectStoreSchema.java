package com.dkgeneric.filenet.content.model;

import org.springframework.util.LinkedCaseInsensitiveMap;

import com.filenet.api.core.ObjectStore;

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
public class ObjectStoreSchema {

	/**
	 * FileNet object store descriptive text.
	 * 
	 * @param descriptiveText the new object store descriptive text
	 * @return Current object store descriptive text
	 */
	private String descriptiveText;
	/**
	 * FileNet object store display name.
	 * 
	 * @param displayName the new object store display name
	 * @return Current object store display name
	 */
	private String displayName;
	/**
	 * FileNet object store guid.
	 * 
	 * @param guid the new object store guid
	 * @return Current object store guid
	 */
	private String guid;
	/**
	 * FileNet object store symbolic name.
	 * 
	 * @param symbolicName the new object store symbolic name
	 * @return Current object store symbolic name
	 */
	private String symbolicName;

	/**
	 * FileNet object store text search enabled flag.
	 * 
	 * @param textSearchEnabled the new object store text search enabled flag value
	 * @return Current object store text search enabled flag value
	 */
	private boolean textSearchEnabled;

	/**
	 *  Class definition map defined in this object store. Key value is class definition symbolic name
	 * 
	 * @param classDefinitions the new class definition map
	 * @return Current class definition map
	 */
	private LinkedCaseInsensitiveMap<ClassDefinition> classDefinitions;

	/**
	 *  Class definition map defined in this object store. Key value is class definition symbolic name
	 * 
	 * @param classDefinitions the new class definition map
	 * @return Current class definition map
	 */
	private LinkedCaseInsensitiveMap<PropertyDefinition> propertyDefinitions;

	/**
	 * Instantiates a new object store schema
	 *
	 * @param p8Def the Filenet object store object
	 */
	public ObjectStoreSchema(ObjectStore p8Def) {
		this.symbolicName = p8Def.get_SymbolicName();
		this.descriptiveText = p8Def.get_DescriptiveText();
		this.displayName = p8Def.get_DisplayName();
		this.textSearchEnabled = p8Def.get_TextSearchEnabled();
	}

	/**
	 * Adds the class definition.
	 *
	 * @param definition the definition
	 */
	public synchronized void addClassDefinition(ClassDefinition definition) {
		if (classDefinitions == null)
			classDefinitions = new LinkedCaseInsensitiveMap<>();
		classDefinitions.put(definition.getSymbolicName(), definition);
	}

	/**
	 * Adds the property definition.
	 *
	 * @param definition the definition
	 */
	public synchronized void addPropertyDefinition(PropertyDefinition definition) {
		if (propertyDefinitions == null)
			propertyDefinitions = new LinkedCaseInsensitiveMap<>();
		propertyDefinitions.put(definition.getSymbolicName(), definition);
	}

	/**
	 * Gets the class definition.
	 *
	 * @param symbolicName the symbolic name
	 * @return the class definition
	 */
	public ClassDefinition getClassDefinition(String symbolicName) {
		return classDefinitions == null ? null : classDefinitions.get(symbolicName);
	}

	/**
	 * Gets the property definition.
	 *
	 * @param symbolicName the symbolic name
	 * @return the property definition
	 */
	public PropertyDefinition getPropertyDefinition(String symbolicName) {
		return propertyDefinitions == null ? null : propertyDefinitions.get(symbolicName);
	}
}
