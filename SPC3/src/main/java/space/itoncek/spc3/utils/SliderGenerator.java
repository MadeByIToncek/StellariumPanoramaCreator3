package space.itoncek.spc3.utils;

import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.database.Target;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class SliderGenerator {
	public static final int framesPerSecond = 50;
	public static String generateScript(StartEndTransition set, boolean preview) {
		return generateMethods() + "\n\n" +
	   generateInit() + "\n\n" +
	   generateTargetOptions(set) + "\n\n" +
	   generateJdToDateTime() + "\n\n" +
	   generatePreStartScript(set) + "\n\n" +
	   generateMainLoop(set, preview);
	}

	private static String generateInit() {
		return Stream.of("core.clear(\"natural\");",
				"ConstellationMgr.setArtFadeDuration(0.01);",
				"SolarSystem.setFlagPlanets(true);",
				"core.setGuiVisible(false);",
				"core.setTimeRate(0);",
				"ConstellationMgr.setFlagLines(false);",
				"ConstellationMgr.setFlagArt(false);",
				"ConstellationMgr.setFlagIsolateSelected(true);",
				"LandscapeMgr.setAtmosphereModel(\"ShowMySky\");",
				"LandscapeMgr.setAtmosphereModelPath(\"D:/Program Files/Stellarium/atmosphere/default\");",
				"LandscapeMgr.setAtmosphereShowMySkyStoppedWithError(true);",
				"core.wait(2);",
				"var label = LandscapeMgr.getAtmosphereShowMySkyStatusText();",
				"LabelMgr.labelScreen(label,10, 10, true, 32.0, \"#ffffff\");",
				"core.wait(1);",
				"LabelMgr.deleteAllLabels();",
				"StelSkyDrawer.setFlagLuminanceAdaptation(false);").parallel().collect(() ->new StringBuilder("\n"), StringBuilder::append, StringBuilder::append).toString();
	}

	private static String generateMainLoop(StartEndTransition set, boolean preview) {
		String str = """
				for(var i = 0; i < steps; i++) {
					var t = i/steps;
					var alt = lerpsmooth(startalt, endalt, t);
					var az = lerpsmooth(startaz, endaz, t);
					var zoom = lerpsmooth(startFov, endFov, t);
					var day = Math.floor(lerpsmooth(startDay+.5, endDay+.5, t));
					var hour = lerpsmooth(startHour, endHour, t);
				\t
					core.setJDay(day+hour);
					core.moveToAltAzi(alt, az, 0.);
					StelMovementMgr.zoomTo(zoom,0);
					core.wait(0.01);
				""";
		if (!preview) {
			str += "core.screenshot((\"\"+i).padStart(4,\"0\"),false,\"%s\",true,\"jpeg\");\n".formatted(determineFolderName(set));
		}
		str += "}";
		return str;
	}

	private static String determineFolderName(StartEndTransition set) {
		if (set.getName() == null || set.getName().isEmpty()) {
			return set.getUuid().toString();
		} else return set.getName();
	}

	private static String generatePreStartScript(StartEndTransition set) {
		return """
				var steps = %d;
				core.moveToAltAzi(startalt, startaz, .01);
				StelMovementMgr.zoomTo(startFov,0);
				core.setJDay(startTime);
				core.wait(3);
				""".formatted(set.getDuration()*framesPerSecond);
	}

	private static String generateJdToDateTime() {
		return """
				var startDay = int(startTime);
				var endDay = int(endTime);
				
				var startHour = frac(startTime);
				var endHour = frac(endTime);
				""";
	}

	private static String generateTargetOptions(StartEndTransition set) {
		return generateTarget(set.getStart(), true) + "\n\n\n" + generateTarget(set.getEnd(), false);
	}

	private static String generateTarget(Target target, boolean start) {
		String prefix = start ? "start" : "end";
		return switch (target.getType()) {
			case AzAlt -> """
					var %salt = %f;
					var %saz = %f;
					var %sFov = %f;
					var %sTime = core.jdFromDateString("%s", "utc");
					""".formatted(
					prefix, target.getAlt(),
					prefix, target.getAz(),
					prefix, target.getZoom(),
					prefix, dateToString(target.getDate(), target.getTime())
			);
			case Object -> """
					var %sObject = "%s"
					var %sFov = %f
					var %sTime = core.jdFromDateString("%s", "utc");
					
					core.setJDay(%sTime);
					core.wait(0.5);
					
					var %salt = core.getObjectInfo(%sObject).altitude
					var %saz = core.getObjectInfo(%sObject).azimuth
					""".formatted(prefix, target.getName(),
					prefix, target.getZoom(),
					prefix, dateToString(target.getDate(), target.getTime()),
					prefix, prefix, prefix, prefix, prefix
			);
		};
	}

	private static String dateToString(LocalDate date, LocalTime time) {
		return LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	private static String generateMethods() {
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
}
