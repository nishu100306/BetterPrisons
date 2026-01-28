package BetterPrisons.modid.ui.custom.binding;

import BetterPrisons.modid.Config;

import java.lang.reflect.Field;

/**
 * Reflection-based implementation of ConfigBinding.
 * Uses reflection to access and modify Config fields.
 */
public class FieldBinding<T> implements ConfigBinding<T> {
    private final Config config;
    private final Field field;
    private final T defaultValue;

    public FieldBinding(Config config, String fieldName, T defaultValue) {
        this.config = config;
        this.defaultValue = defaultValue;

        try {
            this.field = Config.class.getField(fieldName);
            this.field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Config field not found: " + fieldName, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getValue() {
        try {
            return (T) field.get(config);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get config field value", e);
        }
    }

    @Override
    public void setValue(T value) {
        try {
            field.set(config, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set config field value", e);
        }
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }
}
