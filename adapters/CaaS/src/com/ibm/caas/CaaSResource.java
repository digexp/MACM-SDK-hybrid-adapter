/*
 ********************************************************************
 * Licensed Materials - Property of IBM                             *
 *                                                                  *
 * Copyright IBM Corp. 2015 All rights reserved.                    *
 *                                                                  *
 * US Government Users Restricted Rights - Use, duplication or      *
 * disclosure restricted by GSA ADP Schedule Contract with          *
 * IBM Corp.                                                        *
 *                                                                  *
 * DISCLAIMER OF WARRANTIES. The following [enclosed] code is       *
 * sample code created by IBM Corporation. This sample code is      *
 * not part of any standard or IBM product and is provided to you   *
 * solely for the purpose of assisting you in the development of    *
 * your applications. The code is provided "AS IS", without         *
 * warranty of any kind. IBM shall not be liable for any damages    *
 * arising out of your use of the sample code, even if they have    *
 * been advised of the possibility of such damages.                 *
 ********************************************************************
 */

package com.ibm.caas;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import com.worklight.adapters.rest.api.WLServerAPI;
import com.worklight.adapters.rest.api.WLServerAPIProvider;
import com.worklight.core.auth.OAuthSecurity;

@Path("/")
@OAuthSecurity(enabled = false)
public class CaaSResource {

	public static String CREDENTIALS;
	public static String SERVER;
	public static String TENANT;
	private static List<String> COOKIES;
	private HttpURLConnection urlConnection;
	protected boolean includeTypeInformation = false;

	/*
	 * For more info on JAX-RS see
	 * https://jsr311.java.net/nonav/releases/1.1/index.html
	 */

	// Define logger (Standard java.util.Logger)
	static Logger logger = Logger.getLogger(CaaSResource.class.getName());

	// Define the server api to be able to perform server operations
	WLServerAPI api = WLServerAPIProvider.getWLServerAPI();

	/**
	 * Load configuration MACM Configuration
	 */

	public void init() {
		String username = WLServerAPIProvider.getWLServerAPI()
				.getConfigurationAPI()
				.getMFPConfigurationProperty("ibm.macm.username");
		String password = WLServerAPIProvider.getWLServerAPI()
				.getConfigurationAPI()
				.getMFPConfigurationProperty("ibm.macm.password");

		CREDENTIALS = "Basic "
				+ new String(Base64.encodeBase64((username + ":" + password)
						.getBytes()));
		SERVER = WLServerAPIProvider.getWLServerAPI().getConfigurationAPI()
				.getMFPConfigurationProperty("ibm.macm.serverurl");
		
if(SERVER.startsWith("https")){
			relaxHostChecking();
		}
	}
	
