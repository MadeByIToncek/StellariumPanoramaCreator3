package space.itoncek.spc3.managers;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import space.itoncek.spc3.StellariumPanoramaCreator3;
import space.itoncek.spc3.database.SlideTrackTransition;
import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.generics.Manager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class SlidetrackTransitionManager implements Manager {
	private final StellariumPanoramaCreator3 spc3;

	public SlidetrackTransitionManager(StellariumPanoramaCreator3 spc3) {
		this.spc3 = spc3;
	}

	@Override
	public void registerPaths() {
		path("/sttm/{id}/", () -> {
			patch("data", this::changeData);
			get("data", this::getData);
		});
	}

	private void changeData(@NotNull Context ctx) {
		UUID uuid = UUID.fromString(ctx.pathParam("id"));
		JSONObject body = new JSONObject(ctx.body());
		spc3.sf.runInTransaction(em -> {
			SlideTrackTransition stt = em.find(SlideTrackTransition.class, uuid);
			if (stt == null) {
				ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
				return;
			}

			stt.setName(body.getString("name"));
			stt.setDuration(body.getInt("duration"));
			stt.setTarget(body.getString("target"));
			stt.setStartDate(LocalDate.parse(body.getString("startDate")));
			stt.setEndDate(LocalDate.parse(body.getString("endDate")));
			stt.setStartTime(LocalTime.parse(body.getString("startTime")));
			stt.setEndTime(LocalTime.parse(body.getString("endTime")));
			stt.setStartZoom(body.getDouble("startZoom"));
			stt.setEndZoom(body.getDouble("endZoom"));
			ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("result", "ok").toString(4));
		});
	}

	private void getData(@NotNull Context ctx) {
		UUID uuid = UUID.fromString(ctx.pathParam("id"));
		spc3.sf.runInTransaction(em -> {
			SlideTrackTransition stt = em.find(SlideTrackTransition.class, uuid);
			if (stt == null) {
				ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error", "Unable to find that transition").toString(4));
				return;
			}
			ctx.status(HttpStatus.OK).contentType(ContentType.APPLICATION_JSON).result(stt.toSimpleJson().toString(4));
		});
	}

	@Override
	public void close() throws IOException {

	}
}
