package me.aujung.welcomeMessage;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class WelcomeMessage extends JavaPlugin implements Listener {
    private FileConfiguration welcomeConfig;
    private File configFile;
    private String welcomeMessage;
    private boolean broadcastWelcome;

    @Override
    public void onEnable() {
        // Ensure config is created and loaded
        createCustomConfig();

        // Load configuration
        loadConfiguration();

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Log plugin enable
        getLogger().info("WelcomePlugin has been enabled!");
    }

    private void createCustomConfig() {
        configFile = new File(getDataFolder(), "config.yml");

        // Create data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // If config doesn't exist, create it with default values
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();

                // Create a new configuration with comments
                welcomeConfig = YamlConfiguration.loadConfiguration(configFile);

                // Add detailed comments and default values
                welcomeConfig.set("# Welcome Message Configuration", null);
                welcomeConfig.set("# Use color codes with & (e.g., &a for green, &e for yellow)", null);
                welcomeConfig.set("# Placeholders: {player} for player name, {online} for online players", null);

                welcomeConfig.set("welcome-message", "&a➤ &eWelcome &6{player} &eto our server! &7(Players Online: {online})");
                welcomeConfig.set("broadcast-welcome", true);

                welcomeConfig.set("# Advanced Options:", null);
                welcomeConfig.set("first-join-only", false);

                welcomeConfig.set("quit-message", "&c➤ &eGoodbye &6{player} &e! See you next time!");
                welcomeConfig.set("broadcast-quit", true);

                // Save the configuration
                welcomeConfig.save(configFile);
            } catch (IOException e) {
                getLogger().severe("Could not create default configuration file!");
                e.printStackTrace();
            }
        }
    }

    private void loadConfiguration() {
        // Reload the configuration file
        welcomeConfig = YamlConfiguration.loadConfiguration(configFile);

        // Load welcome message with fallback
        welcomeMessage = ChatColor.translateAlternateColorCodes('&',
                welcomeConfig.getString("welcome-message",
                        "&a➤ &eWelcome &6{player} &eto our server!"));

        // Load broadcast setting
        broadcastWelcome = welcomeConfig.getBoolean("broadcast-welcome", true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //hide a default join message
        event.joinMessage(null);

        Player player = event.getPlayer();

        // Count online players
        int onlinePlayers = getServer().getOnlinePlayers().size();

        // Replace placeholders
        String formattedMessage = welcomeMessage
                .replace("{player}", player.getName())
                .replace("{online}", String.valueOf(onlinePlayers));

        // Check if it's first join only
        boolean firstJoinOnly = welcomeConfig.getBoolean("first-join-only", false);
        if (firstJoinOnly && player.hasPlayedBefore()) {
            return;
        }

        // Broadcast or send message
        if (broadcastWelcome) {
            getServer().broadcastMessage(formattedMessage);
        } else {
            player.sendMessage(formattedMessage);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Hide the default quit message
        event.quitMessage(null); // This prevents the default quit message from being broadcast.

        // Custom quit message (optional)
        String quitMessage = welcomeConfig.getString("quit-message", "&c➤ &eGoodbye &6{player} &e! See you next time!");
        quitMessage = ChatColor.translateAlternateColorCodes('&', quitMessage);

        // Replace placeholders
        quitMessage = quitMessage.replace("{player}", event.getPlayer().getName());

        // Broadcast the custom quit message (optional)
        if (welcomeConfig.getBoolean("broadcast-quit", true)) {
            getServer().broadcastMessage(quitMessage);
        }
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender,
                             org.bukkit.command.Command command,
                             String label,
                             String[] args) {
        getLogger().info("Command executed: " + command.getName());
        if (command.getName().equalsIgnoreCase("welcomereload")) {
            if (sender.hasPermission("welcome.reload")) {
                reloadConfig();
                loadConfiguration();
                sender.sendMessage(ChatColor.GREEN + "Welcome plugin configuration reloaded!");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            }
        }
        return false;
    }
}