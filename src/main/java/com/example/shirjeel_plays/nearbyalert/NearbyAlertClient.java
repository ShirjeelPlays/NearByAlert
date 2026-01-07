package com.example.shirjeel_plays.nearbyalert;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class NearbyAlertClient implements ClientModInitializer {

    public static boolean isEnabled = true;
    private static KeyBinding toggleKey;
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        // Load Config
        NearbyAlertConfig.load();
        isEnabled = NearbyAlertConfig.INSTANCE.isEnabled;

        // Register Keybinds
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nearbyalert.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.nearbyalert.general"
        ));
        
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nearbyalert.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.nearbyalert.general"
        ));

        // Handle Keybind press & Tick Logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Toggle Key
            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                NearbyAlertConfig.INSTANCE.isEnabled = isEnabled;
                NearbyAlertConfig.save();
            }
            
            // Menu Key
            while (menuKey.wasPressed()) {
                client.setScreen(new NearbyAlertScreen(client.currentScreen));
            }
            
            NearbyAlertHud.onTick(client);
        });

        // Register HUD
        HudRenderCallback.EVENT.register(new NearbyAlertHud());
    }
}
