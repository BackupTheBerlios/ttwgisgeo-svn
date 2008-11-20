package qualipso.ttworkgisgeo.tri;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.etsi.ttcn.tci.TciCDProvided;
import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriCommunicationSA;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriPortIdList;
import org.etsi.ttcn.tri.TriStatus;
import org.etsi.ttcn.tri.TriTestCaseId;

import qualipso.ttworkgisgeo.CityBean;
import qualipso.ttworkgisgeo.Utilities;

import com.meterware.httpunit.Button;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.TableRow;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;
import com.meterware.httpunit.parsing.HTMLParserFactory;
import com.testingtech.ttcn.tri.TestAdapter;
import com.testingtech.ttcn.tri.TriAddressImpl;
import com.testingtech.ttcn.tri.TriMessageImpl;
import com.testingtech.ttcn.tri.TriStatusImpl;
/**
 * This class is responsible for accessing the SUT. 
 * 
 *
 */
public class Adapter extends TestAdapter implements TriCommunicationSA {
	private Utilities util = new Utilities();
	private static final long serialVersionUID = 6917827107004937619L;
//	private final String GEOSERVER_URL = "http://10.250.20.164:8080/geoserver/wfs";
//	private final String GISCLIENT_URL = "http://150.254.173.202:8090/WebContent/";
	private final String GEOSERVER_URL = "http://150.254.173.202:8090/geoserver/wfs";
	private final String GISCLIENT_URL = "http://syros.eurodyn.com:18088/GISClient3/";
	
