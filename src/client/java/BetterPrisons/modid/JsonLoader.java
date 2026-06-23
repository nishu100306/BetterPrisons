package BetterPrisons.modid;

import BetterPrisons.modid.hud.CooldownHud;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonLoader {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final File COMMANDS_FILE = new File("config/betterprisons/commands.json");
    private static final File ENCHANTS_FILE = new File("config/betterprisons/enchants.json");

    public static List<CooldownHud.CommandDef> loadCommands() {
        if (!COMMANDS_FILE.exists()) {
            createDefaultCommands();
        } else {
            createDefaultCommands();
        }

        try (FileReader reader = new FileReader(COMMANDS_FILE)) {
            Type type = new TypeToken<CommandsWrapper>(){}.getType();
            CommandsWrapper wrapper = GSON.fromJson(reader, type);
            return wrapper != null ? wrapper.commands : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveCommands(List<CooldownHud.CommandDef> commands) {
        COMMANDS_FILE.getParentFile().mkdirs();
        CommandsWrapper wrapper = new CommandsWrapper();
        wrapper.commands = commands;

        try (FileWriter writer = new FileWriter(COMMANDS_FILE)) {
            GSON.toJson(wrapper, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createDefaultCommands() {
        COMMANDS_FILE.getParentFile().mkdirs();
        List<CooldownHud.CommandDef> defaults = new ArrayList<>();

        CooldownHud.CommandDef home = new CooldownHud.CommandDef();
        home.command = "/home";
        home.matchType = "startsWith";
        home.cooldown = 60;
        home.displayName = "Home";
        home.chatPattern = null; // Command-triggered
        home.aliases = null;
        home.icon = "minecraft:red_bed";
        defaults.add(home);

        CooldownHud.CommandDef jet = new CooldownHud.CommandDef();
        jet.command = "/jet";
        jet.matchType = "exact";
        jet.cooldown = 30;
        jet.displayName = "Jet";
        jet.chatPattern = null; // Command-triggered
        jet.aliases = new ArrayList<>();
        jet.aliases.add("/jetpack");
        jet.icon = "minecraft:blaze_powder";
        defaults.add(jet);

        CooldownHud.CommandDef feed = new CooldownHud.CommandDef();
        feed.command = "/feed";
        feed.matchType = "startsWith";
        feed.cooldown = 180;
        feed.displayName = "Feed";
        feed.chatPattern = null; // Command-triggered
        feed.aliases = new ArrayList<>();
        feed.aliases.add("/eat");
        feed.icon = "minecraft:cooked_beef";
        defaults.add(feed);

        CooldownHud.CommandDef fix = new CooldownHud.CommandDef();
        fix.command = "/fix";
        fix.matchType = "startsWith";
        fix.cooldown = 180;
        fix.displayName = "Fix";
        fix.chatPattern = null; // Command-triggered
        fix.aliases = null;
        fix.icon = "minecraft:anvil";
        defaults.add(fix);

        CooldownHud.CommandDef combat = new CooldownHud.CommandDef();
        combat.command = "";
        combat.matchType = "exact";
        combat.cooldown = 10;
        combat.displayName = "Combat";
        combat.chatPattern = "§c§l(!) §cYou have entered combat. Do not log out for 10s!";
        combat.aliases = null;
        combat.icon = "minecraft:diamond_sword";
        defaults.add(combat);

        CooldownHud.CommandDef tpa = new CooldownHud.CommandDef();
        tpa.command = "/tpa ";
        tpa.matchType = "startsWith";
        tpa.cooldown = 180;
        tpa.displayName = "tpa";
        tpa.chatPattern = null; // Set dynamically
        tpa.aliases = null;
        tpa.icon = "minecraft:experience_bottle";
        defaults.add(tpa);

        CooldownHud.CommandDef tpahere = new CooldownHud.CommandDef();
        tpahere.command = "/tpahere ";
        tpahere.matchType = "startsWith";
        tpahere.cooldown = 240;
        tpahere.displayName = "tpahere";
        tpahere.chatPattern = null; // Set dynamically
        tpahere.aliases = null;
        tpahere.icon = "minecraft:experience_bottle";
        defaults.add(tpahere);

        CooldownHud.CommandDef dangle = new CooldownHud.CommandDef();
        dangle.command = "/dangle";
        dangle.matchType = "exact";
        dangle.cooldown = 30;
        dangle.displayName = "Dangle";
        dangle.chatPattern = "§aDangling your item!";
        dangle.aliases = null;
        dangle.icon = "minecraft:fishing_rod";
        defaults.add(dangle);

        CooldownHud.CommandDef adangle = new CooldownHud.CommandDef();
        adangle.command = "/adangle";
        adangle.matchType = "exact";
        adangle.cooldown = 20;
        adangle.displayName = "Adangle";
        adangle.chatPattern = null; // Triggered on send; cancelled by error messages
        adangle.aliases = null;
        adangle.icon = "minecraft:iron_chestplate";
        defaults.add(adangle);

        CommandsWrapper wrapper = new CommandsWrapper();
        wrapper.commands = defaults;

        try (FileWriter writer = new FileWriter(COMMANDS_FILE)) {
            GSON.toJson(wrapper, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<EnchantDef> loadEnchants() {
        if (!ENCHANTS_FILE.exists()) {
            createDefaultEnchants();
        }

        try (FileReader reader = new FileReader(ENCHANTS_FILE)) {
            Type type = new TypeToken<EnchantsWrapper>(){}.getType();
            EnchantsWrapper wrapper = GSON.fromJson(reader, type);
            return wrapper != null ? wrapper.enchants : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static void createDefaultEnchants() {
        ENCHANTS_FILE.getParentFile().mkdirs();
        List<EnchantDef> defaults = new ArrayList<>();

        EnchantDef superBreaker = new EnchantDef();
        superBreaker.id = "super_breaker";
        superBreaker.displayName = "Super Breaker";
        superBreaker.enabled = true;
        superBreaker.chatPattern = "";
        superBreaker.cooldown = 0;
        superBreaker.showOnHud = true;
        defaults.add(superBreaker);

        EnchantsWrapper wrapper = new EnchantsWrapper();
        wrapper.enchants = defaults;

        try (FileWriter writer = new FileWriter(ENCHANTS_FILE)) {
            GSON.toJson(wrapper, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Wrapper classes for JSON structure
    private static class CommandsWrapper {
        public List<CooldownHud.CommandDef> commands;
    }

    private static class EnchantsWrapper {
        public List<EnchantDef> enchants;
    }

    public static class EnchantDef {
        public String id;
        public String displayName;
        public boolean enabled;
        public String chatPattern;
        public int cooldown;
        public boolean showOnHud;
    }
}
