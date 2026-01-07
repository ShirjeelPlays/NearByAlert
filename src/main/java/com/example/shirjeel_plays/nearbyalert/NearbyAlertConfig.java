package com.example.shirjeel_plays.nearbyalert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NearbyAlertConfig {
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("nearbyalert.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public static NearbyAlertConfig INSTANCE = new NearbyAlertConfig();

    public boolean isEnabled = true;
    public List<String> ignoredPlayers = new ArrayList<>();
    public int hudOffset = 80;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, NearbyAlertConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
