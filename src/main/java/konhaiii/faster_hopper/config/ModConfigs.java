package konhaiii.faster_hopper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import konhaiii.faster_hopper.FasterHopper;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ModConfigs {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = Path.of("config", "faster_hopper.json");

	public int goldenHopperCooldownTick = 2;
	public static ModConfigs loadConfig() {
		if (!Files.exists(CONFIG_PATH)) {
			ModConfigs defaultConfig = new ModConfigs();
			defaultConfig.saveConfig();
			return defaultConfig;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			return GSON.fromJson(reader, ModConfigs.class);
		} catch (IOException | JsonSyntaxException exception) {
			FasterHopper.LOGGER.error(exception.getMessage());
			return new ModConfigs();
		}
	}

	public void saveConfig() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException exception) {
			FasterHopper.LOGGER.error(exception.getMessage());
		}
	}
}