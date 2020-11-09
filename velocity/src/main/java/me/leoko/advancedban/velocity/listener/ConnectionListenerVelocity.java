package me.leoko.advancedban.velocity.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.UUIDManager;
import net.kyori.text.TextComponent;

public class ConnectionListenerVelocity {

  @Subscribe
  public void onLogin(LoginEvent event) {
    UUIDManager.get().supplyInternUUID(event.getPlayer().getUsername(), event.getPlayer().getUniqueId());
    String result = Universal.get().callConnection(event.getPlayer().getUsername(), event.getPlayer().getRemoteAddress().getAddress().getHostAddress());
    if (result != null) {
      event.setResult(ResultedEvent.ComponentResult.denied(TextComponent.of(result)));
    }
  }

}
