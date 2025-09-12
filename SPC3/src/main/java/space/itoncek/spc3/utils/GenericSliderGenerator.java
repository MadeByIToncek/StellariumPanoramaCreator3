package space.itoncek.spc3.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class GenericSliderGenerator {

	public static String generateInit() {
		return Stream.of("core.clear(\"natural\");",
				"ConstellationMgr.setArtFadeDuration(0.01);",
				"SolarSystem.setFlagPlanets(true);",
				"core.setGuiVisible(false);",
				"core.setTimeRate(0);",
				"ConstellationMgr.setFlagLines(false);",
				"ConstellationMgr.setFlagArt(false);",
				"ConstellationMgr.setFlagIsolateSelected(true);",
				"LandscapeMgr.setAtmosphereModel(\"ShowMySky\");",
				"LandscapeMgr.setAtmosphereModelPath(\"C:/Users/user/scoop/apps/stellarium/current/atmosphere/default\");",
				"LandscapeMgr.setAtmosphereShowMySkyStoppedWithError(true);",
				"core.wait(2);",
				"var label = LandscapeMgr.getAtmosphereShowMySkyStatusText();",
				"LabelMgr.labelScreen(label,10, 10, true, 32.0, \"#ffffff\");",
				"core.wait(1);",
				"LabelMgr.deleteAllLabels();",
				"StelSkyDrawer.setFlagLuminanceAdaptation(false);").parallel().collect(() ->new StringBuilder("\n"), StringBuilder::append, StringBuilder::append).toString();
	}


	public static String dateToString(LocalDate date, LocalTime time) {
		return LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	public static String generateMethods() {
		return """
				function lerpsmooth(start, end, t)
				{
				    var t2 = lerp(Math.pow(t,2), 1 - Math.pow(1-t,2), t);
					return lerp(start,end,t2);
				}
				
				function lerp(a, b, t)
				{
				    return (1 - t) * a + t * b;
				}
				
				function int(a)
				{
				    return Math.floor(a);
				}
				
				function frac(a) {
				    return a - int(a);
				}
				""";
	}

	public static String generateJdToDateTime() {
		return """
				var startDay = int(startTime);
				var endDay = int(endTime);
				
				var startHour = frac(startTime);
				var endHour = frac(endTime);
				""";
	}
}
