package qualipso.ttworkgisgeo.tci;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.etsi.ttcn.tci.BooleanValue;
import org.etsi.ttcn.tci.CharstringValue;
import org.etsi.ttcn.tci.FloatValue;
import org.etsi.ttcn.tci.HexstringValue;
import org.etsi.ttcn.tci.IntegerValue;
import org.etsi.ttcn.tci.RecordValue;
import org.etsi.ttcn.tci.TciCDProvided;
import org.etsi.ttcn.tci.TciTypeClass;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.UniversalCharstringValue;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriMessage;

import qualipso.ttworkgisgeo.CityBean;

import com.testingtech.ttcn.logging.RTLoggingConstants;
import com.testingtech.ttcn.tci.TciTemplate;
import com.testingtech.ttcn.tci.codec.base.AbstractBaseCodec;
import com.testingtech.ttcn.tri.TriMessageImpl;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;
/**
 * Coder and Decoder class. The encode function receives a Value from the TTCN-3 code and encodes it 
 * into a byte array. The decode function receives a value from the SUT and transforms it into a Value 
 * based on the decoding hypothesis.
 */
public class Codec extends AbstractBaseCodec implements TciCDProvided {
	private final byte DELIMETER = 0x0D;
	public Codec(final RB rb) {		
		super(rb);
	}
	/** 
	 * Receives a value from the TTCN-3 code and transforms it to a byte array that is stored 
	 * in a <code>TriMessage</code> object. Since the Value that comes from the TTCN-3 code is always a RecordValue
	 * the encoding consists of creating the String: "param_name1"+DELIMETER+"param_value1"+DELIMETER+"param_name2"+DELIMETER+"param_value2"+...
	 * @param value The variable received from TTCN-3 code rendered as a Value object.
	 * @return A <code>TriMessage</code> object encapsulating the encoded message as a byte array
	 */
	public TriMessage encode(final Value value) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    String errMsg = "";
		if (value != null) {
			if (((TciTemplate) value).isAnyOrOmit() || ((TciTemplate) value).isAny()) { //cannot have *any* or *omit* in message to send
				errMsg="cannot have *any* or *omit* in send message";
			} else {
				String[] fieldNames = ((RecordValue)value).getFieldNames();
				if (fieldNames!=null && fieldNames.length>0) {
					for (int i=0; i<fieldNames.length; i++) {
						errMsg = errMsg + "1";
						Value fieldValue = ((RecordValue)value).getField(fieldNames[i]);
						
						if (((TciTemplate) fieldValue).isAnyOrOmit() || ((TciTemplate) fieldValue).isAny() 
								|| fieldValue.notPresent()) {
							// cannot have *any* or *omit* in send message
							errMsg= errMsg +"||"+fieldNames[i]+"-->" +fieldValue+"|| isAnyOrOmit:"+ ((TciTemplate) fieldValue).isAnyOrOmit() +
							"--"+"isAny:"+((TciTemplate) fieldValue).isAny() + 
							"--" + "!notPresent():" +!fieldValue.notPresent() +
							"\n"+"cannot have *any* or *omit* in sent record's fields";
						} else {
							byte[] name = fieldNames[i].getBytes();
							baos.write(name,0,name.length);
							baos.write(DELIMETER);
							String stringValue= "";
							switch (fieldValue.getType().getTypeClass()) {
								case TciTypeClass.INTEGER:
									stringValue = Integer.toString(((IntegerValue)fieldValue).getInt());
								    break;						
								case TciTypeClass.BOOLEAN:
									stringValue = Boolean.toString(((BooleanValue)fieldValue).getBoolean());
									break;
								case TciTypeClass.FLOAT:
									stringValue = Float.toString(((FloatValue)fieldValue).getFloat());
								    break;
								case TciTypeClass.CHARSTRING:								
									stringValue = ((CharstringValue)fieldValue).getString();
									break;
								case TciTypeClass.UNIVERSAL_CHARSTRING:
									stringValue = ((UniversalCharstringValue)fieldValue).getString();
									break;											
								case TciTypeClass.HEXSTRING:
								    stringValue = ((HexstringValue)fieldValue).getString();
								    break;						
								default:
									stringValue = "wrongTypeOrValue";
							}	
							byte[] valueToByteArray = stringValue.getBytes();
							baos.write(valueToByteArray,0,valueToByteArray.length);
							baos.write(DELIMETER);
							try {
								baos.flush();
							} catch (Exception e) {
								errMsg = "IOErrorWhileFlushing";
								return new TriMessageImpl(errMsg.getBytes());
							}
						}
					}
					if (baos.size()>0) {
						return new TriMessageImpl(baos.toByteArray());	
					} else {
						errMsg = errMsg + "nothing written to decoder output";
						return new TriMessageImpl(errMsg.getBytes());
						
					}
				} else {
					errMsg = "record has no fields";
					return new TriMessageImpl(errMsg.getBytes());
				}
			}
	    }
		errMsg = "wrongTemplate";		
	    return new TriMessageImpl(errMsg.getBytes());
	}

	/** 
	 * Receives a byte array from the SUT and transforms it to a Value that will be passed to the TTCN-3 code
	 * @param rcvdMessage A <code>TriMessage</code> object containing the SUT response as a byte array
	 * @param decodingHypothesis The <code>Type</code> that is expected by the <code>receive()</code>
	 *  operation of the TTCN-3 code. 
	 */
	public Value decode(TriMessage rcvdMessage, Type decodingHypothesis) {		
		switch (decodingHypothesis.getTypeClass()) {
		case TciTypeClass.RECORD:			
			if (decodingHypothesis.getName().equals("VerifyCity")) {
				final RecordValue mapDetailsValue = (RecordValue)decodingHypothesis.newInstance();
				
				final Value nameValueInit = mapDetailsValue.getField("name");
				final Value countryValueInit = mapDetailsValue.getField("country");
				final Value adminValueInit = mapDetailsValue.getField("administration");
				final Value popClassValueInit = mapDetailsValue.getField("popClass");
				
				CharstringValue nameValue = (CharstringValue)nameValueInit.getType().newInstance();
				CharstringValue countryValue = (CharstringValue)countryValueInit.getType().newInstance();
				CharstringValue adminValue = (CharstringValue)adminValueInit.getType().newInstance();
				CharstringValue popClassValue = (CharstringValue)popClassValueInit.getType().newInstance();
				
				String message = new String(rcvdMessage.getEncodedMessage());
				String data[] = message.split("#");
				RB.getTciTLProvided().tliRT("", System.nanoTime(), "", -1, null, RTLoggingConstants.RT_LOG_DEBUG, "Decoded arraysize: " + data.length);
				if (data.length == 5) {
					String name = data[0].trim();
					String country=data[1].trim();
					String admin=data[2].trim();
					String popClass=data[3].trim();
					
					nameValue.setString(name);
					countryValue.setString(country);
					adminValue.setString(admin);
					popClassValue.setString(popClass);
					
					mapDetailsValue.setField("name", nameValue);
					mapDetailsValue.setField("country", countryValue);
					mapDetailsValue.setField("administration", adminValue);
					mapDetailsValue.setField("popClass", popClassValue);
					
					return mapDetailsValue;					
				}		
			} else if (decodingHypothesis.getName().equals("CityType")) {
				final RecordValue cityTypeValue = (RecordValue)decodingHypothesis.newInstance();
				
				final Value actionValue = cityTypeValue.getField("requestType");				
				final Value cityNameValue = cityTypeValue.getField("cityName");
				final Value longitudeValue = cityTypeValue.getField("longitude");
				final Value latitudeValue = cityTypeValue.getField("latitude");
				final Value adminValue = cityTypeValue.getField("adminName");
				final Value countryValue = cityTypeValue.getField("countryName");
				final Value statusValue = cityTypeValue.getField("status");
				final Value popValue = cityTypeValue.getField("popClass");
				
				CharstringValue actionChar = (CharstringValue)actionValue.getType().newInstance();
				CharstringValue cityNameChar = (CharstringValue)cityNameValue.getType().newInstance();
				CharstringValue longitudeChar = (CharstringValue)longitudeValue.getType().newInstance();
				CharstringValue latitudeChar = (CharstringValue)latitudeValue.getType().newInstance();
				CharstringValue adminChar = (CharstringValue)adminValue.getType().newInstance();
				CharstringValue countryChar = (CharstringValue)countryValue.getType().newInstance();
				CharstringValue statusChar = (CharstringValue)statusValue.getType().newInstance();
				CharstringValue popChar = (CharstringValue)popValue.getType().newInstance();
				CityBean cb = null;
				try {
					ByteArrayInputStream array_in = new ByteArrayInputStream(rcvdMessage.getEncodedMessage());
					ObjectInputStream obj_in = new ObjectInputStream(array_in);
					Object obj = obj_in.readObject();
					if (obj instanceof CityBean) {
						cb = (CityBean)obj;						
					} else {
						//TODO						
					}
				} 
				catch (Exception e) {
					//TODO					
				}
				if (cb!=null) {
					String action = cb.getAction();
					String cityName = cb.getName();
					String longitude = cb.getLongitude();
					String latitude = cb.getLatitude();
					String adminName = cb.getAdminName();
					String country = cb.getCountryName();
					String status = cb.getStatus();
					String pop = cb.getPopClass();
					
					if (action==null || action.equals("")) {action = "";}
					if (cityName==null || cityName.equals("")) {cityName = "";}
					if (longitude==null || longitude.equals("")) {longitude = "0.0";}
					if (latitude==null || latitude.equals("")) {latitude = "0.0";}
					if (adminName==null || adminName.equals("")) {adminName = "";}
					if (country==null || country.equals("")) {country = "";}
					if (status==null || status.equals("")) {status = "";}
					if (pop==null || pop.equals("")) {pop = "";}
					
					actionChar.setString(action);
					cityNameChar.setString(cityName);
					longitudeChar.setString(longitude);
					latitudeChar.setString(latitude);
					adminChar.setString(adminName);
					countryChar.setString(country);
					statusChar.setString(status);
					popChar.setString(pop);
				} else {
					//TODO
				}				
				cityTypeValue.setField("requestType", actionChar);
				cityTypeValue.setField("cityName", cityNameChar);
				cityTypeValue.setField("latitude", latitudeChar);
				cityTypeValue.setField("longitude", longitudeChar);
				cityTypeValue.setField("adminName", adminChar);
				cityTypeValue.setField("countryName", countryChar);
				cityTypeValue.setField("status", statusChar);
				cityTypeValue.setField("popClass", popChar);				
				return cityTypeValue;
			}
		break;
		case TciTypeClass.CHARSTRING:
			String messageString= new String(rcvdMessage.getEncodedMessage());
			CharstringValue charVal = (CharstringValue)decodingHypothesis.newInstance();
			charVal.setString(messageString);
			return charVal; 
		default:
			return null;
		}
		return null;
	}	
}

