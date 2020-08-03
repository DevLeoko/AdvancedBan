package me.leoko.advancedban.core.utils.tabcompletion;

public class BasicTabCompleter extends CleanTabCompleter {
    public BasicTabCompleter(String... firstLayerArguments) {
        super((user, args) -> {
            if(args.length == 1){
                return MutableTabCompleter.list(firstLayerArguments);
            } else {
                return MutableTabCompleter.list();
            }
        });
    }
}
