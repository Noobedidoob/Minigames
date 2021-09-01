package me.noobedidoob.minigames.lasertag.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import me.noobedidoob.minigames.lasertag.methods.PlayerZoomer;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.Minigames;

public class JoinQuitListener implements Listener {
	
	public JoinQuitListener(Minigames minigames) {
		PluginManager pluginManeger = Bukkit.getPluginManager();
		pluginManeger.registerEvents(this, minigames);
	}
	
	
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Session session = Session.getPlayerSession(p);
		if(session == null) {
			for (Session s : Session.getAllSessions()) {
				if(s.invitedPlayers.contains(p)) session.invitedPlayers.remove(p);
			}
		} else {
			session.disconnectPlayer(p);
			e.setQuitMessage("");
			for(Player op : Bukkit.getOnlinePlayers()) {
				if(!session.isInSession(op)) {
					op.sendMessage("§e"+p.getName()+" left");
				}
			}
			PlayerZoomer.zoomPlayerOut(p);
		}

	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		for(Session session : Session.getAllSessions()) {
			if(session.disconnectedPlayers.get(p.getUniqueId()) != null) {
				session.reconnectPlayer(p);
//				p.teleport(PlayerTeleporter.getPlayerSpawnLoc(p));
				e.setJoinMessage("");
				for(Player op : Bukkit.getOnlinePlayers()) {
					if(!session.isInSession(op)) {
						op.sendMessage("§e"+p.getName()+" joined");
					}
				}
				return;
			}
		}

	}
}