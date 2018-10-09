package me.leoko.advancedban.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Messages {
    private final JsonNode node;

    public static Messages load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return new Messages(AdvancedBan.YAML_MAPPER.readTree(reader));
        }
    }

    public JsonNode getMessage(String key) {
        try {
            String[] nodes = key.split("\\.");

            JsonNode finalNode = node;
            for (String node : nodes) {
                finalNode = finalNode.path(node);
            }
            return finalNode;
        } catch (Exception e) {
            return MissingNode.getInstance();
        }
    }
}
