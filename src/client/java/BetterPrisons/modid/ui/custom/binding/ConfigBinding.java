package BetterPrisons.modid.ui.custom.binding;

/**
 * Interface for binding UI widgets to configuration values.
 * Provides a way to get, set, and reset configuration values.
 */
public interface ConfigBinding<T> {
    /**
     * Gets the current value from the configuration.
     */
    T getValue();

    /**
     * Sets the value in the configuration.
     */
    void setValue(T value);

    /**
     * Gets the default value for this configuration.
     */
    T getDefaultValue();

    /**
     * Resets the value to its default.
     */
    default void reset() {
        setValue(getDefaultValue());
    }
}
