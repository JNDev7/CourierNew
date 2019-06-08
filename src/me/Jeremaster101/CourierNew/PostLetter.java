package me.Jeremaster101.CourierNew;

import com.earth2me.essentials.Essentials;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.kitteh.vanish.VanishPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostLetter implements Listener {

    private IsLetter il = new IsLetter();
    private Message msg = new Message();

    @SuppressWarnings("deprecation")
    void send(Player sender, String recipient) {
        if (il.isHoldingOwnLetter(sender)) {
            File outgoingyml = new File(Main.plugin.getDataFolder(), "outgoing.yml");
            FileConfiguration outgoing = YamlConfiguration.loadConfiguration(outgoingyml);
            OfflinePlayer op = Bukkit.getOfflinePlayer(recipient);
            ItemStack letter = sender.getInventory().getItemInMainHand();
            UUID uuid;
            List<ItemStack> letters;

            if (op.hasPlayedBefore()) {
                uuid = op.getUniqueId();
            } else {
                sender.sendMessage(msg.ERROR_PLAYER_NO_EXIST.replace("$PLAYER$", recipient));
                return;
            }

            OfflinePlayer recplayer = Bukkit.getOfflinePlayer(uuid);

            ArrayList<String> lore = new ArrayList<>(letter.getItemMeta().getLore());
            if (lore.get(lore.size() - 1).contains("To ")) {
                sender.sendMessage(msg.ERROR_SENT_BEFORE);
                return;
            } else {
                ItemMeta im = letter.getItemMeta();
                lore.add(ChatColor.DARK_GRAY + "To " + recplayer.getName());
                im.setLore(lore);
                letter.setItemMeta(im);
            }

            if (outgoing.get(uuid.toString()) == null) {
                letters = new ArrayList<>();
            } else
                letters = (List<ItemStack>) outgoing.getList(uuid.toString());

            letters.add(letter);
            outgoing.set(uuid.toString(), letters);

            try {
                outgoing.save(outgoingyml);
            } catch (IOException e) {
                e.printStackTrace();
            }

            sender.getInventory().getItemInMainHand().setAmount(0);
            sender.sendMessage(msg.SUCCESS_SENT.replace("$PLAYER$", recplayer.getName()));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (recplayer.isOnline()) {
                        spawnPostman((Player) recplayer);
                    }
                }
            }.runTaskLater(Main.plugin, Main.plugin.getConfig().getLong("send-recieve-delay"));

        } else if (il.isHoldingLetter(sender)) {
            sender.sendMessage(msg.ERROR_NOT_YOUR_LETTER);
        } else
            sender.sendMessage(msg.ERROR_NO_LETTER);
    }

    void receive(Player recipient) {
        File outgoingyml = new File(Main.plugin.getDataFolder(), "outgoing.yml");
        FileConfiguration outgoing = YamlConfiguration.loadConfiguration(outgoingyml);
        UUID uuid = recipient.getUniqueId();

        if (outgoing.getList(uuid.toString()) != null && outgoing.getList(uuid.toString()).size() > 0) {
            List<ItemStack> letters = (List<ItemStack>) outgoing.getList(uuid.toString());

            for (ItemStack letter : letters.toArray(new ItemStack[0])) {
                if (recipient.getInventory().firstEmpty() < 0) {
                    recipient.sendMessage(msg.ERROR_CANT_HOLD);
                    break;
                } else if (recipient.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    recipient.getInventory().setItemInMainHand(letter);
                    letters.remove(0);
                } else {
                    recipient.getInventory().addItem(letter);
                    letters.remove(0);
                }
            }

            outgoing.set(uuid.toString(), letters);

            try {
                outgoing.save(outgoingyml);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    void spawnPostman(Player recipient) {


        if(Bukkit.getPluginManager().isPluginEnabled("VanishNoPacket"))
        if (((VanishPlugin) Bukkit.getPluginManager().getPlugin("VanishNoPacket")).getManager().isVanished
        (recipient)) {
            recipient.sendMessage(msg.ERROR_VANISHED);
            return;
        }

        if(Bukkit.getPluginManager().isPluginEnabled("Essentials"))
            if(((Essentials) Bukkit.getPluginManager().getPlugin("Essentials")).getVanishedPlayers().contains(recipient.getName())) {
            recipient.sendMessage(msg.ERROR_VANISHED);
            return;
        }

        ArrayList<String> worlds = new ArrayList<>(Main.plugin.getConfig().getStringList("blocked-worlds"));
        ArrayList<String> modes = new ArrayList<>(Main.plugin.getConfig().getStringList("blocked-gamemodes"));
        if (worlds.contains(recipient.getWorld().getName()) || modes.contains(recipient.getGameMode().toString())) {
            recipient.sendMessage(msg.ERROR_WORLD);
            return;
        }

        double radius = Main.plugin.getConfig().getDouble("check-before-spawning-postman-radius");
        for (Entity all : recipient.getNearbyEntities(radius, radius, radius)) {
            if (all instanceof Villager && all.getCustomName().equals(msg.POSTMAN_NAME.replace("$PLAYER$", recipient.getName()))) {
                return;
            }
        }

        Location loc = recipient.getLocation().add(recipient.getLocation().getDirection().setY(0).multiply(3));
        Villager postman = recipient.getWorld().spawn(loc, Villager.class);
        postman.setProfession(Villager.Profession.LIBRARIAN);
        postman.setAdult();
        postman.setCustomName(msg.POSTMAN_NAME.replace("$PLAYER$", recipient.getName()));
        postman.setCustomNameVisible(false);
        postman.setInvulnerable(true);
        postman.setBreed(false);
        postman.setRemoveWhenFarAway(false);
        recipient.sendMessage(msg.SUCCESS_POSTMAN_ARRIVED);
        postman.getWorld().playSound(postman.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1, 1);
        new BukkitRunnable() {
            @Override
            public void run() {
                postman.setFallDistance(0);
                if (postman.isOnGround())
                    postman.teleport(postman.getLocation().setDirection(recipient.getLocation().subtract(postman.getLocation()).toVector()));
                if (postman.isDead()) this.cancel();
            }
        }.runTaskTimer(Main.plugin, 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!postman.isDead()) {
                    postman.remove();
                    recipient.sendMessage(msg.SUCCESS_IGNORED);
                    postman.getWorld().playSound(postman.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            File outgoingyml = new File(Main.plugin.getDataFolder(), "outgoing.yml");
                            FileConfiguration outgoing = YamlConfiguration.loadConfiguration(outgoingyml);

                            if (recipient.isOnline() && outgoing.getList(recipient.getUniqueId().toString()) != null
                                    && outgoing.getList(recipient.getUniqueId().toString()).size() > 0)
                                spawnPostman(recipient);
                        }
                    }.runTaskLater(Main.plugin, Main.plugin.getConfig().getLong("resend-delay"));
                }
            }
        }.runTaskLater(Main.plugin, Main.plugin.getConfig().getLong("remove-postman-ignored-delay"));
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Entity en = e.getRightClicked();
        if (en instanceof Villager) {
            if (en.getCustomName() != null && en.getCustomName().equals(msg.POSTMAN_NAME.replace("$PLAYER$", e.getPlayer().getName())) && en.getCustomName().contains(e.getPlayer().getName())) {
                e.setCancelled(true);
                receive(e.getPlayer());
                en.setCustomName(msg.POSTMAN_NAME_RECEIVED);
                en.getWorld().playSound(en.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!en.isDead()) en.remove();
                    }
                }.runTaskLater(Main.plugin, Main.plugin.getConfig().getLong("remove-postman-recieved-delay"));
            } else if (en.getCustomName() != null && en.getCustomName().contains(" Postman")) e.setCancelled(true);
            if (en.getCustomName() != null && en.getCustomName().equals(msg.POSTMAN_NAME_RECEIVED))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        File outgoingyml = new File(Main.plugin.getDataFolder(), "outgoing.yml");
        FileConfiguration outgoing = YamlConfiguration.loadConfiguration(outgoingyml);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && outgoing.getList(player.getUniqueId().toString()) != null && outgoing.getList(player.getUniqueId().toString()).size() > 0) {
                    spawnPostman(player);
                }
            }
        }.runTaskLater(Main.plugin, Main.plugin.getConfig().getLong("join-recieve-delay"));
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        List<String> worlds = new ArrayList<>(Main.plugin.getConfig().getStringList("blocked-worlds"));
        String to = e.getTo().getWorld().getName();
        String from = e.getFrom().getWorld().getName();
        Player recipient = e.getPlayer();

        if (worlds.contains(from) && !worlds.contains(to)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    File outgoingyml = new File(Main.plugin.getDataFolder(), "outgoing.yml");
                    FileConfiguration outgoing = YamlConfiguration.loadConfiguration(outgoingyml);
                    if (recipient.isOnline() && outgoing.getList(recipient.getUniqueId().toString()) != null
                            && outgoing.getList(recipient.getUniqueId().toString()).size() > 0)
                        spawnPostman(recipient);
                }
            }.runTaskLater(Main.plugin, Main.plugin.getConfig().getLong("resend-from-blocked-delay"));
        }
    }

    @EventHandler
    public void onGamemode(PlayerGameModeChangeEvent e) {
        List<String> modes = new ArrayList<>(Main.plugin.getConfig().getStringList("blocked-gamemodes"));
        GameMode to = e.getNewGameMode();
        GameMode from = e.getPlayer().getGameMode();
        Player recipient = e.getPlayer();
        if (modes.contains(from.toString()) && !modes.contains(to.toString())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    File outgoingyml = new File(Main.plugin.getDataFolder(), "outgoing.yml");
                    FileConfiguration outgoing = YamlConfiguration.loadConfiguration(outgoingyml);
                    if (recipient.isOnline() && outgoing.getList(recipient.getUniqueId().toString()) != null
                            && outgoing.getList(recipient.getUniqueId().toString()).size() > 0)
                        spawnPostman(recipient);
                }
            }.runTaskLater(Main.plugin, Main.plugin.getConfig().getLong("resend-from-blocked-delay"));
        }
    }
}