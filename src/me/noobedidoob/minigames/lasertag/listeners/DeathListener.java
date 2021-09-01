package me.noobedidoob.minigames.lasertag.listeners;

import java.util.HashMap;

import me.noobedidoob.minigames.lasertag.methods.Weapon;
import me.noobedidoob.minigames.utils.Flag;
import me.noobedidoob.minigames.utils.Grenade;
import me.noobedidoob.minigames.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.PluginManager;

import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.lasertag.Lasertag;
import me.noobedidoob.minigames.lasertag.methods.Mod;
import me.noobedidoob.minigames.lasertag.session.Session;

public class DeathListener implements Listener {
	
	public DeathListener(Minigames minigames) {
		PluginManager pluginManeger = Bukkit.getPluginManager();
		pluginManeger.registerEvents(this, minigames);
	}
	
	public enum HitType {
		SHOT,
		PVP,
		GRENADE,
	}
	public static boolean hit(HitType type, Player killer, Player victim, double damage, boolean headshot, boolean snipe, boolean backstab) {
		Session session = Session.getPlayerSession(killer);
		if(session == null) return false;
		if(!session.isInSession(victim)) return false;
		
		if (victim.getGameMode() == GameMode.ADVENTURE) {
			if (damage < victim.getHealth()) {
				if(headshot) damage *= session.getDoubleMod(Mod.HEADSHOT_DAMAGE_MULTILIER);
				if(snipe) damage *= session.getDoubleMod(Mod.SNIPE_SHOT_DAMAGE_MULTIPLIER);
				victim.damage(damage);
				if(type == HitType.SHOT | type == HitType.GRENADE) killer.playSound(killer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0);
				return false;
			} else {
				int points = session.getIntMod(Mod.POINTS);
				if(backstab) points += session.getIntMod(Mod.BACKSTAB_EXTRA_POINTS);
				else {
					if(headshot) points += session.getIntMod(Mod.HEADSHOT_EXTRA_POINTS);
					if(snipe) points += session.getIntMod(Mod.SNIPER_KILL_EXTRA_POINTS);
				}
				switch (type) {
					case SHOT:
						points += session.getIntMod(Mod.SHOT_KILL_EXTRA_POINTS);
						session.addPoints(killer, points, session.getPlayerColor(killer).getChatColor()+killer.getName()+" §7§o"+((snipe)?"sniped":"shot")+" §r"+session.getPlayerColor(victim).getChatColor()+victim.getName()+((headshot)?" §7[§d§nHEADSHOT§r§7] ": "")+" §7(§a+"+points+" point"+((points > 1)?"s":"")+"§7)");
						break;
					case PVP:
						points += session.getIntMod(Mod.PVP_KILL_EXTRA_POINTS);
						session.addPoints(killer, points, session.getPlayerColor(killer).getChatColor()+killer.getName()+" §7§okilled §r"+session.getPlayerColor(victim).getChatColor()+victim.getName()+((backstab)?" §7[§d§nBACKSTAB§r§7] ":"")+" §7(§a+"+points+" point"+((points > 1)?"s":"")+"§7)");
						break;
					case GRENADE:
						if(killer == victim){
							for (Player p : session.getPlayers()) {
								p.sendMessage(session.getPlayerColor(killer).getChatColor()+killer.getName()+" §7§oblew himself up");
							}
						} else {
							points += session.getIntMod(Mod.GRENADE_KILL_EXTRA_POINTS);
							session.addPoints(killer,points,session.getPlayerColor(killer).getChatColor()+killer.getName()+" §7§oblew §r"+session.getPlayerColor(victim).getChatColor()+victim.getName()+" §7§oup "+" §7(§a+"+points+" point"+((points > 1)?"s":"")+"§7)");
						}
				default:
					break;
				}
				if(victim.getPassengers().size() > 0){
					for (Entity passenger : victim.getPassengers()) {
						victim.removePassenger(passenger);
						if(passenger instanceof Snowball){
							Grenade.explodeGrenade((Snowball) passenger, killer);
							victim.setGlowing(false);
						}
					}
				}
				victim.damage(100);
				STREAKED_PLAYERS.putIfAbsent(victim, 0);
				Utils.runLater(()->{
					if(killer != victim) addStreak(killer);
					if (STREAKED_PLAYERS.get(victim) >= session.getIntMod(Mod.MINIMAL_KILLS_FOR_STREAK)) streakShutdown(killer, victim);
					STREAKED_PLAYERS.put(victim, 0);
				},5);
				Lasertag.setPlayerProtected(victim, true);
				return true;
			} 
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerDies(PlayerDeathEvent e) {
		Player p = e.getEntity();
		e.setDeathMessage("");
		STREAKED_PLAYERS.put(p, 0);
		if(Session.isPlayerInSession(p)) {
			if (Session.getPlayerSession(p).withCaptureTheFlag() && Flag.hasPlayerFlag(p)) {
				Flag.getPlayerFlag(p).drop(p.getLocation());
			}
		}
		for (Weapon w: Weapon.values() ) {
			p.setCooldown(w.getType(), 1);
		}
	}
	
	
	private static final HashMap<Player, Integer> STREAKED_PLAYERS = new HashMap<>();
	private static void addStreak(Player p) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return;
		STREAKED_PLAYERS.putIfAbsent(p, 0);
		int streak = STREAKED_PLAYERS.get(p)+1;
		STREAKED_PLAYERS.put(p,streak);
		if(streak >= session.getIntMod(Mod.MINIMAL_KILLS_FOR_STREAK)) {
			session.addPoints(p, session.getIntMod(Mod.STREAK_EXTRA_POINTS),"——§e"+session.getPlayerColor(p).getChatColor()+p.getName()+" §dHas a streak of §a"+streak+"§d! §7(§a+"+session.getIntMod(Mod.STREAK_EXTRA_POINTS)+" extra Point"+((session.getIntMod(Mod.STREAK_EXTRA_POINTS) > 1)?"s":"")+"§7)§e——");
		}
	}
	private static void streakShutdown(Player killer, Player victim) {
		Session session = Session.getPlayerSession(killer);
		if(session == null) return;
		STREAKED_PLAYERS.put(victim, 0);
		session.addPoints(killer, session.getIntMod(Mod.STREAK_SHUTDOWN_EXTRA_POINTS),"——§e"+session.getPlayerColor(killer).getChatColor()+killer.getName()+" §dended the streak of "+session.getPlayerColor(victim).getChatColor()+victim.getName()+"§d! §7(§a+"+session.getIntMod(Mod.STREAK_EXTRA_POINTS)+" extra Point"+((session.getIntMod(Mod.STREAK_EXTRA_POINTS) > 1)?"s":"")+"§7)§e——");
	}
	public static void resetPlayerStreak(Player p) {
		STREAKED_PLAYERS.put(p, 0);
	}
	
}
