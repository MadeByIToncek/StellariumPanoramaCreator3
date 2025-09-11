package space.itoncek.spc3.managers;

import static io.javalin.apibuilder.ApiBuilder.*;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import space.itoncek.spc3.StellariumPanoramaCreator3;
import space.itoncek.spc3.database.KeyStore;
import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.database.Target;
import space.itoncek.spc3.generics.Manager;
import space.itoncek.spc3.utils.SliderGenerator;
import space.itoncek.stellarium.api.StellariumAPI;
import space.itoncek.stellarium.api.objects.AltAz;
import space.itoncek.stellarium.api.objects.StatusResponse;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class StellariumCommsManager implements Manager {
	private final StellariumPanoramaCreator3 spc3;
	private StellariumAPI api;
	ReentrantLock lock = new ReentrantLock();

	public StellariumCommsManager(StellariumPanoramaCreator3 spc3) {
		this.spc3 = spc3;
		this.spc3.sf.runInTransaction(em -> {
			KeyStore ks = em.find(KeyStore.class, KeyStore.KeystoreKeys.STELLARIUM_URL);
			if (ks == null) {
				ks = KeyStore.generateKeystore(KeyStore.KeystoreKeys.STELLARIUM_URL, "http://[::1]:8090");
				em.persist(ks);
			}
			lock.lock();
			api = new StellariumAPI(ks.kvalue);
			lock.unlock();
		});
	}

	public void getStatus(@NotNull Context ctx) {
		try {
			StatusResponse status = api.getMainHandler().getStatus();

			ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(new JSONObject()
					.put("result", status.location().name() + " (" + status.location().latitude() + ", " + status.location().longitude() + ")")
					.toString());
		} catch (IOException | InterruptedException e) {
			ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(ContentType.APPLICATION_JSON).result(new JSONObject()
					.put("result", "unable to connect!")
					.toString());
			log.debug("Stellarium API error!", e);
		}
	}

	public void registerPaths() {
		path("stellarium", () -> {
			get("status", this::getStatus);
			post("preview", this::preview);
			post("render", this::render);
			post("retarget", this::retarget);
			post("copyCurrent", this::copyCurrent);
			post("retargetObject", this::retargetObject);
			post("copyCurrentObject", this::copyCurrentObject);
		});
	}

	private void render(@NotNull Context ctx) throws IOException, InterruptedException {
		if (!api.getScriptHandler().status().isRunning()) {
			JSONObject body = new JSONObject(ctx.body());
			UUID startEndTransitionID = UUID.fromString(body.getString("transition"));
			spc3.sf.runInTransaction(em -> {
				StartEndTransition set = em.find(StartEndTransition.class, startEndTransitionID);
				if (set == null) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
					return;
				}

				try {
					api.getScriptHandler().direct(SliderGenerator.generateScript(set, false));
				} catch (IOException e) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Stellarium error").toString(4));
				}
			});
		}else {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Script is running!").toString(4));
		}
	}

	private void preview(@NotNull Context ctx) throws IOException, InterruptedException {
		if (!api.getScriptHandler().status().isRunning()) {
			JSONObject body = new JSONObject(ctx.body());
			UUID startEndTransitionID = UUID.fromString(body.getString("transition"));
			spc3.sf.runInTransaction(em -> {
				StartEndTransition set = em.find(StartEndTransition.class, startEndTransitionID);
				if (set == null) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
					return;
				}

				try {
					api.getScriptHandler().direct(SliderGenerator.generateScript(set, true));
				} catch (IOException e) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Stellarium error").toString(4));
				}
			});
		}else {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Script is running!").toString(4));
		}
	}

	private void copyCurrentObject(@NotNull Context ctx) throws IOException, InterruptedException {
		if (!api.getScriptHandler().status().isRunning()) {
			JSONObject body = new JSONObject(ctx.body());
			boolean start = body.getString("target").equals("start");
			UUID startEndTransitionID = UUID.fromString(body.getString("transition"));
			spc3.sf.runInTransaction(em -> {
				StartEndTransition set = em.find(StartEndTransition.class, startEndTransitionID);
				if (set == null) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
					return;
				}

				Target t = start ? set.getStart() : set.getEnd();

				if (t.getType() != Target.TargetType.Object) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "That is not an Object target!").toString(4));
					return;
				}

				try {
					StatusResponse status = api.getMainHandler().getStatus();

					final Pattern pattern = Pattern.compile("<h2>(.*?)</h2>", Pattern.MULTILINE);
					final Matcher matcher = pattern.matcher(status.selectionInfo());

					if (!matcher.find()) {
						ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Target does not exist!").toString(4));
						return;
					}

					String name = matcher.group(1);
					String targetName = "";

					if (name.contains("(") && name.contains(")")) {
						String line = Arrays.stream(Arrays.stream(name.split("\\r?<br />")).map(x -> x.split("\\r?<br>")).toList().getFirst()).map(String::trim).toArray(String[]::new)[0];
						final Pattern pattern2 = Pattern.compile("\\((.*?)\\)", Pattern.MULTILINE);
						final Matcher matcher2 = pattern2.matcher(line);

						if (!matcher2.find()) {
							ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Target does not exist!").toString(4));
							return;
						}

						String brackets = "(" + matcher2.group(1) + ")";
						targetName = line.replace(brackets, "").trim();
					} else {
						String[] names = Arrays.stream(name.split("\\r?-")).map(String::trim).toArray(String[]::new);

						Optional<String> hd = Arrays.stream(names).filter(x -> x.startsWith("HD")).findFirst();
						if (hd.isPresent()) {
							targetName = hd.get();
						} else {
							Optional<String> first = Arrays.stream(names).findFirst();
							targetName = first.orElse(name);
						}
					}

					System.out.println(targetName);
					t.setName(targetName);
					t.setDate(status.time().local().toLocalDateTime().toLocalDate());
					t.setTime(status.time().local().toLocalDateTime().toLocalTime());
					t.setZoom((float) status.fov());

					ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result("ok");
				} catch (IOException | InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
		} else {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Script is running!").toString(4));
		}
	}

	private void copyCurrent(@NotNull Context ctx) throws IOException, InterruptedException {
		if (!api.getScriptHandler().status().isRunning()) {
			JSONObject body = new JSONObject(ctx.body());
			boolean start = body.getString("target").equals("start");
			UUID startEndTransitionID = UUID.fromString(body.getString("transition"));
			spc3.sf.runInTransaction(em -> {
				StartEndTransition set = em.find(StartEndTransition.class, startEndTransitionID);
				if (set == null) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
					return;
				}

				Target t = start ? set.getStart() : set.getEnd();

				if (t.getType() != Target.TargetType.AzAlt) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "That is not an AzAlt target!").toString(4));
					return;
				}

				try {

					StatusResponse status = api.getMainHandler().getStatus();
					AltAz altAz = api.getMainHandler().getAltAz();

					t.setDate(status.time().local().toLocalDateTime().toLocalDate());
					t.setTime(status.time().local().toLocalDateTime().toLocalTime());
					t.setAz((float) altAz.az());
					t.setAlt((float) altAz.alt());
					t.setZoom((float) status.fov());

					ctx.status(HttpStatus.OK).contentType(ContentType.TEXT_PLAIN).result("ok");
				} catch (IOException | InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
		} else {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Script is running!").toString(4));
		}
	}

	private void retargetObject(@NotNull Context ctx) throws IOException, InterruptedException {
		if (!api.getScriptHandler().status().isRunning()) {
			JSONObject body = new JSONObject(ctx.body());
			boolean start = body.getString("target").equals("start");
			UUID startEndTransitionID = UUID.fromString(body.getString("transition"));
			spc3.sf.runInTransaction(em -> {
				StartEndTransition set = em.find(StartEndTransition.class, startEndTransitionID);
				if (set == null) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
					return;
				}

				Target t = start ? set.getStart() : set.getEnd();

				if (t.getType() != Target.TargetType.Object) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "That is not an Object target!").toString(4));
					return;
				}

				String object = t.getName();
				float zoom = t.getZoom();

				LocalDate date = t.getDate();
				LocalTime time = t.getTime();

				String command = """
						core.moveToObject("%s",.01);
						core.setTimeRate(0);
						core.setDate("%s");
						StelMovementMgr.zoomTo(%f,.01);
						""".formatted(object, LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), zoom);

				try {
					api.getScriptHandler().directVerbose(command);
				} catch (IOException e) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to connect to the Stellarium instance!").toString(4));
				} finally {
					ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result("ok");
				}
			});
		} else {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Script is running!").toString(4));
		}
	}

	private void retarget(@NotNull Context ctx) throws IOException, InterruptedException {
		if (!api.getScriptHandler().status().isRunning()) {
			JSONObject body = new JSONObject(ctx.body());
			boolean start = body.getString("target").equals("start");
			UUID startEndTransitionID = UUID.fromString(body.getString("transition"));
			spc3.sf.runInTransaction(em -> {
				StartEndTransition set = em.find(StartEndTransition.class, startEndTransitionID);
				if (set == null) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
					return;
				}

				Target t = start ? set.getStart() : set.getEnd();

				if (t.getType() != Target.TargetType.AzAlt) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "That is not an AzAlt target!").toString(4));
					return;
				}

				float az = t.getAz();
				float alt = t.getAlt();
				float zoom = t.getZoom();

				LocalDate date = t.getDate();
				LocalTime time = t.getTime();

				String command = """
						core.moveToAltAzi(%f,%f,.01);
						core.setTimeRate(0);
						core.setDate("%s");
						StelMovementMgr.zoomTo(%f,.01);
						""".formatted(alt, az, LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("Z")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), zoom);

				try {
					api.getScriptHandler().directVerbose(command);
				} catch (IOException e) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to connect to the Stellarium instance!").toString(4));
				} finally {
					ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result("ok");
				}
			});
		} else {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Script is running!").toString(4));
		}
	}

	@Override
	public void close() {
		api.close();
	}
}
