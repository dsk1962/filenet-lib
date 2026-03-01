package com.dkgeneric.filenet.content.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.LinkedCaseInsensitiveMap;

import com.filenet.api.collection.PropertyDefinitionList;

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
public class ClassDefinition {

	/**
	 * FileNet class definition descriptive text.
	 * 
	 * @param descriptiveText the new class definition descriptive text
	 * @return Current class definition descriptive text
	 */
	private String descriptiveText;
	/**
	 * FileNet class definition display name.
	 * 
	 * @param displayName the new class definition display name
	 * @return Current class definition display name
	 */
	private String displayName;
	/**
	 * FileNet class definition guid.
	 * 
	 * @param p8Guid the new class definition guid
	 * @return Current class definition guid
	 */
	private String p8Guid;
	/**
	 * FileNet class definition symbolic name.
	 * 
	 * @param symbolicName the new class definition symbolic name
	 * @return Current class definition symbolic name
	 */
	private String symbolicName;
	/**
	 * FileNet class definition systemOwned flag
	 * 
	 * @param systemOwned the new class definition systemOwned flag value
	 * @return Current class definition systemOwned flag value ( true if class definition is system owned)
	 */
	private boolean systemOwned;
	/**
	 * FileNet class definition hidden flag
	 * 
	 * @param hidden the new class definition hidden flag value
	 * @return Current class definition hidden flag value 
	 */
	private boolean hidden;

	/**
	 * FileNet super class definition
	 * 
	 * @param superClassDefinition the new super class definition value
	 * @return Current super class definition value 
	 */
	private ClassDefinition superClassDefinition;

	/**
	 * FileNet sub class definition list
	 * 
	 * @param superClassDefinition the new sub class definition list
	 * @return Current sub class definition list 
	 */
	private List<ClassDefinition> subClassDefinitions = new ArrayList<>(10);

	/**
	 * FileNet property definition map. Key value is property symbolic name  
	 * 
	 * @param propertyDefinitions the new property definition map
	 * @return Current property definition map 
	 */
	private LinkedCaseInsensitiveMap<PropertyDefinition> propertyDefinitions = new LinkedCaseInsensitiveMap<>();

	/**
	 * Instantiates a new class definition object.
	 *
	 * @param p8Def filenet class definition object
	 */
	public ClassDefinition(com.filenet.api.admin.ClassDefinition p8Def) {
		this.symbolicName = p8Def.get_SymbolicName();
		this.descriptiveText = p8Def.get_DescriptiveText();
		this.displayName = p8Def.get_DisplayName();
		this.systemOwned = p8Def.get_IsSystemOwned();
		this.hidden = p8Def.get_IsHidden() != null && p8Def.get_IsHidden();

		this.hidden = p8Def.get_AllowsInstances() != null && p8Def.get_AllowsInstances();
		this.hidden = p8Def.get_IsCBREnabled() != null && p8Def.get_IsCBREnabled();
		this.p8Guid = p8Def.get_Id().toString();

		PropertyDefinitionList pdl = p8Def.get_PropertyDefinitions();
		for (int i = 0; i < pdl.size(); i++)
			addPropertyDefinition(new PropertyDefinition((com.filenet.api.admin.PropertyDefinition) pdl.get(i)));
	}

	/**
	 * Adds the property definition.
	 *
	 * @param definition the property definition
	 */
	private void addPropertyDefinition(PropertyDefinition definition) {
		propertyDefinitions.put(definition.getSymbolicName(), definition);
	}

	/**
	 * Gets the property definition.
	 *
	 * @param symbolicName the property symbolic name
	 * @return the property definition
	 */
	public PropertyDefinition getPropertyDefinition(String symbolicName) {
		return propertyDefinitions.get(symbolicName);
	}
}
