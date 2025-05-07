package space.itoncek.spc3.managers;

import static io.javalin.apibuilder.ApiBuilder.*;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.websocket.WsConfig;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import space.itoncek.spc3.StellariumPanoramaCreator3;
import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.database.Target;
import space.itoncek.spc3.generics.Manager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class StartEndTransitionManager implements Manager {
	private final StellariumPanoramaCreator3 spc3;

	public StartEndTransitionManager(StellariumPanoramaCreator3 spc3) {
		this.spc3 = spc3;
	}

	@Override
	public void registerPaths() {
		path("/setm/{id}/", () -> {
			get("start", ctx -> {
				getType(ctx, true);
			});
			get("end", ctx -> {
				getType(ctx, false);
			});
			patch("meta", this::changeMeta);
			patch("{part}/mode", this::changeMode);
			patch("{part}", this::changeTarget);
			get("meta", this::getMeta);
		});
	}

	private void changeMeta(@NotNull Context ctx) {
		UUID uuid = UUID.fromString(ctx.pathParam("id"));
		JSONObject body = new JSONObject(ctx.body());
		spc3.sf.runInTransaction(em -> {
			StartEndTransition set = em.find(StartEndTransition.class, uuid);
			if (set == null) {
				ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
				return;
			}

			set.setName(body.getString("name"));
			set.setDuration(body.getInt("duration"));
			ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("result", "ok").toString(4));
		});
	}

	private void changeTarget(@NotNull Context ctx) {
		try {
			UUID uuid = UUID.fromString(ctx.pathParam("id"));
			String part = ctx.pathParam("part");
			JSONObject body = new JSONObject(ctx.body());
			spc3.sf.runInTransaction(em -> {
				StartEndTransition set = em.find(StartEndTransition.class, uuid);
				if (set == null) {
					ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
					return;
				}
				Target target = (part.equals("start")) ? set.getStart() : set.getEnd();
				switch (target.getType()) {
					case AzAlt -> {
						target.setAz(body.getFloat("az"));
						target.setAlt(body.getFloat("alt"));
					}
					case Object -> {
						target.setName(body.getString("target"));
					}
				}
				target.setZoom(body.getFloat("zoom"));
				target.setDate(LocalDate.parse(body.getString("date")));
				target.setTime(LocalTime.parse(body.getString("time")));
				ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("result", "ok").toString(4));
			});
		} catch (IllegalArgumentException e) {
			ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
		}
	}

	private void changeMode(@NotNull Context ctx) {
		UUID uuid = UUID.fromString(ctx.pathParam("id"));
		String part = ctx.pathParam("part");
		Target.TargetType type = Target.TargetType.valueOf(ctx.body());
		spc3.sf.runInTransaction(em -> {
			StartEndTransition set = em.find(StartEndTransition.class, uuid);
			if (set == null) {
				ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
				return;
			}
			Target target = (part.equals("start")) ? set.getStart() : set.getEnd();
			target.setType(type);
			ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("result", "ok").toString(4));
		});
	}

	private void getMeta(@NotNull Context ctx) {
		UUID uuid = UUID.fromString(ctx.pathParam("id"));
		spc3.sf.runInTransaction(em -> {
			StartEndTransition set = em.find(StartEndTransition.class, uuid);
			if (set == null) {
				ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
				return;
			}
			ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(set.toSimpleJson().toString(4));
		});
	}

	private void getType(@NotNull Context ctx, boolean start) {
		UUID uuid = UUID.fromString(ctx.pathParam("id"));
		spc3.sf.runInTransaction(em -> {
			StartEndTransition set = em.find(StartEndTransition.class, uuid);
			if (set == null) {
				ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
				return;
			}
			Target target = start ? set.getStart() : set.getEnd();
			ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(target.toJSON().toString(4));
		});
	}

	@Override
	public void close() throws IOException {

	}
}