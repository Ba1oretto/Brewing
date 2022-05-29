package homeward.plugin.brewing.utilities;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import homeward.plugin.brewing.Main;
import homeward.plugin.brewing.beans.ItemProperties;
import homeward.plugin.brewing.enumerates.ProviderEnum;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class ItemPropertiesUtils {
    private final Logger logger = Main.getInstance().getSLF4JLogger();
    private final Map<String, ItemProperties.Content> itemTier = Main.itemTier();

    // region get provider
    public @Nullable ProviderEnum getProvider(final File file, final ConfigurationSection section) {
        String providerString = section.getString("provider");
        if (providerString == null) {
            logger.warn(String.format("The key %s in %s does not exist or incorrect", BrewingUtils.getPath(section.getCurrentPath(), "provider"), file.getAbsolutePath()));
            return null;
        }

        ProviderEnum provider = ProviderEnum.getProvider(providerString.toUpperCase(Locale.ROOT));

        if (provider == null) {
            logger.warn(String.format("The value %s of key %s in %s is incorrect", providerString, BrewingUtils.getPath(section.getCurrentPath(), "provider"), file.getAbsolutePath()));
            return null;
        }

        return provider;
    }
    // endregion

    // region get display
    public @Nullable ItemProperties.Content getDisplay(final File file, final ConfigurationSection section) {
        ConfigurationSection displaySection = section.getConfigurationSection("display");
        if (displaySection == null) {
            logger.warn(String.format("The key %s in %s does not exist or incorrect", BrewingUtils.getPath(section.getCurrentPath(), "display"), file.getAbsolutePath()));
            return null;
        }

        return getContent(file, displaySection);
    }
    // endregion

    // region get lore
    @SuppressWarnings("unchecked")
    public @Nullable ArrayList<ItemProperties.Content> getLore(final File file, final ConfigurationSection section) {
        List<?> loreList = section.getList("lore", null);
        if (loreList == null) return null;

        AtomicInteger atomicInteger = new AtomicInteger(0);
        ArrayList<ItemProperties.Content> loreL = new ArrayList<>();
        loreList.forEach(object -> {
            int index = atomicInteger.getAndIncrement();
            if (object instanceof Map map) {
                String text = (String) map.getOrDefault("text", null);
                if (text == null) {
                    logger.warn(String.format("The key %s in %s does incorrect", BrewingUtils.getPath(section.getCurrentPath(), "lore[" + index + "].text"), file.getAbsolutePath()));
                    return;
                }

                ItemProperties.Content content = ItemProperties.getContent();
                content.text(text);

                Object colorList = map.getOrDefault("color", null);
                if (colorList instanceof List<?> color) {
                    int r = Integer.parseInt(color.get(0).toString());
                    int g = Integer.parseInt(color.get(1).toString());
                    int b = Integer.parseInt(color.get(2).toString());
                    content.color(new ArrayList<>(Arrays.asList(r, g, b)));
                }

                loreL.add(content);
            } else if (object instanceof String lore) {
                ItemProperties.Content content = ItemProperties.getContent();
                content.text(lore);
                loreL.add(content);
            } else {
                logger.warn(String.format("The value %s of key %s in %s incorrect", object, BrewingUtils.getPath(section.getCurrentPath(), "lore[" + index + "]"), file.getAbsolutePath()));
            }
        });
        return loreL.size() == 0 ? null : loreL;
    }
    // endregion

    // region get material
    public @Nullable Material getMaterial(final File file, final ConfigurationSection section) {
        String materialString = section.getString("material", null);
        if (materialString == null) {
            logger.warn(String.format("The key %s in %s does not exist or incorrect", BrewingUtils.getPath(section.getCurrentPath(), "material"), file.getAbsolutePath()));
            return null;
        }

        Material material = Material.getMaterial(materialString.toUpperCase(Locale.ROOT), false);
        if (material == null) {
            logger.warn(String.format("The value %s of key %s in %s is incorrect", materialString, BrewingUtils.getPath(section.getCurrentPath(), "material"), file.getAbsolutePath()));
            return null;
        }

        return material;
    }
    // endregion

    // region get custom module data
    public int getCustomModuleData(final ConfigurationSection section) {
        return section.getInt("custom-model-data", 0);
    }
    // endregion

    // region get tier
    public String getTier(final File file, final ConfigurationSection section) {
        String tier = section.getString("item-tier", null);
        if (tier == null) return null;

        if (!itemTier.containsKey(tier)) {
            logger.warn(String.format("The value %s of key %s in %s not match item-tier set %s", tier, BrewingUtils.getPath(section.getCurrentPath(), "item-tier"), file.getAbsolutePath(), itemTier.keySet()));
            return null;
        }

        return tier;
    }
    // endregion

    // region get restore food
    public int getRestoreFood(final ConfigurationSection section) {
        return section.getInt("restore.food", 0);
    }
    // endregion

    // region get restore health
    public double getRestoreHealth(final ConfigurationSection section) {
        return section.getDouble("restore.health", 0.0D);
    }
    // endregion

    // region get restore saturation
    public double getRestoreSaturation(final ConfigurationSection section) {
        return section.getDouble("restore.saturation", 0.0D);
    }
    // endregion

    // region get effect
    public ArrayList<ItemProperties.Effect> getEffect(final File file, final ConfigurationSection section) {
        List<?> effectList = section.getList("effect", null);
        if (effectList == null) return null;

        ArrayList<ItemProperties.Effect> effects = new ArrayList<>();
        Gson gson = new Gson();

        AtomicInteger atomicInteger = new AtomicInteger(0);
        effectList.forEach(effectString -> {
            JsonObject effect = JsonParser.parseString(CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, gson.toJson(effectString))).getAsJsonObject();

            JsonElement potionTypeElement = effect.get("potionType");
            if (potionTypeElement == null) {
                logger.warn(String.format("The key %s in %s does not exist or incorrect", BrewingUtils.getPath(section.getCurrentPath(), "effect[" + atomicInteger.getAndIncrement() + "].potion-type"), file.getAbsolutePath()));
                return;
            }
            String potionTypeString = potionTypeElement.getAsString();
            PotionEffectType potionType = PotionEffectType.getByName(potionTypeString);
            if (potionType == null) {
                logger.warn(String.format("The value %s of key %s in %s does not exist or incorrect", potionTypeElement, BrewingUtils.getPath(section.getCurrentPath(), "effect[" + atomicInteger.getAndIncrement() + "].potion-type"), file.getAbsolutePath()));
                return;
            }

            JsonElement durationElement = effect.get("duration");
            int duration;
            if (durationElement == null) duration = 20;
            else duration = durationElement.getAsInt();

            JsonElement amplifierElement = effect.get("amplifier");
            int amplifier;
            if (amplifierElement == null) amplifier = 0;
            else amplifier = amplifierElement.getAsInt();

            JsonElement ambientElement = effect.get("ambient");
            boolean ambient;
            if (ambientElement == null) ambient = false;
            else ambient = ambientElement.getAsBoolean();

            JsonElement showParticlesElement = effect.get("showParticles");
            boolean showParticles;
            if (showParticlesElement == null) showParticles = false;
            else showParticles = showParticlesElement.getAsBoolean();

            JsonElement showIconElement = effect.get("showIcon");
            boolean showIcon;
            if (showIconElement == null) showIcon = false;
            else showIcon = showIconElement.getAsBoolean();

            ItemProperties.Effect effectObject = ItemProperties.getEffect();
            effectObject.potionType(potionType)
                    .duration(duration)
                    .amplifier(amplifier)
                    .ambient(ambient)
                    .showParticles(showParticles)
                    .showIcon(showIcon);

            effects.add(effectObject);
        });

        if (effects.size() != effectList.size()) effects.clear();
        return effects;
    }
    // endregion

    // region get command
    public @Nullable ArrayList<String> getCommand(final ConfigurationSection section) {
        ArrayList<String> command = (ArrayList<String>) section.getStringList("command");
        if (command.size() == 0) return null;
        return command;
    }
    // endregion

    // region get required level
    public int getRequiredLevel(final ConfigurationSection section) {
        return section.getInt("required-level", 0);
    }
    // endregion

    // region get generated content
    @SuppressWarnings("unchecked")
    public ItemProperties.Content getContent(final File file, final ConfigurationSection section) {
        String text = section.getString("text");
        if (text == null) {
            logger.warn(String.format("The key %s in %s does not exist or incorrect", BrewingUtils.getPath(section.getCurrentPath(), "text"), file.getAbsolutePath()));
            return null;
        }

        ItemProperties.Content content = ItemProperties.getContent();

        List<?> rgb = section.getList("color");
        if (rgb != null && rgb.size() == 3) {
            content.color((ArrayList<Integer>) section.getList("color"));
        }

        return content.text(text);
    }
    // endregion
}
