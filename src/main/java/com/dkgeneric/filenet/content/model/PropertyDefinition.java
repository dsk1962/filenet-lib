package com.dkgeneric.filenet.content.model;

import com.filenet.api.admin.PropertyDefinitionBinary;
import com.filenet.api.admin.PropertyDefinitionBoolean;
import com.filenet.api.admin.PropertyDefinitionDateTime;
import com.filenet.api.admin.PropertyDefinitionFloat64;
import com.filenet.api.admin.PropertyDefinitionId;
import com.filenet.api.admin.PropertyDefinitionInteger32;
import com.filenet.api.admin.PropertyDefinitionObject;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.admin.PropertyTemplate;
import com.filenet.api.admin.PropertyTemplateBinary;
import com.filenet.api.admin.PropertyTemplateBoolean;
import com.filenet.api.admin.PropertyTemplateDateTime;
import com.filenet.api.admin.PropertyTemplateFloat64;
import com.filenet.api.admin.PropertyTemplateId;
import com.filenet.api.admin.PropertyTemplateInteger32;
import com.filenet.api.admin.PropertyTemplateString;
import com.filenet.api.constants.Cardinality;
import com.filenet.api.constants.PropertySettability;
import com.filenet.api.constants.TypeID;

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
public class PropertyDefinition {


	// Property type constants
	public static final int STRING = TypeID.STRING_AS_INT;
	public static final int LONG = TypeID.LONG_AS_INT;
	public static final int BOOLEAN = TypeID.BOOLEAN_AS_INT;
	public static final int DATE = TypeID.DATE_AS_INT;
	public static final int DOUBLE = TypeID.DOUBLE_AS_INT;
	public static final int GUID = TypeID.GUID_AS_INT;
	public static final int BINARY = TypeID.BINARY_AS_INT;
	public static final int OBJECT = TypeID.OBJECT_AS_INT;
	/**
	 * FileNet choice list name assigned to this property.
	 * 
	 * @param choiceListName the new property choice list name
	 * @return Current property choice list name
	 */
	private String choiceListName;

	/**
	 * FileNet property default value.
	 * 
	 * @param defaultValue the new property default value
	 * @return Current property default value
	 */
	private Object defaultValue;
	/**
	 * FileNet property max value.
	 * 
	 * @param maxValue the new property max value.
	 * @return Current property max value.
	 */
	private Object maxValue;
	/**
	 * FileNet property minimal value.
	 * 
	 * @param minValue the new property minimal value
	 * @return Current property minimal value
	 */
	private Object minValue;
	/**
	 * FileNet property descriptive text.
	 * 
	 * @param descriptiveText the new property descriptive text
	 * @return Current property descriptive text
	 */
	private String descriptiveText;
	/**
	 * FileNet property display name.
	 * 
	 * @param displayName the new property display name
	 * @return Current property display name
	 */
	private String displayName;
	/**
	 * FileNet property guid.
	 * 
	 * @param p8Guid the new property guid
	 * @return Current property guid
	 */
	private String p8Guid;
	/**
	 * FileNet property symbolic name.
	 * 
	 * @param symbolicName the new property symbolic name
	 * @return Current property symbolic name
	 */
	private String symbolicName;
	/**
	 * FileNet property type. See type constants declared in this class
	 * 
	 * @param type the new property type value
	 * @return Current property type value
	 */
	private int type;

	/**
	 * FileNet property cardinality.
	 * 
	 * @param cardinality the new property cardinality value
	 * @return Current property cardinality value
	 */
	private int cardinality;
	/**
	 * FileNet property max length.
	 * 
	 * @param maxStringLength the new property maxStringLength value
	 * @return Current property maxStringLength value
	 */
	private int maxStringLength;
	/**
	 * FileNet property systemOwned flag
	 * 
	 * @param systemOwned the new property systemOwned flag value
	 * @return Current property systemOwned flag value ( true if property is sytem owned)
	 */
	private boolean systemOwned;
	/**
	 * FileNet property hidden flag
	 * 
	 * @param hidden the new property hidden flag value
	 * @return Current property hidden flag value 
	 */
	private boolean hidden;
	/**
	 * FileNet property  name.
	 * 
	 * @param name the new property name
	 * @return Current property  name
	 */
	private boolean name;
	/**
	 * FileNet property copyToReservation flag
	 * 
	 * @param copyToReservation the new property copyToReservation flag value
	 * @return Current property copyToReservation flag value ( true if will be copied on document checkout)
	 */
	private boolean copyToReservation;
	/**
	 * FileNet property required flag
	 * 
	 * @param required the new property required flag value
	 * @return Current property required flag value ( true if property value is required)
	 */
	private boolean required;
	/**
	 * FileNet property readOnly flag
	 * 
	 * @param required the new property readOnly flag value
	 * @return Current property readOnly flag value ( true if property value is readOnly)
	 */
	private boolean readOnly;
	/**
	 * FileNet property multivalue flag
	 * 
	 * @param multiValue the new property multiValue flag value
	 * @return Current property multiValue flag value ( true if property is multivalue)
	 */
	private boolean multiValue;

