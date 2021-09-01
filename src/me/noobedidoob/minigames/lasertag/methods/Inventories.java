package me.noobedidoob.minigames.lasertag.methods;

import me.noobedidoob.minigames.lasertag.Lasertag.LasertagColor;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.lasertag.session.SessionTeam;
import me.noobedidoob.minigames.utils.Map;
import me.noobedidoob.minigames.utils.Utils;
import me.noobedidoob.minigames.utils.Utils.TimeFormat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Inventories implements Listener{
	
	public static ItemStack getAdditionItem(String displayName) {
		return Utils.getItemStack(Material.LIME_STAINED_GLASS_PANE, displayName);
	}
	public static ItemStack getSubtractionItem(String displayName) {
		return Utils.getItemStack(Material.RED_STAINED_GLASS_PANE, displayName);
	}

	public static final String NEW_SESSION_INVENTORY_TITLE = "§0Set teams amount (1 = §1solo§0)";
	public static void openNewSessionInv(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, NEW_SESSION_INVENTORY_TITLE);

		inv.setItem(2, getSubtractionItem("§c§l-1 §r§bTeam"));
		inv.setItem(4, Utils.getItemStack(Material.BARRIER, "§cNo Teams -> §7(§bSOLO§7)"));
		inv.setItem(6, getAdditionItem("§a§l+1 §r§bTeam"));
		inv.setItem(8, Weapon.LASERGUN.getItem("§aNext"));
		
		p.closeInventory();
		p.openInventory(inv);
	}

	public static final String TIME_INVENTORY_TITLE = "§1Set session time (in minutes)";
	public static void openTimeInv(Player p) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return;

		Inventory inv = Bukkit.createInventory(null, 9, TIME_INVENTORY_TITLE);

		long time = 5;
		if(session.isTimeSet()) time = session.getTime(TimeFormat.MINUTES);
		if(session.getTime(TimeFormat.SECONDS) < 60) time = 1;

		inv.setItem(2, getSubtractionItem("§c§l-1 §r§cminute"));
		inv.setItem(4, Utils.getItemStack(Material.CLOCK, "§bTime: §r"+time+" Minutes", (int) time));
		inv.setItem(6, getAdditionItem("§a§l+1 §r§aminute"));
		inv.setItem(8, Weapon.LASERGUN.getItem("§aNext"));
		
		p.closeInventory();
		p.openInventory(inv);
	}

	public static final String MAP_INVENTORY_TITLE = "§1Choose Map";
	public static void openMapInv(Player p) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return;


		Inventory inv = Bukkit.createInventory(null, 9+(9*((Map.MAPS.size()-1)/9+1)), MAP_INVENTORY_TITLE);
		inv.setItem(4, Utils.getItemStack(Material.PURPLE_STAINED_GLASS_PANE, "§aLet players vote"));
		
		int i = 9;
		for(Map m : Map.MAPS) {
			if(session.isMapPlayable(m)) {
				inv.setItem(i++, Utils.getItemStack(Material.FILLED_MAP, "§r§b"+m.getName()));
			}
		}
		
		p.closeInventory();
		p.openInventory(inv);
	}

	public static final String TEAMS_INVENTORY_TITLE = "§5Set amount of teams";
	public static void openTeamsInv(Player p) {
		if(!Session.isPlayerInSession(p)) return;
		openTeamsInv(p, Session.getPlayerSession(p).getTeamsAmount());
	}
	public static void openTeamsInv(Player p, int teamAmount) {
		if(!Session.isPlayerInSession(p)) return;
		Inventory inv = Bukkit.createInventory(null, 9, TEAMS_INVENTORY_TITLE);
		
		if(teamAmount < 2) {
			inv.setItem(4, Utils.getItemStack(Material.BARRIER, "§cSolo"));
		} else {
			inv.setItem(4, Utils.getLeatherArmorItem(Material.LEATHER_CHESTPLATE, "§aTeams: §b"+teamAmount, LasertagColor.Red.getColor(), teamAmount));
		}

		

		inv.setItem(2, getSubtractionItem("§c§l-1 §r§bTeam"));
		inv.setItem(6, getAdditionItem("§a§l+1 §r§bTeam"));
		inv.setItem(8, Weapon.LASERGUN.getItem("§aNext"));
		
		p.closeInventory();
		p.openInventory(inv);
	}

	public static void setPlayerSessionWaitingInv(Player p) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return;
		for(int i = 0; i < 9; i++) {
			p.getInventory().setItem(i, new ItemStack(Material.AIR));
		}

		String mapTitle = (!session.votingMap() && session.getMap() != null)? "§eMap: §b"+session.getMap().getName() : (session.playerVotes.get(p) != null ? "§eVoted for: §b"+session.playerVotes.get(p).getName() : "§eVote map");
		if(session.isAdmin(p) && session.votingMap()) mapTitle += " §o§7[§6Left-Click to change§7]";
		ItemStack map = /*new ItemStack(Material.PAPER);*/ Utils.getItemStack(Material.PAPER, mapTitle);


		if(session.isTeams()) {
			ItemStack team = /*new ItemStack(Material.LEATHER_CHESTPLATE);*/ Utils.getLeatherArmorItem(Material.LEATHER_CHESTPLATE, session.getPlayerColor(p).getChatColor()+"Change team",session.getPlayerColor(p).getColor(), 1);

			if (session.isAdmin(p)) {
				p.getInventory().setItem(1, team);
				p.getInventory().setItem(2, map);
			} else {
				p.getInventory().setItem(0, team);
				p.getInventory().setItem(1, map);
			}
		} else {
			p.getInventory().setItem((session.isAdmin(p))?1:0, map);
		}

		int addon = (session.isTeams())?1:0;
		if(session.withMultiweapons()) {
			ItemStack weapon = (session.getPlayerSecondaryWeapon(p) != null)? session.getPlayerSecondaryWeapon(p).getColoredItem(session.getPlayerColor(p), "§eSecondary weapon: §d"+session.getPlayerSecondaryWeapon(p).getName()+" §7[§6click to change§7]"):Weapon.DAGGER.getColoredItem(session.getPlayerColor(p),"§eChoose your secondary Weapon");
			p.getInventory().setItem(((session.isAdmin(p))?2:1)+addon, weapon);
		}
		if(session.isAdmin(p)) {
			if(session.withMultiweapons()) addon++;
			if(6+addon >= 7) addon--;

			p.getInventory().setItem(0, Weapon.LASERGUN.getItem("§a§lSTART"));
			p.getInventory().setItem(3+addon, Utils.getItemStack(Material.CLOCK,"§bChange time"));
			p.getInventory().setItem(4+addon, Utils.getItemStack(Material.REDSTONE_TORCH,"§dExtra modes"));
			p.getInventory().setItem(5+addon, Utils.getItemStack(Material.END_CRYSTAL,"§6Change mode"));
			p.getInventory().setItem(6+addon, Utils.getItemStack(Material.PLAYER_HEAD,"§eInvite players"));
		}

		p.getInventory().setItem(8, Utils.getItemStack(Material.BARRIER,"§cLeave"));
	}

	public static final String MAP_VOTE_INVENTORY_TITLE ="§1Vote for a Map";
	public static void openMapVoteInv(Player p) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return;
		
		Inventory inv = Bukkit.createInventory(null, 9*((Map.MAPS.size()-1)/9+1), MAP_VOTE_INVENTORY_TITLE);
		
		int i = 0;
		for(Map m : Map.MAPS) {
			if(session.isMapPlayable(m)) {
				inv.setItem(i++, Utils.getItemStack(Material.FILLED_MAP,m.getName()+" §7(§a"+ ((session.getMapVotes().get(m) != null)?session.getMapVotes().get(m):0)+"§7)"));
			}
		}
		
		p.openInventory(inv);
	}

	public static final String TEAM_CHOOSE_INVENTORY_TITLE = "§1Choose your team!";
	public static void openTeamChooseInv(Player p) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return;
		session.hasTeamChooseInvOpen.add(p);

		Inventory inv = Bukkit.createInventory(null, 9, TEAM_CHOOSE_INVENTORY_TITLE);

		for(SessionTeam t : session.getTeams()) {
			inv.setItem(t.getTeamChooserSlot(), t.getTeamChooser());
		}

		p.openInventory(inv);
	}

	public static final String INVITE_PLAYERS_INVENTORY_TITLE = "§1Choose players to invite:";
	public static void openInviteInv(Player p) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return;
		
		Inventory inv = Bukkit.createInventory(null, 9+(9*((session.getPlayers().length-session.getAdmins().length-1)/9+1)), INVITE_PLAYERS_INVENTORY_TITLE);

		inv.setItem(4, Utils.getItemStack(Material.BLUE_STAINED_GLASS_PANE, "§aInvite everyone"));
		
		int i = 9;
		for(Player op : Bukkit.getOnlinePlayers()) {
			if (op != p && !session.isInSession(op) && !session.invitedPlayers.contains(op)) {
				inv.setItem(i++, Utils.getPlayerSkullItem(op,(Session.getPlayerSession(op) == null ? "§a" : "§c")+op.getName()));
			}
		}
		
		p.openInventory(inv);
	}

	public static final String SECONDARY_WEAPON_CHOOSER_INVENTORY_TITLE = "§0Choose your secondary weapon!";
	public static void openSecondaryWeaponChooserInv(Player p) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return;
		
		Inventory weaponsInv = Bukkit.createInventory(null, 9, SECONDARY_WEAPON_CHOOSER_INVENTORY_TITLE);

		LasertagColor playerColor = session.getPlayerColor(p);
		ItemStack shotgun = Weapon.SHOTGUN.getColoredItem(playerColor);
		ItemStack sniper = Weapon.SNIPER.getColoredItem(playerColor);
		ItemStack disable = Utils.getItemStack(Material.BARRIER,"§cDisable Multiweapons");

		weaponsInv.setContents(new ItemStack[] {null, shotgun, null, null, (session.isAdmin(p))?disable:null, null, null, sniper, null});
		p.closeInventory();
		p.openInventory(weaponsInv);
	}

	public static final String EXTRA_MODES_INVENTORY_TITLE ="§0Choose Extra mode:";
	public static void openExtraModesInv(Player p){
		Session session = Session.getPlayerSession(p);
		if(session == null) return;
		Inventory inv = Bukkit.createInventory(null, 9, EXTRA_MODES_INVENTORY_TITLE);

		inv.setItem(1,(session.withMultiweapons())?Weapon.DAGGER.getColoredItem(LasertagColor.Green, "§cDisable §nMultiweapons")
				: Weapon.DAGGER.getColoredItem(LasertagColor.Red,"§aEnable §nMultiweapons"));
		inv.setItem(3,(session.withGrenades())? Weapon.GRENADE.getColoredItem(LasertagColor.Green, "§cDisable §nGrenades")
				: Weapon.GRENADE.getColoredItem(LasertagColor.Red, "§aEnable §nGrenades"));
		inv.setItem(5,(session.withPointEvents())? Utils.getItemStack(Material.GREEN_DYE,"§cDisable §nPoint Events")
				: Utils.getItemStack(Material.RED_DYE,"§aEnable §nPoint Events"));
		inv.setItem(7,(session.withCaptureTheFlag())? Utils.getItemStack(Material.GREEN_BANNER,"§cDisable §nCapture the Flag")
				: Utils.getItemStack(Material.RED_BANNER,"§aEnable §nCapture the Flag"));
		p.closeInventory();
		p.openInventory(inv);
	}
}
