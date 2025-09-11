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
	public KeystoreKeys kkey;
	public String kvalue;

	public static KeyStore generateKeystore(KeystoreKeys key, String value) {
		KeyStore ks = new KeyStore();
		ks.kkey = key;
		ks.kvalue = value;
		return ks;
	}

	public JSONObject toJSON() {
		return new JSONObject()
				.put("key", kkey)
				.put("value", kvalue);
	}

	public enum KeystoreKeys {
		STELLARIUM_URL,
		RENDERING_PATH
	}
}