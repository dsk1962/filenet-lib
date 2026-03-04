package com.dkgeneric.filenet.content.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;

import com.dkgeneric.commons.common.CommonsCache;
import com.dkgeneric.commons.exceptions.InvalidRequestException;
import com.dkgeneric.filenet.content.common.ECMConstants;
import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.exceptioncodes.ConfigExceptionCodes;
import com.dkgeneric.filenet.content.model.ClassDefinition;
import com.dkgeneric.filenet.content.model.DomainSchema;
import com.dkgeneric.filenet.content.model.ObjectStoreSchema;
import com.dkgeneric.filenet.content.model.PropertyDefinition;
import com.dkgeneric.filenet.content.provider.P8ProviderImpl;
import com.dkgeneric.filenet.content.provider.P8UserConnection;
import com.dkgeneric.filenet.content.request.BaseRequest;
import com.filenet.api.admin.PropertyTemplate;
import com.filenet.api.collection.ClassDefinitionSet;
import com.filenet.api.collection.PropertyTemplateSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Factory;
import com.filenet.api.util.UserContext;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Instantiates a new auth service.
 */
@Component("p8contentlibAuthService")
@DependsOn("appJsonConfigurationService")
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)

/** The Constant log. */
@Slf4j
public class AuthService extends BaseService implements CommonsCache {

	public AuthService(@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		super(clientConfig, utilities, validationService);
	}

	private DomainSchema domainSchema;

	/**
	 * Clear cached class definitions.
	 */
	@Override
	public void clearCaches() {
		domainSchema = null;
	}

	/**
	 * Creates the connection.
	 *
	 * @param request the request
	 * @return the p8 provider impl
	 * @throws ServiceException Signals that Filenet url is not configured correctly or object store name is not set 
	 */
	public P8ProviderImpl createConnection(BaseRequest request) throws ServiceException {
		P8ProviderImpl result = null;
		try {
			if (request.isUseContainerAuthentication())
				result = new P8UserConnection(this);
			else
				result = new P8ProviderImpl(this, request.getUserName(), request.getPassword());
			result.selectObjectStore(request.getDomainName(), request.getObjectStoreName());
			result.setBaseRequest(request);
			return result;
		} catch (ServiceException e) {
			if (result != null)
				result.close();
			throw e;
		}
	}

	public ClassDefinition getClassDefinition(BaseRequest request, String symbolicName) throws ServiceException {
		List<String> sList = new ArrayList<>(1);
		sList.add(symbolicName);
		List<ClassDefinition> result = getClassDefinitions(request, sList);
		return CollectionUtils.isEmpty(result) ? null : result.get(0);
	}

	public List<ClassDefinition> getClassDefinitions(BaseRequest request, List<String> symbolicNameList)
			throws ServiceException {
		if (!StringUtils.hasText(request.getObjectStoreName()))
			request.setObjectStoreName(this.getClientConfig().getObjectStoreName());
		boolean notInCache = domainSchema == null
				|| domainSchema.getObjectStoreSchema(request.getObjectStoreName()) == null;
		List<ClassDefinition> result = new ArrayList<>(symbolicNameList.size());
		if (!notInCache) {
			ObjectStoreSchema objectStoreSchema = domainSchema.getObjectStoreSchema(request.getObjectStoreName());
			for (String symbolicName : symbolicNameList) {
				ClassDefinition classDefinition = objectStoreSchema.getClassDefinition(symbolicName);
				notInCache = notInCache || classDefinition == null;
				if (notInCache)
					break;
				result.add(classDefinition);
			}
		}

		if (notInCache)
			return populateFromP8(request, symbolicNameList);
		return result;
	}

	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 * @throws ServiceException  Signals that Filenet url is not configured correctly
	 */
	public Connection getConnection() throws ServiceException {
		return getConnection(null, null);
	}

	/**
	 * Gets the connection.
	 *
	 * @param userName the user name
	 * @param password the password
	 * @return the connection
	 * @throws ServiceException Signals that Filenet url is not configured correctly
	 */
	public Connection getConnection(String userName, String password) throws ServiceException {
		if (!StringUtils.hasText(getClientConfig().getCpeUrl()))
			throw new ServiceException(ConfigExceptionCodes.CE_URL_MISSING.getExceptionCode());
		log.debug("getConnection. {}", getClientConfig().getCpeUrl());
		Connection connection = Factory.Connection.getConnection(getClientConfig().getCpeUrl());
		UserContext uc = UserContext.get();
		if (!StringUtils.hasText(userName) || !StringUtils.hasText(password)) {
			userName = getClientConfig().getCpeUsername();
			password = getClientConfig().getCpePassword();
		}
		if (!StringUtils.hasText(userName))
			throw new ServiceException(ConfigExceptionCodes.USER_NAME_MISSING.getExceptionCode());
		else if (!StringUtils.hasText(password))
			throw new ServiceException(ConfigExceptionCodes.PASSWORD_MISSING.getExceptionCode());
		Subject subject = UserContext.createSubject(connection, userName, password,
				getClientConfig().getCpeLoginModule());
		uc.pushSubject(subject);
		return connection;
	}

