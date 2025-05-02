package space.itoncek.stellarium.api;


import java.io.Closeable;
import java.net.http.HttpClient;

public class StellariumAPI implements Closeable {
	private final HttpClient client;
	private final MainHandler main;

	public StellariumAPI(String url) {
		client = HttpClient.newHttpClient();
		main = new MainHandler(client,url);
	}

	@Override
	public void close() {
		main.close();
		client.close();
	}

	public MainHandler getMainHandler() {
		return main;
	}
}