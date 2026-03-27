package BetterPrisons.modid.ui.custom.screens;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.ui.custom.containers.ColorPickerPopup;
import BetterPrisons.modid.ui.custom.widgets.ColorPickerWidget;
import BetterPrisons.modid.ui.custom.widgets.DropdownWidget;
import BetterPrisons.modid.waypoint.CustomWaypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Management screen for user-created waypoints.
 *
 * LIST MODE  — scrollable table of existing waypoints.
 * EDIT MODE  — form with name, coords, color picker, and preset swatch grid.
 */
public class WaypointsScreen extends Screen {

    private static final int PANEL_W      = 540;
    private static final int ROW_H        = 24;
    private static final int LIST_TOP     = 58;   // extra room for world selector row
    private static final int BOTTOM_BAR_H = 36;

    // Preset colors (Minecraft dye palette)
    private static final String[] PRESET_NAMES = {
        "White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime",
        "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue",
        "Brown", "Green", "Red", "Black"
    };
    private static final int[] PRESET_COLORS = {
        0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA, 0xFED83D, 0x80C71F,
        0xF38BAA, 0x474F52, 0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA,
        0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21
    };

    // Swatch grid constants
    private static final int SWATCH_SIZE = 14;
    private static final int SWATCH_GAP  = 3;
    private static final int SWATCH_COLS = 4;

    // SWATCH_GRID_H = 4 rows * 14 + 3 gaps * 3 = 65
    private static final int SWATCH_GRID_H = SWATCH_COLS * SWATCH_SIZE + (SWATCH_COLS - 1) * SWATCH_GAP;

    private static final int FORM_H = 293;

    // ----------------------------------------------------------------
    // State
    // ----------------------------------------------------------------

    private int     scrollOffset = 0;
    private int     maxScroll    = 0;

    private boolean editMode  = false;
    private int     editIndex = -1;

    private int pendingX, pendingY, pendingZ;

    // World selector (list mode)
    private String         viewedWorld;
    private DropdownWidget worldDropdown;

    // Edit form widgets
    private TextFieldWidget   nameField;
    private TextFieldWidget   xField, yField, zField;
    private TextFieldWidget   opacityField;
    private TextFieldWidget   onScreenScaleField, offScreenScaleField;
    private ColorPickerWidget colorPickerWidget;
    private ColorPickerPopup  activePopup;

    public WaypointsScreen() {
        super(Text.literal("Waypoints"));
        // Default to whichever world the player is currently in
        this.viewedWorld = BetterPrisonsClient.waypointManager.getCurrentWorld();
    }

    // ----------------------------------------------------------------
    // Init
    // ----------------------------------------------------------------

    @Override
    protected void init() {
        clearChildren();
        activePopup = null;
        if (editMode) {
            initEditForm();
        } else {
            initListButtons();
        }
    }

