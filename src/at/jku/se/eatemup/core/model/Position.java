package at.jku.se.eatemup.core.model;

import at.jku.se.eatemup.core.GPSTool;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "goodieLocations")
public class Position {
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(unique = true)
	private double longitude;
	@DatabaseField(unique = true)
	private double latitude;

	public Position() {

	}

	public double calcDistance(Position position2) {
		return GPSTool.calcDistance(latitude, longitude, position2.latitude,
				position2.longitude);
	}

	public boolean differentFrom(Position p) {
		return p.latitude != this.latitude || p.longitude != this.longitude;
	}

	public boolean distanceLessThan(Position position2, int meters) {
		double dist = calcDistance(position2);
		if ((int) dist < meters)
			return true;
		return false;
	}

	public int getId() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
