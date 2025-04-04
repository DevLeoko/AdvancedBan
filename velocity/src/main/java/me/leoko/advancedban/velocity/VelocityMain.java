package me.leoko.advancedban.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.velocity.listener.ChatListenerVelocity;
import me.leoko.advancedban.velocity.listener.ConnectionListenerVelocity;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;




@Plugin(
        id = "advancedban",
        name = "AdvancedBan",
        version = "2.4.0",
        authors = {"Leoko"},
        dependencies = {
                @Dependency(id = "luckperms", optional = true),
                @Dependency(id = "signedvelocity", optional = true)
        }
)



public class VelocityMain {

    @Inject
    private final Logger logger;
    private final Path dataDirectory;
    private final ProxyServer server;
    private static VelocityMain instance;


    @Inject
    public VelocityMain(ProxyServer server, @DataDirectory Path dataDirectory, Logger logger){
        this.server = server;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        server.getEventManager().register(this, new ConnectionListenerVelocity(server));
        server.getEventManager().register(this, new ChatListenerVelocity());

        //If signed velocity is missing send a warning
        if (!server.getPluginManager().getPlugin("signedvelocity").isPresent()) {
            logger.warn("SignedVelocity is not installed, mute system may kick your players while muted. Please install SignedVelocity to prevent this.");
        }
        Universal.get().setup(new VelocityMethods(server, dataDirectory, logger));

    }

    public static VelocityMain get() {
        return instance;
    }
}
