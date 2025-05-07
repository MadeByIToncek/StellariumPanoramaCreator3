package space.itoncek.spc3;


import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.h2.tools.Server;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import space.itoncek.spc3.database.KeyStore;
import space.itoncek.spc3.database.SlideTrackTransition;
import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.database.Target;
import space.itoncek.spc3.generics.Manager;
import space.itoncek.spc3.managers.KeyStoreManager;
import space.itoncek.spc3.managers.StartEndTransitionManager;
import space.itoncek.spc3.managers.StellariumCommsManager;
import space.itoncek.spc3.managers.TransitionManager;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class StellariumPanoramaCreator3 implements Closeable {
	private final Javalin server;
	public final SessionFactory sf;
	private final Manager[] managers;

	public StellariumPanoramaCreator3() {
		sf = new HibernatePersistenceConfiguration("CVSS")
				.managedClass(KeyStore.class)
				.managedClass(SlideTrackTransition.class)
				.managedClass(StartEndTransition.class)
				.managedClass(Target.class)
				// PostgreSQL
				.jdbcUrl("jdbc:h2:./spc3;AUTO_SERVER=TRUE")
				// Credentials
				.jdbcUsername("sa")
				.jdbcPassword("")
				// Automatic schema export
				.schemaToolingAction(Action.UPDATE)
				// SQL statement logging
				.showSql(true, false, true)
				.createEntityManagerFactory();

		managers = new Manager[]{
				new TransitionManager(this),
				new StellariumCommsManager(this),
				new KeyStoreManager(this),
				new StartEndTransitionManager(this)
		};

		server = Javalin.create(cfg -> cfg.router.apiBuilder(() -> {
			get("/", ctx -> {
				ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_HTML);
				asResourceStream(ctx, "/static/index.html");
			});
			get("/transition/{transition-id}", ctx -> {
				try {
					UUID uuid = UUID.fromString(ctx.pathParam("transition-id"));
					ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_HTML);
					sf.runInTransaction(em -> {
						try {
							SlideTrackTransition stt = em.find(SlideTrackTransition.class, uuid);
							StartEndTransition set = em.find(StartEndTransition.class, uuid);

							if (stt == null && set != null) {
								asResourceStream(ctx, "/static/startend_transition_editor.html");
							} else if (stt != null && set == null) {
								asResourceStream(ctx, "/static/slidetrack_transition_editor.html");
							} else if (stt == null) {
								throw new IllegalArgumentException("Database contains no such UUID!");
							} else {
								throw new IllegalArgumentException("What?");
							}
						} catch (IOException e) {
							ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.TEXT_PLAIN).result("Unable to serve that page!! " + e.getMessage());
						}
					});
				} catch (IllegalArgumentException e) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.TEXT_PLAIN).result("That is not a valid UUID! " + e.getMessage());
				}
			});
			get("/config", ctx -> {
				ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_HTML);
				asResourceStream(ctx, "/static/config.html");
			});
			get("/internal/bulma.min.css", ctx -> {
				ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_CSS);
				asResourceStream(ctx, "/static/internal/bulma.min.css");
			});
			get("/internal/commons.js", ctx -> {
				ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_JS);
				asResourceStream(ctx, "/static/internal/commons.js");
			});
			path("api", () -> {
				for (Manager m : managers) {
					m.registerPaths();
				}
				post("/shutdown", ctx -> {
					ctx.result("ok");
					ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
					ses.schedule(()-> {
						ses.shutdown();
						System.exit(0);
					},250, TimeUnit.MILLISECONDS);
				});
			});
		}));
	}

	private void asResourceStream(Context ctx, String path) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(Objects.requireNonNull(getClass().getResourceAsStream(path)), baos);
		ctx.result(baos.toByteArray());
	}

	private void start() {
		server.start(4444);
	}

	public static void main(String[] args) {
		StellariumPanoramaCreator3 m = new StellariumPanoramaCreator3();
		m.start();

		Runtime.getRuntime().addShutdownHook(new Thread(m::close));
	}

	@Override
	public void close() {
		try {
			server.stop();
			for (Manager m : managers) {
				m.close();
			}
			sf.close();
		} catch (IOException e) {
			log.error("Unable to cleanly shutdown!", e);
		}
	}
}