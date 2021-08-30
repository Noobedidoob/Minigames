package me.noobedidoob.minigames.lasertag.methods;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.utils.Area;
import me.noobedidoob.minigames.utils.Coordinate;

public class PlayerTeleporter {
	
	
	public static Location getPlayerSpawnLoc(Player p) {
		Session session = Session.getPlayerSession(p);
		if(session == null) return Minigames.INSTANCE.spawn;
		if(session.isTeams()) {
			if(session.getMap().withBaseSpawn()) {
				return session.getMap().getTeamSpawnLoc(session.getTeamColor(session.getPlayerTeam(p)));
			} else {
				return session.getMap().getRandomSpawnLocation();
			}
		} else {
			if(session.withCaptureTheFlag() && session.getMap().withCaptureTheFlag() && session.getMap().withBaseSpawn()){
				return session.getMap().getTeamSpawnLoc(session.getPlayerColor(p));
			}
			if(!session.getMap().withRandomSpawn()) {
				return session.getMap().getTeamSpawnLoc(session.getPlayerColor(p));
			} else {
				return session.getMap().getRandomSpawnLocation();
			}
		}
	}
	
	
	
	public static void gatherPlayers(List<Player> winners) {
		Session session = Session.getPlayerSession(winners.get(0));
		if(session == null) return;
		for(Player p : session.getPlayers()) {
			if(winners.contains(p)) {
				p.teleport(Minigames.INSTANCE.spawn);
			}
			else p.teleport(new Area(new Coordinate(Minigames.INSTANCE.spawn.subtract(5, 0, 5)), new Coordinate(Minigames.INSTANCE.spawn.add(5, 0, 5))).getRandomCoordinate().getLocation());
		}
	}
	
	
}
