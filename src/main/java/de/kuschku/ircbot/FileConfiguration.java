package de.kuschku.ircbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FileConfiguration {
	public static final JsonObject fromFile(File input)
			throws FileNotFoundException {
		FileReader reader = new FileReader(input);
		JsonParser parser = new JsonParser();
		JsonObject object = parser.parse(reader).getAsJsonObject();

		return object;
	}

	public static final void toFile(File output, JsonObject input)
			throws IOException {
		FileWriter out = new FileWriter(output);
		out.write(input.toString());
		out.close();
	}
}
