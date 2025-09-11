package space.itoncek.spc3.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class SlideTrackTransition {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	UUID uuid;
	@Basic(fetch = FetchType.LAZY)
	String name;
	@Basic(fetch = FetchType.LAZY)
	String target;
	@Basic(fetch = FetchType.LAZY)
	LocalDate startDate;
	@Basic(fetch = FetchType.LAZY)
	LocalTime startTime;
	@Basic(fetch = FetchType.LAZY)
	LocalDate endDate;
	@Basic(fetch = FetchType.LAZY)
	LocalTime endTime;

	public JSONObject toSimpleJson() {
		return new JSONObject()
				.put("uuid",uuid)
				.put("name",name);
	}
}
