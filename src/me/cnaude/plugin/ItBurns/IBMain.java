/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.ItBurns;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author cnaude
 */
public class IBMain extends JavaPlugin implements Listener {

    public static String LOG_HEADER;
    static final Logger log = Logger.getLogger("Minecraft");
    private File pluginFolder;
    private File configFile;
    private int burnDuration = 100;
    private static boolean debugEnabled = false;
    private static boolean burnOnBreak = false;
    private static boolean burnOnPlace = false;
    private static boolean ignoreBlocks = false;
    private static List<String> ignoreList = new ArrayList<String>();
    private static String breakMsg = "";
    private static String placeMsg = "";
    

    @Override
    public void onEnable() {
        LOG_HEADER = "[" + this.getName() + "]";
        pluginFolder = getDataFolder();
        configFile = new File(pluginFolder, "config.yml");
        createConfig();
        this.getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("itburns").setExecutor(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        logDebug("BlockBreakEvent caught: " + burnOnBreak);
        Block block = event.getBlock();
        if (ignoreBlocks) {
            if (ignoreList.contains(String.valueOf(block.getTypeId()))) {
                logDebug("Ignored: " + block.getTypeId());
                return;
            }
            if (ignoreList.contains(block.getType().toString())) {
                logDebug("Ignored: " + block.getType().toString());
                return;
            }
        }
        if (burnOnBreak) {
            Player player = event.getPlayer();
            if (player != null) {
                logDebug("BlockBreakEvent: Player is not null! E: "
                        + event.isCancelled() + " P: "
                        + player.hasPermission("itburns.burn"));
                if (event.isCancelled() && player.hasPermission("itburns.burn")) {
                    logDebug("Burning player! " + player.getName());
                    player.setFireTicks(burnDuration);
                    if (!breakMsg.isEmpty()) {
                        player.sendMessage(breakMsg);
                    }
                }
            } else {
                logDebug("BlockBreakEvent: Player is null");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        logDebug("BlockPlaceEvent caught: " + burnOnPlace);
        if (burnOnPlace) {
            Player player = event.getPlayer();
            if (player != null) {
                logDebug("BlockPlaceEvent: Player is not null! E: "
                        + event.isCancelled() + " P: "
                        + player.hasPermission("itburns.burn"));
                if (event.isCancelled() && player.hasPermission("itburns.burn")) {
                    logDebug("Burning player! " + player.getName());
                    player.setFireTicks(burnDuration);
                    if (!placeMsg.isEmpty()) {
                        player.sendMessage(placeMsg);
                    }
                }
            } else {
                logDebug("BlockBreakEvent: Player is null");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("itburns.reload")) {
                    reloadConfig();
                    loadConfig();
                    sender.sendMessage("ItBurns configuration file reloaded.");
                } else {
                    sender.sendMessage(ChatColor.RED + "No permission to do this!");
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void loadConfig() {
        burnDuration = getConfig().getInt("burn-duration");
        debugEnabled = getConfig().getBoolean("debug-enabled");
        burnOnBreak = getConfig().getBoolean("burn-events.break");
        burnOnPlace = getConfig().getBoolean("burn-events.place");
        ignoreBlocks = getConfig().getBoolean("ignore-blocks");
        for (String s : getConfig().getStringList("ignore-list")) {
            ignoreList.add(s.toUpperCase());
            logDebug("Ignore block: " + s.toUpperCase());
        }
        breakMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("custom-messages.break"));
        placeMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("custom-messages.place"));        
    }

    private void createConfig() {
        if (!pluginFolder.exists()) {
            try {
                pluginFolder.mkdir();
            } catch (Exception e) {
                logError(e.getMessage());
            }
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                logError(e.getMessage());
            }
        }
    }

    public void logInfo(String _message) {
        log.log(Level.INFO, String.format("%s %s", LOG_HEADER, _message));
    }

    public void logError(String _message) {
        log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
    }

    public void logDebug(String _message) {
        if (debugEnabled) {
            log.log(Level.INFO, String.format("%s [DEBUG] %s", LOG_HEADER, _message));
        }
    }
}
