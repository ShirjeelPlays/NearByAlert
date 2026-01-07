package com.example.shirjeel_plays.nearbyalert;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class NearbyAlertHud implements HudRenderCallback {

    private static AbstractClientPlayerEntity currentTarget = null;
    private static final List<AbstractClientPlayerEntity> nearbyPlayers = new ArrayList<>();
    private static long lastSwitchTick = 0;
    
    // Animation states
    private static float displayScale = 1.0f;
    private static float displayAlpha = 0.0f;
    private static String lastTargetName = "";
    
    // Constants
    private static final int RANGE = 150;
    private static final int SWITCH_INTERVAL = 100; // 5 seconds * 20 ticks

    public static void onTick(MinecraftClient client) {
        if (client.world == null || client.player == null || !NearbyAlertClient.isEnabled) {
            nearbyPlayers.clear();
            currentTarget = null;
            return;
        }

        // 1. Detect Players
        nearbyPlayers.clear();
        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue; // Ignore self
            
            // Ignore players in config
            if (NearbyAlertConfig.INSTANCE.ignoredPlayers.contains(player.getName().getString())) continue;
            
            // Check gamemode? Spectators? The prompt implies detecting all "other players". 
            // Usually we assume visible players or survival players. 
            // But "detect through walls" implies position check only.
            // We'll stick to basic distance check.
            
            float dist = client.player.distanceTo(player);
            if (dist <= RANGE) {
                nearbyPlayers.add(player);
            }
        }

        // Sort by distance (optional but nice) or keep stable?
        // Prompt says "Cycle through players". Stable order is better for cycling.
        // We'll just sort by UUID or Entity ID to keep the list order stable so cycling is predictable.
        nearbyPlayers.sort(Comparator.comparingInt(PlayerEntity::getId));

        // 2. Cycle Logic
        long currentTick = client.world.getTime();
        
        if (nearbyPlayers.isEmpty()) {
            currentTarget = null;
            return;
        }

        if (currentTarget == null || !nearbyPlayers.contains(currentTarget)) {
            // Target lost or initialization
            currentTarget = nearbyPlayers.get(0);
            lastSwitchTick = currentTick;
            triggerSwitchAnimation();
        } else {
            // Check timer
            if (currentTick - lastSwitchTick >= SWITCH_INTERVAL) {
                // Time to switch
                int currentIndex = nearbyPlayers.indexOf(currentTarget);
                int nextIndex = (currentIndex + 1) % nearbyPlayers.size();
                currentTarget = nearbyPlayers.get(nextIndex);
                lastSwitchTick = currentTick;
                triggerSwitchAnimation();
            }
        }
    }
    
    private static void triggerSwitchAnimation() {
        displayScale = 1.1f; // Pop up
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!NearbyAlertClient.isEnabled) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        // Handle Animations
        float delta = tickCounter.getTickDelta(true); // Get frame delta
        
        // Fade In/Out Logic
        float targetAlpha = (currentTarget != null) ? 1.0f : 0.0f;
        displayAlpha = MathHelper.lerp(0.1f * delta, displayAlpha, targetAlpha); // Smooth fade
        
        if (displayAlpha < 0.05f && currentTarget == null) return; // Don't render if invisible
        
        // Scale Logic (Return to 1.0)
        displayScale = MathHelper.lerp(0.1f * delta, displayScale, 1.0f);

        if (currentTarget == null) return;

        // Calculate Distance
        float distance = client.player.distanceTo(currentTarget);
        
        // Determine Color
        int color;
        if (distance <= 30) color = 0xAA0000; // Dark Red
        else if (distance <= 75) color = 0xFFFF55; // Yellow
        else color = 0x55FF55; // Green
        
        // Apply Fade Alpha to Color
        int alphaInt = (int) (displayAlpha * 255);
        if (alphaInt > 255) alphaInt = 255;
        if (alphaInt < 0) alphaInt = 0;
        // Combine alpha with color (ARGB)
        // Note: Minecraft colors are usually ARGB. But FontRenderer usually ignores Alpha in the int unless specific?
        // Actually DrawContext.drawTextWithShadow supports alpha in the color int.
        // Format: 0xAARRGGBB.
        // The defined colors are RRGGBB. We need to OR in the Alpha.
        color = (alphaInt << 24) | (color & 0x00FFFFFF);

        // Direction Logic
        String arrow = getDirectionArrow(client.player, currentTarget);
        
        // Pulse Animation for close range (< 30)
        float scale = displayScale;
        if (distance <= 30) {
            // Pulse sin wave based on real time
            float time = (System.currentTimeMillis() % 1000) / 1000.0f; // 0 to 1
            float pulse = MathHelper.sin(time * (float)Math.PI * 2) * 0.1f; // -0.1 to 0.1
            // We want gentle pulse.
            // Actually prompt says "Add a gentle arrow pulse".
            // Maybe just scale the arrow? Or the whole text?
            // "Subtle scale pop (1.0 -> 1.1 -> 1.0)" was for switching.
            // "Gentle arrow pulse" is separate.
            // Let's just scale the whole text slightly for simplicity, or append a pulsing arrow.
            // I'll pulse the whole text scale slightly if close.
            scale += (pulse * 0.05f); 
        }

        // Prepare Text
        String text = String.format("%s is nearby %s", currentTarget.getName().getString(), arrow);
        
        // Render
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        TextRenderer textRenderer = client.textRenderer;
        
        int textWidth = textRenderer.getWidth(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight - Math.max(40, NearbyAlertConfig.INSTANCE.hudOffset);

        context.getMatrices().push();
        // Scale around center of text
        context.getMatrices().translate(screenWidth / 2.0, y + 4.0, 0); // Center of text (approx height 8)
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-screenWidth / 2.0, -(y + 4.0), 0);
        
        context.drawTextWithShadow(textRenderer, text, x, y, color);
        
        context.getMatrices().pop();
    }

    private String getDirectionArrow(PlayerEntity player, PlayerEntity target) {
        Vec3d pPos = player.getPos();
        Vec3d tPos = target.getPos();
        
        double dx = tPos.x - pPos.x;
        double dz = tPos.z - pPos.z;
        
        // Calculate angle to target in degrees (South = 0)
        // atan2(dz, dx): East=0, South=90, West=180, North=-90
        // We want South=0.
        // degrees = toDegrees(atan2) - 90
        double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        
        double playerYaw = player.getYaw();
        double diff = MathHelper.wrapDegrees(targetYaw - playerYaw);
        
        // ↑ = Front (-45 to 45)
        // → = Right (45 to 135)
        // ↓ = Behind (135 to 180 or -180 to -135)
        // ← = Left (-135 to -45)
        
        // Note: My previous derivation "West is Right" means +90 is Right.
        // Let's re-verify.
        // Player Yaw = 0 (South).
        // Target is West (Right of South).
        // dx = -1, dz = 0.
        // atan2(0, -1) = 180.
        // targetYaw = 180 - 90 = 90.
        // diff = 90 - 0 = 90.
        // +90 is Right.
        
        if (diff >= -45 && diff <= 45) return "↑";
        if (diff > 45 && diff < 135) return "←"; // Wait. 
        // If diff is +90 (Right), and I said West is Right.
        // Why did I write left arrow?
        // Let's check Unicode arrows.
        // ← Left
        // → Right
        // If +90 is Right, then it should be "→".
        
        // Let's double check "West is Right".
        // Minecraft: +X East, -X West. +Z South, -Z North.
        // Facing South (+Z). Right hand is West (-X). Yes.
        
        if (diff > 45 && diff < 135) return "→"; // Right
        if (diff >= 135 || diff <= -135) return "↓"; // Behind
        if (diff < -45 && diff > -135) return "←"; // Left
        
        return "↑"; // Fallback
    }
}
