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
public class Target {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Basic(fetch = FetchType.LAZY)
	UUID uuid;
	@Enumerated(value = EnumType.STRING)
	@Basic(fetch = FetchType.LAZY)
	TargetType type;

	//For object targets
	@Basic(fetch = FetchType.LAZY)
	String name;

	//For AzAlt targets
	@Basic(fetch = FetchType.LAZY)
	float az;
	@Basic(fetch = FetchType.LAZY)
	float alt;

	//For All
	@Basic(fetch = FetchType.LAZY)
	LocalDate date;
	@Basic(fetch = FetchType.LAZY)
	LocalTime time;

	public static Target generateObjectTarget(String name, LocalDate date, LocalTime time) {
		Target t = new Target();
		t.setType(TargetType.Object);

		t.setName(name);

		t.setDate(date);
		t.setTime(time);
		return t;
	}

	public static Target generateAzAltTarget(float az, float alt, LocalDate date, LocalTime time) {
		Target t = new Target();
		t.setType(TargetType.Object);

		t.setAz(az);
		t.setAlt(alt);

		t.setDate(date);
		t.setTime(time);
		return t;
	}

	public JSONObject toJSON() {
		return switch (type) {
			case AzAlt -> new JSONObject()
					.put("uuid", uuid)
					.put("type",type)
					.put("az",az)
					.put("alt",alt)
					.put("date", date.format(DateTimeFormatter.ISO_DATE))
					.put("time", time.format(DateTimeFormatter.ISO_TIME));
			case Object -> new JSONObject()
					.put("uuid", uuid)
					.put("type",type)
					.put("name",name)
					.put("date", date.format(DateTimeFormatter.ISO_DATE))
					.put("time", time.format(DateTimeFormatter.ISO_TIME));
		};
	}

	public enum TargetType {
		AzAlt,
		Object
	}
}
