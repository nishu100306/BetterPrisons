package BetterPrisons.modid.waypoint;

public class CustomWaypoint {
    public String name;
    public int x, y, z;
    public int color;           // 0xRRGGBB
    public int opacity = 255;   // 0-255 beacon beam opacity
    public float onScreenScale  = 1.0f;  // icon scale when waypoint projects on-screen
    public float offScreenScale = 1.0f;  // icon scale for edge indicator when off-screen
    public boolean enabled;
    /** Non-null for auto-added event entries (e.g. "METEOR_NATURAL", "MERCHANT_COAL").
     *  Null for user-created waypoints. */
    public String eventKey;

    public CustomWaypoint() {} // for Gson

    public CustomWaypoint(String name, int x, int y, int z, int color) {
        this.name    = name;
        this.x       = x;
        this.y       = y;
        this.z       = z;
        this.color   = color;
        this.opacity        = 255;
        this.onScreenScale  = 1.0f;
        this.offScreenScale = 1.0f;
        this.enabled = true;
    }

    public boolean isEvent() {
        return eventKey != null;
    }
}
