package me.leoko.advancedban.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.velocity.listener.ChatListenerVelocity;
import me.leoko.advancedban.velocity.listener.ConnectionListenerVelocity;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
    id = "advancedban",
    name = "AdvancedBan",
    version = "@version"
)
public class VelocityMain {

  private final ProxyServer server;
  private final Path dataDirectory;
  private final Logger logger;

  private static VelocityMain instance;

  @Inject
  public VelocityMain(ProxyServer server, @DataDirectory Path dataDirectory, Logger logger) {
    this.instance = this;
    this.server = server;
    this.dataDirectory = dataDirectory;
    this.logger = logger;
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
    return instance;
  }

}
