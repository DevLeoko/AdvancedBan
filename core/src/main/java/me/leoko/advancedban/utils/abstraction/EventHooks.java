package me.leoko.advancedban.utils.abstraction;

import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.InterimData;
import me.leoko.advancedban.utils.punishment.Identifier;
import me.leoko.advancedban.utils.punishment.Punishment;

public class EventHooks {
    public String onConnection(String name, Identifier ip, Identifier uuid) {
        name = name.toLowerCase();

        if (uuid == null)
            return "[AdvancedBan] Failed to fetch your UUID";

        if (ip != null)
            PunishmentManager.get().getIpCache().put(name, ip);

        InterimData interimData = PunishmentManager.get().load(name, uuid, ip);

        if (interimData == null) {
            if (Universal.getInstance().getConfig().getBoolean("LockdownOnError", true))
                return "[AdvancedBan] Failed to load player data!";
            else
                return null;
        }

        Punishment pt = interimData.getBan();

        if (pt == null) {
            interimData.accept();
            return null;
        }

        return pt.getLayoutBSN();
    }

    public boolean onChat(Target target) {
        Punishment mute = PunishmentManager.get().getMute(target.getIdentifier());
        if (mute != null) {
            mute.getLayout().forEach(target::sendMessage);
            return true;
        }
        return false;
    }
}
