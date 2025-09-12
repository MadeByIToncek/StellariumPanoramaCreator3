package space.itoncek.spc3.managers;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.patch;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import space.itoncek.spc3.StellariumPanoramaCreator3;
import space.itoncek.spc3.database.SlideTrackTransition;
import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.database.Target;
import space.itoncek.spc3.generics.Manager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TransitionManager implements Manager {
	private final StellariumPanoramaCreator3 spc3;

	public TransitionManager(StellariumPanoramaCreator3 spc3) {
		this.spc3 = spc3;
	}

	public void listAll(@NotNull Context ctx) {
		spc3.sf.runInTransaction(em -> {
			List<StartEndTransition> set = allEntries(em, StartEndTransition.class);
			List<SlideTrackTransition> stt = allEntries(em, SlideTrackTransition.class);
			JSONArray array = new JSONArray();

			set.stream()
					.map(StartEndTransition::toSimpleJson)
					.forEach(array::put);

			stt.stream()
					.map(SlideTrackTransition::toSimpleJson)
					.forEach(array::put);

			ctx.status(HttpStatus.OK)
					.contentType(ContentType.APPLICATION_JSON)
					.result(array.toString(4));
		});
	}

	public void createTransition(@NotNull Context ctx) {
		JSONObject obj = new JSONObject(ctx.body());
		if (!obj.has("type")) {
			ctx.status(400)
					.contentType(ContentType.APPLICATION_JSON)
					.result(new JSONObject()
							.put("error", "Request needs to contain \"type\" parameter!")
							.toString(4));
			return;
		}
		spc3.sf.runInTransaction(em -> {
			switch (obj.getString("type")) {
				case "start-end" -> {
					Target start = Target.generateObjectTarget("Mars", LocalDate.now(), LocalTime.now());
					Target end = Target.generateObjectTarget("Mars", LocalDate.now(), LocalTime.now().plusHours(1));
					StartEndTransition set = StartEndTransition.createTransition(start, end);
					em.persist(start);
					em.persist(end);
					em.persist(set);
					ctx.status(HttpStatus.OK)
							.contentType(ContentType.APPLICATION_JSON)
							.result(new JSONObject()
									.put("result", "ok")
									.toString(4));
				}
				case "slidetrack" -> {
					SlideTrackTransition stt = SlideTrackTransition.createTransition("Mars",LocalDate.now(), LocalTime.now(), LocalDate.now(), LocalTime.now().plusHours(1),60,60);
					em.persist(stt);
					ctx.status(HttpStatus.OK)
							.contentType(ContentType.APPLICATION_JSON)
							.result(new JSONObject()
									.put("result", "ok")
									.toString(4));
				}
				default -> {
					ctx.status(HttpStatus.OK)
							.contentType(ContentType.APPLICATION_JSON)
							.result(new JSONObject()
									.put("result", "error")
									.toString(4));
				}
			}
		});
	}

	public void deleteTransition(@NotNull Context ctx) {
		log.info(ctx.body());
		JSONObject obj = new JSONObject(ctx.body());
		if (!obj.has("transition") || !(obj.get("transition") instanceof String)) {
			ctx.status(400)
					.contentType(ContentType.APPLICATION_JSON)
					.result(new JSONObject()
							.put("error", "Request needs to contain \"transition\" parameter with type of String!")
							.toString(4));
			return;
		}
		spc3.sf.runInTransaction(em -> {
			SlideTrackTransition stt = em.find(SlideTrackTransition.class, UUID.fromString(obj.getString("transition")));
			if (stt == null) {
				StartEndTransition set = em.find(StartEndTransition.class, UUID.fromString(obj.getString("transition")));
				if (set == null) {
					ctx.status(400)
							.contentType(ContentType.APPLICATION_JSON)
							.result(new JSONObject()
									.put("result", "Unable to find that transition!")
									.toString(4));
					return;
				}

				em.remove(set);
			} else {
				em.remove(stt);
			}
			ctx.status(200)
					.contentType(ContentType.APPLICATION_JSON)
					.result(new JSONObject()
							.put("result", "ok")
							.toString(4));
		});
	}

	public <T> List<T> allEntries(EntityManager em, Class<T> cls) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(cls);
		Root<T> rootEntry = cq.from(cls);
		CriteriaQuery<T> all = cq.select(rootEntry);
		TypedQuery<T> allQuery = em.createQuery(all);
		return allQuery.getResultList();
	}

	public void registerPaths() {
		path("transitions", ()-> {
			get("list", this::listAll);
			put("transition",this::createTransition);
			delete("transition", this::deleteTransition);
		});
	}

	@Override
	public void close() throws IOException {

	}
}
