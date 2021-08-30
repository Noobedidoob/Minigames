package me.noobedidoob.minigames.lasertag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.noobedidoob.minigames.lasertag.commands.ModifierCommands;
import me.noobedidoob.minigames.lasertag.listeners.*;
import me.noobedidoob.minigames.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import me.noobedidoob.minigames.lasertag.commands.SessionCommands;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.lasertag.methods.Inventories;
import me.noobedidoob.minigames.lasertag.methods.Mod;
import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.utils.Area;
import me.noobedidoob.minigames.utils.Coordinate;
import me.noobedidoob.minigames.utils.Map;

public class Lasertag implements Listener{
	public Minigames minigames;
	public static Lasertag INSTANCE;

	public Lasertag(Minigames minigames) {
		INSTANCE = this;
		this.minigames = minigames;
		
		new InteractListener(minigames);
		new HitListener(minigames);
		new DeathListener(minigames);
		new MoveListener(minigames);
		new JoinQuitListener(minigames);
		new DamageListener(minigames);
		new DropSwitchItemListener(minigames);
		new RespawnListener(minigames);

		new InventoryListener(minigames);
	}

	public void enable() {
		LaserCommands laserCommands = new LaserCommands(minigames);
		minigames.getCommand("lasertag").setExecutor(laserCommands);
		minigames.getCommand("lasertag").setTabCompleter(laserCommands);

		SessionCommands sessionCommands = new SessionCommands(minigames);
		minigames.getCommand("session").setExecutor(sessionCommands);
		minigames.getCommand("session").setTabCompleter(sessionCommands);

		ModifierCommands modifierCommands = new ModifierCommands();
		minigames.getCommand("modifier").setExecutor(modifierCommands);
		minigames.getCommand("modifier").setTabCompleter(modifierCommands);

		
		Bukkit.getPluginManager().registerEvents(this, minigames);
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.setExp(1f);
			p.setLevel(0);
			setPlayersLobbyInv(p);
		}
		
