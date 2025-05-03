package space.itoncek.spc3.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

@Getter
@Setter
@Entity
public class KeyStore {
	@Id
	@Enumerated(EnumType.STRING)
	@Basic(fetch = FetchType.LAZY)
	public KeystoreKeys key;
	@Basic(fetch = FetchType.LAZY)
	public String value;

	public static KeyStore generateKeystore(KeystoreKeys key, String value) {
		KeyStore ks = new KeyStore();
		ks.key = key;
		ks.value = value;
		return ks;
	}

	public JSONObject toJSON() {
		return new JSONObject()
				.put("key", key)
				.put("value", value);
	}

	public enum KeystoreKeys {
		STELLARIUM_URL
	}
}