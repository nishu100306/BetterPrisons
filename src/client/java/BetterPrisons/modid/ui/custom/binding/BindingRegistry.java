package BetterPrisons.modid.ui.custom.binding;

import BetterPrisons.modid.ui.custom.core.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for managing widget-to-config bindings.
 * Tracks all bindings and provides methods to save/load values.
 */
public class BindingRegistry {
    private static final Map<Component, ConfigBinding<?>> bindings = new HashMap<>();

    /**
     * Registers a binding between a widget and a config value.
     */
    public static <T> void register(Component component, ConfigBinding<T> binding) {
        bindings.put(component, binding);
    }

    /**
     * Unregisters a component's binding.
     */
    public static void unregister(Component component) {
        bindings.remove(component);
    }

    /**
     * Gets the binding for a component.
     */
    @SuppressWarnings("unchecked")
    public static <T> ConfigBinding<T> getBinding(Component component) {
        return (ConfigBinding<T>) bindings.get(component);
    }

    /**
     * Checks if a component has a binding.
     */
    public static boolean hasBinding(Component component) {
        return bindings.containsKey(component);
    }

    /**
     * Saves all widget values to their corresponding config bindings.
     * This should be called when the user clicks "Done" or "Save".
     */
    public static void saveAll() {
        // This will be implemented when widgets are created
        // Each widget type will need to know how to retrieve its current value
    }

    /**
     * Loads all config values into their corresponding widgets.
     * This should be called when the config screen is opened.
     */
    public static void loadAll() {
        // This will be implemented when widgets are created
        // Each widget type will need to know how to set its value
    }

    /**
     * Clears all bindings.
     * Should be called when the config screen is closed.
     */
    public static void clear() {
        bindings.clear();
    }

    /**
     * Gets the number of registered bindings.
     */
    public static int size() {
        return bindings.size();
    }
}
