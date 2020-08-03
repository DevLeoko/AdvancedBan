package me.leoko.advancedban.core.utils.tabcompletion;

import java.util.ArrayList;
import java.util.Arrays;

public interface MutableTabCompleter extends TabCompleter {

    @Override
    ArrayList<String> onTabComplete(Object user, String[] args);

    static <T> ArrayList<T> list(T... elements){
        return new ArrayList<T>(Arrays.asList(elements));
    }
}