	public PropertyDefinition getPropertyDefinition(P8ProviderImpl p8ProviderImpl, String symbolicName) {
		List<String> sList = new ArrayList<>(1);
		sList.add(symbolicName);
		List<PropertyDefinition> result = getPropertyDefinitions(p8ProviderImpl, sList);
		return CollectionUtils.isEmpty(result) ? null : result.get(0);
	}

	public List<PropertyDefinition> getPropertyDefinitions(P8ProviderImpl p8ProviderImpl,
			List<String> symbolicNameList) {
		ObjectStoreSchema objectStoreSchema = populatePropertiesFromP8(p8ProviderImpl);
		List<PropertyDefinition> result = new ArrayList<>();
		for (String symbolicName : symbolicNameList) {
			int pos = symbolicName.indexOf('.');
			if (pos >= 0)
				symbolicName = symbolicName.substring(pos + 1);

			PropertyDefinition propertyDefinition = objectStoreSchema.getPropertyDefinition(symbolicName);
			if (propertyDefinition == null)
				throw new InvalidRequestException("Unknown property name: {}", symbolicName);
			result.add(propertyDefinition);
		}
		return result;
	}

	/**
	 * Gets the user connection.
	 *
	 * @return the user connection
	 * @throws ServiceException Signals that Filenet url is not configured correctly
	 */
	public Connection getUserConnection() throws ServiceException {
		if (!StringUtils.hasText(getClientConfig().getCpeUrl()))
			throw new ServiceException(ConfigExceptionCodes.CE_URL_MISSING.getExceptionCode());
		return Factory.Connection.getConnection(getClientConfig().getCpeUrl());
	}

	@SuppressWarnings("unchecked")
	private synchronized void populateClassDefinition(ObjectStoreSchema objectStoreSchema,
			com.filenet.api.admin.ClassDefinition p8ClassDefinition, ClassDefinition superClassDefinition,
			P8ProviderImpl p8ProviderImpl) {
		log.debug("Populate class. Symbolic Name: {}, Display Name: {}: ", p8ClassDefinition.get_SymbolicName(),
				p8ClassDefinition.get_DisplayName());
		ClassDefinition classDefinition = new ClassDefinition(p8ClassDefinition);
		if (superClassDefinition == null) {
			com.filenet.api.admin.ClassDefinition p8SuperClassDefinition = p8ClassDefinition.get_SuperclassDefinition();
			if (p8SuperClassDefinition != null)
				superClassDefinition = objectStoreSchema.getClassDefinition(p8SuperClassDefinition.get_SymbolicName());
		}
		objectStoreSchema.addClassDefinition(classDefinition);
		if (superClassDefinition != null) {
			superClassDefinition.getSubClassDefinitions().add(classDefinition);
			classDefinition.setSuperClassDefinition(superClassDefinition);
		}
		ClassDefinitionSet set = p8ClassDefinition.get_ImmediateSubclassDefinitions();
		if (set != null)
			set.iterator().forEachRemaining(clDef -> populateClassDefinition(objectStoreSchema,
					(com.filenet.api.admin.ClassDefinition) clDef, classDefinition, p8ProviderImpl));
	}

	private synchronized ClassDefinition populateClassDefinition(ObjectStoreSchema objectStoreSchema,
			String symbolicName, P8ProviderImpl p8ProviderImpl) {
		com.filenet.api.admin.ClassDefinition p8ClassDefinition = p8ProviderImpl.getClassDefinition(symbolicName);
		boolean isPopulated = false;
		for (;;) {
			com.filenet.api.admin.ClassDefinition p8SuperClassDefinition = p8ClassDefinition.get_SuperclassDefinition();
			if (p8SuperClassDefinition == null) {
				populateClassDefinition(objectStoreSchema, p8ClassDefinition, null, p8ProviderImpl);
				isPopulated = true;
			} else if (objectStoreSchema.getClassDefinition(p8SuperClassDefinition.get_SymbolicName()) != null) {
				populateClassDefinition(objectStoreSchema, p8ClassDefinition,
						objectStoreSchema.getClassDefinition(p8SuperClassDefinition.get_SymbolicName()),
						p8ProviderImpl);
				isPopulated = true;
			} else
				p8ClassDefinition = p8SuperClassDefinition;
			if (isPopulated)
				break;
		}

		return objectStoreSchema.getClassDefinition(symbolicName);
	}

	private List<ClassDefinition> populateClassDefinitions(ObjectStoreSchema objectStoreSchema,
			List<String> symbolicNameList, P8ProviderImpl p8ProviderImpl) {
		List<ClassDefinition> result = new ArrayList<>(symbolicNameList.size());
		if (!CollectionUtils.isEmpty(symbolicNameList))
			for (String symbolicName : symbolicNameList) {
				ClassDefinition classDefinition = objectStoreSchema.getClassDefinition(symbolicName);
				if (classDefinition == null) {
					classDefinition = populateClassDefinition(objectStoreSchema, symbolicName, p8ProviderImpl);
					objectStoreSchema.addClassDefinition(classDefinition);
				}
				result.add(classDefinition);
			}
		return result;
	}

