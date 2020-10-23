package me.leoko.advancedban.utils.tabcompletion;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class PunishmentTabCompleter implements TabCompleter {

    private final boolean temporary;

    public PunishmentTabCompleter(boolean temporary) {
        this.temporary = temporary;
    }

    @Override
    public List<String> onTabComplete(Object user, String[] args) {
        final MethodInterface methodInterface = Universal.get().getMethods();

        List<String> suggestions = new ArrayList<>();

        boolean hiddenTag = false;
        if(args.length > 1 && args[0].equalsIgnoreCase("-s")) {
            args = ArrayUtils.remove(args, 0);
            hiddenTag = true;
        }

        if(args.length == 1){
            if(!hiddenTag)
                suggestions.add("-s");

            for (Object player : methodInterface.getOnlinePlayers()){
                suggestions.add(methodInterface.getName(player));
            }
            suggestions.add("[Name]");
        } else if(temporary && args.length == 2){
            String current = args[args.length-1];
            String amount = current.toLowerCase().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0];
            if(current.equals(""))
                amount = "X";

            if(amount.matches("\\d+|X")){
                for(String unit : new String[]{"s", "m", "h", "d", "w", "mo"}){
                    suggestions.add(amount + unit);
                }
            }
            for (String layout : methodInterface.getKeys(methodInterface.getLayouts(), "Time")) {
                suggestions.add("#"+layout);
            }
        } else if((temporary && args.length == 3) || args.length == 2) {
            suggestions.add("Reason...");
            for (String layout : methodInterface.getKeys(methodInterface.getLayouts(), "Message")) {
                suggestions.add("@"+layout);
            }
        }

        if(args.length > 0){
            String[] finalArgs = args;
            suggestions.removeIf(s -> !s.startsWith(finalArgs[finalArgs.length - 1]));
        }

        return suggestions;
    }
}
