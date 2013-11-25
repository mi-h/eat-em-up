package at.jku.se.eatemup.core;

public class GPSTool {
	/**
	 * This method calculates the distance between two points (given the
	 * latitude/longitude of those points).
	 * 
	 * @param lat1
	 *            - latitude point 1
	 * @param lon1
	 *            - longitude point 1
	 * @param lat2
	 *            - latitude point 2
	 * @param lon2
	 *            - longitude point 2
	 * @param unit
	 *            - unit of measure (M, K, N)
	 * @return the distance between the two points
	 */
	public static double calcDistance(double lat1, double lon1, double lat2,
			double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		return dist * 1609.344;
	}

	/**
	 * <p>
	 * This function converts decimal degrees to radians.
	 * </p>
	 * 
	 * @param deg
	 *            - the decimal to convert to radians
	 * @return the decimal converted to radians
	 */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/**
	 * <p>
	 * This function converts radians to decimal degrees.
	 * </p>
	 * 
	 * @param rad
	 *            - the radian to convert
	 * @return the radian converted to decimal degrees
	 */
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}
}
