package space.itoncek.stellarium.api;

import org.json.JSONObject;
import space.itoncek.stellarium.api.objects.AltAz;
import space.itoncek.stellarium.api.objects.StatusResponse;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainHandler implements Closeable {
	private final HttpClient client;
	private final String url;

	public MainHandler(HttpClient client, String url) {
		this.client = client;

		this.url = url;
	}

	@Override
	public void close() {
		//empty
	}

	public StatusResponse getStatus() throws IOException, InterruptedException {
		return StatusResponse.parse(new JSONObject(getString("/api/main/status")));
	}

	private String getString(String path) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(url + path))
				.build();
		return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
	}

	public AltAz getAltAz() throws IOException, InterruptedException {
		return AltAz.parse(new JSONObject(getString("/api/main/view?coord=altAz")));
	}
}
