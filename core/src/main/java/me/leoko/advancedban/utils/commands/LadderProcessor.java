package me.leoko.advancedban.utils.commands;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.manager.MessageManager;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.TimeManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Command;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

import java.util.List;
import java.util.function.Consumer;

/**
 * Handles /punish: the configured steps of an offence decide about the punishment,
 * and every time a player gets punished for the same offence the next step is used.
 */
public class LadderProcessor implements Consumer<Command.CommandInput> {
    private static final String CALCULATION_PREFIX = "ladder:";
    // The database column for the calculation has 50 characters
    private static final int MAX_NAME_LENGTH = 50 - CALCULATION_PREFIX.length();

    @Override
    public void accept(Command.CommandInput input) {
        MethodInterface mi = Universal.get().getMethods();
        Object sender = input.getSender();
        boolean silent = PunishmentProcessor.processTag(input, "-s");

        // the tag might have been one of the two required arguments
        if (input.getArgs().length < 2) {
            MessageManager.sendMessage(sender, "Punish.Usage", true);
            return;
        }

        String name = input.getPrimary();
        input.next();
        String offence = findOffence(input.getPrimary());
        if (offence == null) {
            MessageManager.sendMessage(sender, "Punish.NotFound", true,
                    "NAME", input.getPrimary(), "OFFENCES", String.join(", ", getOffences()));
            return;
        }
        input.next();

        List<String> steps = mi.getStringList(mi.getLayouts(), "Ladder." + offence + ".Steps");
        if (steps.isEmpty() || offence.length() > MAX_NAME_LENGTH) {
            MessageManager.sendMessage(sender, "Punish.InvalidStep", true, "STEP", "-", "OFFENCE", offence);
            return;
        }

        String uuid = UUIDManager.get().getUUID(name.toLowerCase());
        if (uuid == null) {
            MessageManager.sendMessage(sender, "General.FailedFetch", true, "NAME", name);
            return;
        }

        String calculation = CALCULATION_PREFIX + offence;
        int level = Math.min(PunishmentManager.get().getCalculationLevel(uuid, calculation), steps.size() - 1);
        String step = steps.get(level).trim();

        String[] stepParts = step.split(" +");
        PunishmentType type = PunishmentType.fromCommandName(stepParts[0].toLowerCase());
        if (!isValid(type, stepParts)) {
            MessageManager.sendMessage(sender, "Punish.InvalidStep", true, "STEP", step, "OFFENCE", offence);
            return;
        }

        if (type == PunishmentType.KICK && !mi.isOnline(name.toLowerCase())) {
            MessageManager.sendMessage(sender, "Kick.NotOnline", true, "NAME", name);
            return;
        }

        if (PunishmentProcessor.processExempt(name, uuid, sender, type)) {
            return;
        }

        String reason = mi.getString(mi.getLayouts(), "Ladder." + offence + ".Reason", offence);
        if (reason.matches("[~@].+") && !mi.contains(mi.getLayouts(), "Message." + reason.split(" ")[0].substring(1))) {
            MessageManager.sendMessage(sender, "General.LayoutNotFound", true, "NAME", reason.split(" ")[0].substring(1));
            return;
        }
        if (input.hasNext()) {
            reason += " " + String.join(" ", input.getArgs());
        }

        if (PunishmentProcessor.alreadyPunished(uuid, type)) {
            MessageManager.sendMessage(sender, type.getBasic().getName() + ".AlreadyDone", true, "NAME", name);
            return;
        }

        long end = type.isTemp() ? TimeManager.getTime() + TimeManager.toMilliSec(stepParts[1].toLowerCase()) : -1;
        Punishment.create(name, uuid, reason, mi.getName(sender), type, end, calculation, silent);

        MessageManager.sendMessage(sender, "Punish.Done", true,
                "NAME", name,
                "OFFENCE", offence,
                "STEP", String.valueOf(level + 1),
                "STEPS", String.valueOf(steps.size()),
                "ACTION", step);
    }

    /**
     * Get the names of all offences which got configured.
     *
     * @return the offence names
     */
    public static String[] getOffences() {
        MethodInterface mi = Universal.get().getMethods();
        return mi.getKeys(mi.getLayouts(), "Ladder");
    }

    private static String findOffence(String input) {
        for (String offence : getOffences()) {
            if (offence.equalsIgnoreCase(input)) {
                return offence;
            }
        }
        return null;
    }

    // IP punishments are left out as they are stored for the IP, so they could not be counted for the player
    private static boolean isValid(PunishmentType type, String[] stepParts) {
        if (type == null || type.isIpOrientated()) {
            return false;
        }
        return type.isTemp()
                ? stepParts.length == 2 && stepParts[1].toLowerCase().matches("[1-9][0-9]{0,8}([wdhms]|mo)")
                : stepParts.length == 1;
    }
}
