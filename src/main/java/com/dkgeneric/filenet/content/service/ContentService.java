package com.dkgeneric.filenet.content.service;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.dkgeneric.commons.common.ApplicationValue;
import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.exceptioncodes.ContentServiceExceptionCodes;
import com.dkgeneric.filenet.content.model.P8ContentObjectAuditInfo;
import com.dkgeneric.filenet.content.provider.P8ProviderImpl;
import com.dkgeneric.filenet.content.request.GetContentRequest;
import com.dkgeneric.filenet.content.response.GetContentResponse;
import com.filenet.api.core.Annotation;
import com.filenet.api.core.Document;
import com.filenet.api.core.IndependentlyPersistableObject;

import lombok.extern.slf4j.Slf4j;

/**
 * The Content Service class.
 */
@Component("p8contentlibContentService")

/** The Constant log. */
@Slf4j
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)
public class ContentService extends AuthorizationBasedService {

	// Regex pattern for P8 guid format
	private static final String P8_UUID_REGEX = "\\{[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}\\}";
	private static final Pattern P8_UUID_PATTERN = Pattern.compile(P8_UUID_REGEX);

	@ApplicationValue(key = "nonGuidValueSQL")
	private String nonGuidValueSQL = "SELECT Id,ContentElements FROM Document WHERE DVALegacyRepositoryDocId = '*REALVALUE*' AND DVAAvailabilityStatus = 'Active' AND IsCurrentVersion=true";

	public ContentService(@Qualifier("p8contentlibAuthService") AuthService authService,
			@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		super(authService, clientConfig, utilities, validationService);
	}

	/**
	 * Gets the FileNet object content.
	 *
	 * @param request the request data
	 * @return the content
	 */
	public GetContentResponse getContent(GetContentRequest request) {
		log.debug("getContent.Entry {}", request);
		GetContentResponse result = new GetContentResponse();
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request)) {
			if (!StringUtils.hasText(request.getDocumentId()) && request.getSearchData() == null)
				throw new ServiceException(ContentServiceExceptionCodes.MISSING_INPUT_PARAMETERS.getExceptionCode());
			IndependentlyPersistableObject document = null;
			if (StringUtils.hasText(request.getDocumentId())) {
				if (P8_UUID_PATTERN.matcher(request.getDocumentId()).matches())
					document = p8ProviderImpl.getDocumentById(request.getDocumentId());
				else {
					log.debug("Non guid value provided, Use search. Value: {}", request.getDocumentId());
					String searchValue = StringUtils.replace(request.getDocumentId(), "'", "''");
					document = p8ProviderImpl.searchDocument(StringUtils.replace(nonGuidValueSQL, "*REALVALUE*", searchValue));
				}
			} else
				document = p8ProviderImpl
						.searchDocument(utilities.prepareQuery(request.getSearchData(), p8ProviderImpl));
			if (document == null)
				throw new ServiceException(ContentServiceExceptionCodes.DOCUMENT_NOT_FOUND.getExceptionCode(),
						new Object[] { request.getDocumentId(), request.getSearchData() });
			result.setP8DocumentResource(p8ProviderImpl.getContentResource(document));
			P8ContentObjectAuditInfo auditInfo = new P8ContentObjectAuditInfo();
			auditInfo.setContentResource(result.getP8DocumentResource());
			auditInfo.setObjectClass(document.getClassName());
			if (document instanceof Document g)
				auditInfo.setObjectId(g.get_Id().toString());
			else if (document instanceof Annotation a)
				auditInfo.setObjectId(a.get_Id().toString());
			result.setAuditInfo(auditInfo);
		} catch (Exception e) {
			utilities.setResponseErrors(result, e, request.getDocumentId());
			if (!(e instanceof ServiceException))
				log.error("getContent method catched exception.Request: {}, Response: {}", request, result, e);
		}
		log.debug("getContent.Exit {}", result);
		return result;
	}
}
