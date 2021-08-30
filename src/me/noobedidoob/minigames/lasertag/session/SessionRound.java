package me.noobedidoob.minigames.lasertag.session;

import java.util.*;

import me.noobedidoob.minigames.lasertag.methods.Mod;
import me.noobedidoob.minigames.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.noobedidoob.minigames.lasertag.Lasertag;
import me.noobedidoob.minigames.lasertag.Lasertag.LasertagColor;
import me.noobedidoob.minigames.lasertag.listeners.DeathListener;
import me.noobedidoob.minigames.lasertag.methods.PlayerTeleporter;
import me.noobedidoob.minigames.lasertag.methods.PlayerZoomer;
import me.noobedidoob.minigames.lasertag.methods.Weapon;
import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.utils.Utils.TimeFormat;

public class SessionRound {
	
	private final Session session;

	public SessionRound(Session session) {
		this.session = session;
	}
	
	

	public boolean tagging = false;
	public boolean tagging() {
		return tagging;
	}
	public void setTagging(boolean tagging) {
		this.tagging = tagging;
	}
	
	private BukkitTask timer;
	public void start() {
		if(tagging) return;
		tagging = true;
		for(Player p : session.getPlayers()) {
			p.sendTitle("§a§lGo!", "Kill the players of the other teams", 20, 20*4, 20);
			p.setGameMode(GameMode.ADVENTURE);
			p.getWorld().setDifficulty(Difficulty.PEACEFUL);
			p.teleport(PlayerTeleporter.getPlayerSpawnLoc(p));
			Lasertag.setPlayerProtected(p, true);
			p.sendTitle("§l§aGo!", "", 5, 20, 5);
			Lasertag.setPlayerTesting(p, false);
			DeathListener.resetPlayerStreak(p);
		}
		timer = new BukkitRunnable() {
			@Override
			public void run() {
				if(session.getTime(TimeFormat.SECONDS) > 0) {
					if(session.getTime(TimeFormat.SECONDS) <= 5 && session.getTime(TimeFormat.SECONDS) > 0) {
						for(Player p : session.getPlayers()) {
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2, 0.529732f);
							p.sendTitle("§c"+session.getTime(TimeFormat.SECONDS), "", 0, 21, 5);
						}
					}
					session.setTime(session.getTime(TimeFormat.SECONDS)-1, TimeFormat.SECONDS, false);
//					scoreboard.refresh();
				} else {
					cancel();
					stop(false);
				}
			}
		}.runTaskTimer(Minigames.INSTANCE, 0, 20);
	}
	
	public void preparePlayers() {
		for(Player p : session.getPlayers()) {
			p.setLevel(0);
			setPlayerGameInv(p);
		}
	}
	
	public void setPlayerGameInv(Player p) {
		LasertagColor playerColor = session.getPlayerColor(p);
		p.getInventory().clear();
		p.getInventory().setItem(0, Weapon.LASERGUN.getColoredItem(playerColor));
		if(session.withGrenades()) p.getInventory().setItem((session.withMultiweapons()?3:1), Weapon.GRENADE.getColoredItem(playerColor, session.getIntMod(Mod.GRENADE_MIN_DETONATION_COUNTDOWN)));

		if(session.withMultiweapons()){
			p.getInventory().setItem(1, Weapon.DAGGER.getColoredItem(playerColor));
			ItemStack secondaryWeapon = session.getPlayerSecondaryWeapon(p).getColoredItem(playerColor);
			if(session.getPlayerSecondaryWeapon(p) == Weapon.SNIPER) secondaryWeapon.setAmount(session.getIntMod(Mod.SNIPER_AMMO));
			p.getInventory().setItem(2, secondaryWeapon);
		}

		if (session.isTeams()) {
			p.getInventory().setChestplate(Utils.getLeatherArmorItem(Material.LEATHER_CHESTPLATE, playerColor.getChatColor()+playerColor.getName()+" team armor", playerColor.getColor(),1));
			p.getInventory().setLeggings(Utils.getLeatherArmorItem(Material.LEATHER_LEGGINGS, playerColor.getChatColor()+playerColor.getName()+" team armor", playerColor.getColor(),1));
			p.getInventory().setBoots(Utils.getLeatherArmorItem(Material.LEATHER_BOOTS, playerColor.getChatColor()+playerColor.getName()+" team armor", playerColor.getColor(),1));
		}

	}
	
	
	public void stop(boolean externalStop) {
		tagging = false;
		if(externalStop) {
			if(timer != null) timer.cancel();
			for(Player p : session.getPlayers()) {
				p.setGlowing(false);
				p.sendTitle("§cStopped the game!","",20, 20*4, 20);
				session.setAllPlayersInv();
				p.removePotionEffect(PotionEffectType.GLOWING);
			}
			Minigames.teleportPlayersToSpawn(session.getPlayers());
		} else {
			if(session.isSolo()) evaluateSolo();
			else evaluateTeams();
		}
		

		for(Player p : session.getPlayers()) {
			PlayerZoomer.zoomPlayerOut(p);
			p.setGlowing(false);
		}
		
		session.stop(false, false);
		
	}
	
	
	private void evaluateSolo() {

		Bukkit.getScheduler().scheduleSyncDelayedTask(Minigames.INSTANCE, () -> {
			for(Player p : session.getPlayers()) {
				try {
					p.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
				} catch (NullPointerException e) {
					Minigames.warn("Error occurred when resetting players "+p.getName()+"' scoreboard");
				}
			}
		}, 20*10);
		List<Player> winners;
		winners = new ArrayList<>();
		int amount = 0;
		for(Player p : session.getPlayers()) {
			if(session.getPlayerPoints(p) > amount) {
				amount = session.getPlayerPoints(p);
				winners = new ArrayList<>();
				winners.add(p);
			} else if(session.getPlayerPoints(p) == amount) {
				winners.add(p);
			}
		}
		StringBuilder winnerTeamsString = new StringBuilder();
		int i = 0;
		for(Player p : winners) {
			if(winners.size() > 2) {
				if(i == 0) winnerTeamsString = new StringBuilder(p.getName());
				else if(i < winners.size()) winnerTeamsString.append("§a, §d").append(p.getName());
				else winnerTeamsString.append(" §aand §d").append(p.getName());
			} else if(winners.size() == 2) {
				if(i == 0) winnerTeamsString = new StringBuilder(p.getName());
				else if(i == 1) winnerTeamsString.append(" §aand §d").append(p.getName());
			} else {
				winnerTeamsString = new StringBuilder(p.getName());
			}
			i++;
		}
		
		
		HashMap<Integer, List<Player>> playersInSorted = new HashMap<>();
		
		int maxScore = 0;
		for(Player p : session.getPlayers()) {
			List<Player> newList = new ArrayList<>();
			if (playersInSorted.get(session.getPlayerPoints(p)) != null) {
				newList = playersInSorted.get(session.getPlayerPoints(p));
			}
			newList.add(p);
			playersInSorted.put(session.getPlayerPoints(p), newList);

			if(session.getPlayerPoints(p) > maxScore) maxScore = session.getPlayerPoints(p);
		}
		List<Player[]> sortedInRanks = new ArrayList<>();
		for(int c = maxScore; c >= 0; c--) {
			if(playersInSorted.get(c) != null) {
				List<Player> rankList = playersInSorted.get(c);
				Player[] rankArray = new Player[rankList.size()];
				rankArray = rankList.toArray(rankArray);
				sortedInRanks.add(rankArray);
			}
		}
		StringBuilder leaderboardString = new StringBuilder();
		int r = 1;
		for(Player[] rank : sortedInRanks) {
			leaderboardString.append("§r").append(r).append(". ");
			
			if(rank.length > 1) {
				for(Player p : rank) {
					leaderboardString.append(session.getPlayerColor(p).getChatColor()).append(p.getName()).append("§7, ");
				}
				leaderboardString = new StringBuilder(leaderboardString.substring(0, leaderboardString.length() - 2) + " §7(§d" + session.getPlayerPoints(rank[0]) + "§7)\n");
			} else {
				leaderboardString.append(session.getPlayerColor(rank[0]).getChatColor()).append(rank[0].getName()).append(" §7(§d").append(session.getPlayerPoints(rank[0])).append("§7)\n");
			}
			r++;
		}

		PlayerTeleporter.gatherPlayers(winners);
		for(Player p : session.getPlayers()) {
			if(p.hasPotionEffect(PotionEffectType.GLOWING)) p.removePotionEffect(PotionEffectType.GLOWING);
			p.sendMessage("\n§7—————§a§lPoints§r§7—————\n");
			p.sendMessage(leaderboardString.toString());
			p.sendMessage("§7——————————————\n");
			
			p.sendTitle("§b"+winnerTeamsString+" §awon", "§aScore: §d"+amount, 20, 20*5, 20);
		}
		for(Player w : winners) {
			Utils.runDefinedRepeater(() -> {
				Firework fw = (Firework) Objects.requireNonNull(w.getLocation().getWorld()).spawnEntity(w.getLocation(), EntityType.FIREWORK);
				FireworkMeta fwm = fw.getFireworkMeta();
				FireworkEffect fwe = FireworkEffect.builder().flicker(true).withColor(session.getPlayerColor(w).getColor()).with(Type.BALL).trail(true).build();
				fwm.addEffect(fwe);
				fwm.setPower(1);
				fw.setFireworkMeta(fwm);
			}, 0,50,3);
		}
	
	}
	
	private void evaluateTeams() {
		List<SessionTeam> winnerTeams = new ArrayList<>();
		int amount = 0;
		for(SessionTeam team : session.getTeams()) {
			if(team.getPoints() > amount) {
				amount = session.getTeamPoints(team);
				winnerTeams = new ArrayList<>();
				winnerTeams.add(team);
			} else if(team.getPoints() == amount) {
				winnerTeams.add(team);
			}
		}
		StringBuilder winnerTeamsString = new StringBuilder();
		int i = 0;
		for(SessionTeam team : winnerTeams) {
			if(winnerTeams.size() > 2) {
				if(i == 0) winnerTeamsString = new StringBuilder(session.getTeamColor(team).getChatColor() + "Team " + session.getTeamColor(team).getName());
				else if(i < winnerTeams.size()) winnerTeamsString.append("§r, ").append(session.getTeamColor(team).getChatColor()).append("Team ").append(session.getTeamColor(team).getName());
				else winnerTeamsString.append("§r and ").append(session.getTeamColor(team).getChatColor()).append("Team ").append(session.getTeamColor(team).getName());
			} else if(winnerTeams.size() == 2) {
				if(i == 0) winnerTeamsString = new StringBuilder(session.getTeamColor(team).getChatColor() + "Team " + session.getTeamColor(team).getName());
				else if(i == 1) winnerTeamsString.append(" §rand ").append(session.getTeamColor(team).getChatColor()).append("Team ").append(session.getTeamColor(team).getName());
			} else {
				winnerTeamsString = new StringBuilder(session.getTeamColor(team).getChatColor() + "Team " + session.getTeamColor(team).getName());
			}
			i++;
		}
		
		HashMap<Integer, List<Player>> playersInSorted = new HashMap<>();
		int maxScore = 0;
		for(Player p : session.getPlayers()) {
			List<Player> newList = new ArrayList<>();
			if (playersInSorted.get(session.getPlayerPoints(p)) != null) {
				newList = playersInSorted.get(session.getPlayerPoints(p));
			}
			newList.add(p);
			playersInSorted.put(session.getPlayerPoints(p), newList);
			if(session.getPlayerPoints(p) > maxScore) maxScore = session.getPlayerPoints(p);
		}
		List<Player[]> sortedInRanks = new ArrayList<>();
		for(int c = maxScore; c >= 0; c--) {
			if(playersInSorted.get(c) != null) {
				List<Player> rankList = playersInSorted.get(c);
				Player[] rankArray = new Player[rankList.size()];
				rankArray = rankList.toArray(rankArray);
				sortedInRanks.add(rankArray);
			}
		}
		StringBuilder leaderboardString = new StringBuilder();
		int r = 1;
		for(Player[] rank : sortedInRanks) {
			leaderboardString.append("§r§7").append(r).append(". §r");
			
			if(rank.length > 1) {
				for(Player p : rank) {
					leaderboardString.append(session.getPlayerColor(p).getChatColor()).append(p.getName()).append("§7, ");
				}
				leaderboardString = new StringBuilder(leaderboardString.substring(0, leaderboardString.length() - 2) + " §7(§d" + session.getPlayerPoints(rank[0]) + "§7)\n");
			} else {
				leaderboardString.append(session.getPlayerColor(rank[0]).getChatColor()).append(rank[0].getName()).append(" §7(§d").append(session.getPlayerPoints(rank[0])).append("§7)\n");
			}
			r++;
		}
		
		
		HashMap<Integer, List<SessionTeam>> teamsInSorted = new HashMap<>();
		
		int maxTeamScore = 0;
		for(SessionTeam team : session.getTeams()) {
			List<SessionTeam> newList = new ArrayList<>();
			if (teamsInSorted.get(team.getPoints()) != null) {
				newList = teamsInSorted.get(team.getPoints());
			}
			newList.add(team);
			teamsInSorted.put(team.getPoints(), newList);
			if(session.getTeamPoints(team) > maxTeamScore) maxTeamScore = session.getTeamPoints(team);
		}
		List<List<SessionTeam>> teamsSortedInRanks = new ArrayList<>();
		for(int c = maxTeamScore; c >= 0; c--) {
			if(teamsInSorted.get(c) != null) {
				List<SessionTeam> rankList = teamsInSorted.get(c);
				teamsSortedInRanks.add(rankList);
			}
		}
		
		StringBuilder teamscoreboardString = new StringBuilder();
		int tr = 1;
		for(List<SessionTeam> rankTeamList : teamsSortedInRanks) {
			teamscoreboardString.append("§7").append(tr).append(". ");
			for(SessionTeam team : rankTeamList) {
				String teamName = session.getTeamColor(team).getName();
				String teamNameColor = session.getTeamColor(team).getName().toUpperCase().replace("ORANGE", "GOLD");
				if(tr < rankTeamList.size()-1) teamscoreboardString.append(ChatColor.valueOf(teamNameColor)).append(teamName).append(" Team, ");
				else teamscoreboardString.append(ChatColor.valueOf(teamNameColor)).append(teamName).append(" Team §7(§a").append(session.getTeamPoints(team)).append("§7)\n");
			}
			tr++;
		}
		
		List<Player> winners = new ArrayList<>();
		for(SessionTeam winnerteam : winnerTeams) {
			Collections.addAll(winners, winnerteam.getPlayers());
		}

		PlayerTeleporter.gatherPlayers(winners);
		for(Player p : session.getPlayers()) {
			if(p.hasPotionEffect(PotionEffectType.GLOWING)) p.removePotionEffect(PotionEffectType.GLOWING);
			p.sendTitle(winnerTeamsString+" §rwon", "Score: §d"+amount+"\n§rBest Player: ", 20, 20*5, 20);
			p.sendMessage("\n§7——————§a§lPoints§r§7——————\n");
			p.sendMessage("§r§n§aTeam Score:§r");
			p.sendMessage(teamscoreboardString.toString());
			p.sendMessage("\n§r§n§aPlayer Score:§r");
			p.sendMessage(leaderboardString.toString());
			p.sendMessage("\n§7————————————————\n");
		}
		for(SessionTeam team : winnerTeams) {
			for(Player w : team.getPlayers()) {
				Utils.runDefinedRepeater(()->{
					Firework fw = (Firework) Objects.requireNonNull(w.getLocation().getWorld()).spawnEntity(w.getLocation(), EntityType.FIREWORK);
					FireworkMeta fwm = fw.getFireworkMeta();
					FireworkEffect fwe = FireworkEffect.builder().flicker(true).withColor(session.getPlayerColor(w).getColor()).with(Type.BALL).trail(true).build();
					fwm.addEffect(fwe);
					fwm.setPower(2);
					fw.setFireworkMeta(fwm);
				},0,50,3);
			}
		}
	}

}
