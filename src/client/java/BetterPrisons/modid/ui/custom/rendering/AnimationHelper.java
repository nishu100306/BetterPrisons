package BetterPrisons.modid.ui.custom.rendering;

/**
 * Helper class for smooth animations and transitions.
 */
public class AnimationHelper {

    /**
     * Linear interpolation between two values.
     */
    public static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    /**
     * Ease-out cubic interpolation.
     * Starts fast and slows down at the end.
     */
    public static float easeOutCubic(float progress) {
        return 1 - (float) Math.pow(1 - progress, 3);
    }

    /**
     * Ease-in cubic interpolation.
     * Starts slow and speeds up at the end.
     */
    public static float easeInCubic(float progress) {
        return (float) Math.pow(progress, 3);
    }

    /**
     * Ease-in-out cubic interpolation.
     * Slow at both ends, fast in the middle.
     */
    public static float easeInOutCubic(float progress) {
        return progress < 0.5
                ? 4 * (float) Math.pow(progress, 3)
                : 1 - (float) Math.pow(-2 * progress + 2, 3) / 2;
    }

    /**
     * Ease-out quad interpolation.
     */
    public static float easeOutQuad(float progress) {
        return 1 - (1 - progress) * (1 - progress);
    }

    /**
     * Ease-in quad interpolation.
     */
    public static float easeInQuad(float progress) {
        return progress * progress;
    }

    /**
     * Clamps a value between min and max.
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamps a value between 0 and 1.
     */
    public static float clamp01(float value) {
        return clamp(value, 0, 1);
    }

    /**
     * Simple animation state tracker.
     */
    public static class Animation {
        private float current;
        private float target;
        private float speed;

        public Animation(float initial, float speed) {
            this.current = initial;
            this.target = initial;
            this.speed = speed;
        }

        public void setTarget(float target) {
            this.target = target;
        }

        public void update(float delta) {
            float diff = target - current;
            float change = diff * speed * delta;

            if (Math.abs(diff) < 0.001f) {
                current = target;
            } else {
                current += change;
            }
        }

        public float getCurrent() {
            return current;
        }

        public boolean isComplete() {
            return Math.abs(target - current) < 0.001f;
        }

        public void setCurrent(float value) {
            this.current = value;
        }
    }
}
