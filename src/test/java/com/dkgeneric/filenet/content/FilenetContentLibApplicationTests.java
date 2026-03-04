package com.dkgeneric.filenet.content;

import static com.dkgeneric.commons.config.CommonsErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.CollectionUtils;

import com.dkgeneric.commons.config.CommonsErrorMessages;
import com.dkgeneric.commons.config.GitInformations;
import com.dkgeneric.filenet.content.common.ServiceException;
import com.dkgeneric.filenet.content.common.Utilities;
import com.dkgeneric.filenet.content.config.ApplicationConfig;
import com.dkgeneric.filenet.content.config.P8SearchConfiguration;
import com.dkgeneric.filenet.content.exceptioncodes.ConfigExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.ContentServiceExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.DocumentServiceExceptionCodes;
import com.dkgeneric.filenet.content.exceptioncodes.SearchServiceExceptionCodes;
import com.dkgeneric.filenet.content.model.P8ContentObject;
import com.dkgeneric.filenet.content.model.P8ResultSet;
import com.dkgeneric.filenet.content.model.SearchData;
import com.dkgeneric.filenet.content.model.SearchParameters;
import com.dkgeneric.filenet.content.provider.P8ProviderImpl;
import com.dkgeneric.filenet.content.request.BaseRequest;
import com.dkgeneric.filenet.content.request.CreateDocumentRequest;
import com.dkgeneric.filenet.content.request.CreateDocumentVersionRequest;
import com.dkgeneric.filenet.content.request.GetClassDefinitionRequest;
import com.dkgeneric.filenet.content.request.GetContentRequest;
import com.dkgeneric.filenet.content.request.GetDocumentByIdRequest;
import com.dkgeneric.filenet.content.request.SearchRequest;
import com.dkgeneric.filenet.content.request.UpdateDocumentMetadataRequest;
import com.dkgeneric.filenet.content.resources.P8ContentResource;
import com.dkgeneric.filenet.content.response.BaseResponse;
import com.dkgeneric.filenet.content.response.CreateDocumentResponse;
import com.dkgeneric.filenet.content.response.GetClassDefinitionResponse;
import com.dkgeneric.filenet.content.response.GetContentResponse;
import com.dkgeneric.filenet.content.response.GetDocumentByIdResponse;
import com.dkgeneric.filenet.content.response.RowSearchResponse;
import com.dkgeneric.filenet.content.response.SearchResponse;
import com.dkgeneric.filenet.content.response.UpdateDocumentMetadataResponse;
import com.dkgeneric.filenet.content.service.AuthService;
import com.dkgeneric.filenet.content.service.ContentService;
import com.dkgeneric.filenet.content.service.DocumentService;
import com.dkgeneric.filenet.content.service.SchemaService;
import com.dkgeneric.filenet.content.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filenet.api.core.Document;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.property.Properties;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import ch.qos.logback.classic.Level;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@TestPropertySource(locations = "classpath:/application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
@EnableEncryptableProperties
@Slf4j
class FilenetContentLibApplicationTests {

	@Autowired
	DocumentService documentService;
	@Autowired
	SchemaService schemaService;
	@Autowired
	ContentService contentService;
	@Autowired
	SearchService searchService;
	@Autowired
	AuthService authService;
	@Autowired
	Utilities utilities;
	@Autowired
	ApplicationConfig clientConfig;
	@Autowired
	CommonsErrorMessages errorMessages;
	@Autowired
	P8SearchConfiguration p8SearchConfiguration;
	@Autowired
	private GitInformations gitInformations;
	@Autowired
	protected ObjectMapper objectMapper;

	// json search text
	@Getter
	@Setter
	@Value("${jsonSearchTest}")
	private String jsonSearchTest;

	static final int DOCUMENTSTOCREATE_COUNT = 10;
	static final int TEST_MAX_SIZE = 5;
	static final int TEST_PAGE_SIZE = 4;
	static final Level LIB_LOG_LEVEL = Level.DEBUG;

	static final String TEST_DOCUMENT_CLASS = "FilenetLibTest";
	static final String DOCUMENTTITLE_PROPERTYNAME = "DocumentTitle";
	static final String DOCUMENT_SERVICE_TEST_TITLE = "TestDocumentServiceTitle";
	static final String TEST_TITLE_PREFIX = "TestSearchTitle";
	static final String TEST_CONTENT_PREFIX = "TestContent";
	static final String TEST_CONTENT_TYPE = "text/plain";
	static final String TEST_FILENAME = "FilenetContentLibTest";
	static final String TEST_FILENAME_EXTENSION = ".txt";
	static final String NON_NULL_RESPONSE_CODE_ASSERTMESSAGE = "Non-null error code returned. Error message: ";

	// flag that indicates that test documents where created
	boolean testDocumentsCreated = false;
	// list of document ids created for search tests. Do not add any other document
	// ids
	ArrayList<String> testDocumentIds = new ArrayList<String>(20);
	// DocumentTitle property value that will be used in search. It is initialized
	// when test documents are created
	String documentTitleToSearch = null;
	// id of created document for DocumentService testing
	String createdTestDocumentId;

	// instance of P8ProviderImpl to perform "utility" tasks (like getObject...)
	P8ProviderImpl p8ProviderImpl;

	// configuration tests
	@BeforeAll
	void initialize() throws ServiceException {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.davita.ecm.p8.content")).setLevel(LIB_LOG_LEVEL);
		log.info("Test started.");
		log.info("Initialization code started.");
		log.info(gitInformations.printAllGitInfo());
		String cfgValue = p8SearchConfiguration.getPropertiesToInclude("default");
		assertNotNull(cfgValue);
		assertNotEquals(0, cfgValue.length());

		try {
			p8ProviderImpl = authService.createConnection(new BaseRequest());

			// delete documents created for testing purposes
			BaseResponse response = cleanTestDocuments();
			assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
			testDocumentsCreated = createSearchTestDocuments();
		} catch (Exception e) {
			log.error("Failed to create test documents.", e);
		}
		assertTrue(testDocumentsCreated, "Failed to create test documents.");
	}

	@AfterAll
	void cleanUp() throws ServiceException {
		log.info("Cleanup code started.");
		// delete documents created for testing purposes
		BaseResponse response = cleanTestDocuments();
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
		try {
			if (null != p8ProviderImpl)
				p8ProviderImpl.close();
		} catch (Exception e) {
			// ignore
		}
		log.info("Test completed.");
	}

	@Test
	@Order(1)
	void missingCEUrlnTest() {
		String cfgValue = clientConfig.getCpeUrl();
		clientConfig.setCpeUrl(null);
		BaseRequest request = new BaseRequest();
		BaseResponse response = new BaseResponse();
		try {
			authService.createConnection(request);
		} catch (Exception e) {
			utilities.setResponseErrors(response, e);
		} finally {
			clientConfig.setCpeUrl(cfgValue);
		}
		assertEquals(DEFAULT_ERROR_TYPE, response.getErrorType());
		assertEquals(errorMessages.getMessageCode(ConfigExceptionCodes.CE_URL_MISSING.getExceptionCode()),
				response.getErrorCode());
	}

	@Test
	@Order(2)
	void missingUserNameTest() {
		String cfgValue = clientConfig.getCpeUsername();
		clientConfig.setCpeUsername(null);
		BaseRequest request = new BaseRequest();
		BaseResponse response = new BaseResponse();
		try {
			authService.createConnection(request);
		} catch (Exception e) {
			utilities.setResponseErrors(response, e);
		} finally {
			clientConfig.setCpeUsername(cfgValue);
		}
		assertEquals(DEFAULT_ERROR_TYPE, response.getErrorType());
		assertEquals(errorMessages.getMessageCode(ConfigExceptionCodes.USER_NAME_MISSING.getExceptionCode()),
				response.getErrorCode());
	}

	@Test
	@Order(3)
	void missingPaswordTest() {
		String cfgValue = clientConfig.getCpePassword();
		clientConfig.setCpePassword(null);
		BaseRequest request = new BaseRequest();
		BaseResponse response = new BaseResponse();
		try {
			authService.createConnection(request);
		} catch (Exception e) {
			utilities.setResponseErrors(response, e);
		} finally {
			clientConfig.setCpePassword(cfgValue);
		}
		assertEquals(DEFAULT_ERROR_TYPE, response.getErrorType());
		assertEquals(errorMessages.getMessageCode(ConfigExceptionCodes.PASSWORD_MISSING.getExceptionCode()),
				response.getErrorCode());
	}

	@Test
	@Order(4)
	void missingObjectStoreNameTest() {
		String cfgValue = clientConfig.getObjectStoreName();
		clientConfig.setObjectStoreName(null);
		BaseRequest request = new BaseRequest();
		BaseResponse response = new BaseResponse();
		try {
			authService.createConnection(request);
		} catch (Exception e) {
			utilities.setResponseErrors(response, e);
		} finally {
			clientConfig.setObjectStoreName(cfgValue);
		}
		assertEquals(DEFAULT_ERROR_TYPE, response.getErrorType());
		assertEquals(errorMessages.getMessageCode(ConfigExceptionCodes.OBJECT_STORE_NAME_MISSING.getExceptionCode()),
				response.getErrorCode());
	}

	// search row service tests
	@Test
	@Order(100)
	void rowSearchTest() {
		log.info("Row search test.");
		SearchRequest request = new SearchRequest();
		request.getSearchData().setQuery("SELECT DISTINCT DocumentTitle FROM " + TEST_DOCUMENT_CLASS);
		RowSearchResponse response = searchService.searchRows(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
	}

	// document service tests
	@Test
	@Order(200)
	void createDocument() throws ServiceException {
		log.info("Document service tests started.");
		log.info("Create document test.");

		final String title = TEST_FILENAME;
		final ArrayList<String> toList = new ArrayList<String>();
		toList.add("user1");
		toList.add("user2");

		final String fileName = title + TEST_FILENAME_EXTENSION;
		final String mimeType = TEST_CONTENT_TYPE;
		final String content = "12345";
		final String docClass = TEST_DOCUMENT_CLASS;
		final int contentSize = content.length();

		P8ContentResource p8ContentResource = new P8ContentResource();
		p8ContentResource.setContentType(mimeType);
		p8ContentResource.setSize(contentSize);
		p8ContentResource.setFileName(fileName);
		p8ContentResource.setResourceObject(content.getBytes());

		P8ContentObject p8ContentObject = new P8ContentObject();
		p8ContentObject.setDocumentClass(docClass);
		p8ContentObject.setResource(p8ContentResource);
		p8ContentObject.getProperties().addProperty(DOCUMENTTITLE_PROPERTYNAME, title);

		CreateDocumentRequest req1 = new CreateDocumentRequest();
		req1.setP8ContentObject(p8ContentObject);
		CreateDocumentResponse resp1 = documentService.createDocument(req1);
		assertNull(resp1.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + resp1.getErrorMessage());

		GetContentRequest req2 = new GetContentRequest();
		req2.setDocumentId(resp1.getP8DocumentId());
		GetContentResponse resp2 = contentService.getContent(req2);
		assertNull(resp2.getErrorCode(),
				"Cannot get content for created document. Error message: " + resp2.getErrorMessage());

		P8ContentResource res = resp2.getP8DocumentResource();
		assertEquals(fileName, res.getFileName());
		assertEquals(mimeType, res.getContentType());
		assertEquals(contentSize, res.getSize());
		try {
			assertEquals(content, new String(res.getBytes()));
		} catch (IOException e) {
			// ignore
		}
	}

	@Test
	@Order(201)
	void updateDocumentMetadataById() throws ServiceException {
		log.info("Update document metadata (by document id).");

		final String title = TEST_FILENAME + "_Updated";
		final ArrayList<String> toList = new ArrayList<String>();
		toList.add("user2");
		toList.add("user3");

		UpdateDocumentMetadataRequest req = new UpdateDocumentMetadataRequest();
		req.setId(createdTestDocumentId);
		req.getProperties().addProperty(DOCUMENTTITLE_PROPERTYNAME, title);

		UpdateDocumentMetadataResponse resp = documentService.updateDocumentMetadata(req);
		assertNull(resp.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + resp.getErrorMessage());
		assertEquals(1, resp.getNumberOfUpdatedDocuments());

		checkUpdatedDoc(p8ProviderImpl.getDocumentById(createdTestDocumentId), title, toList);
	}

	@Test
	@Order(202)
	void updateDocumentMetadataBySearch() throws ServiceException {
		log.info("Update document metadata (by document search).");

		final String title = TEST_FILENAME + "_UpdatedBySearch";
		final ArrayList<String> toList = new ArrayList<String>();
		toList.add("user1");
		toList.add("user4");

		UpdateDocumentMetadataRequest req = new UpdateDocumentMetadataRequest();
		// no document information test
		UpdateDocumentMetadataResponse resp = documentService.updateDocumentMetadata(req);
		assertEquals(BAD_ARGUMENTS_ERROR_TYPE, resp.getErrorType());
		assertEquals(resp.getErrorCode(), errorMessages
				.getMessageCode(DocumentServiceExceptionCodes.MISSING_DOCUMENT_PARAMETERS.getExceptionCode()));
		SearchData searchData = new SearchData();
		searchData.setQuery(
				"SELECT Id,DocumentTitle from " + TEST_DOCUMENT_CLASS + " WHERE Id = " + createdTestDocumentId);
		req.setSearchData(searchData);
		// No properties to update in request
		resp = documentService.updateDocumentMetadata(req);
		assertEquals(BAD_ARGUMENTS_ERROR_TYPE, resp.getErrorType());
		assertEquals(
				errorMessages
						.getMessageCode(DocumentServiceExceptionCodes.MISSING_PROPERTIES_PARAMETERS.getExceptionCode()),
				resp.getErrorCode());

		req.getProperties().addProperty(DOCUMENTTITLE_PROPERTYNAME, title);
		resp = documentService.updateDocumentMetadata(req);
		assertNull(resp.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + resp.getErrorMessage());
		assertEquals(1, resp.getNumberOfUpdatedDocuments());

		checkUpdatedDoc(p8ProviderImpl.getDocumentById(createdTestDocumentId), title, toList);
	}

	@Test
	@Order(203)
	void createDocumentVersion() throws ServiceException {
		log.info("Create document vesrion test.");

		final String title = TEST_FILENAME + "_NewVesrion";
		final ArrayList<String> toList = new ArrayList<String>();
		toList.add("user5");
		toList.add("user2");

		final String fileName = title + TEST_FILENAME_EXTENSION;
		final String mimeType = TEST_CONTENT_TYPE;
		final String content = "64321";
		final int contentSize = content.length();

		P8ContentResource p8ContentResource = new P8ContentResource();
		p8ContentResource.setContentType(mimeType);
		p8ContentResource.setSize(contentSize);
		p8ContentResource.setFileName(fileName);
		p8ContentResource.setResourceObject(content.getBytes());

		P8ContentObject p8ContentObject = new P8ContentObject();
		p8ContentObject.setResource(p8ContentResource);
		p8ContentObject.getProperties().addProperty(DOCUMENTTITLE_PROPERTYNAME, title);

		CreateDocumentVersionRequest req1 = new CreateDocumentVersionRequest();
		req1.setP8ContentObject(p8ContentObject);
		req1.setP8ObjectId(createdTestDocumentId);
		CreateDocumentResponse resp1 = documentService.createDocumentVersion(req1);
		assertNull(resp1.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + resp1.getErrorMessage());

		GetContentRequest req2 = new GetContentRequest();
		req2.setDocumentId(resp1.getP8DocumentId());
		GetContentResponse resp2 = contentService.getContent(req2);
		assertNull(resp2.getErrorCode(),
				"Cannot get content for created document. Error message: " + resp2.getErrorMessage());

		P8ContentResource res = resp2.getP8DocumentResource();
		assertEquals(fileName, res.getFileName());
		assertEquals(mimeType, res.getContentType());
		assertEquals(contentSize, res.getSize());
		try {
			assertEquals(content, new String(res.getBytes()));
		} catch (IOException e) {
			// ignore
		}
		p8ProviderImpl.deleteDocument(p8ProviderImpl.getDocumentById(resp1.getP8DocumentId()));
	}

	@Test
	@Order(204)
	void getDocumentByIdTest() {
		log.info("Get document by id.");

		GetDocumentByIdRequest request = new GetDocumentByIdRequest();
		request.setDocumentId(createdTestDocumentId);
		// no document information test
		GetDocumentByIdResponse response = documentService.getDocumentMetadataById(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
	}

	// content service tests
	@Test
	@Order(400)
	void getContentByIdTest() {
		log.info("Content service tests started.");
		log.info("Get content by id.");
		GetContentRequest request = new GetContentRequest();
		request.setDocumentId(testDocumentIds.get(0));
		GetContentResponse response = contentService.getContent(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
	}

	@Test
	@Order(401)
	void getContentByQueryTest() {
		log.info("Get content by search.");
		GetContentRequest request = new GetContentRequest();
		GetContentResponse response = contentService.getContent(request);
		assertEquals(BAD_ARGUMENTS_ERROR_TYPE, response.getErrorType());
		assertEquals(
				errorMessages.getMessageCode(ContentServiceExceptionCodes.MISSING_INPUT_PARAMETERS.getExceptionCode()),
				response.getErrorCode());
		request.setSearchData(new SearchData());
		request.getSearchData().setQuery("SELECT * FROM " + TEST_DOCUMENT_CLASS + " WHERE " + DOCUMENTTITLE_PROPERTYNAME
				+ "='" + documentTitleToSearch + "'");
		response = contentService.getContent(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
	}

	// search service tests
	@Test
	@Order(500)
	void requestValidationSearchTest() {
		log.info("Search service tests started.");
		log.info("Search request validation.");
		// no query test
		SearchRequest request = new SearchRequest();
		SearchResponse response = searchService.searchDocuments(request);
		assertEquals(DEFAULT_ERROR_TYPE, response.getErrorType());
		assertEquals(errorMessages.getMessageCode(SearchServiceExceptionCodes.MISSING_QUERY.getExceptionCode()),
				response.getErrorCode());
		// no filter test
		request = new SearchRequest();
		request.getSearchData().setQuery("select " + Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER + " from "
				+ TEST_DOCUMENT_CLASS + " where " + Utilities.WHERE_FILTER_PLACEHOLDER);
		response = searchService.searchDocuments(request);
		assertEquals(DEFAULT_ERROR_TYPE, response.getErrorType());
		assertEquals(errorMessages.getMessageCode(SearchServiceExceptionCodes.MISSING_WHERE_FILTER.getExceptionCode()),
				response.getErrorCode());
		// no where placeholder test
		request = new SearchRequest();
		request.getSearchData().addSearchCondition(DOCUMENTTITLE_PROPERTYNAME, "TestDocumentForGetContentByQueryTest");
		request.getSearchData().setQuery("SELECT " + Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER + " FROM "
				+ TEST_DOCUMENT_CLASS + " WHERE " + DOCUMENTTITLE_PROPERTYNAME + "='" + documentTitleToSearch + "'");
		response = searchService.searchDocuments(request);
		assertEquals(DEFAULT_ERROR_TYPE, response.getErrorType());
		assertEquals(
				errorMessages.getMessageCode(
						SearchServiceExceptionCodes.MISSING_WHERE_FILTER_PLACEHOLDER.getExceptionCode()),
				response.getErrorCode());
		// no sort data test
		request = new SearchRequest();
		request.getSearchData()
				.setQuery("SELECT Id FROM " + TEST_DOCUMENT_CLASS + " " + Utilities.SORT_ORDER_PLACEHOLDER);
		response = searchService.searchDocuments(request);
		assertEquals(DEFAULT_ERROR_TYPE, response.getErrorType());
		assertEquals(errorMessages.getMessageCode(SearchServiceExceptionCodes.MISSING_SORT_ORDER.getExceptionCode()),
				response.getErrorCode());
	}

	@Test
	@Order(501)
	void plainQuerySearchTest() {
		log.info("Plain query search.");
		SearchRequest request = new SearchRequest();
		// search with default list of properties
		// Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER
		// will be replaced with value from properties file)
		request.getSearchData().setQuery("SELECT " + Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER + " FROM "
				+ TEST_DOCUMENT_CLASS + " WHERE " + DOCUMENTTITLE_PROPERTYNAME + "='" + documentTitleToSearch + "'");
		SearchResponse response = searchService.searchDocuments(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
		assertNotEquals(0, response.getSearchResults().size());

		// search with default list of properties
		// Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER
		// will be replaced with value from properties file and one parameter
		request = new SearchRequest();
		// set parameter
		request.getSearchData().setSearchTemplateParameters(new String[] { documentTitleToSearch });
		request.getSearchData().setQuery("select " + Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER + " from "
				+ TEST_DOCUMENT_CLASS + " where " + DOCUMENTTITLE_PROPERTYNAME + " = ''{0}''");
		response = searchService.searchDocuments(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
		assertNotEquals(0, response.getSearchResults().size());

		// search with default list of properties
		// Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER
		// will be replaced with value from properties file) and
		// filters Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER will be replaced with
		// filter based on
		request = new SearchRequest();
		// add filter
		request.getSearchData().addSearchCondition(DOCUMENTTITLE_PROPERTYNAME, documentTitleToSearch);
		request.getSearchData().setQuery("select " + Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER + " from "
				+ TEST_DOCUMENT_CLASS + " where " + Utilities.WHERE_FILTER_PLACEHOLDER);
		response = searchService.searchDocuments(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
		assertNotEquals(0, response.getSearchResults().size());
	}

	@Test
	@Order(502)
	void searchTemplateWithFiterTest() {
		log.info("Search template with filters.");
		SearchRequest request = new SearchRequest();
		request.getSearchData().setSearchTemplateName("whereconditiontest");
		request.getSearchData().addSearchCondition(DOCUMENTTITLE_PROPERTYNAME, documentTitleToSearch);
		SearchResponse response = searchService.searchDocuments(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
		assertNotEquals(0, response.getSearchResults().size());
	}

	@Test
	@Order(503)
	void searchTemplateWithParametersTest() {
		log.info("Search template with parameters.");
		SearchRequest request = new SearchRequest();
		request.getSearchData().setSearchTemplateName("parameterplacholder");
		request.getSearchData().setSearchTemplateParameters(new String[] { documentTitleToSearch });
		SearchResponse response = searchService.searchDocuments(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
		assertNotEquals(0, response.getSearchResults().size());
	}

	@Test
	@Order(504)
	void resultSetMaxSizeTest() {
		log.info("Max result set size.");
		SearchRequest request = new SearchRequest();
		request.getSearchData()
				.setQuery("select " + Utilities.PROPERTIES_TO_SELECT_PLACEHOLDER + " from " + TEST_DOCUMENT_CLASS);
		request.getSearchParameters().setMaxSize(TEST_MAX_SIZE);
		SearchResponse response = searchService.searchDocuments(request);
		assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
		assertEquals(TEST_MAX_SIZE, response.getSearchResults().size());
	}

	@Test
	@Order(505)
	void paginationTest() {
		log.info("Pagination.");
		int pageNumber = testDocumentIds.size() / TEST_PAGE_SIZE
				+ ((testDocumentIds.size() % TEST_PAGE_SIZE) > 0 ? 1 : 0);
		for (int i = 0; i < pageNumber; i++) {
			SearchRequest request = new SearchRequest();
			request.getSearchData()
					.setQuery("select Id,DocumentTitle from " + TEST_DOCUMENT_CLASS + " where "
							+ DOCUMENTTITLE_PROPERTYNAME + " LIKE '" + TEST_TITLE_PREFIX + "%' ORDER BY "
							+ DOCUMENTTITLE_PROPERTYNAME);
			request.getSearchParameters().setPageSize(TEST_PAGE_SIZE);
			request.getSearchParameters().setStartPage(i + 1);
			SearchResponse response = searchService.searchDocuments(request);
			assertNull(response.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + response.getErrorMessage());
			if (!CollectionUtils.isEmpty(response.getSearchResults()))
				assertEquals(TEST_TITLE_PREFIX + TEST_PAGE_SIZE * i,
						response.getSearchResults().get(0).getProperties().get(DOCUMENTTITLE_PROPERTYNAME));
			if (TEST_PAGE_SIZE * (i + 1) <= testDocumentIds.size())
				assertEquals(TEST_PAGE_SIZE, response.getSearchResults().size());
			else
				assertEquals(testDocumentIds.size() - i * TEST_PAGE_SIZE, response.getSearchResults().size());
			assertEquals(DOCUMENTSTOCREATE_COUNT, response.getTotalDocumentNumber());
			assertEquals(pageNumber, response.getTotalPageNumber());
		}
	}

	@Test
	@Order(600)
	void getClassDefinition() throws ServiceException {
		log.info("Get class definition (Document).");

		GetClassDefinitionRequest req = new GetClassDefinitionRequest();
		req.addSymbolicName("Document");

		GetClassDefinitionResponse resp = schemaService.getClassDefinitions(req);
		assertNull(resp.getErrorCode(), NON_NULL_RESPONSE_CODE_ASSERTMESSAGE + resp.getErrorMessage());
		assertEquals("Document", resp.getClassDefinition().getSymbolicName());
	}

	// Non-test code
	private void checkUpdatedDoc(Document doc, String title, ArrayList<String> toList) {
		Properties prop = doc.getProperties();
		assertEquals(title, prop.getStringValue(DOCUMENTTITLE_PROPERTYNAME));
	}

	Document createTestDocument(String documentTitle, String content) throws IOException, ServiceException {

		final String fileName = documentTitle + TEST_FILENAME_EXTENSION;
		final String docClass = TEST_DOCUMENT_CLASS;
		final int contentSize = content.length();
		final ArrayList<String> toList = new ArrayList<String>();
		toList.add("user1");
		toList.add("user2");

		P8ContentResource p8ContentResource = new P8ContentResource();
		p8ContentResource.setContentType(TEST_CONTENT_TYPE);
		p8ContentResource.setSize(contentSize);
		p8ContentResource.setFileName(fileName);
		p8ContentResource.setResourceObject(content.getBytes());

		P8ContentObject p8ContentObject = new P8ContentObject();
		p8ContentObject.setDocumentClass(docClass);
		p8ContentObject.setResource(p8ContentResource);
		p8ContentObject.getProperties().addProperty(DOCUMENTTITLE_PROPERTYNAME, documentTitle);

		CreateDocumentRequest createDocumentRequest = new CreateDocumentRequest();
		createDocumentRequest.setP8ContentObject(p8ContentObject);
		return p8ProviderImpl.createDocument(p8ContentObject);
	}

	boolean createSearchTestDocuments() throws IOException, ServiceException {
		if (testDocumentsCreated)
			return true;
		log.info("Creating test documents.");
		ArrayList<Document> documents = new ArrayList<Document>(DOCUMENTSTOCREATE_COUNT);
		//p8ProviderImpl.createUpdatingBatch();
		for (int i = 0; i < DOCUMENTSTOCREATE_COUNT; i++) {
			documents.add(createTestDocument(TEST_TITLE_PREFIX + i, TEST_CONTENT_PREFIX + i));
			if (i == 0)
				documentTitleToSearch = TEST_TITLE_PREFIX + i;
		}
		Document testDocument = createTestDocument(DOCUMENT_SERVICE_TEST_TITLE, TEST_CONTENT_PREFIX);
		//p8ProviderImpl.commitUpdateBatch();
		createdTestDocumentId = testDocument.get_Id().toString();
		for (Document document : documents)
			testDocumentIds.add(document.get_Id().toString());
		testDocumentsCreated = true;
		log.info("Test documents created.");
		return true;
	}

	BaseResponse cleanTestDocuments() throws ServiceException {
		BaseResponse response = new BaseResponse();
		P8ResultSet p8ResultSet = p8ProviderImpl.searchDocuments("select Id from " + TEST_DOCUMENT_CLASS + " where " + DOCUMENTTITLE_PROPERTYNAME + " LIKE '" + TEST_TITLE_PREFIX + "%'",
				new SearchParameters());
		for (IndependentlyPersistableObject independentlyPersistableObject : p8ResultSet.getSearchResults()) {
			try {
				p8ProviderImpl.deleteDocument((Document) independentlyPersistableObject);
			} catch (EngineRuntimeException e) {
				ExceptionCode exceptionCode = e.getExceptionCode();
				if (exceptionCode != ExceptionCode.E_OBJECT_NOT_FOUND)
					throw e;
			}
		}
		log.info("{} test document(s) deleted.", p8ResultSet.getSearchResults());
		return response;
	}

}
