package space.itoncek.spc3.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MovementGenerator extends GenericSliderGenerator{
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
				""".formatted(dateToString(date,time));
	}
}
