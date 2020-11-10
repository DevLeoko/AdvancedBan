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

import java.nio.file.Path;

@Plugin(
    id = "advancedban",
    name = "AdvancedBan",
    version = "${project.version}"
)
public class VelocityMain {

  private final ProxyServer server;
  private Path dataDirectory;

  @Inject
  public VelocityMain(ProxyServer server, @DataDirectory Path dataDirectory) {
    this.server = server;
    this.dataDirectory = dataDirectory;
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {

    Universal.get().setup(new VelocityMethods(dataDirectory, server));

    server.getEventManager().register(this, new ChatListenerVelocity());
    server.getEventManager().register(this, new ConnectionListenerVelocity());

  }

  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent event) {
    Universal.get().shutdown();
  }

  public Path getDataFolder() {
    return this.dataDirectory;
  }


}
