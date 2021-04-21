package me.leoko.advancedban.utils.tabcompletion;

import java.util.Collections;
import java.util.List;

public class NullTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(Object user, String[] args) {
        return Collections.emptyList();
    }

}
