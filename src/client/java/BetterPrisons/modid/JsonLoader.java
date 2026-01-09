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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File COMMANDS_FILE = new File("config/betterprisons/commands.json");
    private static final File ENCHANTS_FILE = new File("config/betterprisons/enchants.json");

    public static List<CooldownHud.CommandDef> loadCommands() {
        if (!COMMANDS_FILE.exists()) {
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

    private static void createDefaultCommands() {
        COMMANDS_FILE.getParentFile().mkdirs();
        List<CooldownHud.CommandDef> defaults = new ArrayList<>();

        CooldownHud.CommandDef sellAll = new CooldownHud.CommandDef();
        sellAll.command = "/sell all";
        sellAll.matchType = "exact";
        sellAll.cooldown = 30;
        sellAll.displayName = "Sell All";
        defaults.add(sellAll);

        CooldownHud.CommandDef fix = new CooldownHud.CommandDef();
        fix.command = "/fix";
        fix.matchType = "startsWith";
        fix.cooldown = 300;
        fix.displayName = "Fix All";
        defaults.add(fix);

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
