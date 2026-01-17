package BetterPrisons.modid.misc;

import BetterPrisons.modid.BetterPrisonsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import java.util.HashMap;
import java.util.Map;

public class EasyView {
    public static class TextWithColor {
        public final String text;
        public final int color;

        public TextWithColor(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }
    int count = 0;
    public boolean enabled = true;
    private Map<Integer, String> slotTexts = new HashMap<>();

    public void tick(MinecraftClient client) {
        count++;
        if (!enabled) return;
        if (client.player == null) return;

        if (count % 5 == 0) {
            rescan(client.player.getInventory());
        }

    }

    public void rescan(net.minecraft.entity.player.PlayerInventory inv) {
        // Note: This method is no longer needed since we process items on-the-fly during rendering
        // Keeping it for potential future use
    }

    public boolean isEnergyItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!BetterPrisonsClient.config.easyViewEnergyEnabled) return false;
        try {
            String name = stack.getName().getString();
            return name.endsWith(" Cosmic Energy");
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    public boolean isMoneyNote(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!BetterPrisonsClient.config.easyViewMoneyEnabled) return false;
        try {
            String name = stack.getName().getString();
            return name.startsWith("$");
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    public boolean isGangPointNote(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!BetterPrisonsClient.config.easyViewGangPointsEnabled) return false;
        try {
            String name = stack.getName().getString();
            return name.toUpperCase().endsWith(" GANG POINTS") || name.endsWith(" Gang Points");
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    public boolean isBlackScroll(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!BetterPrisonsClient.config.easyViewBlackScrollEnabled) return false;
        try {
            String name = stack.getName().getString();
            return name.startsWith("Black Scroll (");
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    public boolean isChargeOrb(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!BetterPrisonsClient.config.easyViewChargeOrbEnabled) return false;
        try {
            String name = stack.getName().getString();
            return name.endsWith("% Charge Orb");
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    public boolean isArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!BetterPrisonsClient.config.easyViewArmorEnabled) return false;
        try {
            String itemName = stack.getItem().toString().toLowerCase();
            // Check if it's an armor piece (helmet, chestplate, leggings, boots)
            return itemName.contains("helmet") || itemName.contains("chestplate") ||
                   itemName.contains("leggings") || itemName.contains("boots");
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    public boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!BetterPrisonsClient.config.easyViewWeaponsEnabled) return false;
        try {
            String itemName = stack.getItem().toString().toLowerCase();
            // Check if it's a sword or axe (but not pickaxe)
            return itemName.contains("sword") || (itemName.contains("axe") && !itemName.contains("pickaxe"));
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    public boolean isPickaxe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!BetterPrisonsClient.config.easyViewPickaxesEnabled) return false;
        try {
            String itemName = stack.getItem().toString().toLowerCase();
            return itemName.contains("pickaxe");
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Extract trailing number from item display name
     * For example: "Diamond Pickaxe 15" -> "15"
     */
    private String extractTrailingNumber(String name) {
        try {
            // Split by spaces and get the last part
            String[] parts = name.trim().split("\\s+");
            if (parts.length > 0) {
                String lastPart = parts[parts.length - 1];
                // Check if it's a number
                if (lastPart.matches("\\d+")) {
                    return lastPart;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    public void drawText(int slot, String text) {
        slotTexts.put(slot, text);
    }

    public void processStack(int slot, ItemStack stack) {
        try {
            String name = stack.getName().getString();

            if (isEnergyItem(stack)) {
                String nrg = name.replace(" Cosmic Energy", "");
                nrg = nrg.replaceAll(",", "");
                long nrgValue = Long.parseLong(nrg);
                String formatted = formatCompact(nrgValue);
                drawText(slot, formatted);
            }
            if (isMoneyNote(stack)) {
                String money = name.replace("$", "");
                money = money.replaceAll(",", "");
                money = money.split("\\.")[0];
                long moneyValue = Long.parseLong(money);
                String formatted = formatCompact(moneyValue);
                drawText(slot, formatted);
            }
            if (isGangPointNote(stack)) {
                String points = name.replace(" GANG POINTS", "").replace(" Gang Points", "");
                points = points.replaceAll(",", "");
                long pointsValue = Long.parseLong(points);
                String formatted = formatCompact(pointsValue);
                drawText(slot, formatted);
            }
            if (isBlackScroll(stack)) {
                String percent = name.replace("Black Scroll (", "").replace("%)", "");
                drawText(slot, percent + "%");
            }
            if (isChargeOrb(stack)) {
                String percent = name.replace("% Charge Orb", "");
                drawText(slot, percent + "%");
            }
            if (isArmor(stack)) {
                String number = extractTrailingNumber(name);
                if (number != null) {
                    drawText(slot, number);
                }
            }
            if (isWeapon(stack)) {
                String number = extractTrailingNumber(name);
                if (number != null) {
                    drawText(slot, number);
                }
            }
            if (isPickaxe(stack)) {
                String number = extractTrailingNumber(name);
                if (number != null) {
                    drawText(slot, number);
                }
            }
        } catch (Exception e) {
            // Silently ignore parsing errors
        }
    }

    private String formatCompact(long num) {
        if (num >= 1_000_000) {
            return String.format("%.1fM", num / 1_000_000.0);
        } else if (num >= 1_000) {
            return String.format("%.1fK", num / 1_000.0);
        }
        return String.valueOf(num);
    }

    public String getSlotText(int slotIndex) {
        return slotTexts.get(slotIndex);
    }

    public String processStackForText(ItemStack stack) {
        TextWithColor result = processStackForTextWithColor(stack);
        return result != null ? result.text : null;
    }

    public TextWithColor processStackForTextWithColor(ItemStack stack) {
        if (stack.isEmpty()) return null;

        try {
            String name = stack.getName().getString();

            if (isEnergyItem(stack)) {
                String nrg = name.replace(" Cosmic Energy", "");
                nrg = nrg.replaceAll(",", "");
                long nrgValue = Long.parseLong(nrg);
                return new TextWithColor(formatCompact(nrgValue), 0xFF000000 | BetterPrisonsClient.config.easyViewEnergyColor);
            }
            if (isMoneyNote(stack)) {
                String money = name.replace("$", "");
                money = money.replaceAll(",", "");
                money = money.split("\\.")[0];
                long moneyValue = Long.parseLong(money);
                return new TextWithColor(formatCompact(moneyValue), 0xFF000000 | BetterPrisonsClient.config.easyViewMoneyColor);
            }
            if (isGangPointNote(stack)) {
                String points = name.replace(" GANG POINTS", "").replace(" Gang Points", "");
                points = points.replaceAll(",", "");
                long pointsValue = Long.parseLong(points);
                return new TextWithColor(formatCompact(pointsValue), 0xFF000000 | BetterPrisonsClient.config.easyViewGangPointsColor);
            }
            if (isBlackScroll(stack)) {
                String percent = name.replace("Black Scroll (", "").replace("%)", "");
                return new TextWithColor(percent + "%", 0xFF000000 | BetterPrisonsClient.config.easyViewBlackScrollColor);
            }
            if (isChargeOrb(stack)) {
                String percent = name.replace("% Charge Orb", "");
                return new TextWithColor(percent + "%", 0xFF000000 | BetterPrisonsClient.config.easyViewChargeOrbColor);
            }
            if (isArmor(stack)) {
                String number = extractTrailingNumber(name);
                if (number != null) {
                    return new TextWithColor(number, 0xFF000000 | BetterPrisonsClient.config.easyViewArmorColor);
                }
            }
            if (isWeapon(stack)) {
                String number = extractTrailingNumber(name);
                if (number != null) {
                    return new TextWithColor(number, 0xFF000000 | BetterPrisonsClient.config.easyViewWeaponsColor);
                }
            }
            if (isPickaxe(stack)) {
                String number = extractTrailingNumber(name);
                if (number != null) {
                    return new TextWithColor(number, 0xFF000000 | BetterPrisonsClient.config.easyViewPickaxesColor);
                }
            }
        } catch (Exception e) {
            // Silently ignore parsing errors
        }
        return null;
    }

    public void renderSlotOverlay(DrawContext context, Slot slot) {
        if (!enabled) return;
        if (!slot.hasStack()) return;

        // Use slot.id (inventory index) instead of slot.getIndex() (screen handler index)
        int slotIndex = slot.id;
        String text = slotTexts.get(slotIndex);
        //BetterPrisonsClient.LOGGER.info("Rendering slot {} text: {}", slotIndex, text);
        if (text != null && !text.isEmpty()) {
            int x = slot.x;
            int y = slot.y;
            if (MinecraftClient.getInstance().currentScreen instanceof net.minecraft.client.gui.screen.ingame.InventoryScreen) {
                x += (MinecraftClient.getInstance().currentScreen.width - 176) / 2;
                y += (MinecraftClient.getInstance().currentScreen.height - 166) / 2;
            }

            // Draw text at top-left corner of the slot
            // Use smaller scale and white color with shadow for visibility

            context.getMatrices().pushMatrix();
            context.getMatrices().translate((x + 1), (y + 1)); // Render above items
            context.getMatrices().scale(0.5f, 0.5f);


            // Draw at slot position (scaled coordinates)
            context.drawText(
                MinecraftClient.getInstance().textRenderer,
                text, 0, 0,
                0xFFFFFFFF,
                true // With shadow
            );

            context.getMatrices().popMatrix();
        }
    }
}
