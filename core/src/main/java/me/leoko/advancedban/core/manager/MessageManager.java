package me.leoko.advancedban.core.manager;

import me.leoko.advancedban.core.MethodInterface;
import me.leoko.advancedban.core.Universal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Message Manager is used for a convenient way to retrieve messages from configuration files.<br>
 * The manager is designed for (but not limited to) messages from the <code>message.yml</code> file.
 */
public class MessageManager {

    private static final MethodInterface mi = Universal.get().getMethods();

    /**
     * Get the message from the given path.<br>
     * The parameters work as described in {@link #sendMessage(Object, String, boolean, String...)}.
     *
     * @param path       the path
     * @param parameters the parameters
     * @return the message
     */
    public static String getMessage(String path, String... parameters) {
        String str = mi.getString(mi.getMessages(), path);
        if (str == null) {
            str = "Failed! See console for details!";
            System.out.println("!! Message-Error!\n"
                    + "In order to solve the problem please:"
                    + "\n  - Check the Message.yml-File for any missing or double \" or '"
                    + "\n  - Visit yamllint.com to  validate your Message.yml"
                    + "\n  - Delete the message file and restart the server");
        } else {
            str = replace(str, parameters).replace('&', '§');
        }
        return str;
    }


    /**
     * Get the message from the given path.<br>
     * The parameters work as described in {@link #sendMessage(Object, String, boolean, String...)}.
     *
     * @param path       the path
     * @param prefix     whether to prepend a prefix (can be overridden by the DisablePrefix option)
     * @param parameters the parameters
     * @return the message
     */
    public static String getMessage(String path, boolean prefix, String... parameters) {
        String prefixStr = "";
        if(prefix && !mi.getBoolean(mi.getConfig(), "Disable Prefix", false))
            prefixStr = getMessage("General.Prefix")+" ";

        return prefixStr+getMessage(path, parameters);
    }

    /**
     * Get the layout (basically just a string list) from the given path in the given file.<br>
     * The parameters work as described in {@link #sendMessage(Object, String, boolean, String...)}.
     *
     * @param file       the file (see {@link MethodInterface#getConfig()}, {@link MethodInterface#getMessages()},
     *                   {@link MethodInterface#getLayouts()})
     * @param path       the path
     * @param parameters the parameters
     * @return the layout
     */
    public static List<String> getLayout(Object file, String path, String... parameters) {
        if (mi.contains(file, path)) {
            List<String> list = new ArrayList<>();
            for (String str : mi.getStringList(file, path)) {
                list.add(replace(str, parameters).replace('&', '§'));
            }
            return list;
        }
		System.out.println("!! Message-Error in " + mi.getFileName(file) + "!\n"
		        + "In order to solve the problem please:"
		        + "\n  - Check the " + mi.getFileName(file) + "-File for any missing or double \" or '"
		        + "\n  - Visit yamllint.com to  validate your " + mi.getFileName(file)
		        + "\n  - Delete the message file and restart the server");
		return Collections.singletonList("Failed! See console for details!");
    }

    /**
     * Send message from the given path directly to the given receiver.<br><br>
     * <b>How the <code>parameters</code> work:</b>
     * The amount of parameters given has to be an even number as the parameters are interpreted in pairs.<br>
     * The first parameter is the String to search for and the second one is the one it will be replaced with.<br>
     * Same goes for the third and fourth and for the fifth and sixth and so on.<br><br>
     * <b>e.g.:</b> <code>getMessage("some.path", "NAME", "Leoko", "ID", "#342")</code> will get the message located at
     * "some.path" and replace each <i>%NAME%</i> with <i>Leoko</i> and each <i>%ID%</i> with <i>#342</i>.
     *
     * @param receiver   the receiver (Bukkit or Bungeecord player object)
     * @param path       the path
     * @param prefix     whether to use the global prefix
     * @param parameters the parameters
     */
    public static void sendMessage(Object receiver, String path, boolean prefix, String... parameters) {
        mi.sendMessage(receiver, (prefix && !mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? getMessage("General.Prefix") + " " : "") + getMessage(path, parameters));
    }

    private static String replace(String str, String... parameters) {
        for (int i = 0; i < parameters.length - 1; i = i + 2) {
            str = str.replaceAll("%" + parameters[i] + "%", parameters[i + 1]);
        }
        return str;
    }
}