	/**
	 * Instantiates a new property definition object.
	 *
	 * @param p8Def Filenet property definition object
	 */
	public PropertyDefinition(com.filenet.api.admin.PropertyDefinition p8Def) {
		this.symbolicName = p8Def.get_SymbolicName();
		this.descriptiveText = p8Def.get_DescriptiveText();
		this.displayName = p8Def.get_DisplayName();
		this.type = p8Def.get_DataType().getValue();
		this.multiValue = p8Def.get_Cardinality().getValue() == Cardinality.LIST_AS_INT;
		this.cardinality = p8Def.get_Cardinality().getValue();
		this.systemOwned = p8Def.get_IsSystemOwned();
		this.hidden = p8Def.get_IsHidden();
		this.name = p8Def.get_IsNameProperty();
		this.readOnly = !p8Def.get_Settability().equals(PropertySettability.READ_WRITE);
		this.required = p8Def.get_IsValueRequired();
		this.choiceListName = p8Def.get_ChoiceList() == null ? null : p8Def.get_ChoiceList().get_Name();
		this.copyToReservation = p8Def.get_CopyToReservation();
		this.p8Guid = p8Def.get_Id().toString();

		switch (type) {
		case STRING:
			PropertyDefinitionString pds = (PropertyDefinitionString) p8Def;
			this.maxStringLength = pds.get_MaximumLengthString() == null ? 0 : pds.get_MaximumLengthString();
			this.defaultValue = pds.get_PropertyDefaultString();
			break;
		case DOUBLE:
			PropertyDefinitionFloat64 pdf = (PropertyDefinitionFloat64) p8Def;
			this.maxValue = pdf.get_PropertyMaximumFloat64();
			this.minValue = pdf.get_PropertyMinimumFloat64();
			this.defaultValue = pdf.get_PropertyDefaultFloat64();
			break;
		case DATE:
			PropertyDefinitionDateTime pdd = (PropertyDefinitionDateTime) p8Def;
			this.maxValue = pdd.get_PropertyMaximumDateTime();
			this.minValue = pdd.get_PropertyMinimumDateTime();
			this.defaultValue = pdd.get_PropertyDefaultDateTime();
			break;
		case BINARY:
			PropertyDefinitionBinary pdbin = (PropertyDefinitionBinary) p8Def;
			this.defaultValue = pdbin.get_PropertyDefaultBinary();
			break;
		case BOOLEAN:
			PropertyDefinitionBoolean pdb = (PropertyDefinitionBoolean) p8Def;
			this.defaultValue = pdb.get_PropertyDefaultBoolean();
			break;
		case GUID:
			PropertyDefinitionId pdi = (PropertyDefinitionId) p8Def;
			this.defaultValue = pdi.get_PropertyDefaultId();
			break;
		case LONG:
			PropertyDefinitionInteger32 pdl = (PropertyDefinitionInteger32) p8Def;
			this.maxValue = pdl.get_PropertyMaximumInteger32();
			this.minValue = pdl.get_PropertyMinimumInteger32();
			this.defaultValue = pdl.get_PropertyDefaultInteger32();
			break;
		case OBJECT:
			PropertyDefinitionObject pdo = (PropertyDefinitionObject) p8Def;
			this.defaultValue = pdo.get_PropertyDefaultObject();
			break;
		default:
		}
	}

	/**
	 * Instantiates a new property definition object.
	 *
	 * @param p8Def Filenet property definition object
	 */
	public PropertyDefinition(PropertyTemplate p8Template) {
		this.symbolicName = p8Template.get_SymbolicName();
		this.descriptiveText = p8Template.get_DescriptiveText();
		this.displayName = p8Template.get_DisplayName();
		this.type = p8Template.get_DataType().getValue();
		this.multiValue = p8Template.get_Cardinality().getValue() == Cardinality.LIST_AS_INT;
		this.cardinality = p8Template.get_Cardinality().getValue();
		this.hidden = toBoolean(p8Template.get_IsHidden());
		this.name = toBoolean(p8Template.get_IsNameProperty());
		this.required = toBoolean(p8Template.get_IsValueRequired());
		this.choiceListName = p8Template.get_ChoiceList() == null ? null : p8Template.get_ChoiceList().get_Name();
		this.p8Guid = p8Template.get_Id().toString();

		switch (type) {
		case STRING:
			PropertyTemplateString pds = (PropertyTemplateString) p8Template;
			this.maxStringLength = pds.get_MaximumLengthString() == null ? 0 : pds.get_MaximumLengthString();
			this.defaultValue = pds.get_PropertyDefaultString();
			break;
		case DOUBLE:
			PropertyTemplateFloat64 pdf = (PropertyTemplateFloat64) p8Template;
			this.maxValue = pdf.get_PropertyMaximumFloat64();
			this.minValue = pdf.get_PropertyMinimumFloat64();
			this.defaultValue = pdf.get_PropertyDefaultFloat64();
			break;
		case DATE:
			PropertyTemplateDateTime pdd = (PropertyTemplateDateTime) p8Template;
			this.maxValue = pdd.get_PropertyMaximumDateTime();
			this.minValue = pdd.get_PropertyMinimumDateTime();
			this.defaultValue = pdd.get_PropertyDefaultDateTime();
			break;
		case BINARY:
			PropertyTemplateBinary pdbin = (PropertyTemplateBinary) p8Template;
			this.defaultValue = pdbin.get_PropertyDefaultBinary();
			break;
		case BOOLEAN:
			PropertyTemplateBoolean pdb = (PropertyTemplateBoolean) p8Template;
			this.defaultValue = pdb.get_PropertyDefaultBoolean();
			break;
		case GUID:
			PropertyTemplateId pdi = (PropertyTemplateId) p8Template;
			this.defaultValue = pdi.get_PropertyDefaultId();
			break;
		case LONG:
			PropertyTemplateInteger32 pdl = (PropertyTemplateInteger32) p8Template;
			this.maxValue = pdl.get_PropertyMaximumInteger32();
			this.minValue = pdl.get_PropertyMinimumInteger32();
			this.defaultValue = pdl.get_PropertyDefaultInteger32();
			break;
		default:
		}
	}

	public PropertyDefinition(String name, int type) {
		symbolicName = name;
		this.type = type;
	}

	private boolean toBoolean(Boolean b) {
		return b != null && b;
	}
}