		registerMaps();
		Mod.registerMods(minigames);
		
		
	}
	public void disable() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.setWalkSpeed(0.2f);
		}
		Session.shutdownAllSessions();
	}
	

	private static final Area TEST_AREA = new Area(194, 4, -98, 246, 22, -67);
	public static Area getTestAera() {
		return TEST_AREA;
	}
	private static final HashMap<Player, Boolean> PLAYER_TESTING = new HashMap<>();
	public static boolean isPlayerTesting(Player p) {
		PLAYER_TESTING.putIfAbsent(p, false);
		return PLAYER_TESTING.get(p);
	}
	public static void setPlayerTesting(Player p, boolean testing) {
		PLAYER_TESTING.put(p, testing);
	}
	private static final HashMap<Player, Boolean> PLAYER_PROTECTED = new HashMap<>();
	public static boolean isPlayerProtected(Player p) {
		PLAYER_PROTECTED.putIfAbsent(p, false);
		return PLAYER_PROTECTED.get(p);
	}
	public static void setPlayerProtected(Player p, boolean testing) {
		PLAYER_PROTECTED.put(p, testing);
	}

	

	
	public void registerMaps() {
		ConfigurationSection cs = minigames.getConfig().getConfigurationSection("Lasertag.maps");
		if(cs == null) return;
		for(String name : cs.getKeys(false)) {
			Coordinate coord1 = new Coordinate(cs.getInt(name+".area.x.min"), cs.getInt(name+".area.y.min"), cs.getInt(name+".area.z.min"));
			Coordinate coord2 = new Coordinate(cs.getInt(name+".area.x.max"), cs.getInt(name+".area.y.max"), cs.getInt(name+".area.z.max"));
			Map map = new Map(name, new Area(coord1, coord2), minigames.world);
			
			boolean withRandomSpawn = cs.getBoolean(name+".area.randomspawn");
			map.setWithRandomSpawn(withRandomSpawn);
			
			boolean withBaseSpawn = cs.getBoolean(name+".basespawn.enabled");
			map.setWithBaseSpawn(withBaseSpawn);

			boolean withCTF = cs.getBoolean(name+".basespawn.capture-the-flag");
			map.setWithCaptureTheFlag(withCTF);
			
			if(withBaseSpawn) {
				ConfigurationSection subCs = cs.getConfigurationSection(name+".basespawn");
				if(subCs == null) return;
				for(String colorName : subCs.getKeys(false)) {
					if(!colorName.equalsIgnoreCase("enabled") && !colorName.equalsIgnoreCase("capture-the-flag") && !colorName.equalsIgnoreCase("protectionradius")) {
						LasertagColor baseColor = LasertagColor.getFromString(colorName);
						Coordinate baseCoord = new Coordinate(subCs.getDouble(colorName+".x"), subCs.getDouble(colorName+".y"), subCs.getDouble(colorName+".z"));
						Coordinate baseFlagCoord = (withCTF)? new Coordinate(subCs.getDouble(colorName+".flag.x"),subCs.getDouble(colorName+".flag.y"),subCs.getDouble(colorName+".flag.z")):null;
						map.setTeamSpawnCoords(baseColor, baseCoord, baseFlagCoord);
					}
				}
				map.setProtectionRaduis(cs.getInt(name+".basespawn.protectionradius"));
			}
		}
	}
	
	
	public enum LasertagColor {
		Red(ChatColor.RED, 255, 0, 0),
		Blue(ChatColor.BLUE, 0, 160, 255),
		Green(ChatColor.GREEN, 100, 255, 0),
		Yellow(ChatColor.YELLOW, 255, 255, 0),
		Purple(ChatColor.LIGHT_PURPLE, 150, 0, 255),
		Gray(ChatColor.GRAY, 150, 150, 150),
		Orange(ChatColor.GOLD, 255, 150, 0),
		White(ChatColor.WHITE, 255, 255, 255);
		
		private final Color color;
		private final ChatColor chatColor;
		
		LasertagColor(ChatColor chatColor, int r, int g, int b) {
			this.chatColor = chatColor;
			this.color = Color.fromRGB(r, g, b);
		}

		public ChatColor getChatColor() {
			return chatColor;
		}
		public Color getColor() {
			return color;
		}
		
		public String getName() {
			return this.name();
		}
		
		public static LasertagColor getFromString(String s) {
			for(LasertagColor name : LasertagColor.values()) {
				if(name.name().equalsIgnoreCase(s)) return name;
			}
			return null;
		}
	}
	public static void animateExpBar(Player p, long ticks) {
		p.setExp(0);
		new BukkitRunnable() {
			final float funit = 1f/ticks;
			float f = 0f;
			float t = 0;
			@Override
			public void run() {
				if(t < ticks) {
					t++;
					f = f + funit;
					p.setExp(f);
				} else {
					cancel();
					p.setExp(1f);
				}
			}
		}.runTaskTimer(Minigames.INSTANCE, 0, 1);
	}
	
	public static void setPlayersLobbyInv(Player p) {
		p.getInventory().clear();
		p.getInventory().setItem(0, Utils.getItemStack(Material.COMPASS,"§aFind sessions"));
		p.getInventory().setItem(1, Utils.getItemStack(Material.NETHER_STAR,"§eCreate new session"));
	}

	//TODO	implement grenade
	public static void openPlayerFindSessionInv(Player p) {
		Inventory inv = Bukkit.createInventory(null, (((Session.getAllSessions().length-1)/9)+1)*9, "§0Join a session:");
		
		int i = 0;
		for(Session session : Session.getAllSessions()) {
			if(!session.tagging()) {
				List<String> lore = new ArrayList<>();
				lore.add("§7 - §f"+((session.isSolo()?"Solo":session.getTeamsAmount()+" Teams"))+((!session.withMultiweapons()&&!session.withCaptureTheFlag())?"":(session.withCaptureTheFlag() && session.withMultiweapons())?" §7(§6MultiWeapons§7, §6Capture the Flag§7)":" §7(§6"+((session.withMultiweapons())?"MultiWeapons":"Capture the Flag")+"§7)"));
				if(session.getPlayers().length > 1){
					lore.add("§7 - §7Players:");
					for(Player a : session.getAdmins()) {
						if(a != session.getOwner()) lore.add("§b   "+a.getName());
					}
					for(Player ap : session.getPlayers()) {
						if(!session.isAdmin(ap)) lore.add("§a   "+ap.getName());
					}
				}
				inv.setItem(i++, Utils.getPlayerSkullItem(session.getOwner(),"§d"+session.getOwner().getName()+"'s §asession", lore));
			}
		}
		
		p.openInventory(inv);
	}
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getItem() == null | !e.getAction().name().contains("RIGHT")) return;
		if(e.getItem().getType() == Material.COMPASS) {
			openPlayerFindSessionInv(e.getPlayer());
		} else if(e.getItem().getType() == Material.NETHER_STAR) {
			Inventories.openNewSessionInv(e.getPlayer());
		}
	}
	@EventHandler
	public void onPlayerClickInventory(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		try {
			if(e.getClickedInventory().getItem(e.getSlot()) == null) return;
		} catch (Exception exception){
			return;
		}
		if(e.getSlot() < e.getInventory().getSize()+1 && e.getInventory().getItem(e.getSlot()) != null && e.getInventory().getItem(e.getSlot()).getType().equals(Material.PLAYER_HEAD) && e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().contains("session")) {
			Session s = Session.getSessionFromName(e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().replaceAll("§a", "").replaceAll("§d", "").replaceAll("'s session", "").replaceAll(" ", ""));
			if(s != null) {
				if(!s.isPlayerBanned((Player) e.getWhoClicked())) {
					if(!s.tagging()) {
						p.closeInventory();
						s.addPlayer((Player) e.getWhoClicked());
					} else Session.sendMessage(p, "§cThe session is already running! Please wait!");
				} else Session.sendMessage(p, "§cYou've been banned from this session! Ask the owner to unban you!");
			} else Session.sendMessage(p, "§cError occured! Couldn't find session!");
		}
	}

	@EventHandler
	public void onPlayerChangeGameMode(PlayerGameModeChangeEvent e){
		if(Session.getPlayerSession(e.getPlayer()) != null && Session.getPlayerSession(e.getPlayer()).tagging()) e.setCancelled(true);
	}
	
	
	
	
	
	
	public static Logger logger = Bukkit.getLogger();
	@SuppressWarnings("unused")
	public static void inform(String msg) {
		logger.log(Level.INFO, "[LasetTag] "+msg);
	}
	@SuppressWarnings("unused")
	public static void warn(String msg) {
		logger.warning("[LasetTag] "+msg);
	}
	@SuppressWarnings("unused")
	public static void severe(String msg) {
		logger.severe("[LasetTag] "+msg);
	}
	
}