	 /**
	  * Pass throughout CERTs [workaround]
	  */
	public void relaxHostChecking(){

		// Override SSL Trust manager without certificate chains validation
	    TrustManager[] trustAllCerts = new TrustManager[] {
	       new X509TrustManager() {
	          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	            return null;
	          }

	          public void checkClientTrusted(X509Certificate[] certs, String authType) { }

	          public void checkServerTrusted(X509Certificate[] certs, String authType) { }

	       }
	    };

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		    // Hostname verification. 
		    HostnameVerifier allHostsValid = new HostnameVerifier() {
		    	/**
		    	 * Verify that the host name is an acceptable match with the server's authentication scheme.
		    	 * @hostname - the host name
		    	 * @session - SSLSession used on the connection to host
		    	 * @return true if the host name is acceptable
		    	 */
		        public boolean verify(String hostname, SSLSession session) {
		          return true;
		        }
		    };
		   // Sets the default HostnameVerifier by all-trusting host verifier.
		    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		    
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Logs into the CaaS system.
	 * 
	 * @return {@link Integer} status code -- equal 201 if success
	 */
	public Integer connect() throws URISyntaxException, IOException {
		// log message to server log
		logger.info(this.getClass().getSimpleName()
				+ " Connect to MACM INSTANCE / Basic Auth...");
		init();
				
		try {

			URL url = null;
			if (TENANT == null)

			{
				url = new URL(
						SERVER
								+ InternalConstants.SERVER_DEFAULT_CONTEXT_ROOT_MY_CONTENTHANDLER
								+ "?"
								+ InternalConstants.CAAS_QUERY_PARAMETER_BASICAUTH);

			} else {
				url = new URL(
						SERVER
								+ InternalConstants.SERVER_DEFAULT_CONTEXT_ROOT_MY_CONTENTHANDLER
								+ "/"
								+ TENANT
								+ "?"
								+ InternalConstants.CAAS_QUERY_PARAMETER_BASICAUTH);
			}

			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Authorization", CREDENTIALS);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = urlConnection.getResponseCode();

		if (statusCode == 201 || statusCode == 204) {
			setCookies(urlConnection.getHeaderFields().get("Set-Cookie"));
		}
		return statusCode;
	}

	/**
	 * Fetch content from MACM instance by doing a HTTP GET request and set
	 * Cookies
	 * 
	 * @param url
	 *            {@link String} MACM content url
	 * @return {@link InputStream}
	 */
	public InputStream openStream(String url) throws IOException {

		urlConnection = (HttpURLConnection) new URL(url).openConnection();
		urlConnection.setRequestMethod("GET");
		urlConnection.setUseCaches(false);

		HttpURLConnection.setFollowRedirects(true);

		if (COOKIES != null) {
			for (String cookie : CaaSResource.COOKIES) {
				urlConnection.addRequestProperty("Cookie",
						cookie.split(";", 1)[0]);
			}
		}

		if (urlConnection.getResponseCode() == 401
				|| urlConnection.getResponseCode() == 400) {
			try {
				connect();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} finally {
				openStream(url);
			}
		}

		return urlConnection.getInputStream();
	}

	/**
	 * Returns a list of all content of the given type in the system.
	 * 
	 * @param tenant
	 *            (@link String) Root of the MACM instance
	 * @param path
	 *            (@link String) contentType - Acceptable values will vary
	 *            depending on the types defined by the specific portal. Current
	 *            values are: ["Articles", "Biographies", "Catalog",
	 *            "Notification", "Offerings", "Plain Texts", "Rich Texts"]
	 * @param oid
	 *            (@link String) Unique categorie identifier (Ex:
	 *            73959b0d-7e87-44d9-b6ca-5ef7343bc545 )
	 * @return an {@link Object} list of items and names of fields { names: {
	 *         title: 0, id: 1, ... } values: [ ["title", "id value", ... ] }
	 * 
	 * 
	 *         Path for method:
	 *         "<server address>/CAAS_Hybrid/adapters/CaaS/items?type={type}&lib={lib-name}"
	 *         "<server address>/CAAS_Hybrid/adapters/CaaS/items?oid={oid}"
	 * 
	 *         for instance
	 *         "<server address>/CAAS_Hybrid/adapters/CaaS/items?type=Offer&lib=MACM Default Application"
	 * @throws Exception
	 */
	@GET
	@Path("/items")
	@Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
	public String CAASContentItemsRequest(@QueryParam("tenant") String tenant,
			@QueryParam("type") String type, @QueryParam("oid") String oid,
			@QueryParam("pageSize") int pageSize,
			@QueryParam("pageNumber") int pageNumber,
			@QueryParam("categories.all") String categoriesall,
			@QueryParam("keywords.all") String keywordsall,
			@QueryParam("categories.any") String categoriesany,
			@QueryParam("keywords.any") String keywordsany,
			@QueryParam("titleFilter") String titleFilter,
			@QueryParam("workflowstatus") String workflowstatus,
			@QueryParam("sortcriteria") String sortcriteria,
			@QueryParam("property") String propertyKeys,
			@QueryParam("element") String elementKeys,
			@QueryParam("lib") String libName,
			@QueryParam("created.since") String csince,
			@QueryParam("created.before") String cbefore,
			@QueryParam("modified.since") String msince,
			@QueryParam("modified.before") String mbefore)

	throws Exception {
		if (SERVER == null) {
			init();
		}

		String source = SERVER + InternalConstants.SERVER_CONTEXT_ROOT;

		if (tenant != null) {
			TENANT = tenant;
			source += "/" + tenant + "/caas";
		} else {
			source += "/caas";
		}

		String categories = null;
		FilterBy fCategoriesType = null;
		String keywords = null;
		FilterBy fKeywordsType = null;

		if (categoriesall != null) {

			categories = categoriesall;
			fCategoriesType = FilterBy.ALL;

		} else if (categoriesany != null) {
			categories = categoriesany;
			fCategoriesType = FilterBy.ANY;
		} else {
			categories = null;
			fCategoriesType = null;
		}

		if (keywordsall != null) {
			keywords = keywordsall;
			fKeywordsType = FilterBy.ALL;

		} else if (keywordsany != null) {
			keywords = keywordsany;
			fKeywordsType = FilterBy.ANY;
		} else {
			keywords = null;
			fKeywordsType = null;
		}

		if ((oid != null)) {
			source += buildQuery(RequestBy.ID, oid, pageSize, pageNumber,
					sortcriteria, fCategoriesType, categories, fKeywordsType,
					keywords, titleFilter, workflowstatus, elementKeys,
					propertyKeys, libName, csince, cbefore, msince, mbefore);

		} else {
			if (type != null) {
				source += buildQuery(RequestBy.PATH, type, pageSize,
						pageNumber, sortcriteria, fCategoriesType, categories,
						fKeywordsType, keywords, titleFilter, workflowstatus,
						elementKeys, propertyKeys, libName, csince, cbefore,
						msince, mbefore);
			} else {
				return null;
			}
		}

		return jsonFormatter(IOUtils.toString(openStream(source)), true)
				.toString(4);
	}

	JSONObject jsonFormatter(String content, Boolean isCategorie)
			throws JSONException {

		JSONObject mData = new JSONObject(content);

		JSONObject header = new JSONObject(mData.opt("header"));
		Map<Integer, String> propertyIndexes, elementIndexes;
		Map<String, Object> itemsObject;
		List<Map<String, Object>> items = new ArrayList<>();

		elementIndexes = parseIndexes(header, "elementIndex");
		propertyIndexes = parseIndexes(header, "propertyIndex");

		JSONArray values = mData.optJSONArray("values");

		for (int i = 0; i < values.length(); i++) {
			JSONArray value = values.getJSONArray(i);

			itemsObject = new HashMap<String, Object>();

			Map<String, Object> propertiesObject = new HashMap<String, Object>();

			for (int j = 0; j < value.length(); j++) {

				if (propertyIndexes.containsKey(j)) {
					itemsObject.put(propertyIndexes.get(j), value.optString(j));

				} else if (elementIndexes.containsKey(j)) {

					propertiesObject.put(elementIndexes.get(j),
							value.optString(j));
				} else {
					break;
				}

				if (!elementIndexes.isEmpty()) {
					itemsObject.put("properties", propertiesObject);
				}
			}

			items.add(itemsObject);
		}

		String listofProperties = mData.optString("listProperties");
		JSONObject tmp;
		if (listofProperties == null || listofProperties.isEmpty()) {
			tmp = new JSONObject();
		} else {
			tmp = new JSONObject(listofProperties);
		}
		if (isCategorie) {
			tmp.putOnce("items", new JSONArray(items));
		} else {

			tmp.putOnce("item", new JSONArray(items));
		}

		return tmp;
	}

	Map<Integer, String> parseIndexes(JSONObject header, String indexesName)
			throws JSONException {
		Map<Integer, String> map = new HashMap<Integer, String>();
		if (header.has(indexesName)) {
			JSONObject json = header.getJSONObject(indexesName);
			@SuppressWarnings("unchecked")
			Iterator<String> it = json.keys();
			while (it.hasNext()) {
				String name = it.next();
				map.put(json.getInt(name), name);
			}
		}
		return map;
	}

	/**
	 * Returns a single piece of content
	 * 
	 * @param tenant
	 *            (@link String) Root of the MACM instance
	 * @param path
	 *            (@link String) Path of requested content - /Offer/Offer 1
	 * @param oid
	 *            (@link String) ID of requested content
	 * @return (@link Object) of requested content { names: { title: 0, id: 1,
	 *         ... } values: [ ["title", "id value", ... ] }
	 * 
	 * 
	 *         Path for method:
	 *         "<server address>/CAAS_Hybrid/adapters/CaaS/item?path={path}"
	 *         "<server address>/CAAS_Hybrid/adapters/CaaS/item?oid={oid}"
	 * 
	 * @throws Exception
	 */

	@GET
	@Path("/item")
	@Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
	public String CAASContentItemRequest(@QueryParam("tenant") String tenant,
			@QueryParam("type") String type, @QueryParam("oid") String oid,
			@QueryParam("lib") String libName) throws Exception {
		if (SERVER == null) {
			init();
		}

		String source = SERVER + InternalConstants.SERVER_CONTEXT_ROOT;

		if (tenant != null) {
			TENANT = tenant;
			source += "/" + tenant + "/caas";
		} else {
			source += "/caas";
		}

		if ((oid != null)) {
			source += buildQuery(RequestBy.ID, oid, 0, 0, null, null, null,
					null, null, null, null, null, null, null, null, null, null,
					null);

		} else {

			if (type != null) {
				source += buildQuery(RequestBy.PATH, type, 0, 0, null, null,
						null, null, null, null, null, null, null, libName,
						null, null, null, null);
			} else {
				return null;
			}
		}

		return jsonFormatter(IOUtils.toString(openStream(source)), false)
				.toString(4);
	}

	/**
	 * Retrieves the asset at the given URL on the CaaS server and returns it in
	 * Base64
	 * 
	 * @param (@link String) assetURL
	 * @return (@link Response) base64 encoded image
	 * 
	 *         Path for method:
	 *         "<server address>/CAAS_Hybrid/adapters/CaaS/asset?assetURL={assetURL}"
	 */
	@GET
	@Path("/asset")
	public Response CAASAssetRequest(@QueryParam("assetURL") String assetURL)
			throws IOException, URISyntaxException {

		if (SERVER == null) {
			connect();
		}
		return Response.ok(
				new ByteArrayInputStream(IOUtils.toByteArray(openStream(SERVER
						+ assetURL))), urlConnection.getContentType()).build();
	}

	/**
	 * Retrieves all openned projects
	 * 
	 * @return (@link Response) List of open projects
	 * @throws Exception
	 */
	@GET
	@Path("/projects")
	public String CAASOpenProjectsRequest() throws Exception {

		if (SERVER == null) {
			connect();
		}

		String source = SERVER + InternalConstants.SERVER_CONTEXT_ROOT;
		source += buildQuery(RequestBy.URL, null, 0, 0, null, null, null, null,
				null, null, null, null, null,
				InternalConstants.CAAS_OPEN_PROJECTS, null, null, null, null);

		return jsonFormatter(IOUtils.toString(openStream(source)), true)
				.toString(4);
	}

	/**
	 * Set the Cookies's List
	 * 
	 * @param cookies
	 *            (List<String>) Requested cookies
	 */
	public void setCookies(List<String> cookies) {
		CaaSResource.COOKIES = cookies;
	}

	public enum RequestBy {
		/**
		 * Indicates a request where the content is obtained by specifiying its
		 * content id.
		 */
		ID,
		/**
		 * Indicates a request where the content is obtained by specifiying its
		 * content path.
		 */
		PATH,
		/**
		 * Indicates a request where the content is obtained by specifiying a
		 * URL.
		 */
		URL
	}

	public enum CriteriaEnum {
		lastmodifieddate, creationdate, position, title, author, expirydate, publishdate, status
	}

	public enum FilterBy {
		ALL, ANY
	}

	/**
	 * The identifier for the content retrived by this request. Its meaning
	 * dependens on the type of request: a single content a item, a list of
	 * content items or an image URL.
	 */

	String buildQuery(RequestBy contentIdentifierType,
			String contentIdentifier, int pageSize, int pageNumber,
			/* Map<String, Boolean> */String sortCriteria,
			FilterBy fCategorieType, String categoryFilter,
			FilterBy fKeywordsType, String keywordFilter, String titleFilter,
			String workflowstatus, String elementKeys, String propertyKeys,
			String libName, String csince, String cbefore, String msince,
			String mbefore) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append('?').append(
				encodeParam(InternalConstants.CAAS_QUERY_PARAMETER_MIME_TYPE,
						InternalConstants.CAAS_QUERY_PARAMETER_MIME_TYPE_JSON));
		sb.append('&').append(
				encodeParam(InternalConstants.CAAS_QUERY_PARAMETER_PAGE,
						InternalConstants.CAAS_QUERY_PARAMETER_PAGE_DEFAULT));
		if (contentIdentifierType == RequestBy.ID) {
			sb.append('&').append(
					encodeParam(InternalConstants.CAAS_QUERY_PARAMETER_URILE,
							InternalConstants.CAAS_URILE_BY_ID
									+ contentIdentifier));
		} else if (contentIdentifierType == RequestBy.PATH) {
			sb.append('&')
					.append(encodeParam(
							InternalConstants.CAAS_QUERY_PARAMETER_URILE,
							InternalConstants.CAAS_URILE_BY_PATH
									+ libName
									+ InternalConstants.CAAS_CONTENT_TYPE_DEFAULT
									+ contentIdentifier));
		} else {
			sb.append('&')
					.append(encodeParam(
							InternalConstants.CAAS_QUERY_PARAMETER_URILE,
							InternalConstants.CAAS_URILE_BY_PATH
									+ InternalConstants.CAAS_OPEN_PROJECTS))
					.append('&').append(encodeParam("current", true));
		}

		if (pageSize != 0) {
			sb.append('&').append(encodeParam("ibm.pageSize", pageSize));
		}
		if (pageNumber != 0) {
			sb.append('&').append(encodeParam("ibm.pageNumber", pageNumber));
		}
		if (sortCriteria != null) {
			sb.append('&')
					.append(encodeParam("ibm.sortcriteria", sortCriteria));
		}
		if (categoryFilter != null) {

			if (fCategorieType != null) {
				if (fCategorieType == FilterBy.ALL) {
					sb.append('&').append(
							encodeParam("ibm.filter.categories.all",
									categoryFilter));
				} else {
					sb.append('&').append(
							encodeParam("ibm.filter.categories.any",
									categoryFilter));
				}
			}
		}
		if (keywordFilter != null) {
			if (fKeywordsType != null) {
				if (fKeywordsType == FilterBy.ALL) {
					sb.append('&').append(

					encodeParam("ibm.filter.keywords.all", keywordFilter));
				} else {
					sb.append('&').append(
							encodeParam("ibm.filter.keywords.any",
									keywordFilter));
				}
			}
		}
		if (titleFilter != null) {
			sb.append('&').append(encodeParam("ibm.filter.title", titleFilter));
		}
		if (workflowstatus != null) {
			sb.append('&').append(
					encodeParam("ibm.filter.workflowstatus", workflowstatus));
		}

		if (elementKeys != null) {
			sb.append('&').append(encodeParam("ibm.element.keys", elementKeys));
		}

		if (propertyKeys != null) {
			sb.append('&').append(
					encodeParam("ibm.property.keys", propertyKeys));
		}

		if (csince != null) {
			sb.append('&').append(
					encodeParam("ibm.filter.created.since", csince));
		}

		if (cbefore != null) {
			sb.append('&').append(
					encodeParam("ibm.filter.created.before", cbefore));
		}
		if (msince != null) {
			sb.append('&').append(
					encodeParam("ibm.filter.modified.since", msince));
		}

		if (mbefore != null) {
			sb.append('&').append(
					encodeParam("ibm.filter.modified.before", mbefore));
		}

		if (includeTypeInformation) {
			sb.append('&').append(encodeParam("ibm.type.information", "true"));
		}
		return sb.toString();
	}

