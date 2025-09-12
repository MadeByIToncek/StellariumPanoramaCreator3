package space.itoncek.spc3.utils;

import space.itoncek.spc3.database.SlideTrackTransition;
import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.database.Target;

import java.io.File;

public class SlidetrackGenerator extends GenericSliderGenerator{
	public static final int framesPerSecond = 50;
	public static String generateScript(SlideTrackTransition set, boolean preview, String basePath) {
		return generateMethods() + "\n\n" +
	   generateInit() + "\n\n" +
	   generateTargetOptions(set) + "\n\n" +
	   generateJdToDateTime() + "\n\n" +
	   generatePreStartScript(set) + "\n\n" +
	   generateMainLoop(set, preview, basePath);
	}

	private static String generateMainLoop(SlideTrackTransition set, boolean preview, String basePath) {
		String str = """
				for(var i = 0; i < steps; i++) {
					var t = i/steps;
					var zoom = lerpsmooth(startFov, endFov, t);
					var day = Math.floor(lerpsmooth(startDay+.5, endDay+.5, t));
					var hour = lerpsmooth(startHour, endHour, t);
					core.setJDay(day+hour);
					core.moveToObject(target, 0.);
					StelMovementMgr.zoomTo(zoom,0);
					core.wait(0.01);
				""";
		if (!preview) {
			str += "	core.screenshot((\"\"+i).padStart(4,\"0\"),false,\"%s\",true,\"jpeg\");\n".formatted(determineFolderName(set, basePath));
		}
		str += "}";
		return str;
	}

	private static String determineFolderName(SlideTrackTransition set, String basePath) {
		String s;
		if (set.getName() == null || set.getName().isEmpty()) {
			s = basePath + "\\" + set.getUuid().toString();
		} else {
			s = basePath + "\\" + set.getName();
		}
		new File(s).mkdirs();
		s = s.replace("\\","/");
		return s;
	}

	private static String generatePreStartScript(SlideTrackTransition set) {
		return """
				var steps = %d;
				var target = "%s";
				core.moveToObject(target, .01);
				StelMovementMgr.zoomTo(startFov,0);
				core.setJDay(startTime);
				core.wait(3);
				""".formatted(set.getDuration()*framesPerSecond,set.getTarget());
	}

	private static String generateTargetOptions(SlideTrackTransition set) {
		return """
				var startFov = %f;
				var endFov = %f;
				
				var startTime = core.jdFromDateString("%s", "utc");
				var endTime = core.jdFromDateString("%s", "utc");
				""".formatted(set.getStartZoom(), set.getEndZoom(), dateToString(set.getStartDate(),set.getStartTime()), dateToString(set.getEndDate(), set.getEndTime()));
	}
}
