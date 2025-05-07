package space.itoncek.stellarium.api.objects;

import org.json.JSONArray;
import org.json.JSONObject;

public record AltAz(double alt, double az) {
	public static AltAz parse(JSONObject o) {
		JSONArray arr = new JSONArray(o.getString("altAz"));
		System.out.println(arr.toString(4));
		double[] azalt = toAzAlt(arr.getDouble(0), arr.getDouble(1), arr.getDouble(2));
		return new AltAz(azalt[1],azalt[0]);
	}

	private static double[] toAzAlt(double x, double y, double z) {
		double alt = Math.asin(z);
		double azPrime = Math.atan2(y, x); // in radians

		double az = Math.toRadians(180) - azPrime; // Az = 180° - Az'
		if (az < 0) az += 2 * Math.PI;
		if (az >= 2 * Math.PI) az -= 2 * Math.PI;

		return new double[] { Math.toDegrees(az), Math.toDegrees(alt) };
	}

}
