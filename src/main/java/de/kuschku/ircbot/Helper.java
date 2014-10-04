package de.kuschku.ircbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Helper {
	public static final ImmutableList<String> parseArgs(String command,
			String prefix) {
		if (command.contains(" ") && command.startsWith(prefix)) {
			return ImmutableList
					.copyOf((command.substring(prefix.length()) + " ")
							.split(" "));
		} else {
			return null;
		}
	}

	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JsonObject readJsonFromUrl(String url)
			throws MalformedURLException, IOException {
		InputStream is = new URL(url).openStream();

		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is,
				Charset.forName("UTF-8")))) {
			String jsonText = readAll(rd);
			JsonObject json = new JsonParser().parse(jsonText)
					.getAsJsonObject();
			return json;
		}
	}

	/**
	 * Parses an URL query string and returns a map with the parameter values.
	 * The URL query string is the part in the URL after the first '?' character
	 * up to an optional '#' character. It has the format
	 * "name=value&name=value&...". The map has the same structure as the one
	 * returned by javax.servlet.ServletRequest.getParameterMap(). A parameter
	 * name may occur multiple times within the query string. For each parameter
	 * name, the map contains a string array with the parameter values.
	 * 
	 * @param s	an URL query string.
	 * @return	a map containing parameter names as keys and parameter values as
	 *         	map values.
	 * @author 	Christian d'Heureuse, Inventec Informatik AG, Switzerland,
	 *         	www.source-code.biz.
	 */
	public static Map<String, String[]> parseUrlQueryString(String s) {
		if (s == null)
			return new HashMap<String, String[]>(0);
		// In map1 we use strings and ArrayLists to collect the parameter
		// values.
		HashMap<String, Object> map1 = new HashMap<String, Object>();
		int p = 0;
		while (p < s.length()) {
			int p0 = p;
			while (p < s.length() && s.charAt(p) != '=' && s.charAt(p) != '&')
				p++;
			String name = urlDecode(s.substring(p0, p));
			if (p < s.length() && s.charAt(p) == '=')
				p++;
			p0 = p;
			while (p < s.length() && s.charAt(p) != '&')
				p++;
			String value = urlDecode(s.substring(p0, p));
			if (p < s.length() && s.charAt(p) == '&')
				p++;
			Object x = map1.get(name);
			if (x == null) {
				// The first value of each name is added directly as a string to
				// the map.
				map1.put(name, value);
			} else if (x instanceof String) {
				// For multiple values, we use an ArrayList.
				ArrayList<String> a = new ArrayList<String>();
				a.add((String) x);
				a.add(value);
				map1.put(name, a);
			} else {
				@SuppressWarnings("unchecked")
				ArrayList<String> a = (ArrayList<String>) x;
				a.add(value);
			}
		}
		// Copy map1 to map2. Map2 uses string arrays to store the parameter
		// values.
		HashMap<String, String[]> map2 = new HashMap<String, String[]>(
				map1.size());
		for (Map.Entry<String, Object> e : map1.entrySet()) {
			String name = e.getKey();
			Object x = e.getValue();
			String[] v;
			if (x instanceof String) {
				v = new String[] { (String) x };
			} else {
				@SuppressWarnings("unchecked")
				ArrayList<String> a = (ArrayList<String>) x;
				v = new String[a.size()];
				v = a.toArray(v);
			}
			map2.put(name, v);
		}
		return map2;
	}

	private static String urlDecode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Error in urlDecode.", e);
		}
	}
	
	public static class URLParamEncoder {

		public static String encode(String input) {
			StringBuilder resultStr = new StringBuilder();
			for (char ch : input.toCharArray()) {
				if (isUnsafe(ch)) {
					resultStr.append('%');
					resultStr.append(toHex(ch / 16));
					resultStr.append(toHex(ch % 16));
				} else {
					resultStr.append(ch);
				}
			}
			return resultStr.toString();
		}

		private static boolean isUnsafe(char ch) {
			if (ch > 128 || ch < 0)
				return true;
			return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
		}

		private static char toHex(int ch) {
			return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
		}

	}
	
	public static final String truncate(String input, int length) {
		if (input.length()>length)
			return input.substring(0,length)+"…";
		else
			return input;
	}
}