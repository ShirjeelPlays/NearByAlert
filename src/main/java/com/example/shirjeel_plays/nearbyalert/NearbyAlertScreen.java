package com.example.shirjeel_plays.nearbyalert;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class NearbyAlertScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget nameInput;
    private ButtonWidget toggleButton;
    private ButtonWidget decOffsetButton;
    private ButtonWidget incOffsetButton;
    // private IgnoredPlayerListWidget playerList;

    public NearbyAlertScreen(Screen parent) {
        super(Text.literal("Nearby Alert Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Toggle Button
        toggleButton = ButtonWidget.builder(
                Text.literal("Mod Enabled: " + (NearbyAlertConfig.INSTANCE.isEnabled ? "ON" : "OFF")),
                button -> {
                    NearbyAlertConfig.INSTANCE.isEnabled = !NearbyAlertConfig.INSTANCE.isEnabled;
                    NearbyAlertClient.isEnabled = NearbyAlertConfig.INSTANCE.isEnabled; // Sync with main client static
                    button.setMessage(Text.literal("Mod Enabled: " + (NearbyAlertConfig.INSTANCE.isEnabled ? "ON" : "OFF")));
                    NearbyAlertConfig.save();
                })
                .dimensions(this.width / 2 - 100, 40, 200, 20)
                .build();
        this.addDrawableChild(toggleButton);

        // Name Input Field
        nameInput = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 70, 150, 20, Text.literal("Player Name"));
        nameInput.setMaxLength(16);
        nameInput.setPlaceholder(Text.literal("Enter player name"));
        this.addDrawableChild(nameInput);

        // Add Button
        ButtonWidget addButton = ButtonWidget.builder(Text.literal("Add"), button -> addPlayer())
                .dimensions(this.width / 2 + 55, 70, 45, 20)
                .build();
        this.addDrawableChild(addButton);

        decOffsetButton = ButtonWidget.builder(Text.literal("-5"), b -> {
            NearbyAlertConfig.INSTANCE.hudOffset = Math.max(40, NearbyAlertConfig.INSTANCE.hudOffset - 5);
            NearbyAlertConfig.save();
        }).dimensions(this.width / 2 - 100, 95, 45, 20).build();
        this.addDrawableChild(decOffsetButton);

        incOffsetButton = ButtonWidget.builder(Text.literal("+5"), b -> {
            NearbyAlertConfig.INSTANCE.hudOffset = Math.min(160, NearbyAlertConfig.INSTANCE.hudOffset + 5);
            NearbyAlertConfig.save();
        }).dimensions(this.width / 2 + 55, 95, 45, 20).build();
        this.addDrawableChild(incOffsetButton);

        // Player List Title
        // (Rendered in render method)

        refreshList();
    }
    
    private void addPlayer() {
        String name = nameInput.getText().trim();
        if (!name.isEmpty() && !NearbyAlertConfig.INSTANCE.ignoredPlayers.contains(name)) {
            NearbyAlertConfig.INSTANCE.ignoredPlayers.add(name);
            NearbyAlertConfig.save();
            nameInput.setText("");
            refreshList();
        }
    }

    private void removePlayer(String name) {
        NearbyAlertConfig.INSTANCE.ignoredPlayers.remove(name);
        NearbyAlertConfig.save();
        refreshList();
    }

    // A simple way to manage the dynamic list of buttons
    private final List<ClickableWidget> listWidgets = new ArrayList<>();

    private void refreshList() {
        // Remove old list widgets
        for (ClickableWidget widget : listWidgets) {
            this.remove(widget);
        }
        listWidgets.clear();

        int y = 110;
        for (String name : NearbyAlertConfig.INSTANCE.ignoredPlayers) {
            // Player Name Label (We'll just draw text in render, but we need a remove button)
            
            // Remove Button
            ButtonWidget removeBtn = ButtonWidget.builder(Text.literal("X"), b -> removePlayer(name))
                    .dimensions(this.width / 2 + 80, y, 20, 20)
                    .build();
            
            this.addDrawableChild(removeBtn);
            listWidgets.add(removeBtn);
            
            y += 24;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal("HUD Distance: " + NearbyAlertConfig.INSTANCE.hudOffset + " px"), this.width / 2 - 100, 95 - 12, 0xFFFFFF);
        
        // Draw list background/area hint?
        // Draw names next to buttons
        int y = 110;
        for (String name : NearbyAlertConfig.INSTANCE.ignoredPlayers) {
            context.drawTextWithShadow(this.textRenderer, name, this.width / 2 - 100, y + 6, 0xFFFFFF);
            y += 24;
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && this.nameInput.isFocused()) {
            addPlayer();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
