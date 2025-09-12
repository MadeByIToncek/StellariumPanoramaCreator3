package space.itoncek.stellarium.api;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONObject;
import space.itoncek.stellarium.api.objects.ScriptStatus;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ScriptHandler implements Closeable {
	private final java.net.http.HttpClient client;
	private final String url;

	public ScriptHandler(java.net.http.HttpClient client, String url) {
		this.client = client;
		this.url = url;
	}

	public ScriptStatus status() throws IOException, InterruptedException {
		return ScriptStatus.parse(new JSONObject(getString("/api/scripts/status")));
	}

	public void directVerbose(String script) throws IOException {
		direct("let label = LabelMgr.labelScreen(\"SPC3 is running!\", 100, 100, true, 24, \"#ffffff\", true, 1000);\n"+script);
	}

	public void direct(String script) throws IOException {
		System.out.println(script);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url + "/api/scripts/direct");

		httpPost.setEntity(MultipartEntityBuilder.create().addTextBody("code",script).build());

		httpclient.execute(httpPost);
		httpclient.close();
	}

	private String getString(String path) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(url + path))
				.build();
		return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
	}

	@Override
	public void close() {
		// nothing
	}
}
