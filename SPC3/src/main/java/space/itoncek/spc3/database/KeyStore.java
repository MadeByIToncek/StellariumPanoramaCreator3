package space.itoncek.spc3.database;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

	public enum KeystoreKeys {
		STELLARIUM_URL
	}
}