package BetterPrisons.modid.mixin.client;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.chestsearch.ChestSearchFilterRule;
import BetterPrisons.modid.chestsearch.ChestSearchFilterState;
import BetterPrisons.modid.chestsearch.ChestSearchMatcher;
import BetterPrisons.modid.chestsearch.ChestSearchState;
import BetterPrisons.modid.chestsearch.ClueScrollOverlay;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds a simple item search bar and a no-code filter-rule sidebar to container
 * screens (chests, etc.). Matching slots are tinted with a highlight color.
 *
 * Ported from the AdvancedChestSearch reference mod, minus the JavaScript engine
 * and per-rule type/color settings.
 */
@Mixin(HandledScreen.class)
public abstract class ContainerSearchMixin extends Screen {
    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;

    @Unique private TextFieldWidget bp$searchField;

    protected ContainerSearchMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void bp$init(CallbackInfo ci) {
        if (!BetterPrisonsClient.config.chestSearchEnabled) return;
        bp$buildSearchBar();
        bp$buildSidebar();
    }

    @Unique
    private void bp$buildSearchBar() {
        int fieldW = 120;
        int fieldH = 16;
        int btnW = 36;
        int gap = 4;
        int totalW = fieldW + gap + btnW;
        int barX = this.x + (this.backgroundWidth - totalW) / 2;
        int barY = this.y + this.backgroundHeight + 4;

        bp$searchField = new TextFieldWidget(this.textRenderer, barX, barY, fieldW, fieldH, Text.literal("Search"));
        bp$searchField.setMaxLength(1024);
        bp$searchField.setText(ChestSearchState.query == null ? "" : ChestSearchState.query);
        bp$searchField.setChangedListener(s -> ChestSearchState.query = s);
        this.addDrawableChild(bp$searchField);

        ButtonWidget filterToggle = ButtonWidget.builder(
                Text.literal(ChestSearchFilterState.sidebarOpen ? "Filt." : "Filt+"),
                btn -> {
                    ChestSearchFilterState.sidebarOpen = !ChestSearchFilterState.sidebarOpen;
                    if (ChestSearchFilterState.sidebarOpen && ChestSearchFilterState.rules.isEmpty()) {
                        ChestSearchFilterState.addRule();
                    }
                    this.clearAndInit();
                }
        ).dimensions(barX + fieldW + gap, barY, btnW, fieldH).build();
        this.addDrawableChild(filterToggle);
    }