	@PostConstruct
	void populateDefaultSchema() {

		try {
			if (getClientConfig().isPopulateDefaultSchemaOnStart()) {
				if (StringUtils.hasText(getClientConfig().getCpeUsername())
						&& StringUtils.hasText(getClientConfig().getCpePassword())
						&& StringUtils.hasText(getClientConfig().getObjectStoreName())) {
					log.info("Populate default object store schema. Object store: {}",
							getClientConfig().getObjectStoreName());
					getClassDefinition(new BaseRequest(), ECMConstants.P8_DOCUMENT_CLASSNAME);
					getClassDefinition(new BaseRequest(), ECMConstants.P8_CUSTOMOBJECT_CLASSNAME);
				} else
					log.info("Populate default object store schema skipped. Incomplete configuration.");
			} else
				log.info("Populate default object store schema skipped(populate flag value is false).");
		} catch (Exception e) {
			log.error("Default schema population failed", e);
		}
	}

	private List<ClassDefinition> populateFromP8(BaseRequest request, List<String> symbolicNameList)
			throws ServiceException {
		try (P8ProviderImpl p8ProviderImpl = createConnection(request)) {
			if (domainSchema == null)
				domainSchema = new DomainSchema(p8ProviderImpl.getDomain());
			ObjectStoreSchema objectStoreSchema = domainSchema.getObjectStoreSchema(request.getObjectStoreName());
			if (objectStoreSchema == null) {
				objectStoreSchema = new ObjectStoreSchema(p8ProviderImpl.getObjectStore());
				domainSchema.addObjectStoreSchema(objectStoreSchema);
				// populate predefined base classes
				String[] defaultNames = { ECMConstants.P8_DOCUMENT_CLASSNAME, ECMConstants.P8_CUSTOMOBJECT_CLASSNAME };
				try {
					populateClassDefinitions(objectStoreSchema, Arrays.asList(defaultNames), p8ProviderImpl);
				} catch (Exception e) {
					log.error("Default schema population failed", e);
				}
			}
			return populateClassDefinitions(objectStoreSchema, symbolicNameList, p8ProviderImpl);
		}
	}

	@SuppressWarnings("unchecked")
	private ObjectStoreSchema populatePropertiesFromP8(P8ProviderImpl p8ProviderImpl) {
		if (domainSchema == null)
			domainSchema = new DomainSchema(p8ProviderImpl.getDomain());
		ObjectStoreSchema objectStoreSchema = domainSchema
				.getObjectStoreSchema(p8ProviderImpl.getObjectStore().get_SymbolicName());
		if (objectStoreSchema == null) {
			objectStoreSchema = new ObjectStoreSchema(p8ProviderImpl.getObjectStore());
			domainSchema.addObjectStoreSchema(objectStoreSchema);
		}
		if (CollectionUtils.isEmpty(objectStoreSchema.getPropertyDefinitions())) {
			PropertyTemplateSet pts = p8ProviderImpl.getObjectStore().get_PropertyTemplates();
			LinkedCaseInsensitiveMap<PropertyDefinition> map = new LinkedCaseInsensitiveMap<>();
			pts.iterator().forEachRemaining(pt -> {
				PropertyDefinition pd = new PropertyDefinition((PropertyTemplate) pt);
				map.put(pd.getSymbolicName(), pd);
			});
			PropertyDefinition pdef = new PropertyDefinition(ECMConstants.P8_DATECREATED_PROPERTYNAME,
					PropertyDefinition.DATE);
			map.put(pdef.getSymbolicName(), pdef);
			pdef = new PropertyDefinition(ECMConstants.P8_DATELASTMODIFIED_PROPERTYNAME, PropertyDefinition.DATE);
			map.put(pdef.getSymbolicName(), pdef);
			pdef = new PropertyDefinition(ECMConstants.P8_CREATOR_PROPERTYNAME, PropertyDefinition.STRING);
			map.put(pdef.getSymbolicName(), pdef);
			pdef = new PropertyDefinition(ECMConstants.P8_LASTMODIFIER_PROPERTYNAME, PropertyDefinition.STRING);
			map.put(pdef.getSymbolicName(), pdef);
			pdef = new PropertyDefinition(ECMConstants.P8_MIMETYPE_PROPERTYNAME, PropertyDefinition.STRING);
			map.put(pdef.getSymbolicName(), pdef);
			pdef = new PropertyDefinition(ECMConstants.P8_ID_PROPERTYNAME, PropertyDefinition.GUID);
			map.put(pdef.getSymbolicName(), pdef);
			objectStoreSchema.setPropertyDefinitions(map);
		}
		return objectStoreSchema;
	}

	/**
	 * Restore subject.
	 */
	public void restoreSubject() {
		UserContext.get().popSubject();
	}
}
