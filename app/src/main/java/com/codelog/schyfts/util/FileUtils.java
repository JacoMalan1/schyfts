package com.codelog.schyfts.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.*;

public class FileUtils {

    @NotNull
    public static String readResourceToString(String resource) throws IOException {

        InputStream s = FileUtils.class.getClassLoader().getResourceAsStream(resource);

        if (s == null) {
            throw new IOException(String.format("Couldn't load file '%s'", resource));
        }

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s));
        String line;
        while ((line = reader.readLine()) != null)
            builder.append(line).append('\n');

        reader.close();

        return builder.toString();

    }

    public static JSONObject readJSONResource(String resource) throws IOException {

        String contents = readResourceToString(resource);
        var j = new JSONObject();
        return new JSONObject(contents);

    }

    public static JSONObject readJSONFile(String fileName) throws IOException {
        return new JSONObject(readFileToString(fileName));
    }

    public static void writeFile(String fileName, String contents) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

        writer.write(contents);
        writer.flush();
        writer.close();

    }

    @NotNull
    public static String readFileToString(String fileName) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            builder.append(line).append('\n');

        reader.close();

        return builder.toString();

    }
}
