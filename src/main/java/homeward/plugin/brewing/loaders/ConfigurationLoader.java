package homeward.plugin.brewing.loaders;


import com.google.common.base.Charsets;
import homeward.plugin.brewing.Main;
import homeward.plugin.brewing.enumerates.EnumBase;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * cannot be used when plugin main class instancing
 */
public class ConfigurationLoader {
    private final File dataFolder = Main.getInstance().getDataFolder();

    private final File defaultConfigFile = new File(dataFolder, "config.yml");

    private final File itemFolder = new File(dataFolder, "items");
    private final File recipeFolder = new File(dataFolder, "recipes");

    @Getter private final Map<ConfigEnum, LinkedHashMap<File, YamlConfiguration>> configurationList = new LinkedHashMap<>();

    // region reload
    public void reload() {
        createDefaultConfigDirectory();
        createItemConfigDirectory();
        createRecipeConfigDirectory();

        configurationList.clear();
        loadDefaultConfig();
        loadItemConfig();
        loadRecipeConfig();

        TierLoader.getInstance().loadItemTier();
        ItemPropertiesLoader.getInstance().loadItems();
    }
    // endregion

    // region load configuration files
    private void loadDefaultConfig() {
        this.updateConfigurationList(ConfigEnum.DEFAULT_CONFIG, defaultConfigFile, YamlConfiguration.loadConfiguration(defaultConfigFile));
    }

    private void loadItemConfig() {
        this.loadCustomConfig(itemFolder, ConfigEnum.ITEM_CONFIG);
    }

    private void loadRecipeConfig() {
        this.loadCustomConfig(recipeFolder, ConfigEnum.RECIPE_CONFIG);
    }
    // endregion

    // region create resource file from jar
    private void createDefaultConfigDirectory() {
        if (!defaultConfigFile.exists()) {
            Main.getInstance().saveResource("config.yml", false);
        }
    }

    private void createItemConfigDirectory() {
        String[] itemList = itemFolder.list();
        if (!itemFolder.exists() || itemList == null || itemList.length == 0) {
            Main.getInstance().saveResource("items/tier.yml", false);
            Main.getInstance().saveResource("items/template.yml", false);
        }
    }

    private void createRecipeConfigDirectory() {
        String[] recipeList = recipeFolder.list();
        if (!recipeFolder.exists() || recipeList == null || recipeList.length == 0) {
            Main.getInstance().saveResource("recipes/test1.yml", false);
        }
    }
    // endregion

    // region load custom config
    private void loadCustomConfig(File folder, ConfigEnum type) {
        File[] files = folder.listFiles();

        if (files != null && files.length != 0) {
            for (File file : files) {
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                this.updateConfigurationList(type, file, configuration);
            }
        }
    }
    // endregion

    // region update configuration list
    private void updateConfigurationList(ConfigEnum type, File file, YamlConfiguration configuration) {
        LinkedHashMap<File, YamlConfiguration> configMap = configurationList.get(type);

        if (configMap == null) {
            configMap = new LinkedHashMap<>();
        }

        configMap.put(file, configuration);
        configurationList.put(type, configMap);
    }
    // endregion

    // region initialize class
    private ConfigurationLoader() {}

    private static volatile ConfigurationLoader instance;

    public static ConfigurationLoader getInstance() {
        if (instance == null) {
            synchronized (ConfigurationLoader.class) {
                if (instance == null) {
                    instance = new ConfigurationLoader();
                }
            }
        }
        return instance;
    }
    // endregion

    // region config enum
    enum ConfigEnum implements EnumBase {
        DEFAULT_CONFIG("default config"),
        ITEM_CONFIG("item config"),
        RECIPE_CONFIG("recipe config");

        private final String type;

        ConfigEnum(String type) {
            this.type = type;
        }

        @Override
        public String getString() {
            return type;
        }
    }
    // endregion
}