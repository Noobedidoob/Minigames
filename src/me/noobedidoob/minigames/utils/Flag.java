package me.noobedidoob.minigames.utils;

import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.lasertag.Lasertag.LasertagColor;
import me.noobedidoob.minigames.lasertag.methods.Mod;
import me.noobedidoob.minigames.lasertag.session.Session;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Flag implements Listener {

    private Session session;
    private ArmorStand armorStand;
    private final ItemStack banner;
    private final Location baseLocation;
    private final LasertagColor color;

    private Player playerAttachedTo;


    public Flag(Location baseLocation, LasertagColor color){
        this.color = color;
        this.baseLocation = baseLocation;
        this.banner = new ItemStack(Material.valueOf(color.getChatColor().name()+"_BANNER"));

        Bukkit.getPluginManager().registerEvents(this, Minigames.INSTANCE);
    }

    private boolean playerGlowing = false;
    BukkitTask repeater;
    public void attach(Player p){
        this.playerAttachedTo = p;
        p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0);
        for(Player fp : session.getPlayers()){
            if(session.getPlayerColor(fp) == color) fp.playSound(fp.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0);
        }
        p.getEquipment().setHelmet(banner);
        p.getInventory().setItem(8,Utils.getItemStack(banner.getType(),"§eDrop flag"));
        PLAYER_FLAG.put(p,this);
        p.setGlowing(true);
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN+""+ChatColor.BOLD+"Bring the flag to your base!"));

        if(repeater != null && !repeater.isCancelled()) repeater.cancel();
        repeater = Utils.runTimer(()->{
            playerAttachedTo.setGlowing(!playerGlowing);
            playerGlowing = !playerGlowing;
        },0,10);

        armorStand.getEquipment().clear();
        armorStand.setGlowing(false);
        armorStand.setGravity(false);
        armorStand.teleport(baseLocation.clone().add(0, session.getMap().getArea().getHeight(),0));
    }

    public void drop(Location dropLocation){
        removeFromPlayer();

        Location loc = dropLocation.clone();
        while (loc.getY() < session.getMap().getArea().getMinY()){
            loc.add(0,1,0);
        }
        armorStand.getEquipment().setHelmet(banner);
        armorStand.teleport(loc.subtract(0,1,0));
        armorStand.setGravity(true);
        armorStand.setGlowing(true);
    }

    public void teleportToBase(){
        removeFromPlayer();
        armorStand.teleport(baseLocation.clone().subtract(0,1,0));
        armorStand.getEquipment().setHelmet(banner);
        armorStand.setGlowing(false);
        armorStand.setGravity(false);
    }

    private void removeFromPlayer(){
        if (playerAttachedTo != null) {
            PLAYER_FLAG.put(playerAttachedTo,null);
            playerAttachedTo.setGlowing(false);
            playerAttachedTo.getEquipment().setHelmet(new ItemStack(Material.AIR));
            playerAttachedTo.getInventory().setItem(8,new ItemStack(Material.AIR));
            if(repeater != null && !repeater.isCancelled()) repeater.cancel();
            playerGlowing = false;
            Utils.runLater(()->{
                try {
                    playerAttachedTo.getInventory().setItem(8,new ItemStack(Material.AIR));
                } catch (NullPointerException ignored) {
                }
                playerAttachedTo = null;
            }, 5);
        }
    }

    private boolean coolingDown = false;
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if (session != null && session.isInSession(e.getPlayer())) {
            Player p = e.getPlayer();
            LasertagColor playerColor = session.getPlayerColor(p);
            if(playerAttachedTo == null){
                if (e.getTo().distance(armorStand.getLocation()) < 1.5) {
                    if(isAtBase()){
                        if(!playerColor.equals(color)) {
                            attach(e.getPlayer());
                        }
                    } else {
                        if(playerColor.equals(color)) {
                            teleportToBase();
                            p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0);
                        } else {
                            attach(e.getPlayer());
                        }
                    }
                }
            } else {
                if(p == playerAttachedTo && session.getMap().getBaseCoord(playerColor).getLocation().distance(p.getLocation()) < session.getMap().getProtectionRaduis()){
                    if (session.getMap().getBaseFlag(playerColor).isAtBase()) {
                        teleportToBase();
                        if(!coolingDown) {
                            try {
                                session.addPoints(p, session.getIntMod(Mod.CAPTURE_THE_FLAG_POINTS), playerColor.getChatColor() + p.getName() + " §7§ocaptured the flag from " + color.getChatColor() + ((session.isTeams() ? "team " + color : session.getPlayerFromColor(color).getName())) +" §7(§a+"+session.getIntMod(Mod.CAPTURE_THE_FLAG_POINTS)+" point"+((session.getIntMod(Mod.CAPTURE_THE_FLAG_POINTS) > 1)?"s":"")+"§7)");
                            } catch (Exception exception) {
                                Bukkit.broadcastMessage("uff");
                            }
                            coolingDown = true;
                            Utils.runLater(()-> coolingDown = false, 20);
                        }
                    } else {
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GOLD+""+ChatColor.UNDERLINE+""+ChatColor.BOLD+"Your flag is not at your base!"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (playerAttachedTo != null && e.getPlayer() == playerAttachedTo) {
            if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) | e.getAction().equals(Action.RIGHT_CLICK_AIR)){
                if(e.getItem() != null && e.getItem().getItemMeta().getDisplayName().toLowerCase().contains("drop flag")){
                    Vector direction = playerAttachedTo.getLocation().getDirection().multiply(3);
                    Location loc = playerAttachedTo.getLocation().add(direction);
                    while (loc.getY() < session.getMap().getArea().getMinY()){
                        loc.add(0,1,0);
                    }
                    drop(loc);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        if (playerAttachedTo != null && e.getPlayer() == playerAttachedTo) {
            Vector direction = playerAttachedTo.getLocation().getDirection().multiply(3);
            Location loc = playerAttachedTo.getLocation().add(direction);
            while (loc.getY() < session.getMap().getArea().getMinY()){
                loc.add(0,1,0);
            }
            drop(loc);
        }
    }


    public Session getSession() {
        return session;
    }

    public Location getBaseLocation() {
        return baseLocation;
    }

    public LasertagColor getColor() {
        return color;
    }

    public Player getPlayerAttachedTo() {
        return playerAttachedTo;
    }

    public boolean isAttached() {
        return playerAttachedTo != null;
    }

    public boolean isAtBase(){
        return playerAttachedTo == null && armorStand.getLocation().distance(baseLocation.clone().subtract(0,1,0)) < 0.1;
    }

    public boolean isEnabled(){
        return session != null;
    }
    public void enable(Session session){
        for(Player p : session.getPlayers()){
            p.setGlowing(false);
        }

        this.session = session;
        Location loc = baseLocation.clone().subtract(0,1,0);
        while(loc.getBlock().getType().isAir()){
            loc.subtract(0,1,0);
        }
        armorStand = (ArmorStand) baseLocation.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.getEquipment().setHelmet(banner);

        new BukkitRunnable(){
            double prevY = armorStand.getLocation().getY();
            @Override
            public void run() {
                if(!isAttached() && armorStand.getLocation().distance(baseLocation) < 1) teleportToBase();
                if(armorStand.getLocation().getY()+1 < session.getMap().getArea().getMinY()){
                    teleportToBase();
                }

                if (armorStand.getLocation().getY() == prevY) {
                    if(armorStand.getLocation().getBlock().getType().isAir()){
                        if (session.getMap().getArea().getMaxY()-prevY > 5) {
                            armorStand.setGravity(false);
                            armorStand.teleport(armorStand.getLocation().subtract(0,1,0));
                        }
                    }
                }
                prevY = armorStand.getLocation().getY();
            }
        }.runTaskTimer(session.minigames,20,5);
    }
    public void disable(){
        this.session = null;
        if(armorStand != null) {
            armorStand.getEquipment().setHelmet(new ItemStack(Material.AIR));
            armorStand.remove();
        }

        if(playerAttachedTo != null){
            PLAYER_FLAG.put(playerAttachedTo,null);
            playerAttachedTo.getEquipment().setHelmet(new ItemStack(Material.AIR));
            playerAttachedTo.setGlowing(false);
            this.playerAttachedTo = null;
        }

        if(repeater != null) repeater.cancel();
    }

    private static final HashMap<Player, Flag> PLAYER_FLAG = new HashMap<>();
    public static Flag getPlayerFlag(Player p){
        return PLAYER_FLAG.get(p);
    }
    public static boolean hasPlayerFlag(Player p){
        return (PLAYER_FLAG.get(p) != null);
    }



}
