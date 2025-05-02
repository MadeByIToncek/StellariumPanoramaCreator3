package space.itoncek.spc3.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.UUID;

@Getter
@Setter
@Entity
public class StartEndTransition {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Basic(fetch = FetchType.LAZY)
	UUID uuid;
	@Basic(fetch = FetchType.LAZY)
	String name;

	@ManyToOne(targetEntity = Target.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	Target start;

	@ManyToOne(targetEntity = Target.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	Target end;

	public static StartEndTransition createTransition(Target start, Target end) {
		StartEndTransition set = new StartEndTransition();
		set.setStart(start);
		set.setEnd(end);
		return set;
	}

	public JSONObject toSimpleJson() {
		return new JSONObject()
				.put("uuid",uuid)
				.put("name",name);
	}
}
