package space.itoncek.spc3.managers;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import space.itoncek.stellarium.api.StellariumAPI;
import space.itoncek.spc3.StellariumPanoramaCreator3;
import space.itoncek.spc3.database.KeyStore;
import space.itoncek.stellarium.api.objects.StatusResponse;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class StellariumCommsManager implements Closeable {
	private final StellariumPanoramaCreator3 spc3;
	private StellariumAPI api;
	ReentrantLock lock = new ReentrantLock();

	public StellariumCommsManager(StellariumPanoramaCreator3 spc3) {
		this.spc3 = spc3;
		this.spc3.sf.runInTransaction(em -> {
			KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.STELLARIUM_URL);
			if(ks == null) {
				ks = KeyStore.generateKeystore(KeyStore.KeystoreKeys.STELLARIUM_URL,"http://[::1]:8090");
				em.persist(ks);
			}
			lock.lock();
			api = new StellariumAPI(ks.value);
			lock.unlock();
		});
	}

	public void getStatus(@NotNull Context ctx) {
		try {
			StatusResponse status = api.getMainHandler().getStatus();

			ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(new JSONObject()
							.put("result",status.location().name() + " (" + status.location().latitude() + ", " + status.location().longitude() + ")")
					.toString());
		} catch (IOException | InterruptedException e) {
			ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.APPLICATION_JSON).result(new JSONObject()
					.put("result","unable to connect!")
					.toString());
			log.debug("Stellarium API error!", e);
		}
	}

	public void registerPaths() {
		path("stellarium", ()-> {
			get("status", this::getStatus);
		});
	}

	@Override
	public void close() {
		api.close();
	}
}
