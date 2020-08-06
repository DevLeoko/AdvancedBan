package me.leoko.advancedban.utils.tabcompletion;

import java.util.List;

@FunctionalInterface
public interface TabCompleter {
    List<String> onTabComplete(Object user, String[] args);
}
