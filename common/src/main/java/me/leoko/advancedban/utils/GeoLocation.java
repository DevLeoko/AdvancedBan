package me.leoko.advancedban.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.experimental.UtilityClass;
import me.leoko.advancedban.AdvancedBan;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;

@UtilityClass
public class GeoLocation {
    private static final char[] URL_1 = "https://ipapi.co/".toCharArray();
    private static final char[] URL_2 = "/json/".toCharArray();
    private static final String KEY = "country_name";

    public static Optional<String> getLocation(InetAddress address) {
        if (address == null) {
            return Optional.empty();
        }
        StringBuilder url = new StringBuilder();
        url.append(URL_1).append(address.getHostAddress()).append(URL_2);

        try {
            HttpURLConnection request = (HttpURLConnection) new URL(url.toString()).openConnection();
            request.setConnectTimeout(2000);
            request.setReadTimeout(2000);
            request.connect();
            System.out.println("Connect");

            JsonNode json = AdvancedBan.JSON_MAPPER.readTree(request.getInputStream());

            if (!json.has(KEY)) {
                return Optional.empty();
            }

            JsonNode key = json.get(KEY);
            if (key.getNodeType() != JsonNodeType.STRING) {
                return Optional.empty();
            }
            return Optional.ofNullable(key.textValue());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to get geo location info", e);
        }
    }
}