    private void initListButtons() {
        int panelX = (width - PANEL_W) / 2;
        int listH  = height - LIST_TOP - BOTTOM_BAR_H;
        int btnY   = height - BOTTOM_BAR_H + 8;

        addDrawableChild(ButtonWidget.builder(Text.literal("Add Waypoint"), btn -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                BlockPos pos = mc.player.getBlockPos();
                pendingX = pos.getX(); pendingY = pos.getY(); pendingZ = pos.getZ();
            } else {
                pendingX = 0; pendingY = 0; pendingZ = 0;
            }
            editIndex = -1;
            editMode  = true;
            init();
        }).dimensions(panelX, btnY, 130, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Delete World"), btn -> {
            String worldToDelete = viewedWorld;
            client.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        BetterPrisonsClient.waypointManager.removeWorld(worldToDelete);
                        List<String> remaining = BetterPrisonsClient.waypointManager.getWorlds();
                        String current = BetterPrisonsClient.waypointManager.getCurrentWorld();
                        if (remaining.contains(current)) {
                            viewedWorld = current;
                        } else if (!remaining.isEmpty()) {
                            viewedWorld = remaining.get(0);
                        } else {
                            viewedWorld = current;
                        }
                        scrollOffset = 0;
                    }
                    client.setScreen(WaypointsScreen.this);
                    init();
                },
                Text.literal("Delete World?"),
                Text.literal("Delete all waypoints for \"" + worldToDelete + "\"? This cannot be undone.")
            ));
        }).dimensions(panelX + PANEL_W - 196, btnY, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> close())
            .dimensions(panelX + PANEL_W - 80, btnY, 80, 20).build());

        // World selector dropdown — ensure current world is in the list
        List<String> worlds = BetterPrisonsClient.waypointManager.getWorlds();
        if (worlds.isEmpty()) worlds = Arrays.asList(viewedWorld);
        if (!worlds.contains(viewedWorld)) viewedWorld = worlds.get(0);
        int worldIndex = Math.max(0, worlds.indexOf(viewedWorld));
        worldDropdown = new DropdownWidget("World:", worlds, worldIndex);
        worldDropdown.setPosition(panelX, LIST_TOP - 40);
        worldDropdown.setOnChange(w -> {
            viewedWorld = w;
            List<CustomWaypoint> wps2 = BetterPrisonsClient.waypointManager.getAll(viewedWorld);
            int lh = height - LIST_TOP - BOTTOM_BAR_H;
            maxScroll    = Math.max(0, wps2.size() * ROW_H - lh);
            scrollOffset = 0;
        });

        List<CustomWaypoint> wps = BetterPrisonsClient.waypointManager.getAll(viewedWorld);
        maxScroll    = Math.max(0, wps.size() * ROW_H - listH);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    private void initEditForm() {
        int formW = 300;
        int formX = (width - formW) / 2;
        int formY = (height - FORM_H) / 2;

        nameField = new TextFieldWidget(textRenderer, formX, formY + 26, formW, 18, Text.literal("Name"));
        nameField.setMaxLength(32);
        addDrawableChild(nameField);

        xField = new TextFieldWidget(textRenderer, formX,       formY + 68, 90, 18, Text.literal("X"));
        yField = new TextFieldWidget(textRenderer, formX + 95,  formY + 68, 90, 18, Text.literal("Y"));
        zField = new TextFieldWidget(textRenderer, formX + 190, formY + 68, 90, 18, Text.literal("Z"));
        xField.setMaxLength(8); yField.setMaxLength(8); zField.setMaxLength(8);
        addDrawableChild(xField); addDrawableChild(yField); addDrawableChild(zField);

        // Determine initial color
        int initialColor = 0xFFFFFF;
        if (editIndex >= 0) {
            List<CustomWaypoint> wps = BetterPrisonsClient.waypointManager.getAll(viewedWorld);
            if (editIndex < wps.size()) initialColor = wps.get(editIndex).color;
        }

        colorPickerWidget = new ColorPickerWidget("Color", initialColor);
        colorPickerWidget.setPosition(formX, formY + 96);

        colorPickerWidget.setOnPopupOpen(popup -> {
            Consumer<Integer> origConfirm = popup.getOnConfirm();
            Runnable origCancel          = popup.getOnCancel();
            popup.setOnConfirm(color -> { if (origConfirm != null) origConfirm.accept(color); activePopup = null; });
            popup.setOnCancel(() ->       { if (origCancel  != null) origCancel.run();           activePopup = null; });
            activePopup = popup;
        });

        opacityField = new TextFieldWidget(textRenderer, formX + 115, formY + 200, 45, 18, Text.literal("Opacity"));
        opacityField.setMaxLength(3);
        addDrawableChild(opacityField);

        onScreenScaleField = new TextFieldWidget(textRenderer, formX + 115, formY + 221, 45, 18, Text.literal("OnScale"));
        onScreenScaleField.setMaxLength(5);
        addDrawableChild(onScreenScaleField);

        offScreenScaleField = new TextFieldWidget(textRenderer, formX + 115, formY + 242, 45, 18, Text.literal("OffScale"));
        offScreenScaleField.setMaxLength(5);
        addDrawableChild(offScreenScaleField);

        // Pre-fill text fields
        if (editIndex >= 0) {
            List<CustomWaypoint> wps = BetterPrisonsClient.waypointManager.getAll(viewedWorld);
            if (editIndex < wps.size()) {
                CustomWaypoint wp = wps.get(editIndex);
                nameField.setText(wp.name);
                xField.setText(String.valueOf(wp.x));
                yField.setText(String.valueOf(wp.y));
                zField.setText(String.valueOf(wp.z));
                opacityField.setText(String.valueOf(wp.opacity));
                onScreenScaleField.setText(String.valueOf(wp.onScreenScale));
                offScreenScaleField.setText(String.valueOf(wp.offScreenScale));
            }
        } else {
            if (pendingX != 0 || pendingY != 0 || pendingZ != 0) {
                xField.setText(String.valueOf(pendingX));
                yField.setText(String.valueOf(pendingY));
                zField.setText(String.valueOf(pendingZ));
            }
            opacityField.setText(String.valueOf(BetterPrisonsClient.config.customWaypointDefaultOpacity));
            onScreenScaleField.setText(String.valueOf(BetterPrisonsClient.config.customWaypointOnScreenScale));
            offScreenScaleField.setText(String.valueOf(BetterPrisonsClient.config.customWaypointOffScreenScale));
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), btn -> saveEdit())
            .dimensions(formX, formY + 263, 145, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), btn -> cancelEdit())
            .dimensions(formX + 150, formY + 263, 150, 20).build());
    }

    // ----------------------------------------------------------------
    // Edit helpers
    // ----------------------------------------------------------------

    private void saveEdit() {
        if (nameField == null) return;
        String name = nameField.getText().trim();
        if (name.isEmpty()) return;

        int x, y, z;
        try {
            x = Integer.parseInt(xField.getText().trim());
            y = Integer.parseInt(yField.getText().trim());
            z = Integer.parseInt(zField.getText().trim());
        } catch (NumberFormatException e) { return; }

        int color = colorPickerWidget != null ? (colorPickerWidget.getColor() & 0xFFFFFF) : 0xFFFFFF;

        int opacity = 255;
        if (opacityField != null) {
            try { opacity = Math.max(0, Math.min(255, Integer.parseInt(opacityField.getText().trim()))); }
            catch (NumberFormatException ignored) {}
        }

        float onScale = 1.0f;
        if (onScreenScaleField != null) {
            try { onScale = Math.max(0.1f, Math.min(10f, Float.parseFloat(onScreenScaleField.getText().trim()))); }
            catch (NumberFormatException ignored) {}
        }

        float offScale = 1.0f;
        if (offScreenScaleField != null) {
            try { offScale = Math.max(0.1f, Math.min(10f, Float.parseFloat(offScreenScaleField.getText().trim()))); }
            catch (NumberFormatException ignored) {}
        }

        CustomWaypoint wp = new CustomWaypoint(name, x, y, z, color);
        wp.opacity        = opacity;
        wp.onScreenScale  = onScale;
        wp.offScreenScale = offScale;
        if (editIndex >= 0) {
            List<CustomWaypoint> wps = BetterPrisonsClient.waypointManager.getAll(viewedWorld);
            if (editIndex < wps.size()) wp.enabled = wps.get(editIndex).enabled;
            BetterPrisonsClient.waypointManager.update(editIndex, wp, viewedWorld);
        } else {
            BetterPrisonsClient.waypointManager.add(wp);
        }

        cancelEdit();
    }

    private void cancelEdit() {
        activePopup         = null;
        colorPickerWidget   = null;
        opacityField        = null;
        onScreenScaleField  = null;
        offScreenScaleField = null;
        worldDropdown       = null;
        editMode          = false;
        editIndex         = -1;
        init();
    }

    // ----------------------------------------------------------------
    // Render
    // ----------------------------------------------------------------

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xC0101010);

        // Title drawn first — form backdrop starts at formY-12 which is always below y=16
        String screenTitle = editMode
            ? (editIndex >= 0 ? "Edit Waypoint" : "New Waypoint")
            : "Waypoints";
        ctx.drawTextWithShadow(textRenderer, Text.literal(screenTitle),
            (width - textRenderer.getWidth(screenTitle)) / 2, 16, 0xFFFFFFFF);

        int panelX = (width - PANEL_W) / 2;
        ctx.fill(panelX - 4, LIST_TOP - 6,
                 panelX + PANEL_W + 4, height - BOTTOM_BAR_H + 4, 0x90000000);

        if (!editMode) {
            renderList(ctx, mouseX, mouseY, panelX);
            // World dropdown expanded list rendered above list content, before MC widgets
            if (worldDropdown != null) {
                worldDropdown.renderExpandedList(ctx, mouseX, mouseY, delta);
            }
        } else {
            renderEditForm(ctx, mouseX, mouseY, delta);
        }

        // Render MC widgets (buttons, text fields)
        super.render(ctx, mouseX, mouseY, delta);

        // Render color picker popup on top of everything
        if (activePopup != null) {
            activePopup.render(ctx, mouseX, mouseY, delta);
        }
    }

    private void renderList(DrawContext ctx, int mouseX, int mouseY, int panelX) {
        int listH = height - LIST_TOP - BOTTOM_BAR_H;

        // World selector dropdown (rendered before scissor so it can expand freely)
        if (worldDropdown != null) {
            worldDropdown.setPosition(panelX, LIST_TOP - 40);
            worldDropdown.render(ctx, mouseX, mouseY, 0);
        }

        ctx.fill(panelX, LIST_TOP - 8, panelX + PANEL_W, LIST_TOP - 3, 0x40FFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Name"),   panelX + 24,  LIST_TOP - 17, 0xFF888888);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Coords"), panelX + 200, LIST_TOP - 17, 0xFF888888);

        ctx.enableScissor(panelX, LIST_TOP, panelX + PANEL_W, LIST_TOP + listH);

        List<CustomWaypoint> wps = BetterPrisonsClient.waypointManager.getAll(viewedWorld);
        for (int i = 0; i < wps.size(); i++) {
            CustomWaypoint wp  = wps.get(i);
            int            rowY = LIST_TOP + i * ROW_H - scrollOffset;
            if (rowY + ROW_H < LIST_TOP || rowY > LIST_TOP + listH) continue;

            if (i % 2 == 0) ctx.fill(panelX, rowY, panelX + PANEL_W, rowY + ROW_H, 0x15FFFFFF);
            if (mouseY >= rowY && mouseY < rowY + ROW_H
                    && mouseX >= panelX && mouseX <= panelX + PANEL_W) {
                ctx.fill(panelX, rowY, panelX + PANEL_W, rowY + ROW_H, 0x1AFFFFFF);
            }

            ctx.fill(panelX + 5, rowY + 5, panelX + 19, rowY + 19, 0xFF000000 | (wp.color & 0xFFFFFF));

            int nameColor = wp.enabled ? 0xFFFFFFFF : 0xFF777777;
            ctx.drawTextWithShadow(textRenderer, Text.literal(wp.name), panelX + 24, rowY + 8, nameColor);

            String coords = wp.x + ", " + wp.y + ", " + wp.z;
            ctx.drawTextWithShadow(textRenderer, Text.literal(coords), panelX + 200, rowY + 8, 0xFFAAAAAA);

            int rx = panelX + PANEL_W;
            ctx.drawTextWithShadow(textRenderer, Text.literal(wp.enabled ? "§aON " : "§cOFF"), rx - 102, rowY + 8, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("§7Edit"), rx - 60, rowY + 8, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal("§c✕"),    rx - 16, rowY + 8, 0xFFFFFFFF);
        }

        ctx.disableScissor();

        if (maxScroll > 0 && !wps.isEmpty()) {
            int sbH    = listH - 4;
            int thumbH = Math.max(20, (int)(sbH * (float)listH / (wps.size() * ROW_H)));
            int thumbY = LIST_TOP + 2 + (int)((sbH - thumbH) * (float)scrollOffset / maxScroll);
            ctx.fill(panelX + PANEL_W + 4, LIST_TOP + 2, panelX + PANEL_W + 8, LIST_TOP + 2 + sbH, 0x30FFFFFF);
            ctx.fill(panelX + PANEL_W + 4, thumbY, panelX + PANEL_W + 8, thumbY + thumbH, 0x90FFFFFF);
        }

        if (wps.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, Text.literal("No waypoints for this world."),
                (width - textRenderer.getWidth("No waypoints for this world.")) / 2,
                LIST_TOP + listH / 2 - 10, 0xFF666666);
            ctx.drawTextWithShadow(textRenderer, Text.literal("Use 'Add Waypoint' below to add one."),
                (width - textRenderer.getWidth("Use 'Add Waypoint' below to add one.")) / 2,
                LIST_TOP + listH / 2 + 4, 0xFF555555);
        }
    }

    private void renderEditForm(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int formW = 300;
        int formX = (width - formW) / 2;
        int formY = (height - FORM_H) / 2;

        int backdropBottom = formY + FORM_H - 10;

        // Backdrop
        ctx.fill(formX - 12, formY - 12, formX + formW + 12, backdropBottom, 0xD0101010);
        ctx.fill(formX - 13, formY - 13, formX + formW + 13, formY - 12,         0xFF3A3A3A);
        ctx.fill(formX - 13, backdropBottom, formX + formW + 13, backdropBottom + 1, 0xFF3A3A3A);
        ctx.fill(formX - 13, formY - 12, formX - 12, backdropBottom,                 0xFF3A3A3A);
        ctx.fill(formX + formW + 12, formY - 12, formX + formW + 13, backdropBottom, 0xFF3A3A3A);

        ctx.drawTextWithShadow(textRenderer, Text.literal("Name:"),             formX,       formY + 14,  0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("X:"),               formX,       formY + 56,  0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Y:"),               formX + 95,  formY + 56,  0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Z:"),               formX + 190, formY + 56,  0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Opacity (0-255):"), formX,       formY + 205, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Scale on-screen:"), formX,       formY + 226, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, Text.literal("Scale off-screen:"),formX,       formY + 247, 0xFFAAAAAA);

        // Color picker
        if (colorPickerWidget != null) {
            colorPickerWidget.setPosition(formX, formY + 96);
            colorPickerWidget.render(ctx, mouseX, mouseY, delta);
        }

        // Preset swatch grid
        renderColorSwatches(ctx, mouseX, mouseY, formX, formY + 120);
    }

    /**
     * Renders a 4×4 grid of colored swatches. Hovering a swatch highlights it and
     * shows the color name to the right of the grid.
     */
    private void renderColorSwatches(DrawContext ctx, int mouseX, int mouseY, int startX, int startY) {
        ctx.drawTextWithShadow(textRenderer, Text.literal("Preset:"), startX, startY, 0xFFAAAAAA);

        int gridY = startY + 12;
        int hoveredIndex = -1;

        for (int i = 0; i < PRESET_COLORS.length; i++) {
            int col = i % SWATCH_COLS;
            int row = i / SWATCH_COLS;
            int sx = startX + col * (SWATCH_SIZE + SWATCH_GAP);
            int sy = gridY  + row * (SWATCH_SIZE + SWATCH_GAP);

            boolean hovered = mouseX >= sx && mouseX < sx + SWATCH_SIZE
                           && mouseY >= sy && mouseY < sy + SWATCH_SIZE;
            if (hovered) hoveredIndex = i;

            // Fill
            ctx.fill(sx, sy, sx + SWATCH_SIZE, sy + SWATCH_SIZE, 0xFF000000 | (PRESET_COLORS[i] & 0xFFFFFF));

            // Border: bright white when hovered, subtle dark otherwise
            int border = hovered ? 0xFFFFFFFF : 0x80222222;
            ctx.fill(sx - 1, sy - 1, sx + SWATCH_SIZE + 1, sy,                  border); // top
            ctx.fill(sx - 1, sy + SWATCH_SIZE, sx + SWATCH_SIZE + 1, sy + SWATCH_SIZE + 1, border); // bottom
            ctx.fill(sx - 1, sy, sx,                  sy + SWATCH_SIZE, border); // left
            ctx.fill(sx + SWATCH_SIZE, sy, sx + SWATCH_SIZE + 1, sy + SWATCH_SIZE, border); // right
        }

        // Show hovered color name to the right of the grid, vertically centered
        if (hoveredIndex >= 0) {
            int gridW     = SWATCH_COLS * (SWATCH_SIZE + SWATCH_GAP) - SWATCH_GAP;
            int nameX     = startX + gridW + 8;
            int nameY     = gridY + SWATCH_GRID_H / 2 - 4;
            int nameColor = 0xFF000000 | (PRESET_COLORS[hoveredIndex] & 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, Text.literal(PRESET_NAMES[hoveredIndex]), nameX, nameY, nameColor);
        }
    }

    /** Returns the preset index at (mouseX, mouseY) within the swatch grid, or -1. */
    private int getSwatchAt(double mouseX, double mouseY, int startX, int startY) {
        int gridY = startY + 12;
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            int col = i % SWATCH_COLS;
            int row = i / SWATCH_COLS;
            int sx = startX + col * (SWATCH_SIZE + SWATCH_GAP);
            int sy = gridY  + row * (SWATCH_SIZE + SWATCH_GAP);
            if (mouseX >= sx && mouseX < sx + SWATCH_SIZE
             && mouseY >= sy && mouseY < sy + SWATCH_SIZE) {
                return i;
            }
        }
        return -1;
    }

    // ----------------------------------------------------------------
    // Input — all events forwarded to popup first, then to colorPickerWidget
    // ----------------------------------------------------------------

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mx = click.x(), my = click.y();
        int    btn = click.button();

        if (activePopup != null) {
            activePopup.mouseClicked(mx, my, btn);
            return true;
        }

        if (!editMode && worldDropdown != null) {
            if (worldDropdown.mouseClicked(mx, my, btn)) return true;
        }

        if (editMode && colorPickerWidget != null) {
            if (colorPickerWidget.mouseClicked(mx, my, btn)) return true;
        }

        // Preset swatch click (edit mode only)
        if (editMode && colorPickerWidget != null) {
            int formX = (width - 300) / 2;
            int formY = (height - FORM_H) / 2;
            int idx = getSwatchAt(mx, my, formX, formY + 120);
            if (idx >= 0) {
                colorPickerWidget.setColor(PRESET_COLORS[idx]);
                return true;
            }
        }

        if (!editMode) {
            int panelX = (width - PANEL_W) / 2;
            int listH  = height - LIST_TOP - BOTTOM_BAR_H;

            if (mx >= panelX && mx <= panelX + PANEL_W
                    && my >= LIST_TOP && my < LIST_TOP + listH) {
                int i   = (int)((my + scrollOffset - LIST_TOP) / ROW_H);
                List<CustomWaypoint> wps = BetterPrisonsClient.waypointManager.getAll(viewedWorld);
                if (i >= 0 && i < wps.size()) {
                    int rx = panelX + PANEL_W;
                    if (mx >= rx - 20 && mx <= rx) {
                        BetterPrisonsClient.waypointManager.remove(i, viewedWorld); init(); return true;
                    }
                    if (mx >= rx - 65 && mx < rx - 20) {
                        editIndex = i; editMode = true; init(); return true;
                    }
                    if (mx >= rx - 110 && mx < rx - 65) {
                        wps.get(i).enabled = !wps.get(i).enabled;
                        BetterPrisonsClient.waypointManager.save(); return true;
                    }
                }
            }
        }

        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (activePopup != null) {
            activePopup.mouseReleased(click.x(), click.y(), click.button());
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (activePopup != null) {
            activePopup.mouseDragged(click.x(), click.y(), click.button(), deltaX, deltaY);
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hScroll, double vScroll) {
        if (activePopup != null) {
            activePopup.mouseScrolled(mouseX, mouseY, hScroll, vScroll);
            return true;
        }
        if (!editMode) {
            if (worldDropdown != null && worldDropdown.mouseScrolled(mouseX, mouseY, hScroll, vScroll)) return true;
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(vScroll * ROW_H)));
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (activePopup != null) {
            activePopup.keyPressed(keyInput.key(), keyInput.scancode(), keyInput.modifiers());
            return true;
        }
        if (editMode && keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
            cancelEdit();
            return true;
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean charTyped(CharInput charInput) {
        if (activePopup != null) {
            activePopup.charTyped((char) charInput.codepoint(), charInput.modifiers());
            return true;
        }
        return super.charTyped(charInput);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !editMode;
    }
}
