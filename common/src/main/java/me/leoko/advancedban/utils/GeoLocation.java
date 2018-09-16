package me.leoko.advancedban.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.experimental.UtilityClass;
import me.leoko.advancedban.AdvancedBan;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;

@UtilityClass
public class GeoLocation {
    private static final String URL = "http://freegeoip.net/json/";
    private static final String KEY = "country_name";

    public static Optional<String> getLocation(InetAddress address) {
        if (address == null) {
            return Optional.empty();
        }
        String url = URL + address.getHostAddress();

        try {
            HttpURLConnection request = (HttpURLConnection) new URL(url).openConnection();
            request.connect();

            JsonNode json = AdvancedBan.JSON_MAPPER.readTree(new InputStreamReader(request.getInputStream()));

            if (!json.has(KEY)) {
                return Optional.empty();
            }

            JsonNode key = json.get(KEY);
            if (key.getNodeType() != JsonNodeType.STRING) {
                return Optional.empty();
            }
            return Optional.ofNullable(key.textValue());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
