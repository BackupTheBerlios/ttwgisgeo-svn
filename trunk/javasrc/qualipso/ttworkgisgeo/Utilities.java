package qualipso.ttworkgisgeo;
/*
 * Utility class that helps create the XML messages 
 */
public class Utilities {
	public String getInsertCityQuery(CityBean cb) {
		String coordinates = cb.getLongitude() + "," + cb.getLatitude();
		String query = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>" +
		"<wfs:Transaction " +
		" xmlns:wfs='http://www.opengis.net/wfs'" +
		" xmlns:ogc=\"http://www.opengis.net/ogc\"" +
		" xmlns:topp=\"http://www.openplans.org/topp\""+
		" xmlns:gml=\"http://www.opengis.net/gml\"" +
		" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
		"<wfs:Insert><topp:tasmania_cities xmlns:topp='http://www.openplans.org/topp'>" +
		"	<topp:the_geom>" +
		"		<gml:MultiPoint xmlns:gml='http://www.opengis.net/gml' srsName='epsg:4326'>" +
		"		<gml:pointMember>" + 
		"          <gml:Point>" +
		"            <gml:coordinates decimal='.' cs=',' ts=' '>" +
						coordinates + 
					"</gml:coordinates>" +
		"          </gml:Point>" +
		"        </gml:pointMember>" + "      </gml:MultiPoint>" +
		"    </topp:the_geom>" +
		"    <topp:CITY_NAME>" + cb.getName() + "</topp:CITY_NAME>" +
		"    <topp:ADMIN_NAME>"+cb.getAdminName()+"</topp:ADMIN_NAME>" +
		"    <topp:CNTRY_NAME>"+cb.getCountryName()+"</topp:CNTRY_NAME>" +
		"    <topp:STATUS>"+cb.getStatus() +"</topp:STATUS>" +
		"    <topp:POP_CLASS>"+cb.getPopClass()+"</topp:POP_CLASS>" +
		"  </topp:tasmania_cities></wfs:Insert></wfs:Transaction>";
		return query;
	}
	
	public String getRetrieveCityQuery(CityBean cityBean) {		
		String filter = getANDFilterQuery(cityBean);
		String query = "<wfs:GetFeature version=\"1.1.0\" service=\"WFS\"" +
						" xmlns:wfs=\"http://www.opengis.net/wfs\" " +
						" xmlns:ogc=\"http://www.opengis.net/ogc\"" +
						" xmlns:topp=\"http://www.openplans.org/topp\""+
						" xmlns:gml=\"http://www.opengis.net/gml\"" +
						" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
						" xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\">" +
						" <wfs:Query typeName=\"topp:tasmania_cities\">" + filter + " </wfs:Query>" +
						"</wfs:GetFeature>";
		return query;
	}
	
	public String getUpdateCityQuery(CityBean oldCity, CityBean newCity) {
		
		return null;
	}
	
	public String getDeleteCityQuery(CityBean cityBean) {
		String filter = getANDFilterQuery(cityBean);
		String query = "<?xml version=\'1.0\' encoding=\'UTF-8\'?>"+
		" <wfs:Transaction " +
		"   xmlns:wfs='http://www.opengis.net/wfs' " +
		"   xmlns:ogc=\"http://www.opengis.net/ogc\"" +
		"   xmlns:topp=\"http://www.openplans.org/topp\"" +
		"   xmlns:gml=\"http://www.opengis.net/gml\">" +
		"   <wfs:Delete typeName=\"topp:tasmania_cities\">" +
		  filter +
		 "   </wfs:Delete>" +
		 " </wfs:Transaction>	";
		return query;
	}
	
	private String getANDFilterQuery(CityBean cityBean) {
		String cityName = cityBean.getName();
		String cityLongitude = cityBean.getLongitude();
		String cityLatitude = cityBean.getLatitude();
		String cityAdmin = cityBean.getAdminName();
		String countryName = cityBean.getCountryName();
		String cityStatus = cityBean.getStatus();
		String popClass = cityBean.getPopClass();
		
		String namePart = "";
		String coordPart = "";
		String adminPart = "";
		String countryPart = "";
		String statusPart = "";
		String popPart = "";
		
		if (cityName !=null && !cityName.equals("")) {
			namePart = 	"<PropertyIsEqualTo>" +
						"  <PropertyName>topp:CITY_NAME</PropertyName>" +
						"  <Literal>" + cityName +"</Literal>" +
						"</PropertyIsEqualTo>";
		}
		
		if ((cityLongitude!=null && !cityLongitude.equals("")) 
				&& (cityLatitude!=null && !cityLatitude.equals("")) ) {
			String coordinates  = cityLongitude +"," + cityLatitude ;
			coordPart = "<Equals>" +
						"  <PropertyName>the_geom</PropertyName>" +
						"  <gml:MultiPoint srsName=\"http://www.opengis.net/gml/srs/epsg.xml#4326\">" +
						"    <gml:pointMember> " +
						"      <gml:Point> " +
						"        <gml:coordinates decimal=\".\" cs=\",\" ts=\" \">"+coordinates+"</gml:coordinates>" +
						"      </gml:Point>" +
                        "    </gml:pointMember>" +
                        "  </gml:MultiPoint>" +
						"</Equals>";
		}
		
		if (cityAdmin!=null && !cityAdmin.equals("")) {
			adminPart =	"<PropertyIsEqualTo>" +
						"  <PropertyName>topp:ADMIN_NAME</PropertyName>" +
						"  <Literal>" + cityAdmin +"</Literal>" +
						"</PropertyIsEqualTo>";
		}
		
		if (countryName!=null && !countryName.equals("")) {
			countryPart =	"<PropertyIsEqualTo>" +
							"  <PropertyName>topp:CNTRY_NAME</PropertyName>" +
							"  <Literal>" + countryName +"</Literal>" +
							"</PropertyIsEqualTo>";
			
		}
		
		if (cityStatus!=null && !cityStatus.equals("")) {
			statusPart =	"<PropertyIsEqualTo>" +
							"  <PropertyName>topp:STATUS</PropertyName>" +
							"  <Literal>" + cityStatus +"</Literal>" +
							"</PropertyIsEqualTo>";
		}
		
		if (popClass!=null && !popClass.equals("")) {
			popPart =	"<PropertyIsEqualTo>" +
						"  <PropertyName>topp:POP_CLASS</PropertyName>" +
						"  <Literal>" + popClass +"</Literal>" +
						"</PropertyIsEqualTo>";
		}
		
		String query =
		"   <ogc:Filter>" +
		"   <And>" +
		  namePart + coordPart + adminPart + countryPart + statusPart + popPart +
        "   </And>" +
		"   </ogc:Filter>";
		
		return query;	
	}
}