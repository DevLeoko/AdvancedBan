package me.leoko.advancedban.core.utils.tabcompletion;

import lombok.AllArgsConstructor;
import me.leoko.advancedban.core.MethodInterface;
import me.leoko.advancedban.core.Universal;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PunishmentTabCompleter implements TabCompleter {

    private final boolean temporary;


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
            suggestions.stream().filter(s -> s.startsWith(finalArgs[finalArgs.length - 1]))
                    .forEach(suggestions::remove);
        }

        return suggestions;
    }
}
