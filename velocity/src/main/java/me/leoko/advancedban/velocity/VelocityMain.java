package me.leoko.advancedban.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.leoko.advancedban.velocity.listener.ChatListenerVelocity;
import me.leoko.advancedban.velocity.listener.ConnectionListenerVelocity;
import org.slf4j.Logger;
import me.leoko.advancedban.Universal;

import java.nio.file.Path;


@Plugin(
        id = "advancedban",
        name = "AdvancedBan",
        version = "1.0-SNAPSHOT",
        dependencies = {
                @Dependency(id = "luckperms", optional = true)
        }
)
public class VelocityMain {

    @Inject
    private final Logger logger;
    private final Path dataDirectory;
    private final ProxyServer server;

    private static VelocityMain velocityMain;

    @Inject
    public VelocityMain(Logger logger, @DataDirectory Path dataDirectory, ProxyServer server) {
        velocityMain = this;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.server = server;
    }



    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Universal.get().setup(new VelocityMethods(dataDirectory, server, logger));

        server.getEventManager().register(this, new ChatListenerVelocity());
        server.getEventManager().register(this, new ConnectionListenerVelocity());

    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        Universal.get().shutdown();
    }


    public static VelocityMain get() {
        return velocityMain;
    }
}
