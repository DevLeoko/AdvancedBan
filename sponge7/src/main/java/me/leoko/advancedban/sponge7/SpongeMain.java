package me.leoko.advancedban.sponge7;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.VersionInfo;
import me.leoko.advancedban.sponge7.listener.ChatListenerSponge;
import me.leoko.advancedban.sponge7.listener.ConnectionListenerSponge;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;

@Plugin(
        id = "advancedban",
        name = "AdvancedBan",
        version = VersionInfo.VERSION,
        dependencies = {
                @Dependency(id = "luckperms", optional = true)
        }
)
public class SpongeMain {

    @Inject
    private Game game;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path dataDirectory;

    @Inject
    private Logger logger;

    @Inject
    private PluginContainer pluginContainer;

    private static SpongeMain spongeMain;

    @Listener
    public void onProxyInitialization(GameStartedServerEvent event) {

        Universal.get().setup(new SpongeMethods(dataDirectory, game, logger));

        game.getEventManager().registerListeners(this, new ChatListenerSponge());
        game.getEventManager().registerListeners(this, new ConnectionListenerSponge());

    }

    @Listener
    public void onProxyShutdown(GameStoppingServerEvent event) {
        Universal.get().shutdown();
    }


    public static SpongeMain get() {
        return spongeMain;
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

}
