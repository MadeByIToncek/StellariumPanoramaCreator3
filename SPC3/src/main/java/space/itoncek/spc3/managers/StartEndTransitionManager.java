package space.itoncek.spc3.managers;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import space.itoncek.spc3.StellariumPanoramaCreator3;
import space.itoncek.spc3.database.StartEndTransition;
import space.itoncek.spc3.database.Target;
import space.itoncek.spc3.generics.Manager;

import java.io.IOException;
import java.util.UUID;

public class StartEndTransitionManager implements Manager
{
	private final StellariumPanoramaCreator3 spc3;

	public StartEndTransitionManager(StellariumPanoramaCreator3 spc3) {
		this.spc3 = spc3;
	}

	@Override
	public void registerPaths() {
		path("/setm/{id}/", ()-> {
			get("start", ctx -> {
				getType(ctx,true);
			});
			get("end", ctx -> {
				getType(ctx,false);
			});
		});
	}

	private void getType(@NotNull Context ctx, boolean start) {
		UUID uuid = UUID.fromString(ctx.pathParam("id"));
		spc3.sf.runInTransaction(em -> {
			StartEndTransition set = em.find(StartEndTransition.class, uuid);
			if(set == null) {
				ctx.status(HttpStatus.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).result(new JSONObject().put("error","Unable to find that transition").toString(4));
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
