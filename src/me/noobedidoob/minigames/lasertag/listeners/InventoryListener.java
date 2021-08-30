package me.noobedidoob.minigames.lasertag.listeners;

import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.lasertag.Lasertag;
import me.noobedidoob.minigames.lasertag.methods.Inventories;
import me.noobedidoob.minigames.lasertag.methods.Weapon;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.lasertag.session.SessionTeam;
import me.noobedidoob.minigames.utils.Map;
import me.noobedidoob.minigames.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryListener implements Listener {

    private final Minigames minigames;
    public InventoryListener(Minigames minigames){
        this.minigames = minigames;
        Bukkit.getPluginManager().registerEvents(this,minigames);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayeClickInventory(InventoryClickEvent e){

        try {
            if(!(e.getWhoClicked() instanceof Player)) return;
            Inventory inv = e.getClickedInventory();
            InventoryView view = e.getView();

            if(inv == null) return;
            if(view == null) return;
            int slot = e.getSlot();
            try {
                if(inv.getItem(slot) == null) return;
            } catch (ArrayIndexOutOfBoundsException exception) {
                return;
            }

            Player p = (Player) e.getWhoClicked();
            Session session = Session.getPlayerSession(p);

            if(session == null){
                if(view.getTitle().equals(Inventories.NEW_SESSION_INVENTORY_TITLE)) {
                    if(slot == 2 && inv.getItem(2).getType() == Material.RED_STAINED_GLASS_PANE) {
                        if(inv.getItem(4).getAmount() > 1) {
                            inv.getItem(4).setAmount(inv.getItem(4).getAmount()-1);
                            if(inv.getItem(4).getAmount() == 1) {
                                inv.getItem(4).setType(Material.BARRIER);
                                ItemMeta teamMeta = inv.getItem(4).getItemMeta();
                                teamMeta.setDisplayName("§cNo Teams -> §7(§bSOLO§7)");
                                inv.getItem(4).setItemMeta(teamMeta);
                            } else {
                                ItemMeta teamMeta = inv.getItem(4).getItemMeta();
                                teamMeta.setDisplayName("§a"+inv.getItem(4).getAmount()+" §bTeams");
                                inv.getItem(4).setItemMeta(teamMeta);
                            }
                        }
                    } else if(slot == 6 && inv.getItem(6).getType() == Material.LIME_STAINED_GLASS_PANE) {
                        if(inv.getItem(4).getAmount() < 8) {
                            inv.getItem(4).setAmount(inv.getItem(4).getAmount()+1);
                            if(inv.getItem(4).getType() == Material.BARRIER) {
                                ItemStack amount = new ItemStack(Material.LEATHER_CHESTPLATE, 2);
                                LeatherArmorMeta aMeta = (LeatherArmorMeta) amount.getItemMeta();
                                aMeta.setColor(Lasertag.LasertagColor.Red.getColor());
                                aMeta.setDisplayName("§aTeams: §b2");
                                aMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
                                amount.setItemMeta(aMeta);
                                inv.setItem(4, amount);
                            } else {
                                ItemMeta timeMeta = inv.getItem(4).getItemMeta();
                                timeMeta.setDisplayName("§a"+inv.getItem(4).getAmount()+" §bTeams");
                                inv.getItem(4).setItemMeta(timeMeta);
                            }
                        }
                    } else if(slot == 8 && inv.getItem(8).getType() == Weapon.LASERGUN.getType()) {
                        if(inv.getItem(4).getAmount() < 2) Session.sendMessage(p, "§aCreated a new solo session!");
                        else Session.sendMessage(p, "§aCreated a new teams session with §d"+inv.getItem(4).getAmount()+" §ateams!");
                        new Session(minigames, p, inv.getItem(4).getAmount());
                        p.closeInventory();
                    }
                }
            } else {
                if(session.tagging()) return;
                if(session.isAdmin(p)) {
                    if(view.getTitle().equals(Inventories.TIME_INVENTORY_TITLE)){
                        if(slot == 2 && inv.getItem(2).getType() == Material.RED_STAINED_GLASS_PANE) {
                            if(inv.getItem(4).getAmount() > 1) inv.getItem(4).setAmount(inv.getItem(4).getAmount()-1);
                        } else if(slot == 6 && inv.getItem(6).getType() == Material.LIME_STAINED_GLASS_PANE) {
                            if(inv.getItem(4).getAmount() < 60) inv.getItem(4).setAmount(inv.getItem(4).getAmount()+1);
                            else Session.sendMessage(p, "§cMaxi time is 1 hour!");
                        } else if(slot == 8 && inv.getItem(8).getType() == Weapon.LASERGUN.getType()) {
                            session.setTime(inv.getItem(4).getAmount(), Utils.TimeFormat.MINUTES, true);
                            //						Session.sendMessage(p, "Time set to §b"+inv.getItem(4).getAmount()+" §eminutes!");
                            if(session.isMapNull()) Inventories.openMapInv(p);
                            else p.closeInventory();
                        }
                        ItemMeta timeMeta = inv.getItem(4).getItemMeta();
                        timeMeta.setDisplayName("§bTime: §r"+inv.getItem(4).getAmount()+" Minutes");
                        inv.getItem(4).setItemMeta(timeMeta);
                    }


                    else if(view.getTitle().equals(Inventories.MAP_INVENTORY_TITLE)) {
                        if(slot == 4) {
                            session.setMap(null);
                            Session.sendMessage(p, "§aMap vote enabled!");
                            p.closeInventory();
                            if(session.isTeams() && !session.isTeamsAmountSet()) Inventories.openTeamsInv(p);
                            session.setAllPlayersInv();
                        } else if(slot > 8 && slot-9 < Map.MAPS.size() && inv.getItem(slot).getType() == Material.FILLED_MAP) {
                            Map m = Map.getMapByName(inv.getItem(slot).getItemMeta().getDisplayName().toLowerCase().substring(2));
                            session.setMap(m);
                            p.closeInventory();
                            for(Player ap : session.getPlayers()) {
                                Inventories.setPlayerSessionWaitingInv(ap);
                            }
                            if(session.isTeams() && !session.isTeamsAmountSet()) Inventories.openTeamsInv(p);
                            session.setAllPlayersInv();
                        }
                    }


                    else if(view.getTitle().equals(Inventories.TEAMS_INVENTORY_TITLE)) {
                        if(slot == 2) {
                            if(inv.getItem(4).getAmount() > 1) {
                                inv.getItem(4).setAmount(inv.getItem(4).getAmount()-1);
                                if(inv.getItem(4).getAmount() == 1) inv.setItem(4, Utils.getItemStack(Material.BARRIER, "§cSolo"));
                                else if(inv.getItem(4).getAmount() == 2) inv.setItem(2, Inventories.getSubtractionItem("§c§lSolo"));
                            }
                        } else if(slot == 4) {
                            if(inv.getItem(4).getAmount() < 2) Inventories.openTeamsInv(p, 2);
                            else if(inv.getItem(4).getAmount() >= 2) Inventories.openTeamsInv(p, 1);
                        } else if(slot == 6) {
                            if(inv.getItem(4).getAmount() < 8) inv.getItem(4).setAmount(inv.getItem(4).getAmount()+1);
                            if(inv.getItem(4).getAmount() == 2) inv.setItem(4, Utils.getLeatherArmorItem(Material.LEATHER_CHESTPLATE, "§aTeams: §b2", Lasertag.LasertagColor.Red.getColor(), 2));else if(inv.getItem(4).getAmount() == 3) inv.setItem(2, Inventories.getSubtractionItem("§c§l-1 §r§bTeam"));
                        } else if(slot == 8) {
                            session.setTeamsAmount(inv.getItem(4).getAmount());
                            p.closeInventory();
                            session.refreshScoreboard();
                            session.setAllPlayersInv();
                        }
                        ItemMeta timeMeta = inv.getItem(4).getItemMeta();
                        timeMeta.setDisplayName("§a"+inv.getItem(4).getAmount()+" §bTeams");
                        inv.getItem(4).setItemMeta(timeMeta);
                    }


                    else if(view.getTitle().equals(Inventories.ADD_ADMIN_INVENTORY_TITLE)) {
                        if(slot == 4) {
                            boolean doNonAdminsExist = true;
                            for(Player ap : session.getPlayers()) {
                                if(!session.isAdmin(ap)) {
                                    doNonAdminsExist = false;
                                    session.addAdmin(ap);
                                }
                            }
                            if(doNonAdminsExist) {
                                p.closeInventory();
                                Session.sendMessage(p, "§aEverybody is an §badmin §anow!");
                                session.broadcast("§d" + p.getName() + " §amade everybody an §badmin§a!", p);
                            }
                        } else if(slot > 8) {
                            Player ap = Bukkit.getPlayer(inv.getItem(slot).getItemMeta().getDisplayName().substring(2));
                            if(session.isInSession(ap) && !session.isAdmin(ap)) {
                                session.addAdmin(ap);
                                Session.sendMessage(p, "§aMade §b"+ap.getName()+" §aan §eadmin");
                            }
                            inv.setItem(slot, new ItemStack(Material.AIR));
                        }

                    }

                    else if(view.getTitle().equals(Inventories.EXTRA_MODES_INVENTORY_TITLE)){
                        if(slot == 1){
                            inv.setItem(1,(!session.withMultiweapons())? Weapon.DAGGER.getColoredItem(Lasertag.LasertagColor.Green, "§cDisable §nMultiweapons")
                                    : Weapon.DAGGER.getColoredItem(Lasertag.LasertagColor.Red, "§aEnable §nMultiweapons"));
                            session.setWithMultiWeapons(!session.withMultiweapons());
                        } else if(slot == 3){
                            inv.setItem(3,(!session.withGrenades())? Weapon.GRENADE.getColoredItem(Lasertag.LasertagColor.Green, "§cDisable §nGrenades")
                                    : Weapon.GRENADE.getColoredItem(Lasertag.LasertagColor.Red, "§aEnable §nGrenades"));
                            session.setWithGrenades(!session.withGrenades());
                        } else if(slot == 5){
                            inv.setItem(5,(!session.withPointEvents())?Utils.getItemStack(Material.GREEN_DYE,"§cDisable §nPoint Events")
                                    : Utils.getItemStack(Material.RED_DYE,"§aEnable §nPoint Events"));
                            session.setWithPointEvents(!session.withPointEvents());
                        } else if(slot == 7){
                            inv.setItem(7,(!session.withCaptureTheFlag())?Utils.getItemStack(Material.GREEN_BANNER,"§cDisable §nCapture the Flag")
                                    : Utils.getItemStack(Material.RED_BANNER,"§aEnable §nCapture the Flag"));
                            session.setWithCaptureTheFlag(!session.withCaptureTheFlag());
                        }
                    }
                }


                if (session.isTeams() && session.hasTeamChooseInvOpen.contains(p)) {
                    if (view.getTitle().equals(Inventories.TEAM_CHOOSE_INVENTORY_TITLE)) {
                        SessionTeam chosenTeam = SessionTeam.getTeamByChooserSlot(slot, session);
                        SessionTeam currentTeam = session.getPlayerTeam(p);

                        if (chosenTeam != currentTeam) {
                            session.addPlayerToTeam(p, chosenTeam);
                            for (Player ip : session.hasTeamChooseInvOpen) {
                                ip.getOpenInventory().getTopInventory().setItem(currentTeam.getTeamChooserSlot(), currentTeam.getTeamChooser());
                                ip.getOpenInventory().getTopInventory().setItem(chosenTeam.getTeamChooserSlot(), chosenTeam.getTeamChooser());
                            }
                            Session.sendMessage(p, "§aYou're now in " + chosenTeam.getChatColor() + "team " + chosenTeam.getLasertagColor());
                            LeatherArmorMeta meta = (LeatherArmorMeta) p.getInventory().getItem(p.getInventory().first(Material.LEATHER_CHESTPLATE)).getItemMeta();
                            meta.setColor(chosenTeam.getColor());
                            p.getInventory().getItem(p.getInventory().first(Material.LEATHER_CHESTPLATE)).setItemMeta(meta);
                        }
                    }
                }

                else if(session.votingMap() && view.getTitle().equals(Inventories.MAP_VOTE_INVENTORY_TITLE)) {
                    session.playerVoteMap(p, Map.getMapByName(inv.getItem(slot).getItemMeta().getDisplayName().toLowerCase().substring(0, inv.getItem(slot).getItemMeta().getDisplayName().length()-10)));
                    p.closeInventory();
                    p.getInventory().getItem(p.getInventory().first(Material.PAPER)).getItemMeta().setDisplayName("§eVoted for: §d"+Map.MAPS.get(slot).getName());
                }

                if(view.getTitle().equals(Inventories.SECONDARY_WEAPON_CHOOSER_INVENTORY_TITLE)){
                    int i = 1;
                    if(session.isAdmin(p)) i = 2;
                    if(session.isTeams() && session.isAdmin(p)) i = 3;
                    if(slot == 4 && session.isAdmin(p)){
                        session.setWithMultiWeapons(false);
                        p.closeInventory();
                    } else if(slot == 1) {
                        session.setPlayerSecondaryWeapon(p, Weapon.SHOTGUN);
                        Session.sendMessage(p, "§eYou chose §dShotgun §eas secondary weapon");
                        p.getInventory().setItem(i, Weapon.SHOTGUN.getColoredItem(session.getPlayerColor(p), "§eSecondary weapon: §bShotgun §7[§6click to change§7]"));
                        p.closeInventory();
                    } else if(slot == 7){
                        session.setPlayerSecondaryWeapon(p, Weapon.SNIPER);
                        Session.sendMessage(p, "§eYou chose §dSniper §eas secondary weapon");
                        p.getInventory().setItem(i, Weapon.SNIPER.getColoredItem(session.getPlayerColor(p), "§eSecondary weapon: §bSniper §7[§6click to change§7]"));
                        p.closeInventory();
                    } 
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCloseInventory(InventoryCloseEvent e) {
        try {
            Player p = (Player) e.getPlayer();
            InventoryView view = e.getView();
            Session session = Session.getPlayerSession(p);
            if(session == null) return;
            if(session.tagging()) return;
            if(session.getOwner() == p) {
                new BukkitRunnable(){
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void run() {
                        try {
                            if(p.getOpenInventory() != null && p.getOpenInventory().getTitle().equals(view.getTitle())) throw new NullPointerException();

                            if (view.getTitle().equals(Inventories.TIME_INVENTORY_TITLE)) {
                                if (!session.isTimeSet()) Inventories.openTimeInv(p);
                            } else if (view.getTitle().equals(Inventories.MAP_INVENTORY_TITLE)) {
                                if (session.isMapNull()) Inventories.openMapInv(p);
                            } else if (view.getTitle().equals(Inventories.TEAMS_INVENTORY_TITLE)) {
                                if (session.isTeams() && !session.isTeamsAmountSet()) Inventories.openTeamsInv(p);
                            } else if(view.getTitle().equals(Inventories.SECONDARY_WEAPON_CHOOSER_INVENTORY_TITLE)) {
                                if(session.withMultiweapons() && !session.isPlayerReady(p)) Inventories.openSecondaryWeaponChooserInv(p);
                            }
                        } catch (NullPointerException ignored){
                        }
                    }
                }.runTaskLater(minigames,5);
            }

            if(e.getInventory().contains(Material.LEATHER_CHESTPLATE) && view.getItem(e.getInventory().first(Material.LEATHER_CHESTPLATE)).getItemMeta().getDisplayName().toUpperCase().contains("RED")) {
                session.hasTeamChooseInvOpen.remove(p);
            }
        } catch (Exception ignored) {

        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Session session = Session.getPlayerSession(p);
        if(session == null) return;
        if(session.tagging()) return;
        if(session.waiting()) {
            if((e.getAction().equals(Action.RIGHT_CLICK_BLOCK) | e.getAction().equals(Action.RIGHT_CLICK_AIR)) && e.getItem() != null) {
                ItemStack item = e.getItem();
                if(item.getType() == Weapon.LASERGUN.getType() && !Lasertag.isPlayerTesting((p))) {
                    session.attemptStart(p);
                } else if(item.getType() == Material.PAPER) {
                    if(session.votingMap()) {
                        /*if(!session.hasPlayerVoted.get(p))*/ Inventories.openMapVoteInv(p);
//                        else if(session.isAdmin(p)) Inventories.openMapInv(p);
                    } else if(session.isAdmin(p)) Inventories.openMapInv(p);
                } else if(item.getType() == Material.LEATHER_CHESTPLATE) {
                    Inventories.openTeamChooseInv(p);
                } else if(item.getType() == Material.END_CRYSTAL) {
                    Inventories.openTeamsInv(p);
                } else if(item.getType() == Material.DIAMOND_HELMET) {
                    if(session.getPlayers().length > 1 && session.getAdmins().length < session.getPlayers().length) Inventories.openAddAdminInv(p);
                    else Session.sendMessage(p,"§cThere are no players to promote!");
                } else if(item.getType() == Material.CLOCK) {
                    Inventories.openTimeInv(p);
                } else if(item.getType() == Material.BARRIER) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(minigames, () -> session.leavePlayer(p), 1);
                } else if(session.withMultiweapons() && (item.getType() == Weapon.DAGGER.getType() || item.getType() == Weapon.SHOTGUN.getType() || item.getType() == Weapon.SNIPER.getType())) {
                    Inventories.openSecondaryWeaponChooserInv(p);
                } else if(item.getType().equals(Material.REDSTONE_TORCH)) {
                    Inventories.openExtraModesInv(p);
                }
            }
        }
        e.setCancelled(true);
    }
}
