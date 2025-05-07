package space.itoncek.stellarium.api.objects;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record StatusResponse(Location location, String selectionInfo, Time time, double fov) {
	public static StatusResponse parse(JSONObject object) {
		return new StatusResponse(
				Location.parse(object.getJSONObject("location")),
				object.getString("selectioninfo"),
				Time.parse(object.getJSONObject("time")),
				object.getJSONObject("view").getDouble("fov")
		);
	}

	public record Time(double deltaT,
						double gmtShift,
						boolean isTimeNow,
						double jday,
						ZonedDateTime local,
						double timeRate,
						ZonedDateTime utc
						) {
		public static Time parse(JSONObject object) {
			return new Time(
					object.getDouble("deltaT"),
					object.getDouble("gmtShift"),
					object.getBoolean("isTimeNow"),
					object.getDouble("jday"),
					LocalDateTime.parse(object.getString("local")).atOffset(getZoneOffset(object.getString("timeZone"))).toZonedDateTime(),
					object.getDouble("timerate"),
					ZonedDateTime.parse(object.getString("utc"))
			);
		}

		private static ZoneOffset getZoneOffset(String timeZone) {
			// Compile regular expression
			final Pattern pattern = Pattern.compile("UTC\\+(0?[0-9]|1[0-9]|2[0-3]):(0?[0-9]|[1-5][0-9])", Pattern.CASE_INSENSITIVE);
			// Match regex against input
			final Matcher matcher = pattern.matcher(timeZone);
			// Use results...
			matcher.find();
			String hour = matcher.group(1);
			String minute = matcher.group(2);
			return ZoneOffset.ofHoursMinutes(Integer.parseInt(hour), Integer.parseInt(minute));
		}
	}

	public record Location(double altitude,
							String landscapeKey,
							double latitude,
							double longitude,
							String name,
							String planet,
							String region,
							String role,
							String state
	) {
		public static Location parse(JSONObject object) {
			return new Location(
					object.getDouble("altitude"),
					object.getString("landscapeKey"),
					object.getDouble("latitude"),
					object.getDouble("longitude"),
					object.getString("name"),
					object.getString("planet"),
					object.getString("region"),
					object.getString("role"),
					object.getString("state")
			);
		}
	}
}
