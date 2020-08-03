package me.leoko.advancedban.core.utils.tabcompletion;

import me.leoko.advancedban.core.Universal;

import java.util.ArrayList;

public class CleanTabCompleter implements MutableTabCompleter {
    private final MutableTabCompleter rawTabCompleter;
    public static final String PLAYER_PLACEHOLDER = "PLAYERS";

    public CleanTabCompleter(MutableTabCompleter rawTabCompleter) {
        this.rawTabCompleter = rawTabCompleter;
    }

    @Override
    public ArrayList<String> onTabComplete(Object user, String[] args) {
        ArrayList<String> suggestions = rawTabCompleter.onTabComplete(user, args);

        if(!suggestions.isEmpty() && suggestions.get(0).equals(PLAYER_PLACEHOLDER)) {
            suggestions.remove(0);
            for (Object player : Universal.get().getMethods().getOnlinePlayers()){
                suggestions.add(Universal.get().getMethods().getName(player));
            }
        }

        if(args.length > 0)
            suggestions.removeIf(s -> !s.startsWith(args[args.length - 1]));

        return suggestions;
    }
}
