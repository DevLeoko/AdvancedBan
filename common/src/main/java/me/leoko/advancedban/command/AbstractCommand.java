package me.leoko.advancedban.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.Getter;
import me.leoko.advancedban.AdvancedBan;
import me.leoko.advancedban.AdvancedBanCommandSender;
import me.leoko.advancedban.punishment.Punishment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Getter
public abstract class AbstractCommand {
    private final String name;
    private final String permission;
    private final String configSection;
    private final String[] aliases;

    AbstractCommand(String name, String permission, String configSection, String... aliases) {
        this.name = name.toLowerCase();
        this.permission = permission;
        this.configSection = configSection;
        this.aliases = aliases;
    }

    public final void execute(AdvancedBanCommandSender sender, String[] args) {
        sender.getAdvancedBan().runAsyncTask(() -> {
            if (permission != null && !sender.hasPermission(permission)) {
                sender.sendCustomMessage("General.NoPerms");
                return;
            }
            if (!this.onCommand(sender, args)) {
                sender.sendCustomMessage(getConfigSection() + ".Usage");
            }
        });
    }

    public abstract boolean onCommand(AdvancedBanCommandSender sender, String[] args);

    public final String getName() {
        return name;
    }

    protected final void sendPermissionMessage(AdvancedBanCommandSender sender) {
        sender.sendCustomMessage("General.NoPerms");
    }

    protected final void performList(AdvancedBanCommandSender sender, int cPage, List<Punishment> punishments, String name, boolean history) {
        final AdvancedBan advancedBan = sender.getAdvancedBan();
        if (name == null || name.isEmpty()) {
            name = "N/A";
        }
        if (punishments.isEmpty()) {
            sender.sendCustomMessage(getConfigSection() + ".NoEntries", true, "NAME", name);
            return;
        }
        for (Punishment pnt : punishments) {
            if (advancedBan.getPunishmentManager().isExpired(pnt)) {
                advancedBan.getPunishmentManager().deletePunishment(pnt);
            }
        }
        if (punishments.size() / 5.0 + 1 > cPage) {
            String prefix = advancedBan.getConfiguration().isPrefixDisabled() ? "" : advancedBan.getMessageManager().getMessage("General.Prefix");
            for (String str : advancedBan.getMessageManager().getMessageList(getConfigSection() + ".Header", "PREFIX", prefix, "NAME", name)) {
                sender.sendMessage(str);
            }

            SimpleDateFormat format = new SimpleDateFormat(advancedBan.getConfiguration().getDateFormat());
            for (int i = (cPage - 1) * 5; i < cPage * 5 && punishments.size() > i; i++) {
                Punishment pnt = punishments.get(i);
                for (String str : advancedBan.getMessageManager().getMessageList(getConfigSection() + ".Entry",
                        "PREFIX", prefix,
                        "NAME", pnt.getName(),
                        "DURATION", advancedBan.getPunishmentManager().getDuration(pnt, history),
                        "OPERATOR", pnt.getOperator(),
                        "REASON", advancedBan.getMessageManager().getReasonOrDefault(pnt.getReason()),
                        "TYPE", pnt.getType().getConfSection(),
                        "ID", pnt.getId().orElse(-1) + "",
                        "DATE", format.format(new Date(pnt.getStart())))) {
                    sender.sendMessage(str);

                }
            }
            sender.sendCustomMessage(getConfigSection() + ".Footer", false,
                    "CURRENT_PAGE", cPage + "",
                    "TOTAL_PAGES", (punishments.size() / 5 + (punishments.size() % 5 != 0 ? 1 : 0)) + "",
                    "COUNT", punishments.size() + "");
            if (punishments.size() / 5.0 + 1 > cPage + 1) {
                sender.sendCustomMessage(getConfigSection() + ".PageFooter", false, "NEXT_PAGE", (cPage + 1) + "", "NAME", name);
            }
        } else {
            sender.sendCustomMessage(getConfigSection() + ".OutOfIndex", true, "PAGE", cPage + "");
        }
    }

    protected String buildReason(AdvancedBanCommandSender sender, String[] args, int reasonBegin) {
        StringBuilder reason = new StringBuilder();
        for (int i = reasonBegin; i < args.length; i++) {
            reason.append(" ").append(args[i]);
        }
        if (!args[reasonBegin].matches("@.+") && !args[reasonBegin].matches("~.+")) {
            return reason.substring(1);
        } else {
            JsonNode layout = sender.getAdvancedBan().getLayouts().getLayout("Message." + args[reasonBegin].substring(1));
            if (layout.getNodeType() != JsonNodeType.STRING) {
                sender.sendCustomMessage("General.LayoutNotFound", true, "NAME", args[reasonBegin].substring(1));
                return null;
            }
            return reason.substring(1);
        }
    }
}