	String encodeParam(String key, Object value) throws Exception {
		StringBuilder sb = new StringBuilder(URLEncoder.encode(key,
				InternalConstants.UTF_8).replace("+", "%20"));
		sb.append('=').append(
				value == null ? "" : URLEncoder.encode(value.toString(),
						InternalConstants.UTF_8).replace("+", "%20"));
		return sb.toString();
	}

	/**
	 * This class is package-protected so as not to be exposed to clients.
	 */
	static class InternalConstants {

		// Logging stuff
		static final String LOG_ENTRY = " ENTRY ";
		static final String LOG_EXIT = " EXIT ";

		// Android system properties
		static final String PROPERTY_HTTP_AGENT = "http.agent";

		// HTTP connection stuff
		static final String HTTP_SCHEME_HTTP = "http";
		static final String HTTP_SCHEME_HTTPS = "https";
		static final String HTTP_HEADER_USER_AGENT = "User-Agent";
		static final String HTTP_HEADER_ACCEPT_LANGUAGE = "Accept-Language";

		static final String HTTP_HEADER_FIELD_CAAS_RESPONSE = "caas-response";

		// Server connection stuff
		static final String SERVER_DEFAULT_SCHEME = HTTP_SCHEME_HTTP;
		static final String SERVER_DEFAULT_PORT = "80";
		static final String SERVER_DEFAULT_CONTEXT_ROOT = "/wps/mypoc";
		static final String SERVER_CONTEXT_ROOT = "/wps/myportal";
		static final String SERVER_DEFAULT_INSTANCE = "";
		static final String SERVER_DEFAULT_CONTEXT_ROOT_MY_CONTENTHANDLER = "/wps/mycontenthandler";
		static final String SERVER_DEFAULT_PROJECT = "$project";

