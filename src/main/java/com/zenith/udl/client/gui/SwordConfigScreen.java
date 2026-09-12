package com.zenith.udl.client.gui;
import com.zenith.udl.config.item.ItemSettingModule;
import com.zenith.udl.config.item.SwordConfig;
import com.zenith.udl.network.NetworkHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
public class SwordConfigScreen extends Screen {
    private final ItemStack swordStack;
    private boolean useUnsafe;
    private final Map<ItemSettingModule, Boolean> featureStates = new HashMap<>();
    private static final int PANEL_WIDTH = 340;
    private static final int PANEL_PADDING = 15;
    private static final int ROW_HEIGHT = 28;
    private static final int SUB_FEATURE_INDENT = 12;
    private static final List<SettingNode> SETTING_NODES = List.of(
            new UseUnsafeGroupNode(List.of(
                    ItemSettingModule.SERVER_ENTITY_MANAGER,
                    ItemSettingModule.ENTITY_TICK_LIST,
                    ItemSettingModule.CLIENT_ENTITY_STORAGE
            )),
            new StandaloneNode(ItemSettingModule.DELETE_ENTITY_SAVE_DATA),
            new StandaloneNode(ItemSettingModule.ENTITY_BAN),
            new StandaloneNode(ItemSettingModule.HIDDEN_ENTITY)
    );
    public SwordConfigScreen(ItemStack swordStack) {
        super(Component.literal("UltraDamage-Library Settings"));
        this.swordStack = swordStack;
        this.useUnsafe = SwordConfig.isUseUnsafe(swordStack);
        for (ItemSettingModule module : ItemSettingModule.values()) {
            featureStates.put(module, SwordConfig.isFeatureEnabled(swordStack, module));
        }
    }
    @Override
    protected void init() {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = 40;
        int contentWidth = PANEL_WIDTH - (PANEL_PADDING * 2);
        int currentY = panelY + 35;
        for (SettingNode node : SETTING_NODES) {
            if (node instanceof UseUnsafeGroupNode useUnsafeNode) {
                ModernToggleButton parentBtn = new ModernToggleButton(
                        panelX + PANEL_PADDING, currentY, contentWidth, 24,
                        Component.literal("Use Unsafe").withStyle(ChatFormatting.RED),
                        Component.literal("サーバー/クライアントのエンティティ処理を書き換えます。予期しない動作やクラッシュを引き起こす可能性があります。").withStyle(ChatFormatting.RED),
                        this.useUnsafe,
                        (value) -> {
                            this.useUnsafe = value;
                            this.rebuildWidgets();
                        }
                );
                this.addRenderableWidget(parentBtn);
                currentY += ROW_HEIGHT;
                for (ItemSettingModule childModule : useUnsafeNode.childModules()) {
                    boolean isEnabled = featureStates.getOrDefault(childModule, false);
                    ModernToggleButton childBtn = new ModernToggleButton(
                            panelX + PANEL_PADDING + SUB_FEATURE_INDENT, currentY, contentWidth - SUB_FEATURE_INDENT, 24,
                            childModule.getDisplayName(),
                            childModule.getDescription(),
                            isEnabled,
                            (value) -> featureStates.put(childModule, value)
                    );
                    childBtn.active = this.useUnsafe;
                    this.addRenderableWidget(childBtn);
                    currentY += ROW_HEIGHT;
                }
            } else if (node instanceof ModuleGroupNode groupNode) {
                boolean isParentEnabled = featureStates.getOrDefault(groupNode.parentModule(), false);
                ModernToggleButton parentBtn = new ModernToggleButton(
                        panelX + PANEL_PADDING, currentY, contentWidth, 24,
                        groupNode.parentModule().getDisplayName(),
                        groupNode.parentModule().getDescription(),
                        isParentEnabled,
                        (value) -> {
                            featureStates.put(groupNode.parentModule(), value);
                            this.rebuildWidgets();
                        }
                );
                this.addRenderableWidget(parentBtn);
                currentY += ROW_HEIGHT;
                for (ItemSettingModule childModule : groupNode.childModules()) {
                    boolean isEnabled = featureStates.getOrDefault(childModule, false);
                    ModernToggleButton childBtn = new ModernToggleButton(
                            panelX + PANEL_PADDING + SUB_FEATURE_INDENT, currentY, contentWidth - SUB_FEATURE_INDENT, 24,
                            childModule.getDisplayName(),
                            childModule.getDescription(),
                            isEnabled,
                            (value) -> featureStates.put(childModule, value)
                    );
                    childBtn.active = isParentEnabled;
                    this.addRenderableWidget(childBtn);
                    currentY += ROW_HEIGHT;
                }
            } else if (node instanceof StandaloneNode standaloneNode) {
                boolean isEnabled = featureStates.getOrDefault(standaloneNode.module(), false);
                ModernToggleButton btn = new ModernToggleButton(
                        panelX + PANEL_PADDING, currentY, contentWidth, 24,
                        standaloneNode.module().getDisplayName(),
                        standaloneNode.module().getDescription(),
                        isEnabled,
                        (value) -> featureStates.put(standaloneNode.module(), value)
                );
                btn.active = true;
                this.addRenderableWidget(btn);
                currentY += ROW_HEIGHT;
            }
        }
        currentY += 10;
        this.addRenderableWidget(new ModernButton(
                panelX + PANEL_PADDING, currentY, contentWidth, 24,
                Component.literal("Save and Close"),
                (b) -> saveAndClose()
        ));
    }
    private void saveAndClose() {
        int mask = 0;
        for (ItemSettingModule module : ItemSettingModule.values()) {
            if (featureStates.getOrDefault(module, false)) {
                mask |= (1 << module.ordinal());
            }
        }
        NetworkHandler.sendUpdateSwordConfig(this.useUnsafe, mask);
        this.onClose();
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xCC000000);
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = 40;
        int totalRows = SETTING_NODES.stream().mapToInt(SettingNode::getRowCount).sum();
        int panelH = 35 + (totalRows * ROW_HEIGHT) + 10 + 24 + PANEL_PADDING;
        guiGraphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelH, 0xFF111111);
        guiGraphics.renderOutline(panelX, panelY, PANEL_WIDTH, panelH, 0xFF00A8A8);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 10, 0xFFD700);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    public sealed interface SettingNode permits StandaloneNode, ModuleGroupNode, UseUnsafeGroupNode {
        int getRowCount();
    }
    public record StandaloneNode(ItemSettingModule module) implements SettingNode {
        @Override public int getRowCount() { return 1; }
    }
    public record ModuleGroupNode(ItemSettingModule parentModule, List<ItemSettingModule> childModules) implements SettingNode {
        @Override public int getRowCount() { return 1 + childModules.size(); }
    }
    public record UseUnsafeGroupNode(List<ItemSettingModule> childModules) implements SettingNode {
        @Override public int getRowCount() { return 1 + childModules.size(); }
    }
    public static class ModernToggleButton extends AbstractWidget {
        private boolean isOn;
        private final Component description;
        private final Consumer<Boolean> onPress;
        public ModernToggleButton(int x, int y, int width, int height, Component title, Component description, boolean initialState, Consumer<Boolean> onPress) {
            super(x, y, width, height, title);
            this.description = description;
            this.isOn = initialState;
            this.onPress = onPress;
            this.setTooltip(Tooltip.create(description));
        }
        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int alpha = this.active ? 255 : 100;
            int bgColor = (alpha << 24) | 0x1A1A1A;
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            int borderColor = (this.active && this.isHovered) ? 0xFF00A8A8 : 0xFF444444;
            guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, borderColor);
            int textColor = this.active ? 0xEEEEEE : 0x888888;
            guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.getX() + 6, this.getY() + (this.height - 8) / 2, textColor);
            int toggleW = 40;
            int toggleH = this.height - 8;
            int toggleX = this.getX() + this.width - toggleW - 4;
            int toggleY = this.getY() + 4;
            int toggleBgColor = this.isOn ? 0xFF00AA00 : 0xFF333333;
            int finalToggleBg = (alpha << 24) | (toggleBgColor & 0x00FFFFFF);
            guiGraphics.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, finalToggleBg);
            guiGraphics.renderOutline(toggleX, toggleY, toggleW, toggleH, 0xFF666666);
            int knobSize = toggleH - 4;
            int knobX = this.isOn ? (toggleX + toggleW - 2 - knobSize) : (toggleX + 2);
            int knobY = toggleY + 2;
            guiGraphics.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, 0xFFFFFFFF);
            if (this.active && this.isHovered) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x22FFFFFF);
            }
        }
        @Override
        public void onClick(double mouseX, double mouseY) {
            if (this.active) {
                this.isOn = !this.isOn;
                this.onPress.accept(this.isOn);
            }
        }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
    public static class ModernButton extends AbstractWidget {
        private final Consumer<ModernButton> onPress;
        public ModernButton(int x, int y, int width, int height, Component message, Consumer<ModernButton> onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }
        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int color = this.active ? (this.isHovered ? 0xFF00A8A8 : 0xFF007A7A) : 0xFF333333;
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color);
            guiGraphics.renderOutline(this.getX(), this.getY(), this.width, this.height, 0xFF555555);
            int textColor = this.active ? 0xFFFFFF : 0x888888;
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
            if (this.active && this.isHovered) {
                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x22FFFFFF);
            }
        }
        @Override
        public void onClick(double mouseX, double mouseY) {
            if (this.active) {
                this.onPress.accept(this);
            }
        }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }
    }
}