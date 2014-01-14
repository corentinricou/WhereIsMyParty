package info.androidhive.slidingmenu;

import java.io.IOException;
import java.util.List;

import android.location.Address;
import android.location.Geocoder;

public class Event {

	private String name;
	
	private String location;
	
	private Double latitude;
	
	private Double longitude;
	
	private Boolean isAlreadyTriggered = false;
	
	private int id;
	
	public Boolean IsAlreadyTriggered() {
		return isAlreadyTriggered;
	}

	public void setIsAlreadyTriggered(Boolean isAlreadyTriggered) {
		this.isAlreadyTriggered = isAlreadyTriggered;
	}

	public Event(String name, String location, Double latitude, Double longitude, int id) {
		super();
		this.name = name;
		this.location = location;
		this.latitude = latitude;
		this.longitude = longitude;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
}
