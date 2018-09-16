package me.leoko.advancedban.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Layouts {

    private final JsonNode node;

    public static Layouts load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return new Layouts(AdvancedBan.YAML_MAPPER.readTree(reader));
        }
    }

    public JsonNode getLayout(String key) {
        try {
            String[] nodes = key.split("\\.");
            if (nodes.length < 2) {
                return null;
            }

            JsonNode finalNode = node;
            for (String node : nodes) {
                finalNode = finalNode.get(node);
            }
            return finalNode;
        } catch (Exception e) {
            return null;
        }
    }
}
