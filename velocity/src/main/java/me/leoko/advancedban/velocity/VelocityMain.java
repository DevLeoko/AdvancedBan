package me.leoko.advancedban.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.leoko.advancedban.Universal;
import me.leoko.advancedban.velocity.listener.ChatListenerVelocity;
import me.leoko.advancedban.velocity.listener.ConnectionListenerVelocity;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Plugin(
        id = VelocityMain.VelocityPluginInfo.PLUGIN_ID,
        name = VelocityMain.VelocityPluginInfo.PLUGIN_NAME,
        version = VelocityMain.VelocityPluginInfo.PLUGIN_VERSION,
        authors = {"Leoko", "Fede1132(Added velocity support)"}
)
public class VelocityMain {
    public static class VelocityPluginInfo {
        public static final String PLUGIN_ID = "advancedban";
        public static final String PLUGIN_NAME = "AdvancedBan";
        public static final String PLUGIN_VERSION = "2.2.1";
    }
    private static VelocityMain instance;
    private ArrayList<File> loaded = new ArrayList<>();
    public final ProxyServer server;
    public final Logger logger;
    public final File folder;

    @Inject
    public VelocityMain(ProxyServer server, Logger logger, @DataDirectory final Path folder) {
        instance = this;
        this.server = server;
        this.logger = logger;
        this.folder = folder.toFile();
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        Universal.get().setup(new VelocityMethods());
        server.getEventManager().register(this, new ChatListenerVelocity());
        server.getEventManager().register(this, new ConnectionListenerVelocity());
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        Universal.get().shutdown();
    }

    public static VelocityMain get() {
        return instance;
    }
}
