package space.itoncek.spc3.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Setter
@Entity
public class SlideTrackTransition {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	UUID uuid;
	@Basic(fetch = FetchType.LAZY)
	int duration;
	@Basic(fetch = FetchType.LAZY)
	String name;
	@Basic(fetch = FetchType.LAZY)
	String target;
	@Basic(fetch = FetchType.LAZY)
	LocalDate startDate;
	@Basic(fetch = FetchType.LAZY)
	LocalTime startTime;
	@Basic(fetch = FetchType.LAZY)
	double startZoom;
	@Basic(fetch = FetchType.LAZY)
	LocalDate endDate;
	@Basic(fetch = FetchType.LAZY)
	LocalTime endTime;
	@Basic(fetch = FetchType.LAZY)
	double endZoom;

	public static SlideTrackTransition createTransition(String target, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, double startZoom, double endZoom) {
		SlideTrackTransition stt = new SlideTrackTransition();
		stt.name = "";
		stt.target = target;
		stt.startDate = startDate;
		stt.startTime = startTime;
		stt.startZoom = startZoom;
		stt.endDate = endDate;
		stt.endTime = endTime;
		stt.endZoom = endZoom;
		return stt;
	}

	public JSONObject toSimpleJson() {
		return new JSONObject()
				.put("uuid", uuid)
				.put("name", name)
				.put("duration",duration)
				.put("target", target)
				.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
				.put("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
				.put("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
				.put("endTime", endTime.format(DateTimeFormatter.ISO_LOCAL_TIME))
				.put("startZoom", startZoom)
				.put("endZoom", endZoom);
	}
}
