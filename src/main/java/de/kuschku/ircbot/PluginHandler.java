package de.kuschku.ircbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PluginHandler {
	List<ListenerAdapter<PircBotX>> list = new ArrayList<ListenerAdapter<PircBotX>>();
	Configuration.Builder<PircBotX> builder;
	
	public PluginHandler(Configuration.Builder<PircBotX> builder) {
		this.builder = builder;
	}
	
	public void addHandler(String className) {
		try {
			list.add(enableHandler(className));
		} catch (ClassNotFoundException | NoSuchMethodException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void build() {
		list.forEach(x -> {builder.addListener(x);Client.log(Level.INFO, "Successfully loaded handler "+ x.getClass().getCanonicalName());});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static final <T> T newInstance(final String className,
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
	
	static final ListenerAdapter<PircBotX> loadPluginJar(File file, String className) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		URLClassLoader loader = new URLClassLoader(new URL[]{ file.getAbsoluteFile().toURI().toURL()}); 
		loader.loadClass(className);
		ListenerAdapter<PircBotX> handler = newInstance(className);
		loader.close();
		return handler;
	}
	
	static final ListenerAdapter<PircBotX> enableHandler(String handler) throws ClassNotFoundException, NoSuchMethodException
				, InstantiationException , IllegalAccessException
				, IllegalArgumentException , InvocationTargetException {
		try {
			return newInstance(handler);
		} catch (ClassNotFoundException | NoSuchMethodException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			Client.log(Level.WARNING, "Handler could not be activated: "
					+ handler);
			throw e;
		}
	}
	
	static JsonArray getHandlersFromJar(File jar) throws ZipException, IOException {
	    ZipFile zipFile = new ZipFile(jar);
	    Enumeration<? extends ZipEntry> entries = zipFile.entries();

	    while(entries.hasMoreElements()){
	        ZipEntry entry = entries.nextElement();
	        if (entry.getName().equalsIgnoreCase("plugin.json")) {
	        	InputStreamReader reader = new InputStreamReader(zipFile.getInputStream(entry));
	        	JsonObject object = new JsonParser().parse(reader).getAsJsonObject();
	        	return object.get("handlers").getAsJsonArray();
	        }
	    }
	    zipFile.close();
	    throw new FileNotFoundException();
	}
	
	static List<String> loadPlugin(File jar) throws ZipException, IOException, ClassNotFoundException {
		JsonArray handlers = getHandlersFromJar(jar);
		List<String> classnames = new ArrayList<String>();
		handlers.forEach(json -> classnames.add(json.getAsString()));
		loadLibrary(jar);
		return classnames;
	}
	
	void loadPlugins(File path) {
		if (!path.exists());
			path.mkdirs();
		for (File file : path.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".jar")) {
				try {
					List<String> handlers = loadPlugin(file.getAbsoluteFile());
					handlers.forEach(handler -> addHandler(handler));
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
    public static synchronized void loadLibrary(java.io.File jar)
    {
    	try {
            /*We are using reflection here to circumvent encapsulation; addURL is not public*/
            java.net.URLClassLoader loader = (java.net.URLClassLoader)ClassLoader.getSystemClassLoader();
            java.net.URL url = jar.toURI().toURL();
            /*Disallow if already loaded*/
            for (java.net.URL it : java.util.Arrays.asList(loader.getURLs())){
                if (it.equals(url)){
                    return;
                }
            }
            java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
            method.setAccessible(true); /*promote the method to public access*/
            method.invoke(loader, new Object[]{url});
    	}
        catch (Exception e) {
        }
    }
    
    public static void loadHandler(Listener<PircBotX> handler) {
    	Client.getClient().bot.getConfiguration().getListenerManager().addListener(handler);
    }
    
    public static void unloadHandler(Listener<PircBotX> handler) {
    	Client.getClient().bot.getConfiguration().getListenerManager().removeListener(handler);
    }
    
    public static Set<Listener<PircBotX>> listHandlers() {
    	return Client.getClient().bot.getConfiguration().getListenerManager().getListeners();
    }
    
    public static void loadLib(String filename) {
    	try {
			List<String> handlers = loadPlugin(new File(Client.getClient().options.pluginpath,filename));;
			handlers.forEach(handler -> {try {unloadHandler(listHandlers()
					.parallelStream()
					.filter(x -> x.getClass().getCanonicalName()
							.equalsIgnoreCase(handler))
					.findFirst().get());} catch (Exception e) {} try {loadHandler(enableHandler(handler));} catch (Exception e) {}});
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
    }
    
    public static ImmutableSet<String> listLibs() {
    	List<String> list = new ArrayList<String>();
		for (File file : new File(Client.getClient().options.pluginpath).listFiles()) {
			if (file.isFile() && file.getName().endsWith(".jar")) {
				list.add(file.getName());
			}
		}
		return ImmutableSet.copyOf(list);
    }
}
