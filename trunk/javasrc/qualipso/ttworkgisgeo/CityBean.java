package qualipso.ttworkgisgeo;

import java.io.Serializable;

public class CityBean implements Serializable {
	private String action="";
	private String name="";
	private String longitude="";
	private String latitude="";
	private String adminName="";
	private String countryName="";
	private String status="";
	private String popClass="";
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getAdminName() {
		return adminName;
	}
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	public String getCountryName() {
		return countryName;
	}
	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPopClass() {
		return popClass;
	}
	public void setPopClass(String popClass) {
		this.popClass = popClass;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}

}
