package de.kuschku.ircbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;

import de.kuschku.ircbot.handlers.QuakeNetLoginHandler;

public class Client {
	
	public static FileConfiguration fileConfiguration;
	private PircBotX bot;

	public static void main(String[] args) {
		Options options = new Options();
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
			new Client(options);
		} catch (CmdLineException e) {
			e.printStackTrace();
		}
	}

	public Client(Options options) {
		Logger.getLogger(Client.class.getCanonicalName()).setLevel(Level.WARNING);
		
		try {
			fileConfiguration = FileConfiguration.fromFile(new File(options.configpath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		// Setup this bot
		Configuration.Builder<PircBotX> builder = new Configuration.Builder<PircBotX>()
				.setName(fileConfiguration.get("name"))
				.setLogin(fileConfiguration.get("auth_name"))
				.setAutoNickChange(false).setCapEnabled(true)
				.setServerHostname(fileConfiguration.get("hostname"));
		
		switch (fileConfiguration.get("auth_type")) {
		case "NickServ":
			builder.setNickservPassword(fileConfiguration.get("auth_password"));
			break;
		case "TheQBot":
			enableHandler(builder,QuakeNetLoginHandler.class.getCanonicalName());
			break;
		default:
			break;
		}
		
		for (String channel : fileConfiguration.get("channel").split(",")) {
			builder.addAutoJoinChannel(channel);
		}

		for (String handler : fileConfiguration.get("handler").split(",")) {
			enableHandler(builder,handler);
		}

		this.bot = new PircBotX(builder.buildConfiguration());

		try {
			this.bot.startBot();
		} catch (Exception ex) {
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void enableHandler(Configuration.Builder<PircBotX> builder, String handler) {
		try {
			builder.addListener((Listener<PircBotX>) newInstance(handler));
			Client.log(Level.INFO,"Successfully loaded handler "+handler);
		} catch (ClassNotFoundException | NoSuchMethodException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			Client.log(Level.WARNING,"Handler could not be activated: " + handler);
		}
	}

	public static class Options {
		@Option(name = "-config")
		private String configpath = "config.yml";
	}
	
	public static void log(Level level, String message) {
		Logger.getLogger(Client.class.getCanonicalName()).log(level, message);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T newInstance(final String className,
			final Object... args) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		// Derive the parameter types from the parameters themselves.
		Class[] types = new Class[args.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = args[i].getClass();
		}
		return (T) Class.forName(className).getConstructor(types)
				.newInstance(args);
	}
}
