package com.skycat.wbshop;

import com.mojang.serialization.Codec;
import com.skycat.wbshop.util.LogLevel;
import com.skycat.wbshop.util.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GlobalConfig {
    public static final File CONFIG_FILE = new File("config/wbshop/itemValues.txt");
    public static final Codec<GlobalConfig> CODEC = Utils.hashMapCodec(Registries.ITEM.getCodec(), "item", Codec.LONG, "value")
            .xmap(GlobalConfig::new, GlobalConfig::getItemValueCache);
    /**
     * Stores all the cached values of items
     */
    private HashMap<Item, Long> itemValueCache;
    /**
     * Stores the configured values of items. The String values are regexes that match Identifiers. Results are cached in {@link GlobalConfig#itemValueCache}.
     */
    private HashMap<Pattern, Long> itemValueRules;
    private boolean dirty = false;

    public GlobalConfig() {
        itemValueCache = new HashMap<>();
        itemValueRules = new HashMap<>();
    }

    public GlobalConfig(HashMap<Item, Long> itemValueCache) {
        this.itemValueCache = itemValueCache;
        itemValueRules = new HashMap<>();
    }

    public static GlobalConfig load() {
        if (!CONFIG_FILE.exists()) { // If the file doesn't exist, make a new one.
            GlobalConfig newConfig =new GlobalConfig();
            newConfig.markDirty(); // Just so that the file will be around to edit.
            return newConfig;
        }
        GlobalConfig newConfig = new GlobalConfig();
        try (Scanner scanner = new Scanner(CONFIG_FILE)) {
            while (scanner.hasNextLine()) { // For each line
                String[] tokens = scanner.nextLine().split(";", 2); // Split at the semicolon
                try {
                    newConfig.itemValueRules.put(Pattern.compile(tokens[0]), Long.parseLong(tokens[1])); // Add the regex and long
                } catch (NumberFormatException e) {
                    Utils.log("Failed to parse item value for regex \"" + tokens[0] + "\": value \"" + tokens[1] + "\" could not be parsed as long, skipping.", LogLevel.WARN);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find global config save file, even after checking it exists. That's a problem.");
        }
        return newConfig;
    }

    private long getItemValue(ItemConvertible item) {
        return getItemValue(item.asItem());
    }

    public long getItemValue(Item item) {
        if (item.equals(Items.AIR)) return 0; // Air is always worth 0
        Long cachedValue = itemValueCache.get(item);
        if (cachedValue != null) return cachedValue; // If we have the value cached, use that.
        long value = 1;
        for (Map.Entry<Pattern, Long> entry : itemValueRules.entrySet()) {
            if (entry.getKey().matcher(Registries.ITEM.getId(item).toString()).find()) { // If it matches
                value = entry.getValue(); // Set the new value
                break;
            }
        }
        itemValueCache.put(item, value); // Cache the determined value
        return value;
    }

    private HashMap<Item, Long> getItemValueCache() {
        return itemValueCache;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void markDirty() {
        dirty = true;
    }

    /**
     * Attempt to save the config if it needs saving.
     *
     * @return True if saving was successful (or unneeded), false if it failed.
     */
    public boolean save() {
        if (!isDirty()) return true;
        try (PrintWriter pw = new PrintWriter(CONFIG_FILE)) {
            itemValueRules.forEach((pattern, value) -> pw.write(pattern.toString() + ";" + value + "\n")); // Write it all :)))
        } catch (FileNotFoundException e) {
            return false;
        }
        setDirty(false);
        return true;
    }

    public void setItemValue(ItemConvertible item, long value) {
        itemValueRules.put(Pattern.compile(Registries.ITEM.getId(item.asItem()).toString()), value);
        markDirty();
    }
}
