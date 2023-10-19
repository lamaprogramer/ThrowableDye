package net.iamaprogrammer.config.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.iamaprogrammer.ThrowableDye;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigRegistry {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path runDirectory = FabricLoader.getInstance().getConfigDir();


    public static class Builder<T> {
        private final String modid;
        private final Class<T> config;
        private T defaultConfig;


        public Builder(String modid, Class<T> config) {
            this.modid = modid;
            this.config = config;
        }

        public Builder<T> withDefaultConfig(T defaultConfig) {
            this.defaultConfig = defaultConfig;
            return this;
        }

        public T register() {
            Path configLocation = findOrCreateConfig(this.modid);
            try {
                return gson.fromJson(new FileReader(configLocation.toString()), this.config);
            } catch (FileNotFoundException e) {
                ThrowableDye.LOGGER.error("Config File Not Found. Falling back to default config.");
                return this.defaultConfig;
            } catch (JsonSyntaxException e) {
                ThrowableDye.LOGGER.error("Invalid Json Syntax. Falling back to default config.");
                return this.defaultConfig;
            }
        }

        private Path findOrCreateConfig(String modid) {
            Path path = Path.of(runDirectory.toString(), modid+".json");
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path.getParent());
                    String data = this.defaultConfig != null ? gson.toJson(this.defaultConfig) : "";
                    Files.writeString(path, data);
                } catch (IOException e) {
                    ThrowableDye.LOGGER.error("Could not create config.");
                }
            }

            return path;
        }

    }
}
