package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Simple text label widget for displaying dynamic text in the UI framework.
 */
public class LabelWidget extends Component {
    private String text;
    private int color;

    public LabelWidget(String text, int color) {
        this.text = text;
        this.color = color;
        this.height = 14;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawText(client.textRenderer, text, x, y + 3, color, false);
        this.width = client.textRenderer.getWidth(text);
    }
}