		static final String SERVER_DEFAULT_QUERY_URI_JSECURITY_CHECK = "!ut/p/model/service-document/j_security_check";
		static final String SERVER_DEFAULT_QUERY_URI_PARAMETER_J_USERNAME = "j_username";
		static final String SERVER_DEFAULT_QUERY_URI_PARAMETER_J_PASSWORD = "j_password";

		// Encoding stuff
		static final String DEFAULT_ENCODING = "UTF-8";

		// CaaS stuff
		static final String CAAS_QUERY_PARAMETER_URILE = "urile";

		static final String CAAS_QUERY_PARAMETER_BASICAUTH = "uri=login:basicauth";

		static final String CAAS_URILE_BY_ID = "wcm:oid:";
		static final String CAAS_URILE_BY_PATH = "wcm:path:";
		static final String CAAS_QUERY_PARAMETER_PAGE = "page";
		static final String CAAS_QUERY_PARAMETER_MIME_TYPE = "mime-type";
		static final String CAAS_QUERY_PARAMETER_PAGE_DEFAULT = "ibm.portal.caas.page";
		static final String CAAS_CONTENT_TYPE_DEFAULT = "/Content Types/";
		static final String CAAS_OPEN_PROJECTS = "MACM System/Views/Open Projects";

		static final String CAAS_QUERY_PARAMETER_MIME_TYPE_JSON = "application/json";
		static final String CAAS_QUERY_PARAMETER_MIME_TYPE_XML = "application/xml";
		static final String CAAS_QUERY_PARAMETER_MIME_TYPE_DEFAULT = CAAS_QUERY_PARAMETER_MIME_TYPE_JSON;

