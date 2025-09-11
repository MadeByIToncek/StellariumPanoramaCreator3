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
	String name;
	String target;
	LocalDate startDate;
	LocalTime startTime;
	LocalDate endDate;
	LocalTime endTime;

	public JSONObject toSimpleJson() {
		return new JSONObject()
				.put("uuid",uuid)
				.put("name",name);
	}
}
