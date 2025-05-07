package space.itoncek.stellarium.api.objects;

import org.json.JSONObject;

public record ScriptStatus(boolean isRunning, String scriptId) {

	public static ScriptStatus parse(JSONObject o) {
		return new ScriptStatus(o.getBoolean("scriptIsRunning"),o.getString("runningScriptId"));
	}
}