		// Response handling
		static final String CAAS_RESPONSE_JSON_PREFIX_IBM = "ibm.portal.caas";
		static final String CAAS_RESPONSE_JSON_TITLE = "title";
		static final String CAAS_RESPONSE_JSON_ID = "id";
		static final String CAAS_RESPONSE_JSON_AUTHORS = "authors";
		static final String CAAS_RESPONSE_JSON_LAST_MODIFIED = "lastmodifieddate";

		/**
		 * The UTF-8 charset string.
		 */
		static final String UTF_8 = "utf-8";
	}

	static class Constants {
		/**
		 * Default orders.
		 */
		public enum Order {
			ASC, DESC;
		}

		public static final String CONTEXT_KEY_ACCEPT_LANGUAGES = "Accept-Language";
		public static final String CONTEXT_VALUE_ACCEPT_LANGUAGES_DEFAULT = "DEFAULT";
		public static final String CONTEXT_KEY_USER_AGENT = "User-Agent";
		public static final String CONTEXT_VALUE_USER_AGENT_DEFAULT = "DEFAULT";
		public static final String CONTEXT_KEY_INDEX_START = "index-start";
		public static final String CONTEXT_KEY_INDEX_COUNT = "index-count";
		public static final String CONTEXT_KEY_ORDER_CRITERIA = "order-criteria";
		public static final String CONTEXT_KEY_ORDER = "order";
		public static final String CONTEXT_VALUE_ORDER_DEFAULT = "DEFAULT"; // equals
																			// Order.ASC
		public static final String CONTEXT_KEY_LOCATION = "location";
		public static final String CONTEXT_VALUE_LOCATION_DEFAULT = "DEFAULT"; // equals
																				// current
																				// location

		public static final String DATA_KEY_ID = "id";
		public static final String DATA_KEY_TITLE = "title";
	}
}
