/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.ItBurns;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
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
        logDebug("BlockBreakEvent caught");
        Player player = event.getPlayer();
        if (player != null) {
            logDebug("BlockBreakEvent: Player is not null! E: "
                    + event.isCancelled() + " P: "
                    + player.hasPermission("itburns.burn"));
            if (event.isCancelled() && player.hasPermission("itburns.burn")) {
                logDebug("Burning player! " + player.getName());
                player.setFireTicks(burnDuration);
            }
        } else {
            logDebug("BlockBreakEvent: Player is null");
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
