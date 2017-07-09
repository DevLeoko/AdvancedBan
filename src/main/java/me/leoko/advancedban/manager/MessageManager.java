package me.leoko.advancedban.manager;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leoko @ dev.skamps.eu on 13.07.2016.
 */
public class MessageManager {
    private static final MethodInterface mi = Universal.get().getMethods();

    public static String getMessage(String path, String... parameters) {
        String str = mi.getString(mi.getMessages(), path);
        if (str == null) {
            str = "Failed! See console for details!";
            System.out.println("!! Message-Error!\n" +
                    "In order to solve the problem please:" +
                    "\n  - Check the Message.yml-File for any missing or double \" or '" +
                    "\n  - Visit yamllint.com to  validate your Message.yml" +
                    "\n  - Delete the message file and restart the server");
        } else {
            str = replace(str, parameters).replace('&', '§');
        }
        return str;
    }

    public static List<String> getLayout(Object file, String path, String... parameters) {
        if (mi.contains(file, path)) {
            List<String> list = new ArrayList<>();
            for (String str : mi.getStringList(file, path)) {
                list.add(replace(str, parameters).replace('&', '§'));
            }
            return list;
        } else {
            System.out.println("!! Message-Error in " + mi.getFileName(file) + "!\n" +
                    "In order to solve the problem please:" +
                    "\n  - Check the " + mi.getFileName(file) + "-File for any missing or double \" or '" +
                    "\n  - Visit yamllint.com to  validate your " + mi.getFileName(file) +
                    "\n  - Delete the message file and restart the server");
            return null;
        }
    }

    public static List<String> getLayout(String path, String... parameters) {
        return getLayout(mi.getLayouts(), path, parameters);
    }

    public static void sendMessage(Object sender, String path, boolean prefix, String... parameters) {
        mi.sendMessage(sender, (prefix ? getMessage("General.Prefix") + " " : "") + getMessage(path, parameters));
    }

    private static String replace(String str, String... parameters) {
        for (int i = 0; i < parameters.length - 1; i = i + 2) {
            str = str.replaceAll("%" + parameters[i] + "%", parameters[i + 1]);
        }
        return str;
    }
}
