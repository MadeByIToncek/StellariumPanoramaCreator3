package space.itoncek.spc3.managers;

import static io.javalin.apibuilder.ApiBuilder.*;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import space.itoncek.spc3.StellariumPanoramaCreator3;
import space.itoncek.spc3.database.KeyStore;
import space.itoncek.spc3.generics.Manager;

import java.io.IOException;

@Slf4j
public class KeyStoreManager implements Manager {
	private final StellariumPanoramaCreator3 spc3;

	public KeyStoreManager(StellariumPanoramaCreator3 spc3) {
		this.spc3 = spc3;
	}


	@Override
	public void registerPaths() {
		path("keystore", ()->{
			get("{id}", this::keystoreGet);
			put("{id}", this::keystoreStore);
			patch("{id}", this::keystoreUpdate);
		});
	}

	private void keystoreUpdate(@NotNull Context ctx) {
		String id = ctx.pathParam("id");
		String body = ctx.body();
		try {
			KeyStore.KeystoreKeys ks = KeyStore.KeystoreKeys.valueOf(id);

			spc3.sf.runInTransaction(em -> {
				KeyStore kv = em.find(KeyStore.class, ks);
				kv.setValue(body);
				ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("result","ok").toString(4));
			});
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject()
					.put("result","no key with that name!")
					.toString(4));
		}
	}

	private void keystoreStore(@NotNull Context ctx) {
		String id = ctx.pathParam("id");
		String body = ctx.body();
		try {
			KeyStore.KeystoreKeys ks = KeyStore.KeystoreKeys.valueOf(id);

			spc3.sf.runInTransaction(em -> {
				KeyStore kv = KeyStore.generateKeystore(ks,body);
				em.persist(kv);
				ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("result","ok").toString(4));
			});
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject()
					.put("result","no key with that name!")
					.toString(4));
		}
	}

	public void keystoreGet(Context ctx) {
		String id = ctx.pathParam("id");
		try {
			KeyStore.KeystoreKeys ks = KeyStore.KeystoreKeys.valueOf(id);

			spc3.sf.runInTransaction(em -> {
				KeyStore kv = em.find(KeyStore.class, ks);
				if(kv == null) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject()
							.put("result","key has no value assigned!")
							.toString(4));
					return;
				}
				ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(kv.toJSON().toString(4));
			});
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject()
					.put("result","no key with that name exists!")
					.toString(4));
		}
	}

	@Override
	public void close() throws IOException {

	}
}
