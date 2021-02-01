package me.leoko.advancedban;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class AbstractConfigurateMethodInterface extends AbstractMethodInterface<ConfigurationNode> {

    public AbstractConfigurateMethodInterface(Path dataDirectory) {
        super(dataDirectory);
    }

    @Override
    protected ConfigurationNode loadConfiguration(Path configPath) throws IOException {
        YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder().setPath(configPath).build();
        return loader.load();
    }

    @Override
    public Boolean getBoolean(Object file, String path) {
        return getConfigNode(file, path).getBoolean();
    }

    @Override
    public String getString(Object file, String path) {
        return getConfigNode(file, path).getString();
    }

    @Override
    public Long getLong(Object file, String path) {
        return getConfigNode(file, path).getLong();
    }

    @Override
    public Integer getInteger(Object file, String path) {
        return getConfigNode(file, path).getInt();
    }

    @Override
    public List<String> getStringList(Object file, String path) {
        try {
            return getConfigNode(file, path).getList(TypeToken.of(String.class));
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getBoolean(Object file, String path, boolean def) {
        return getConfigNode(file, path).getBoolean(def);
    }

    @Override
    public String getString(Object file, String path, String def) {
        return getConfigNode(file, path).getString(def);
    }

    @Override
    public long getLong(Object file, String path, long def) {
        return getConfigNode(file, path).getLong(def);
    }

    @Override
    public int getInteger(Object file, String path, int def) {
        return getConfigNode(file, path).getInt(def);
    }

    @Override
    public boolean contains(Object file, String path) {
        return getConfigNode(file, path).isEmpty();
    }

    private ConfigurationNode getConfigNode(Object file, String path) {
        return ((ConfigurationNode) file).getNode((Object[]) path.split("\\."));
    }


}
