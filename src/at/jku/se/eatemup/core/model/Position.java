package at.jku.se.eatemup.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import at.jku.se.eatemup.core.GPSTool;

@DatabaseTable(tableName="goodieLocations")
public class Position {
	@DatabaseField(generatedId=true)
	private int id;
	@DatabaseField
	private double longitude;
	@DatabaseField
	private double latitude;
	
	public Position(){
		
	}

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
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
