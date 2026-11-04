package me.leoko.advancedban.bungee.utils;

import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import me.leoko.advancedban.Universal;

/**
 * Gives access to the RedisBungee API across its forks.
 * The API methods are the same, but the maintained fork moved the entry point away from the plugin class.
 */
public final class RedisBungeeHook {

    private RedisBungeeHook() {
    }

    /**
     * Get the RedisBungee API. Only call this if RedisBungee is installed.
     *
     * @return the api
     */
    public static RedisBungeeAPI getApi() {
        try {
            return RedisBungeeAPI.getRedisBungeeApi();
        } catch (NoSuchMethodError legacyVersion) {
            try {
                return (RedisBungeeAPI) Class.forName("com.imaginarycode.minecraft.redisbungee.RedisBungee").getMethod("getApi").invoke(null);
            } catch (ReflectiveOperationException exc) {
                throw new IllegalStateException("Unsupported version of RedisBungee", exc);
            }
        }
    }

    /**
     * Register pub-sub channels. Newer versions of RedisBungee don't require this and will eventually remove the method.
     *
     * @param channels the channels
     */
    @SuppressWarnings("removal")
    public static void registerChannels(String... channels) {
        try {
            getApi().registerPubSubChannels(channels);
        } catch (NoSuchMethodError noLongerRequired) {
            Universal.get().debug("RedisBungee does not require channels to be registered");
        }
    }
}
