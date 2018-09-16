package me.leoko.advancedban.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import me.leoko.advancedban.AdvancedBan;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class MySQLConfiguration {
    @JsonProperty("MySQL")
    private MySQL mySQL;

    public static MySQLConfiguration load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return AdvancedBan.YAML_MAPPER.readValue(reader, MySQLConfiguration.class);
        }
    }

    @Getter
    @ToString
    public static class MySQL {
        @JsonProperty("IP")
        private String address = "localhost";
        @JsonProperty("DB-Name")
        private String databaseName = "YourDatabase";
        @JsonProperty("Username")
        private String username = "root";
        @JsonProperty("Password")
        private String password = "pw123";
        @JsonProperty("Port")
        private int port = 3306;
    }
}
