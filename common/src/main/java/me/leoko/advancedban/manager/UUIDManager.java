package me.leoko.advancedban.manager;

import lombok.RequiredArgsConstructor;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanPlayer;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

@RequiredArgsConstructor
public class UUIDManager {
    private final AdvancedBan advancedBan;
    private FetcherMode mode;
    private final Map<String, UUID> activeUUIDs = new HashMap<>();

    public void onEnable() {
        if (advancedBan.getConfiguration().getUuidFetcher().isDynamic()) {
            if (!advancedBan.isOnlineMode()) {
                mode = FetcherMode.INTERNAL;
            } else {
                mode = advancedBan.getMode();
            }
        }else{
            if (!advancedBan.getConfiguration().getUuidFetcher().isEnabled()) {
                mode = FetcherMode.DISABLED;
            } else if (!advancedBan.getConfiguration().getUuidFetcher().isIntern()) {
                mode = FetcherMode.INTERNAL;
            }else{
                mode = FetcherMode.RESTFUL;
            }
        }
    }

    public Optional<UUID> getInitialUUID(String name) {
        name = name.toLowerCase();
        Optional<UUID> uuid = Optional.empty();

        if (mode == FetcherMode.DISABLED) {
            return uuid;
        }

        if (mode == FetcherMode.INTERNAL || mode == FetcherMode.MIXED) {
            uuid = advancedBan.getInternalUUID(name);
        }

        if (!uuid.isPresent() && advancedBan.isMojangAuthed()) {
            String url = advancedBan.getConfiguration().getUuidFetcher().getRestApi().getUrl();
            String key = advancedBan.getConfiguration().getUuidFetcher().getRestApi().getKey();
            try {
                uuid = Optional.ofNullable(askAPI(url, name, key));
            } catch (Exception e) {
                advancedBan.getLogger().warn("Failed to retrieve UUID of " + name + " using REST-API");
                advancedBan.getLogger().logException(e);
            }
        }

        if (!uuid.isPresent() && advancedBan.isMojangAuthed()) {
            advancedBan.getLogger().debug("Trying to fetch UUID form BackUp-API...");
            String url = advancedBan.getConfiguration().getUuidFetcher().getBackupApi().getUrl();
            String key = advancedBan.getConfiguration().getUuidFetcher().getBackupApi().getKey();
            try {
                uuid = Optional.ofNullable(askAPI(url, name, key));
            } catch (Exception e) {
                advancedBan.getLogger().severe("Failed to retrieve UUID of " + name + " using BACKUP REST-API");
                advancedBan.getLogger().logException(e);
            }
        }
        return uuid;
    }

    public Optional<UUID> getUUID(String name) {
        if (activeUUIDs.containsKey(name)) {
            return Optional.ofNullable(activeUUIDs.get(name));
        }
        return getInitialUUID(name);
    }

    @SuppressWarnings("resource")
    public Optional<String> getNameFromUUID(UUID uuid, boolean forceInitial) {
        if (mode == FetcherMode.DISABLED) {
            return Optional.empty();
        }

        if (mode == FetcherMode.INTERNAL || mode == FetcherMode.MIXED) {
            Optional<String> name = advancedBan.getPlayer(uuid).map(AdvancedBanPlayer::getName);
            if (name.isPresent()) {
                return name;
            }
        }

        if (!forceInitial) {
            for (Entry<String, UUID> rs : activeUUIDs.entrySet()) {
                if (rs.getValue().equals(uuid)) {
                    return Optional.of(rs.getKey());
                }
            }
        }

        try {
            String s = new Scanner(new URL("https://api.mojang.com/user/profiles/" + uuid + "/names").openStream(), "UTF-8").useDelimiter("\\A").next();
            s = s.substring(s.lastIndexOf('{'), s.lastIndexOf('}') + 1);
            return Optional.ofNullable(AdvancedBan.JSON_MAPPER.readTree(s).get("name").textValue());
        } catch (Exception exc) {
            return Optional.empty();
        }
    }

    private UUID askAPI(String url, String name, String key) throws Exception {
        HttpURLConnection request = (HttpURLConnection) new URL(url.replaceAll("%NAME%", name).replaceAll("%TIMESTAMP%", new Date().getTime() + "")).openConnection();
        request.connect();

        String uuidString = AdvancedBan.JSON_MAPPER.readTree(new InputStreamReader(request.getInputStream())).get(key).textValue();

        if (uuidString == null) {
            throw new NoSuchFieldException(key + " does not exist");
        }

        UUID uuid = UUID.fromString(uuidString);
        activeUUIDs.put(name, uuid);

        return uuid;
    }

    public FetcherMode getMode() {
        return mode;
    }

    public enum FetcherMode{
        DISABLED, INTERNAL, MIXED, RESTFUL;
    }
}