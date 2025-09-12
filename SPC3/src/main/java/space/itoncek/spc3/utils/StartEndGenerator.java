package space.itoncek.spc3.utils;

import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.database.Target;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class StartEndGenerator extends GenericSliderGenerator{
	public static final int framesPerSecond = 50;
	public static String generateScript(StartEndTransition set, boolean preview, String basePath) {
		return generateMethods() + "\n\n" +
	   generateInit() + "\n\n" +
	   generateTargetOptions(set) + "\n\n" +
	   generateJdToDateTime() + "\n\n" +
	   generatePreStartScript(set) + "\n\n" +
	   generateMainLoop(set, preview, basePath);
	}

	private static String generateMainLoop(StartEndTransition set, boolean preview, String basePath) {
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
			str += "	core.screenshot((\"\"+i).padStart(4,\"0\"),false,\"%s\",true,\"jpeg\");\n".formatted(determineFolderName(set, basePath));
		}
		str += "}";
		return str;
	}

	private static String determineFolderName(StartEndTransition set, String basePath) {
		String s;
		if (set.getName() == null || set.getName().isEmpty()) {
			s = basePath + "\\" + set.getUuid().toString().replace("-","");
		} else {
			s = basePath + "\\" + set.getName();
		}
		new File(s).mkdirs();
		s = s.replace("\\","/");
		return s;
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
}