    @Unique
    private void bp$buildSidebar() {
        if (!ChestSearchFilterState.sidebarOpen) return;

        // Position to the right of the container background; fall back to the left
        // if there isn't enough room.
        int sidebarW = 140;
        int sx = this.x + this.backgroundWidth + 8;
        if (sx + sidebarW > this.width) {
            sx = this.x - sidebarW - 8;
        }
        int sy = Math.max(8, this.y - 75);

        int rowY = sy + 14; // leave room for the header (drawn in render TAIL)

        // Match mode toggle: OR (any) vs AND (all).
        ButtonWidget modeBtn = ButtonWidget.builder(
                Text.literal(ChestSearchFilterState.matchAll ? "Match: All" : "Match: Any"),
                btn -> {
                    ChestSearchFilterState.matchAll = !ChestSearchFilterState.matchAll;
                    btn.setMessage(Text.literal(ChestSearchFilterState.matchAll ? "Match: All" : "Match: Any"));
                }
        ).dimensions(sx, rowY, 140, 18).build();
        this.addDrawableChild(modeBtn);
        rowY += 22;

        // One block per rule: [value field], then [type] [color] [X]
        for (int i = 0; i < ChestSearchFilterState.rules.size(); i++) {
            final int idx = i;
            ChestSearchFilterRule rule = ChestSearchFilterState.rules.get(i);

            TextFieldWidget valField = new TextFieldWidget(
                    this.textRenderer, sx, rowY, 140, 16, Text.literal("name")
            );
            valField.setMaxLength(64);
            valField.setText(rule.value);
            valField.setChangedListener(s -> rule.value = s);
            this.addDrawableChild(valField);

            rowY += 18;

            ButtonWidget typeBtn = ButtonWidget.builder(
                    Text.literal(rule.type.label),
                    btn -> {
                        rule.type = rule.type.next();
                        btn.setMessage(Text.literal(rule.type.label));
                    }
            ).dimensions(sx, rowY, 60, 18).build();
            this.addDrawableChild(typeBtn);

            ButtonWidget colorBtn = ButtonWidget.builder(
                    Text.literal(ChestSearchFilterState.colorName(rule.color)),
                    btn -> {
                        rule.color = ChestSearchFilterState.nextColor(rule.color);
                        btn.setMessage(Text.literal(ChestSearchFilterState.colorName(rule.color)));
                    }
            ).dimensions(sx + 62, rowY, 60, 18).build();
            this.addDrawableChild(colorBtn);

            ButtonWidget delBtn = ButtonWidget.builder(
                    Text.literal("X"),
                    btn -> {
                        ChestSearchFilterState.removeRule(idx);
                        this.clearAndInit();
                    }
            ).dimensions(sx + 124, rowY, 16, 18).build();
            this.addDrawableChild(delBtn);

            rowY += 22;
        }

        // Add-rule button (hidden when at cap).
        if (ChestSearchFilterState.rules.size() < ChestSearchFilterState.MAX_RULES) {
            ButtonWidget addBtn = ButtonWidget.builder(
                    Text.literal("+ Add Rule"),
                    btn -> {
                        ChestSearchFilterState.addRule();
                        this.clearAndInit();
                    }
            ).dimensions(sx, rowY, 140, 18).build();
            this.addDrawableChild(addBtn);
        }
    }

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void bp$drawSlot(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) return;

        // Chest search color highlight
        if (BetterPrisonsClient.config.chestSearchEnabled) {
            int color = ChestSearchMatcher.matchColor(stack);
            if (color != ChestSearchMatcher.NO_MATCH) {
                context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
            }
        }

        // Clue Scroll Sorting — big step number overlay
        if (BetterPrisonsClient.config.clueScrollSortingEnabled) {
            ClueScrollOverlay.render(context, slot.x, slot.y, this.textRenderer, stack);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void bp$renderOverlays(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!BetterPrisonsClient.config.chestSearchEnabled) return;
        if (!ChestSearchFilterState.sidebarOpen) return;

        int sidebarW = 140;
        int sx = this.x + this.backgroundWidth + 8;
        if (sx + sidebarW > this.width) sx = this.x - sidebarW - 8;
        int sy = Math.max(8, this.y - 75);
        // Subtle backdrop so the widgets don't float on bare world/screen.
        context.fill(sx - 4, sy - 4, sx + sidebarW + 4,
                sy + 14 + 22 + ChestSearchFilterState.rules.size() * 40 + 22, 0x80000000);
        context.drawTextWithShadow(this.textRenderer,
                Text.literal("Filter Rules"), sx, sy, 0xFFFFFFFF);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void bp$clearFocusOnOutsideClick(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (!BetterPrisonsClient.config.chestSearchEnabled) return;
        // Clicking outside a focused text field should release it, so the inventory
        // key works again and HandledScreen can close the screen.
        if (this.getFocused() instanceof TextFieldWidget field
                && !field.isMouseOver(click.x(), click.y())) {
            field.setFocused(false);
            this.setFocused(null);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void bp$keyPressed(KeyInput keyInput, CallbackInfoReturnable<Boolean> cir) {
        if (!BetterPrisonsClient.config.chestSearchEnabled) return;
        // 256 = GLFW_KEY_ESCAPE — let escape close the screen normally.
        if (keyInput.key() == 256) return;
        // If ANY of our text fields are focused, dispatch via Screen and short-circuit
        // so HandledScreen's "close on inventory key" branch doesn't fire mid-typing.
        if (this.getFocused() instanceof TextFieldWidget) {
            super.keyPressed(keyInput);
            cir.setReturnValue(true);
        }
    }
}
