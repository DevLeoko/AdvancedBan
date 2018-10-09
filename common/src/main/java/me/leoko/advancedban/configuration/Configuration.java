package me.leoko.advancedban.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.leoko.advancedban.AdvancedBan;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class Configuration {

    @JsonProperty("UseMySQL")
    private boolean usingMySQL = false;
    @JsonProperty("VerboseLogging")
    private boolean verboseLogging = false;
    @JsonProperty("DefaultReason")
    private String defaultReason = "\u00A72None";
    @JsonProperty("TimeDiff")
    private long timeDifferential = 0;
    @JsonProperty("MuteCommands")
    private List<String> muteCommands = Collections.emptyList();
    @JsonProperty("ExemptPlayers")
    private List<String> exemptPlayers = Collections.emptyList();
    @JsonProperty("DateFormat")
    private String dateFormat = "dd.MM.yyyy-HH:mm";
    @JsonProperty("EnableAllPermissionNodes")
    private boolean allPermissionNodesEnabled = false;
    @JsonProperty("UUID-Fetcher")
    private UUIDFetcher uuidFetcher = new UUIDFetcher();
    @JsonProperty("WarnActions")
    private Map<Integer, String> warnActions = Collections.emptyMap();
    @JsonProperty("TempPerms")
    private Map<Integer, Long> tempPerms = Collections.emptyMap();
    @JsonProperty("Debug")
    private boolean debug = false;
    @JsonProperty("Log Purge Days")
    private int purgeLogDays = 10;
    @JsonProperty("Disable Prefix")
    private boolean prefixDisabled = false;

    public static Configuration load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return AdvancedBan.YAML_MAPPER.readValue(reader, Configuration.class);
        }
    }

    @Getter
    @ToString
    public static class UUIDFetcher {
        @JsonProperty("Enabled")
        private boolean enabled = true;
        @JsonProperty("Dynamic")
        private boolean dynamic = true;
        @JsonProperty("Intern")
        private boolean intern = false;
        @JsonProperty("REST-API")
        private UUIDApi restApi = new UUIDApi("https://api.mojang.com/users/profiles/minecraft/%NAME%?at=%TIMESTAMP%", "id");
        @JsonProperty("BackUp-API")
        private UUIDApi backupApi = new UUIDApi("https://us.mc-api.net/v3/uuid/%NAME%", "uuid");
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UUIDApi {
        @JsonProperty("URL")
        private String url;
        @JsonProperty("Key")
        private String key;
    }
}