	private final byte DELIMETER = 0x0D;
	private final String ERROR = "ERROR";
	private final String SUCCESS = "SUCCESS";
	/**
	 * Calls the constructor of the superclass.
	 */
	public Adapter() {
		super();
	}
	/**
	 * Several codecs can be set for an Adapter and this function is called to set the proper one.
	 * 
	 * @param codecName A String containing the identifier of a Codec. If the argument is 
	 * null or an empty String then the default Codec is set. 
	 * @return The Codec with the identifier of <code>codecName</code>. If none is selected then 
	 * the default Codec is returned.
	 */
	public TciCDProvided getCodec(String codecName) {
		if (codecName == null || codecName.equals("")) {
			codecName = "GEOServerCodec";
		}

		TciCDProvided codec = super.getCodec(codecName);

		if (codec != null) {
			return codec;
		}
		if (codecName.equals("GEOServerCodec")) {
			codec = new qualipso.ttworkgisgeo.tci.Codec(RB);
			codecs.put(codecName, codec);
		} else {
			// unknown codec...
		}
		return codec;
	}
	/** 
	 * Just returns a new TriStatusImpl() object.
	 * @param testcase identfier of the testcase that is going to be executed
	 * @param tsiList a list of all the test system ports defined for the test system
	 * @return the status of the execution of the operation
	 */
	public TriStatus triExecuteTestcase(final TriTestCaseId testcase,
			final TriPortIdList tsiList) {
		return new TriStatusImpl();
	}
	/**
	 * The mapping is done by the system we just have to call the triMap()
	 * function of the <code>CsaDef</code> object, which is inherited from <code>TestAdapter</code>
	 * @param componentPortId
	 * @param tsiPortId
	 * @return The status of the mapping operation
	 */
	public TriStatus triMap(TriPortId componentPortId, TriPortId tsiPortId) {
		// the mapping is done by the system we just have to call the triMap()
		// function of the CsaDef object, which is inherited from TestAdapter
		TriStatus mapStatus = CsaDef.triMap(componentPortId, tsiPortId);
		if (mapStatus.getStatus() != TriStatus.TRI_OK) {
			return mapStatus;
		}
		return new TriStatusImpl();
	}
	/** 
	 * This function sends the message to the SUT and enqueues the answer so that it will be read by the 
	 * TTCN-3 executable. It returns the status of the operation.
	 *  
	 * @param componentId identifier of the component sending the message
	 * @param tsiPortId identifier of the port sending the message
	 * @param address address, if defined, of a port in the SUT 
	 * @param sendMessage a <code>TriMessage</code> encapsulating the encoded message to be sent in a byte array
	 * @return The status of the operation. Indicates local success (<code>TriStatus.TRI_OK</code>) 
	 * or failure (<code>TriStatus.TRI_ERROR</code>) of the operation.
	 */
	public TriStatus triSend(final TriComponentId componentId,
			final TriPortId tsiPortId, final TriAddress address,
			final TriMessage sendMessage) {
		HTMLParserFactory.setReturnHTMLDocument(false);
		String errorMessage = ERROR;
		byte[] encMessage = sendMessage.getEncodedMessage();
		HashMap<String, String> hm = null;
		if (encMessage!=null) {
			try {
				hm = getParametersFromMessage(encMessage);
			} catch (Exception e) {
				errorMessage = errorMessage + e.getMessage();
				TriMessage sendErrorMessage = new TriMessageImpl(errorMessage.getBytes());
				Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, sendErrorMessage);
				return new TriStatusImpl();			
			}			
		}   
		if (hm!=null) {			
			String requestType = (String) hm.get("requestType");
			if (requestType.equals("addCityGIS")) {
				CityBean cb = new CityBean();
				cb.setAction("addCityGIS");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));				
				String response = insertCityUsingGISClient(cb);
				String message = "";
				if (response.contains(ERROR)) {
					message=ERROR;
				} else {
					message=SUCCESS;
				}
				TriMessage receivedMessage = new TriMessageImpl(message.getBytes());				
				Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, receivedMessage);		
				return new TriStatusImpl();					
			} else if (requestType.equals("addCityGEO")) {
				CityBean cb = new CityBean();
				cb.setAction("addCityGEO");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));						
				String insertCityQuery = util.getInsertCityQuery(cb);
				String serverResponse = sendQueryToServer(insertCityQuery);
				String result =  "";
				if (serverResponse.contains("SUCCESS")) {
					result = SUCCESS;
				} else {
					result = ERROR;
				}
				TriMessage receivedMessage = new TriMessageImpl(result.getBytes());				
				Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, receivedMessage);		
				return new TriStatusImpl();					
			} else if (requestType.equalsIgnoreCase("updateCity")) {
				CityBean oldCity = new CityBean();
				CityBean newCity = new CityBean();
				
				oldCity.setName(hm.get("oldCityName"));
				oldCity.setLatitude(hm.get("oldLatitude"));
				oldCity.setLongitude(hm.get("oldLongitude"));
				oldCity.setAdminName(hm.get("oldAdminName"));
				oldCity.setCountryName(hm.get ("oldCountryName"));
				oldCity.setStatus(hm.get("oldStatus"));
				oldCity.setPopClass(hm.get("oldPopClass"));
				
				newCity.setName(hm.get("newCityName"));
				newCity.setLatitude(hm.get("newLatitude"));
				newCity.setLongitude(hm.get("newLongitude"));
				newCity.setAdminName(hm.get("newAdminName"));
				newCity.setCountryName(hm.get ("newCountryName"));
				newCity.setStatus(hm.get("newStatus"));
				newCity.setPopClass(hm.get("newPopClass"));
				
				String response = updateCityUsingGISClient(oldCity, newCity);
				TriMessage receivedMessage = new TriMessageImpl(response.getBytes());				
				Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, receivedMessage);		
				return new TriStatusImpl();					
			} else if (requestType.equalsIgnoreCase("retrieveCity")) {
				CityBean cb = new CityBean();
				cb.setAction("addCityGEO");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));
				String query = util.getRetrieveCityQuery(cb);
				String serverResponse = sendQueryToServer(query);	
				CityBean receivedCity = createCityBean(serverResponse, "retrieveCity");
				if (receivedCity == null) {
					receivedCity = new CityBean();
				}
				byte[] b;
				try {
					ByteArrayOutputStream array_out = new ByteArrayOutputStream();
					ObjectOutputStream obj_out = new ObjectOutputStream(array_out);
					obj_out.writeObject(receivedCity);
					b = array_out.toByteArray();					
				} catch (IOException e) {
					String m = "error" + e.getStackTrace();
					b = m.getBytes();					
				}
				TriMessage receivedMessage = new TriMessageImpl(b);				
				Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, receivedMessage);		
				return new TriStatusImpl();				
			} else if (requestType.equalsIgnoreCase("deleteGIS")) {
				CityBean cb = new CityBean();
				cb.setAction("deleteGIS");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));
				String response = deleteCityUsingGISClient(cb);
				byte[] b = response.getBytes();
				TriMessage receivedMessage = new TriMessageImpl(b);				
				Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, receivedMessage);		
				return new TriStatusImpl();		
				
			} else if (requestType.equalsIgnoreCase("deleteGEO")) {
				CityBean cb = new CityBean();
				cb.setAction("deleteGEO");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));
				String query = util.getDeleteCityQuery(cb);
				String serverResponse = sendQueryToServer(query);
				String result =  "";
				if (serverResponse.contains("SUCCESS")) {
					result = SUCCESS;
				} else {
					result = ERROR;
				}
				TriMessage receivedMessage = new TriMessageImpl(result.getBytes());				
				Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, receivedMessage);		
				return new TriStatusImpl();					
			} else if (requestType.equalsIgnoreCase("retrieveCityGIS")) {
				CityBean cb = new CityBean();
				cb.setAction("retrieveCityGIS");
				cb.setName(hm.get("cityName"));
				cb.setLatitude(hm.get("latitude"));
				cb.setLongitude(hm.get("longitude"));
				cb.setAdminName(hm.get("adminName"));
				cb.setCountryName(hm.get ("countryName"));
				cb.setStatus(hm.get("status"));
				cb.setPopClass(hm.get("popClass"));
				CityBean responseCity = retrieveCityFromGISClient(cb);
				byte[] b;
				if (responseCity == null) {
					responseCity = new CityBean();				
				}
				try {
					ByteArrayOutputStream array_out = new ByteArrayOutputStream();
					ObjectOutputStream obj_out = new ObjectOutputStream(array_out);
					obj_out.writeObject(responseCity);
					b = array_out.toByteArray();					
				} catch (IOException e) {
					String m = ERROR + e.getStackTrace();
					b = m.getBytes();					
				}	
				TriMessage sendErrorMessage = new TriMessageImpl(b);
				Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, sendErrorMessage);
				return new TriStatusImpl();				
			}
			errorMessage += "Unknown action requested.";
			TriMessage receivedMessage = new TriMessageImpl(errorMessage.getBytes());				
			Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, receivedMessage);		
			return new TriStatusImpl();	
		} else {
			errorMessage += "Unable to get the decode the request parameters. "+ new String(encMessage);
			TriMessage sendErrorMessage = new TriMessageImpl(errorMessage.getBytes());
			Cte.triEnqueueMsg(tsiPortId, new TriAddressImpl(new byte[] {}), componentId, sendErrorMessage);
			return new TriStatusImpl();
		}
	}

	/**
	 * Receives the encoded message and creates a <code>HashMap</code> where the key is the parameter 
	 * value and the value is the parameter value. 
	 * @param message The message as a delimited byte array. 
	 * @return A <code>HashMap</code> containing the parameters of the TTCN-3 <code>record</code>. 
	 * The key of the <code>HashMap</code> is the name of the parameter and the value is the value of the parameter.
	 */
	private HashMap<String, String> getParametersFromMessage(byte[] message) throws Exception {
		HashMap<String, String> values = new HashMap<String, String>();
		int lastDelimiterIndex = 0;
		boolean isName = true; // is what we are currently reading the *name* of
							   // a field or its *value*?
		String paramName = "";
		for (int i = 0; i < message.length; i++) {
			if (message[i] == DELIMETER) {
				byte[] nArr = new byte[i - lastDelimiterIndex];
				System.arraycopy(message, lastDelimiterIndex, nArr, 0, nArr.length);
				lastDelimiterIndex = i + 1;
				if (isName) {
					paramName = new String(nArr);
					isName = false;
				} else {
					String val = new String(nArr);
					values.put(paramName, val);
					isName = true;
				}
			}
		}
		return values;
	}
	/** 
	 * This function sends an XML query to GEOServer's REST web service interface, and returns 
	 * the answer received by GEOServer. 
	 * @param A WFS-T compliant query
	 * @return An XML response received by GEOServer.
	 */
	private String sendQueryToServer(String xmlQuery) {
		StringBuffer response = new StringBuffer() ;
		try {	
			URL geoserverURL = new URL(GEOSERVER_URL);
			HttpURLConnection conn = (HttpURLConnection)geoserverURL.openConnection();	
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty( "User-agent", "tester" );
			conn.setRequestProperty("Content-Type", "application/xml"); 
			conn.setInstanceFollowRedirects(true);
			
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream()) ;
			out.write(xmlQuery);
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";	
			while ((line=in.readLine())!=null) {	
				response.append(line);
			}
			in.close();
		} catch (Exception e) {
			//  something
			response.append("error: " + e.toString());
		}		
		return response.toString();
	}
	/**
	 * 
	 * @param hm
	 * @return
	 */
	private String getMapFromGISClient(HashMap<String, String> hm) {
		String message = "";
		String gisClientURL = GISCLIENT_URL;
		String version = hm.get("version");
		String layers  = hm.get("layers");
		String styles  = hm.get("styles");
		String crs     = hm.get("crs");
		String bbox    = hm.get("bbox");
		String width   = hm.get("width");
		String height  = hm.get("height");
		String format  = hm.get("format");
		try {					
			//navigate to form and add the details
			URL url = new URL(gisClientURL);
			WebConversation wc = new WebConversation();			
			WebRequest req = new GetMethodWebRequest(url,"");
			WebResponse mainPageResponse = wc.getResponse(req);
			WebResponse linksFrame = wc.getFrameContents("methods");
			WebLink mapsLink = linksFrame.getLinkWith("GetMap");
			mapsLink.click();
			WebResponse mapsWholePage = wc.getCurrentPage();
			WebResponse mapsFormFrame = wc.getFrameContents("inputs");
			WebForm[] allForms = mapsFormFrame.getForms();
			WebForm mapForm = allForms[0];
			mapForm.setParameter("version", version);
			mapForm.setParameter("layers", layers);
			mapForm.setParameter("styles", styles);
			mapForm.setParameter("srs", crs);  // is it srs or crs? the standard says crs but the parameter name here is srs
			mapForm.setParameter("bbox", bbox);
			mapForm.setParameter("width", width);
			mapForm.setParameter("height", height);
			mapForm.setParameter("format", format);
			mapForm.submit();
			WebResponse responsePage = wc.getCurrentPage();
			WebResponse mapPage = wc.getFrameContents("result");
			InputStream is = mapPage.getInputStream();
			String contentType = mapPage.getContentType();
			BufferedImage bi = ImageIO.read(is);
			is.close();						
			int imageHeight = bi.getHeight();
			int imageWidth = bi.getWidth();	 
			String returnedHeight = new Integer(imageHeight).toString();
			String returnedWidth = new Integer(imageWidth).toString();
			message = contentType +" " + returnedHeight +" " + returnedWidth;			
		} catch (Exception e) {				
			message = ERROR + e.getStackTrace();
		} 
		return message;
	}
	/**
	 * Inserts a City feature through GISClient's web interface (using HttpUnit to manipulate it)  
	 * @param cb a CityBean containing the parameter values of the City feature 
	 * @param urlString the URL of GISClient's web interface
	 * @return a String that can be either SUCCESS or ERROR depending on the outcome of the operation
	 */
	private String insertCityUsingGISClient(CityBean cb) {
		
		String response = ERROR;
		String cityName = cb.getName();
		String latitude = cb.getLatitude();
		String longitude = cb.getLongitude();
		String adminName = cb.getAdminName();
		String countryName = cb.getCountryName();
		String status = cb.getStatus();
		String popClass = cb.getPopClass();
		try {
			// navigate to add new city form
			URL url = new URL(GISCLIENT_URL);
			WebConversation wc = new WebConversation();
			WebRequest wr = new GetMethodWebRequest(url, "");
			WebResponse mainPage = wc.getResponse(wr);
			WebResponse linksFrame = wc.getFrameContents("methods");
			WebLink citiesLink = linksFrame.getLinkWith("Cities");
			citiesLink.click();
			
			mainPage = wc.getCurrentPage();
			WebResponse citiesFrame = wc.getFrameContents("inputs");
			WebLink addCityLink = citiesFrame.getLinkWith("Add new city");
			addCityLink.click();
			
			mainPage = wc.getCurrentPage();
			WebResponse addCityFrame = wc.getFrameContents("inputs");
			
			WebForm cityForm = addCityFrame.getForms()[0];			
			cityForm.setParameter("CITY_NAME",cityName);
			cityForm.setParameter("LATITUDE",latitude);
			cityForm.setParameter("LONGITUDE",longitude);
			cityForm.setParameter("ADMIN_NAME",adminName);
			cityForm.setParameter("CNTRY_NAME",countryName);
			cityForm.setParameter("STATUS",status);
			cityForm.setParameter("POP_CLASS",popClass);
			SubmitButton citySubmitButton = cityForm.getSubmitButton("method", "Insert");
			citySubmitButton.click();				
			mainPage = wc.getCurrentPage();
			//Now we have to navigate to actions and commit the new city
			WebResponse actionsFrame = wc.getFrameContents("methods");
			WebLink actionsLink = actionsFrame.getLinkWith("Actions");
			actionsLink.click();
			mainPage = wc.getCurrentPage();
			WebResponse commitFrame = wc.getFrameContents("inputs");
			WebForm commitForm = commitFrame.getForms()[0];
			SubmitButton commitCityButton = commitForm.getSubmitButton("method", "Commit");
			commitCityButton.click(); //click on the first commit button
			mainPage = wc.getCurrentPage();
			WebResponse verifyCommitFrame = wc.getFrameContents("inputs");
			WebForm verifyCommitForm = verifyCommitFrame.getForms()[0];
			Button verifyCommitButton = verifyCommitForm.getButtons()[0];
			verifyCommitButton.click(); //click on the second commit button
			mainPage = wc.getCurrentPage();
			WebResponse responseFrame = wc.getFrameContents("inputs");
			String xmlResponse = responseFrame.getText();
			if (xmlResponse.toLowerCase().contains("success")) {
				response = SUCCESS;
			} else {
				response = ERROR;
			}		

		} catch(Exception e) {
			response = ERROR;
		}				
		return response;
	}
	/** 
	 * Creates a CityBean object after parsing the XML message returned by GEOServer after a retrieve request.
	 * The CityBean will be send to the Codec to create a TTCN-3 CityType object
	 * @param xmlText GEOServer's XML message response 
	 * @param action the action parameter that must be set to the CityBean.
	 * @return the CityBean that will be sent to the Codec.
	 */
	private CityBean createCityBean(String xmlText, String action) {
		CityBean cb = new CityBean();
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(new ByteArrayInputStream(xmlText.getBytes()));
			List cities = doc.selectNodes("/wfs:FeatureCollection/gml:featureMember/topp:tasmania_cities");
			if (cities!=null) {
				if (cities.size() > 1 || cities.size() < 1) {
					//throw new Exception("Error: found " + cities.size() + " cities. Must be exactly one." );							
				}
				Node city = (Node)cities.get(0);
				cb.setAction(action);
				cb.setName(city.valueOf("topp:CITY_NAME"));
				String coords = city.valueOf("topp:the_geom/gml:MultiPoint/gml:pointMember/gml:Point/gml:coordinates");
				String[] coordArray = coords.split(",");
				String longitudeString = coordArray[0];
				String latitudeString = coordArray[1];
				cb.setLatitude(latitudeString);
				cb.setLongitude(longitudeString);
				cb.setAdminName(city.valueOf("topp:ADMIN_NAME"));
				cb.setCountryName(city.valueOf("topp:CNTRY_NAME"));
				cb.setStatus(city.valueOf("topp:STATUS"));
				cb.setPopClass(city.valueOf("topp:POP_CLASS"));
			}			
		} catch (Exception e) {
			
		}
		return cb;
	}
	/** 
	 * Update a City through GISClient's web interface (using HttpUnit)
	 * @param oldCity the City feature that already exists and will be updated
	 * @param newCity a CityBean that contains the new values to set to the City feature
	 * @return a String that can be either SUCCESS or ERROR depending on the outcome of the operation
	 */
	private String updateCityUsingGISClient(CityBean oldCity, CityBean newCity) {
		String response = ERROR;
		try {
			URL url = new URL(GISCLIENT_URL);
			WebConversation wc = new WebConversation();
			WebRequest wr = new GetMethodWebRequest(url, "");
			WebResponse mainPage = wc.getResponse(wr);
			WebResponse linksFrame = wc.getFrameContents("methods");
			WebLink citiesLink = linksFrame.getLinkWith("Cities");
			citiesLink.click();
			// find the row that has the old city
			mainPage = wc.getCurrentPage();
			WebResponse citiesFrame = wc.getFrameContents("inputs");
			WebTable citiesTable = citiesFrame.getTables()[0];
			
			String[][] allTable = citiesTable.asText();
			WebLink editLink = null;
			String lastline = "" + allTable.length;
			for (int line=0; line < allTable.length; line++) {
				String[] aLine = allTable[line];
				lastline +=":" + aLine[0] + aLine[1]+aLine[2]+aLine[3];
				if (aLine.length>3 && 
					aLine[0].trim().equals(oldCity.getName()) &&
					aLine[1].trim().equals(oldCity.getCountryName()) &&
					aLine[2].trim().equals(oldCity.getAdminName()) &&
					aLine[3].trim().equals(oldCity.getPopClass()) ) 
				{
					TableCell tcell = citiesTable.getTableCell(line,4);
					editLink = tcell.getLinks()[0];
				}				
			}	
			if (editLink!=null) {
				editLink.click();
				// find the row that has the old city
				mainPage = wc.getCurrentPage();
				WebResponse editFrame = wc.getFrameContents("inputs");
				WebForm cityForm = editFrame.getForms()[0];			
				cityForm.setParameter("CITY_NAME",newCity.getName());
				cityForm.setParameter("LATITUDE",newCity.getLatitude());
				cityForm.setParameter("LONGITUDE",newCity.getLongitude());
				cityForm.setParameter("ADMIN_NAME",newCity.getAdminName());
				cityForm.setParameter("CNTRY_NAME",newCity.getCountryName());
				cityForm.setParameter("STATUS",newCity.getStatus());
				cityForm.setParameter("POP_CLASS",newCity.getPopClass());
				SubmitButton citySubmitButton = cityForm.getSubmitButton("method", "Update");
				citySubmitButton.click();
				mainPage = wc.getCurrentPage();
				//Now we have to navigate to actions and commit the new city
				WebResponse actionsFrame = wc.getFrameContents("methods");
				WebLink actionsLink = actionsFrame.getLinkWith("Actions");
				actionsLink.click();
				mainPage = wc.getCurrentPage();
				WebResponse commitFrame = wc.getFrameContents("inputs");
				WebForm commitForm = commitFrame.getForms()[0];
				SubmitButton commitCityButton = commitForm.getSubmitButton("method", "Commit");
				commitCityButton.click(); //click on the first commit button
				mainPage = wc.getCurrentPage();
				WebResponse verifyCommitFrame = wc.getFrameContents("inputs");
				WebForm verifyCommitForm = verifyCommitFrame.getForms()[0];
				Button verifyCommitButton = verifyCommitForm.getButtons()[0];
				verifyCommitButton.click(); //click on the second commit button
				mainPage = wc.getCurrentPage();
				WebResponse responseFrame = wc.getFrameContents("inputs");
				String xmlResponse = responseFrame.getText();
				if (xmlResponse.toLowerCase().contains("success")) {
					response = SUCCESS;
				} else {
					response = ERROR;
				}						
			} else {
				response = ERROR + ": no such element";
			}
		} catch (Exception e) {
			response = ERROR + e.toString();
		}		
		return response;		
	}
	/** 
	 * Deletes a City feature through GISClient's web interface (using HttpUnit).
	 * @param cb A CityBean containing the parameters of the City feature to be deleted
	 * @return a String that can be either SUCCESS or ERROR depending on the outcome of the operation
	 */	
	private String deleteCityUsingGISClient(CityBean cb) {
		String response = "";
		try {
			URL url = new URL(GISCLIENT_URL);
			WebConversation wc = new WebConversation();
			WebRequest wr = new GetMethodWebRequest(url, "");
			WebResponse mainPage = wc.getResponse(wr);
			WebResponse linksFrame = wc.getFrameContents("methods");
			WebLink citiesLink = linksFrame.getLinkWith("Cities");
			citiesLink.click();
			mainPage = wc.getCurrentPage();
			WebResponse citiesFrame = wc.getFrameContents("inputs");
			response += citiesFrame.getText() ;
			WebTable citiesTable = citiesFrame.getTables()[0];
			String[][] allTable = citiesTable.asText();
			WebLink editLink = null;
			for (int line=0; line < allTable.length; line++) {
				String[] aLine = allTable[line];
				if (aLine.length>3 && 
					aLine[0].trim().equals(cb.getName()) &&
					aLine[1].trim().equals(cb.getCountryName()) &&
					aLine[2].trim().equals(cb.getAdminName()) &&
					aLine[3].trim().equals(cb.getPopClass()) ) 
				{
					TableCell tcell = citiesTable.getTableCell(line,4);
					editLink = tcell.getLinks()[0];
				}				
			}	
			if (editLink!=null) {
				editLink.click();
				mainPage = wc.getCurrentPage();
				WebResponse editFrame = wc.getFrameContents("inputs");			
				WebForm cityForm = editFrame.getForms()[1];
				SubmitButton citySubmitButton = cityForm.getSubmitButton("method", "Delete");
				citySubmitButton.click();
				mainPage = wc.getCurrentPage();
				//Now we have to navigate to actions and commit the new city
				WebResponse actionsFrame = wc.getFrameContents("methods");
				WebLink actionsLink = actionsFrame.getLinkWith("Actions");
				actionsLink.click();
				mainPage = wc.getCurrentPage();
				WebResponse commitFrame = wc.getFrameContents("inputs");
				WebForm commitForm = commitFrame.getForms()[0];
				SubmitButton commitCityButton = commitForm.getSubmitButton("method", "Commit");
				commitCityButton.click(); //click on the first commit button
				mainPage = wc.getCurrentPage();
				WebResponse verifyCommitFrame = wc.getFrameContents("inputs");
				WebForm verifyCommitForm = verifyCommitFrame.getForms()[0];
				Button verifyCommitButton = verifyCommitForm.getButtons()[0];
				verifyCommitButton.click(); //click on the second commit button
				mainPage = wc.getCurrentPage();
				WebResponse responseFrame = wc.getFrameContents("inputs");
				String theResponse = responseFrame.getText();
				if (theResponse.toLowerCase().contains("success")) {
					response = SUCCESS;
				} else {
					response = theResponse;
				}					
			} else {
				response = ERROR;
			}		
		} catch (Exception e) {
			response = ERROR;
		}		
		return response;
	}	
	
	/** 
	 * Retrieves a City feature from GISClient's web interface.
	 * @param cb A CityBean object containing the parameters of the City feature to be retrieved.
	 * @return a CityBean object containing the parameters of the retrieved City feature. If none is found it returns <code>null</code>
	 */
	private CityBean retrieveCityFromGISClient(CityBean cb) {
		String response =ERROR;
		CityBean cityFound = new CityBean();
		
		try {
			URL url = new URL(GISCLIENT_URL);
			WebConversation wc = new WebConversation();
			WebRequest wr = new GetMethodWebRequest(url, "");
			WebResponse mainPage = wc.getResponse(wr);
			WebResponse linksFrame = wc.getFrameContents("methods");
			WebLink citiesLink = linksFrame.getLinkWith("Cities");
			citiesLink.click();
			// find the row that has the old city
			mainPage = wc.getCurrentPage();
			WebResponse citiesFrame = wc.getFrameContents("inputs");
			WebTable citiesTable = citiesFrame.getTables()[0];
			String[][] allTable = citiesTable.asText();
			WebLink editLink = null;
			String lastline = "" + allTable.length;
			for (int line=0; line < allTable.length; line++) {
				String[] aLine = allTable[line];
				if (aLine.length>3 && 
					aLine[0].trim().equals(cb.getName()) &&
					aLine[1].trim().equals(cb.getCountryName()) &&
					aLine[2].trim().equals(cb.getAdminName()) &&
					aLine[3].trim().equals(cb.getPopClass()) ) 
				{
					TableCell tcell = citiesTable.getTableCell(line,4);
					editLink = tcell.getLinks()[0];
				}				
			}	
			if (editLink!=null) {
				response += editLink.getText();
				editLink.click();
				mainPage = wc.getCurrentPage();
				WebResponse editFrame = wc.getFrameContents("inputs");
				WebForm cityForm = editFrame.getForms()[0];			
				cityFound.setAction("retrieveCityGIS");
				cityFound.setName(cityForm.getParameterValue("CITY_NAME"));
				cityFound.setLatitude(cityForm.getParameterValue("LATITUDE"));
				cityFound.setLongitude(cityForm.getParameterValue("LONGITUDE"));
				cityFound.setAdminName(cityForm.getParameterValue("ADMIN_NAME"));
				cityFound.setCountryName(cityForm.getParameterValue("CNTRY_NAME"));
				cityFound.setStatus(cityForm.getParameterValue("STATUS"));
				cityFound.setPopClass(cityForm.getParameterValue("POP_CLASS"));
				return cityFound;
			} else {
				return null;
			}			
		} catch (Exception e) {
			return null;
		}		
	}
}

