package BetterPrisons.modid.waypoint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class WaypointManager {

    private static final File WAYPOINTS_FILE = new File("config/betterprisons/waypoints.json");
    private static final Gson GSON           = new GsonBuilder().setPrettyPrinting().create();

    /** World key → waypoints for that world. */
    private final Map<String, List<CustomWaypoint>> worldWaypoints = new LinkedHashMap<>();

    /** Key of the world the player is currently in. */
    private String currentWorld = "unknown";

    // ----------------------------------------------------------------
    // World management
    // ----------------------------------------------------------------

    /** Returns the dimension key string of the world the client is currently in. */
    public static String detectWorldKey() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            return client.world.getRegistryKey().getValue().toString();
        }
        return "unknown";
    }

    /**
     * Called when the player enters a world. Updates the current world key and
     * ensures an entry exists in the map.
     */
    public void setCurrentWorld(String world) {
        this.currentWorld = world;
        worldWaypoints.computeIfAbsent(world, k -> new ArrayList<>());
    }

    public String getCurrentWorld() {
        return currentWorld;
    }

    /** Sorted list of all world keys that have waypoints saved. */
    public List<String> getWorlds() {
        List<String> worlds = new ArrayList<>(worldWaypoints.keySet());
        Collections.sort(worlds);
        return worlds;
    }

    // ----------------------------------------------------------------
    // Persistence
    // ----------------------------------------------------------------

    public void load() {
        worldWaypoints.clear();
        if (!WAYPOINTS_FILE.exists()) return;
        try (FileReader reader = new FileReader(WAYPOINTS_FILE)) {
            Type type = new TypeToken<Map<String, List<CustomWaypoint>>>(){}.getType();
            Map<String, List<CustomWaypoint>> loaded = GSON.fromJson(reader, type);
            if (loaded != null) worldWaypoints.putAll(loaded);
        } catch (Exception e) {
            // File corrupt or unreadable — start fresh
        }
    }

    public void save() {
        WAYPOINTS_FILE.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(WAYPOINTS_FILE)) {
            GSON.toJson(worldWaypoints, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------

    private List<CustomWaypoint> listFor(String world) {
        return worldWaypoints.computeIfAbsent(world, k -> new ArrayList<>());
    }

    /** Remove a world and all its waypoints entirely. */
    public void removeWorld(String world) {
        worldWaypoints.remove(world);
        save();
    }

    /** Add a waypoint to the current world. */
    public void add(CustomWaypoint wp) {
        listFor(currentWorld).add(wp);
        save();
    }

    /** Remove by index from a specific world. */
    public void remove(int index, String world) {
        List<CustomWaypoint> wps = listFor(world);
        if (index >= 0 && index < wps.size()) {
            wps.remove(index);
            save();
        }
    }

    /** Remove by index from the current world. */
    public void remove(int index) {
        remove(index, currentWorld);
    }

    /** Update by index in a specific world. */
    public void update(int index, CustomWaypoint wp, String world) {
        List<CustomWaypoint> wps = listFor(world);
        if (index >= 0 && index < wps.size()) {
            wps.set(index, wp);
            save();
        }
    }

    /** Update by index in the current world. */
    public void update(int index, CustomWaypoint wp) {
        update(index, wp, currentWorld);
    }

    /** All waypoints for a specific world. */
    public List<CustomWaypoint> getAll(String world) {
        return listFor(world);
    }

    /** All waypoints for the current world. */
    public List<CustomWaypoint> getAll() {
        return listFor(currentWorld);
    }

    /** Only enabled, user-created waypoints for the current world — used by renderers.
     *  Event waypoints (eventKey != null) are excluded; EventsHud renders those. */
    public List<CustomWaypoint> getEnabled() {
        List<CustomWaypoint> result = new ArrayList<>();
        for (CustomWaypoint wp : listFor(currentWorld)) {
            if (wp.enabled && !wp.isEvent()) result.add(wp);
        }
        return result;
    }

    // ----------------------------------------------------------------
    // Event waypoint helpers (auto-added from EventsHud)
    // ----------------------------------------------------------------

    private static final String OVERWORLD = "minecraft:overworld";

    /**
     * Removes all event waypoints (eventKey != null) from all worlds.
     * Called on world join to clear stale entries from the previous session.
     */
    public void clearAllEventWaypoints() {
        for (List<CustomWaypoint> list : worldWaypoints.values()) {
            list.removeIf(CustomWaypoint::isEvent);
        }
        save();
    }

    /**
     * Adds an event waypoint to the overworld list if no waypoint already
     * exists at the same coordinates. Does nothing if a duplicate is found.
     */
    public void addEventWaypoint(int x, int y, int z, int color, String name, String eventKey) {
        List<CustomWaypoint> wps = listFor(OVERWORLD);
        for (CustomWaypoint wp : wps) {
            if (wp.x == x && wp.y == y && wp.z == z) return; // already present
        }
        CustomWaypoint wp = new CustomWaypoint(name, x, y, z, color);
        wp.eventKey = eventKey;
        wps.add(wp);
        save();
    }

    /**
     * Removes the event waypoint at the given coordinates from the overworld list.
     * Only removes entries that have a non-null eventKey (auto-added entries).
     */
    public void removeEventWaypoint(int x, int y, int z) {
        List<CustomWaypoint> wps = listFor(OVERWORLD);
        wps.removeIf(wp -> wp.isEvent() && wp.x == x && wp.y == y && wp.z == z);
        save();
    }

    /**
     * Returns true if an event waypoint exists at the given coordinates in the overworld.
     */
    public boolean hasEventWaypoint(int x, int y, int z) {
        for (CustomWaypoint wp : listFor(OVERWORLD)) {
            if (wp.isEvent() && wp.x == x && wp.y == y && wp.z == z) return true;
        }
        return false;
    }
}
