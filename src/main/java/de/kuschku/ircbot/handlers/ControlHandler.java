package de.kuschku.ircbot.handlers;

import java.io.File;
import java.util.Locale;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import com.google.common.collect.ImmutableSet;

import de.kuschku.ircbot.Client;
import de.kuschku.ircbot.PluginHandler;

public class ControlHandler extends ListenerAdapter<PircBotX> {

	@Override
	public void onPrivateMessage(PrivateMessageEvent<PircBotX> event) {
		if (event.getUser().getHostmask().equalsIgnoreCase("kuschku.de")) {

			String message = event.getMessage();
			String[] args = message.split(" ");
			if (message.startsWith("!")) {
				switch (args[0].toLowerCase(Locale.ROOT)) {
				case "!plugins":
					switch (args[1].toLowerCase(Locale.ROOT)) {
					case "list":
						// List plugins
						event.getUser().send()
								.notice("The following plugins are loaded:");
						PluginHandler.listHandlers().forEach(
								handler -> event
										.getUser()
										.send()
										.notice(" - "
												+ handler.getClass()
														.getCanonicalName()));
						break;
					case "disable":
						// Disable one plugin
						String className = args[2];
						Listener<PircBotX> listener = PluginHandler
								.listHandlers()
								.parallelStream()
								.filter(x -> x.getClass().getCanonicalName()
										.equalsIgnoreCase(className))
								.findFirst().get();
						PluginHandler.unloadHandler(listener);
						event.getUser().send()
								.notice("Plugin has been unloaded: " + args[2]);
						break;
					case "enable":
						// Enable one plugin
						className = args[2];
						listener = null;
						listener = PluginHandler
								.listHandlers()
								.parallelStream()
								.filter(x -> x.getClass().getCanonicalName()
										.equalsIgnoreCase(className))
								.findFirst().get();
						PluginHandler.loadHandler(listener);
						event.getUser().send()
								.notice("Plugin has been loaded: " + args[2]);
						break;
					}
					break;
				case "!libs":
					switch (args[1].toLowerCase(Locale.ROOT)) {
					case "list":
						System.out.println(args[1]);
						// List plugins
						ImmutableSet<String> list = PluginHandler.listLibs();
						event.getUser()
								.send()
								.notice(String.format(
										"%d Lib-File(s) available in %s",
										list.size(),
										new File(
												Client.getClient().options.pluginpath)
												.getAbsolutePath()));
						list.forEach(handler -> event.getUser().send()
								.notice(" - " + handler));
						break;
					case "load":
						System.out.println(args[1]);
						// Enable one plugin
						String libName = args[2];
						PluginHandler.loadLib(libName);
						event.getUser().send()
								.notice("Lib has been loaded: " + args[2]);
						break;
					}
					break;
				}
			}
		} else {
			event.getUser().send().notice("You do not have the permissions necessary for this");
		}
	}
}
