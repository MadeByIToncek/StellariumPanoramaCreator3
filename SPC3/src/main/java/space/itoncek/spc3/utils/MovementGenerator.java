package space.itoncek.spc3.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MovementGenerator {
	public static String generateCenterObject(String objectName) {
		return """
				core.moveToObject("%s",.01);
				""".formatted(objectName);
	}
	public static String generateZoom(double zoom) {
		return """
				StelMovementMgr.zoomTo(%f,.01);
				""".formatted(zoom);
	}
	public static String generateDateTime(LocalDate date, LocalTime time) {
		return """
				core.setTimeRate(0);
				core.setDate("%s");
				""".formatted(LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
	}
}
