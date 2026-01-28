package BetterPrisons.modid.ui.custom.rendering;

/**
 * Utility class for color operations and conversions.
 */
public class ColorUtils {

    /**
     * Converts HSV to RGB color.
     * @param hue Hue (0-360)
     * @param saturation Saturation (0-1)
     * @param value Value/Brightness (0-1)
     * @return RGB color as integer (0xAARRGGBB)
     */
    public static int hsvToRgb(float hue, float saturation, float value) {
        int h = (int) (hue / 60);
        float f = hue / 60 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        float r, g, b;
        switch (h % 6) {
            case 0:
                r = value;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = value;
                b = p;
                break;
            case 2:
                r = p;
                g = value;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = value;
                break;
            case 4:
                r = t;
                g = p;
                b = value;
                break;
            default:
                r = value;
                g = p;
                b = q;
                break;
        }

        int ri = (int) (r * 255);
        int gi = (int) (g * 255);
        int bi = (int) (b * 255);

        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }

    /**
     * Converts RGB to HSV.
     * @param rgb RGB color as integer (0xAARRGGBB)
     * @return float array {hue (0-360), saturation (0-1), value (0-1)}
     */
    public static float[] rgbToHsv(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float hue = 0;
        if (delta > 0) {
            if (max == rf) {
                hue = 60 * (((gf - bf) / delta) % 6);
            } else if (max == gf) {
                hue = 60 * (((bf - rf) / delta) + 2);
            } else {
                hue = 60 * (((rf - gf) / delta) + 4);
            }
        }
        if (hue < 0) {
            hue += 360;
        }

        float saturation = max == 0 ? 0 : delta / max;
        float value = max;

        return new float[]{hue, saturation, value};
    }

    /**
     * Parses a hex color string to RGB integer.
     * Supports formats: "RRGGBB", "#RRGGBB", "AARRGGBB", "#AARRGGBB"
     */
    public static int parseHex(String hex) {
        hex = hex.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        try {
            if (hex.length() == 6) {
                return 0xFF000000 | Integer.parseInt(hex, 16);
            } else if (hex.length() == 8) {
                return (int) Long.parseLong(hex, 16);
            }
        } catch (NumberFormatException e) {
            // Return black on error
            return 0xFF000000;
        }

        return 0xFF000000;
    }

    /**
     * Converts RGB integer to hex string.
     * @param includeAlpha Whether to include alpha component
     */
    public static String toHex(int rgb, boolean includeAlpha) {
        if (includeAlpha) {
            return String.format("%08X", rgb);
        } else {
            return String.format("%06X", rgb & 0xFFFFFF);
        }
    }

    /**
     * Creates an ARGB color from components.
     */
    public static int argb(int alpha, int red, int green, int blue) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }

    /**
     * Creates an RGB color from components (full alpha).
     */
    public static int rgb(int red, int green, int blue) {
        return argb(255, red, green, blue);
    }
}
