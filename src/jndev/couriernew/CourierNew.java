package jndev.couriernew;

import jndev.couriernew.config.ConfigType;
import jndev.couriernew.config.Configs;
import jndev.couriernew.letter.LetterSender;
import jndev.couriernew.postman.PostmanChecker;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/**
 * Register permissions, commands, and events, as well as save the default config, load the config, and delete
 * leftover postmen. Also copies the letter image for map letters
 *
 * @author Jeremy Noesen
 */
public class CourierNew extends JavaPlugin {
    
    public static CourierNew plugin;
    
    private final Permission letter = new Permission("couriernew.letter");
    private final Permission postone = new Permission("couriernew.post.one");
    private final Permission postmultiple = new Permission("couriernew.post.multiple");
    private final Permission postallonline = new Permission("couriernew.post.allonline");
    private final Permission postall = new Permission("couriernew.post.all");
    private final Permission courier = new Permission("couriernew.help");
    private final Permission shred = new Permission("couriernew.shred");
    private final Permission shredall = new Permission("couriernew.shredall");
    private final Permission unread = new Permission("couriernew.unread");
    private final Permission reload = new Permission("couriernew.reload");
    
    public static CourierNew getInstance() {
        return plugin;
    }
    
    /**
     * Runs when getInstance() is enabled by the server
     */
    public void onEnable() {
        plugin = this;
    
        Configs.getConfig(ConfigType.MESSAGE).saveDefaultConfig();
        Configs.getConfig(ConfigType.POSTMEN).saveDefaultConfig();
        Configs.getConfig(ConfigType.OUTGOING).saveDefaultConfig();
        Configs.getConfig(ConfigType.CONFIG).saveDefaultConfig();
        
        Message.reloadMessages();
    
        plugin.getServer().getConsoleSender().sendMessage(Message.STARTUP);
        
        PluginManager pm = Bukkit.getPluginManager();
        
        pm.registerEvents(new LetterSender(), this);
        
        pm.addPermission(letter);
        pm.addPermission(postone);
        pm.addPermission(postmultiple);
        pm.addPermission(postallonline);
        pm.addPermission(postall);
        pm.addPermission(courier);
        pm.addPermission(shred);
        pm.addPermission(shredall);
        pm.addPermission(unread);
        pm.addPermission(reload);
        
        getCommand("letter").setExecutor(new CommandExec());
        getCommand("post").setExecutor(new CommandExec());
        getCommand("cnhelp").setExecutor(new CommandExec());
        getCommand("shred").setExecutor(new CommandExec());
        getCommand("shredall").setExecutor(new CommandExec());
        getCommand("unread").setExecutor(new CommandExec());
        getCommand("cnreload").setExecutor(new CommandExec());
        
        new BukkitRunnable() {
            @Override
            public void run() {
                
                int count = 0;
                PostmanChecker pc = new PostmanChecker();
                plugin.getServer().getConsoleSender().sendMessage(Message.CLEANING);
                
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (pc.isPostman(entity)) {
                            entity.remove();
                            count++;
                        }
                    }
                }
                
                plugin.getServer().getConsoleSender().sendMessage(Message.DONE_CLEANING.replace("$COUNT$",
                        Integer.toString(count)));
                
            }
        }.runTaskLater(plugin, 2);
        
        //saveResource("paper.png", false);
    }
    
    /**
     * runs when getInstance() is disabled by server, makes sure no method tries to reference the getInstance() anymore.
     */
    public void onDisable() {
        plugin = null;
    }
}

//todo vault support

//todo item cost for letter

//todo cancelable letters

//todo add to letter not page