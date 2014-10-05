package de.kuschku.ircbot.handlers;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;

import com.google.gson.JsonObject;

import de.kuschku.ircbot.Client;

public class QuakeNetLoginHandler extends ListenerAdapter<PircBotX> {
	@Override
	public void onConnect(ConnectEvent<PircBotX> event) throws Exception {
		JsonObject auth = Client.getConfig(QuakeNetLoginHandler.class).getAsJsonObject("connection").getAsJsonObject("authentication");
		String authName = auth.get("name").getAsString();
		String authPassword = auth.get("password").getAsString();
		event.getBot().sendRaw().rawLine(String.format("AUTH %s %s",authName,authPassword));
		event.getBot().sendIRC().mode(event.getBot().getNick(), "+x");
	}
}
