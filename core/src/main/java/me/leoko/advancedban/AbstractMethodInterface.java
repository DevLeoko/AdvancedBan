package me.leoko.advancedban;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

public abstract class AbstractMethodInterface<C> implements MethodInterface {

	private final Path dataFolder;
	private final Map<ConfigType, C> configs = new EnumMap<>(ConfigType.class);

	protected AbstractMethodInterface(Path dataFolder) {
		this.dataFolder = dataFolder;
	}

	@Override
	public final void loadFiles() {
		try {
			Files.createDirectories(dataFolder);

			// Copy defaults if files do not exist
			for (ConfigType config : ConfigType.values()) {
				copyDefaultResourceIfNecessary(config);
			}
			// Load configurations
			for (ConfigType config : ConfigType.values()) {
				Path configPath = getPath(config);
				if (config == ConfigType.MYSQL && !Files.exists(configPath)) {
					configPath = getPath(ConfigType.CONFIG);
				}
				configs.put(config, loadConfiguration(configPath));
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private void copyDefaultResourceIfNecessary(ConfigType config) throws IOException {
		Path configPath = getPath(config);
		if (config != ConfigType.MYSQL && !Files.exists(configPath)) {
			try (InputStream inputStream = getClass().getResource("/" + config.fileName).openStream()) {
				Files.copy(inputStream, configPath);
			}
		}
	}

	protected abstract C loadConfiguration(Path configPath) throws IOException;

	private Path getPath(ConfigType config) {
		return dataFolder.resolve(config.fileName);
	}

	protected enum ConfigType {
		CONFIG("config.yml"), MESSAGES("Messages.yml"), LAYOUTS("Layouts.yml"),
		/**
		 * The MySQL.yml file is only maintained for legacy support. If it does not
		 * exist, {@link CONFIG} is used
		 * 
		 */
		MYSQL("MySQL.yml");

		final String fileName;

		private ConfigType(String fileName) {
			this.fileName = fileName;
		}

	}

	@Override
	public final C getConfig() {
		return configs.get(ConfigType.CONFIG);
	}

	@Override
	public final C getMessages() {
		return configs.get(ConfigType.MESSAGES);
	}

	@Override
	public final C getLayouts() {
		return configs.get(ConfigType.LAYOUTS);
	}

	@Override
	public final C getMySQLFile() {
		return configs.get(ConfigType.MYSQL);
	}

}
