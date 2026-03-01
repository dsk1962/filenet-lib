package com.dkgeneric.filenet.content.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.model.P8Object;
import com.dkgeneric.filenet.content.model.P8Properties;
import com.dkgeneric.filenet.content.model.P8ResultSet;
import com.dkgeneric.filenet.content.model.P8RowResultSet;
import com.dkgeneric.filenet.content.provider.P8ProviderImpl;
import com.dkgeneric.filenet.content.request.ResultSetProcessingRequest;
import com.dkgeneric.filenet.content.request.SearchRequest;
import com.dkgeneric.filenet.content.response.ResultSetProcessingResponse;
import com.dkgeneric.filenet.content.response.RowSearchResponse;
import com.dkgeneric.filenet.content.response.SearchResponse;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.query.RepositoryRow;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class SearchService.
 */
@Component("p8contentlibSearchService")
@ConditionalOnProperty(name = "com.davita.ecm.p8.content.service.enabled", matchIfMissing = true)

/** The Constant log. */
@Slf4j
public class SearchService extends AuthorizationBasedService {

	public SearchService(@Qualifier("p8contentlibAuthService") AuthService authService,
			@Qualifier("p8ContentLibConfig") ApplicationConfig clientConfig,
			@Qualifier("p8contentlibUtilities") Utilities utilities, ValidationService validationService) {
		super(authService, clientConfig, utilities, validationService);
	}

	/**
	 * Search documents.
	 *
	 * @param request the request
	 * @return the search response
	 */
	public ResultSetProcessingResponse processDocuments(ResultSetProcessingRequest request) {
		log.debug("processDocuments.Entry {}", request);
		ResultSetProcessingResponse result = new ResultSetProcessingResponse();
		String query = null;
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request);) {
			query = utilities.prepareQuery(request.getSearchData(), p8ProviderImpl);
			// try to get query by template name
			p8ProviderImpl.processDocuments(query, request.getSearchParameters(), request.getResultSetProcessor());
			result.setResult(request.getResultSetProcessor().getProcessingResult());
		} catch (Exception e) {
			utilities.setResponseErrors(result, e);
			if (!(e instanceof ServiceException))
				log.error("processDocuments method catched exception. Request: {}, Response: {}", request, result, e);
		}
		log.debug("searchDocuments.Exit {}", result);
		return result;
	}

	/**
	 * Search documents.
	 *
	 * @param request the request
	 * @return the search response
	 */
	public SearchResponse searchDocuments(SearchRequest request) {
		log.debug("searchDocuments.Entry {}", request);
		SearchResponse result = new SearchResponse();
		String query = null;
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request);) {
			query = utilities.prepareQuery(request.getSearchData(), p8ProviderImpl);
			// try to get query by template name
			ArrayList<P8Object> p8ObjectList = new ArrayList<>();
			P8ResultSet p8ResultSet = p8ProviderImpl.searchDocuments(query, request.getSearchParameters());
			for (IndependentlyPersistableObject independentObject : p8ResultSet.getSearchResults())
				p8ObjectList.add(p8ProviderImpl.createP8Object(independentObject));
			result.setSearchResults(p8ObjectList);
			result.setTotalPageNumber(p8ResultSet.getTotalPageNumber());
			result.setTotalDocumentNumber(p8ResultSet.getTotalDocumentNumber());
		} catch (Exception e) {
			utilities.setResponseErrors(result, e);
			if (!(e instanceof ServiceException))
				log.error("searchDocuments method catched exception. Request: {}, Response: {}", request, result, e);
		}
		log.debug("searchDocuments.Exit {}", result);
		return result;
	}

	/**
	 * Search row set.
	 *
	 * @param request the request
	 * @return the row set search response
	 */
	public RowSearchResponse searchRows(SearchRequest request) {
		log.debug("searchRows.Entry {}", request);
		RowSearchResponse result = new RowSearchResponse();
		String query = null;
		try (P8ProviderImpl p8ProviderImpl = authService.createConnection(request);) {
			query = utilities.prepareQuery(request.getSearchData(), p8ProviderImpl);
			// try to get query by template name
			ArrayList<P8Properties> p8PropertiesList = new ArrayList<>();
			P8RowResultSet p8ResultSet = p8ProviderImpl.searchRows(query, request.getSearchParameters());
			for (RepositoryRow repositoryRow : p8ResultSet.getSearchResults())
				p8PropertiesList.add(P8ProviderImpl.createP8Properties(repositoryRow));
			result.setSearchResults(p8PropertiesList);
			result.setTotalPageNumber(p8ResultSet.getTotalPageNumber());
			result.setTotalRows(p8ResultSet.getTotalRows());
		} catch (Exception e) {
			utilities.setResponseErrors(result, e);
			if (!(e instanceof ServiceException))
				log.error("searchRows method catched exception. Request: {}, Response: {}", request, result, e);
		}
		log.debug("searchRows.Exit {}", result);
		return result;
	}

}
