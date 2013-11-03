package at.jku.se.eatemup.core.model;

import at.jku.se.eatemup.core.GPSTool;

public class Position {
	private double longitude;
	private double latitude;

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public boolean distanceLessThan(Position position2, int meters){
		double dist = calcDistance(position2);
		if ((int)dist < meters) return true;
		return false;
	}
	
	public double calcDistance(Position position2){
		return GPSTool.calcDistance(latitude, longitude, position2.latitude, position2.longitude);
	}
}
