package me.noobedidoob.minigames.lasertag.session;

import java.util.*;

import me.noobedidoob.minigames.lasertag.methods.Inventories;
import me.noobedidoob.minigames.lasertag.methods.PointEvents;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import me.noobedidoob.minigames.lasertag.Lasertag;
import me.noobedidoob.minigames.lasertag.Lasertag.LasertagColor;
import me.noobedidoob.minigames.lasertag.listeners.DeathListener;
import me.noobedidoob.minigames.lasertag.methods.Weapon;
import me.noobedidoob.minigames.lasertag.methods.Mod;
import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.utils.Map;
import me.noobedidoob.minigames.utils.Utils;
import me.noobedidoob.minigames.utils.Utils.TimeFormat;
import me.noobedidoob.minigames.utils.Pair;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Session implements Listener{
	
	public SessionScoreboard scoreboard;
	public SessionRound round;
	public SessionModifiers modifiers;
	
	private Player owner;
	private String code;
	
	public final Minigames minigames;
	public Session(Minigames minigames, Player owner, boolean solo) {
		this.minigames = minigames;
		scoreboard = new SessionScoreboard(this);
		round = new SessionRound(this);
		modifiers = new SessionModifiers();
		
		this.solo = solo;
		
		this.owner = owner;
		addPlayer(owner);
		addAdmin(owner);
		this.code = owner.getName();
		NAME_SESSION.put(code, this);
		
		Inventories.openTimeInv(owner);
		SESSIONS.add(this);
		
		for(Map m : Map.MAPS) getMapVotes().put(m, 0);
		
	}
	public Session(Minigames minigames, Player owner, int teamsAmount) {
		this.minigames = minigames;
		scoreboard = new SessionScoreboard(this);
		round = new SessionRound(this);
		modifiers = new SessionModifiers();

		this.owner = owner;

		if(teamsAmount < 2) {
			solo = true;
			this.teamAmountSet = true;
		} else {
			solo = false;
			setTeamsAmount(teamsAmount);
			teams.get(0).addPlayer(owner);
		}

		addPlayer(owner);
		addAdmin(owner);
		this.code = owner.getName();
		NAME_SESSION.put(code, this);
		
		Inventories.openTimeInv(owner);
		SESSIONS.add(this);
		
		for(Map m : Map.MAPS) getMapVotes().put(m, 0);
	}
	
	private boolean running = false;
	public void start(boolean countdown) {
		if(!round.tagging() && !running) {
			if(setSessionMap()){
				running = true;
				round.preparePlayers();
				if(!countdown) round.start();
				else {
					for(Player p : players) p.sendTitle("§aStarting Lasetag in §d5","§eMap: §b"+map.getName(),5,30,5);
					Bukkit.getScheduler().scheduleSyncDelayedTask(minigames, () -> {
						for(Player p : players) p.sendTitle("§aStarting Lasetag in §d4","§eMap: §b"+map.getName(), 0,30,5);
						Bukkit.getScheduler().scheduleSyncDelayedTask(minigames, () -> {
							for (Player p : players) p.sendTitle("§aStarting Lasetag in §d3", "§eMap: §b" + map.getName(), 0, 30, 5);
							Bukkit.getScheduler().scheduleSyncDelayedTask(minigames, () -> {
								for (Player p : players) p.sendTitle("§aStarting Lasetag in §d2", "§eMap: §b" + map.getName(), 0, 30, 5);
								Bukkit.getScheduler().scheduleSyncDelayedTask(minigames, () -> {
									for (Player p : players) p.sendTitle("§aStarting Lasetag in §d1", "§eMap: §b" + map.getName(), 0, 20, 5);
									Bukkit.getScheduler().scheduleSyncDelayedTask(minigames, () -> {
										round.start();
										if(withEvents) pointEvents.startEvents();
										System.out.println("starting "+withEvents);
									}, 20);
								}, 20);
							}, 20);
						}, 20);
					}, 20);
				}
			}
		}
	}
	
	public boolean justStopped = false;
	public void stop(boolean external, boolean closeSession) {
		justStopped = true;
		if (external) {
			if (round.tagging()) {
				round.stop(true);
			} 
		}
		setAllPlayersWaitingInv();
		if(withEvents) pointEvents.stopEvents();
		
		if(votedBefore) mapState = MapState.VOTING;
		else mapState = MapState.SET;
		votedBefore = false;
		
		for(Player p : players) {
			playerVotes.put(p, null);
		}
		for(Map m : Map.MAPS) {
			getMapVotes().put(m, 0);
		}

		if(withCaptureTheFlag){
			map.disableCTF();
		}
		
		this.time = 5*60;
		setTime(ogTime, TimeFormat.SECONDS, false);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(minigames, () -> {
			try {
				for (Player p : players) {
					playerPoints.put(p, 0);
					p.setLevel(0);
				}
				for (SessionTeam t : teams) {
					t.setPoints(0);
				}
				refreshScoreboard();
			} catch (Exception ignored) { }
		}, 20*10);

		Utils.runLater(()-> justStopped = false,20*5);

		running = false;
		if(closeSession) close();
	}

	public void shutdown() {
		if(round.tagging()) round.stop(true);
		if(withCaptureTheFlag){
			map.disableCTF();
		}
		for (Player p : players) {
			p.setLevel(0);
		}
	}

	public void close() {
		if(tagging()) stop(true, false);
			Bukkit.getScheduler().scheduleSyncDelayedTask(minigames, () -> {
			for(Player p : players) {
				DeathListener.resetPlayerStreak(p);
				setPlayerSession(p, null);
				PLAYER_SESSION.put(p, null);
				p.getInventory().clear();
				removePlayer(p);
			}
			removePlayerSessinos();
			modifiers.reset();
			NAME_SESSION.put(code, null);
			players = null;
			owner = null;
			admins = null;
			removeThisSession();
			Minigames.inform("Closed the session from "+code);
		}, 20);
	}
	
	
	
	private boolean solo;
	public boolean isSolo() {
		return solo;
	}
	public boolean isTeams() {
		return !solo;
	}
	public boolean waiting() {
		return !round.tagging();
	}
	public boolean tagging() {
		return round.tagging();
	}

	public enum MapState{
		SET,
		VOTING,
		NULL
	}
	
	private Map map;
	private MapState mapState = MapState.NULL;
	public void setMap(Map m) {
		if(m == null) {
			mapState = MapState.VOTING;
			broadcast("§aMap vote enabled! You can vote the map for this round!");
			for(Map am : Map.MAPS) {
				getMapVotes().put(am, 0);
			}
			for(Player p : players) {
				playerVotes.put(p, null);
			}
		} else {
			mapState = MapState.SET;
			this.map = m;
			broadcast("§aPlaying on the map §b"+m.getName());
		}
		refreshScoreboard();
	}
	public Map getMap() {
		return map;
	}
	public boolean isMapSet() {
		return mapState == MapState.SET;
	}
	public boolean votingMap() {
		return mapState == MapState.VOTING;
	}
	public boolean isMapNull() {
		return mapState == MapState.NULL;
	}

	public HashMap<Player, Map> playerVotes = new HashMap<>();
	public void playerVoteMap(Player p, Map m) {
		playerVotes.put(p, m);
		getMapVotes().put(m, getMapVotes().get(m)+1);
		refreshScoreboard();
	}
	public HashMap<Map, Integer> getMapVotes(){
		HashMap<Map, Integer> mapVotes = new HashMap<>();
		playerVotes.forEach((p, m) -> {
			if(m != null){
				if(mapVotes.containsKey(m)){
					mapVotes.put(m, mapVotes.get(m)+1);
				} else mapVotes.put(m, 1);
			}
		});
		return mapVotes;
	}
	public int getVotedMapAmount(){
		List<Map> list = new ArrayList<>();
		for(Player p : players){
			if(playerVotes.get(p) != null && !list.contains(playerVotes.get(p))) list.add(playerVotes.get(p));
		}
		return list.size();
	}

	boolean votedBefore = false;
	private boolean setSessionMap() {
		if(mapState == MapState.VOTING) {
			votedBefore = true;
			Map m = null;
			int maxVote = 0;
			for(Map am : Map.MAPS) {
				if(getMapVotes().get(am) != null && maxVote < getMapVotes().get(am)) {
					maxVote = getMapVotes().get(am);
					m = am;
				}
			}

			if(m == null){
				admins.forEach(a -> sendMessage(a, "§cYou need at least one vote to start the game!"));
				return false;
			}

			this.map = m;
			
			if(!isMapPlayable(map)) {
				broadcast("§cThe map §6"+map.getName()+" §cis not playable!");
				for(Map am : Map.MAPS) {
					getMapVotes().put(am, 0);
					refreshScoreboard();
				}
				return false;
			}
			
			broadcast("§ePlaying with in the map §b"+map.getName());
			mapState = MapState.SET;
			refreshScoreboard();
		}
		if(map == null) {
			if(mapState == MapState.VOTING) broadcast("§cAn error occured while setting the map! Please try again or choose another map!");
			else {
				for(Player a : admins) {
					sendMessage(a, "§cAn error occured while setting the map! Please try again or choose another map!");
				}
			}
			for(Map am : Map.MAPS) {
				getMapVotes().put(am, 0);
				refreshScoreboard();
			}
			mapState = MapState.NULL;
			refreshScoreboard();
			Inventories.openMapInv(owner);
			return false;
		}
		if(withCaptureTheFlag) {
			List<LasertagColor> colors = new ArrayList<>();
			if (solo) {
				for(Player ap : players){
					colors.add(getPlayerColor(ap));
				}
			} else {
				for(SessionTeam team : teams){
					colors.add(team.getLasertagColor());
				}
			}
			map.enableCTF(this, colors);
		} else map.disableCTF();
		return true;
	}
	public boolean isMapPlayable(Map m) {
		if(solo && !m.withRandomSpawn() && players.size() > m.getBaseAmount()) return false;
		if(withCaptureTheFlag && !m.withCaptureTheFlag()) return false;
		return solo || m.withRandomSpawn() || teamsAmount <= m.getBaseAmount();
	}
	
	
	
	private int time = 300;
	private int ogTime = time;
	private boolean timeSet = false;
	public void setTime(int time, TimeFormat format, boolean announce) {
		switch (format) {
		case MINUTES:
			this.time = time*60;
			break;
		case HOURS:
			this.time = (time*60)*60;
			break;
		default:
			this.time = time;
			break;
		}
		if(this.time > 3600) this.time = 36000;
		if(waiting()) this.ogTime = this.time;
		timeSet = true;
		scoreboard.refresh();
		if(announce) {
			if(format == TimeFormat.HOURS) broadcast("§aSession time was set to §b"+Utils.getTimeFormatFromLong(this.time, TimeFormat.HOURS)+" §ehours");
			else if(format == TimeFormat.MINUTES) broadcast("§aRound time was set to §b"+Utils.getTimeFormatFromLong(this.time, TimeFormat.MINUTES)+" §eminutes");
			else broadcast("§aRound time was set to §b"+this.time+" §eseconds");
		}
	}
	public int getTime(TimeFormat format) {
		switch (format) {
		case MINUTES:
			return time/60;
		case HOURS:
			return (time/60)/60;
		default:
			return time;
		}
	}
	public boolean isTimeSet() {
		return timeSet;
	}
	
	
	
	

	public List<Player> bannedPlayers = new ArrayList<>();
	@SuppressWarnings("deprecation")
	public void sendInvitation(Player p) {
		sendMessage(p, "§eYou've been invited to the session of §b"+owner.getName());
		TextComponent linkMsg = new TextComponent("JOIN");
		linkMsg.setColor(net.md_5.bungee.api.ChatColor.GOLD);
		linkMsg.setBold(true);
		linkMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/session join "+owner.getName()));
		linkMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Join the session of "+owner.getName()).create()));
		
		p.spigot().sendMessage(linkMsg);
		bannedPlayers.remove(p);
	}
	public boolean isPlayerBanned(Player p) {
		return bannedPlayers.contains(p);
	}
	

	

	private ArrayList<Player> admins = new ArrayList<>();
	public boolean isAdmin(Player p) {
		return admins.contains(p);
	}
	public Player getOwner() {
		return owner;
	}
	public void addAdmin(Player p) {
		admins.add(p);
		if(p != owner) sendMessage(p, "§aYou've been promoted to §eAdmin§e!");
		setPlayerInv(p);
	}
	public void removeAdmin(Player p) {
		if(admins.contains(p)) {
			admins.remove(p);
			sendMessage(p, "§cYou're not longer an §eAdmin§e!");
			setPlayerInv(p);
		}
	}
	public Player[] getAdmins() {
		return admins.toArray(new Player[0]);
	}
	
	
	
	
	private boolean withCaptureTheFlag = false;
	public void setWithCaptureTheFlag(boolean ctf){
		if (withCaptureTheFlag != ctf) {
			withCaptureTheFlag = ctf;
			broadcast(((ctf)?"§aEnabled":"§cDisabled")+" §bcapture the flag mode!");
			if(ctf){
				if(mapState == MapState.SET && (!map.withCaptureTheFlag() | players.size() > map.getBaseAmount())) {
					map = Map.MAPS.get(0);
					Inventories.openMapInv(owner);
				} else if(mapState == MapState.VOTING){
					getMapVotes().clear();
					setMap(null);
					playerVotes.forEach((player, m) -> {
						if(m != null) {
							sendMessage(player, "§cPlease re-vote your map!");
							playerVotes.put(player,null);
						}
					});
				}
			}
			refreshScoreboard();
		}
	}
	public boolean withCaptureTheFlag(){
		return withCaptureTheFlag;
	}

	private boolean withGrenade = false;
	public void setWithGrenades(boolean withGrenade){
		if(withGrenade == this.withGrenade) return;
		broadcast(((withGrenade)?"§aEnabled":"§cDisabled")+" §bgrenades!");
		this.withGrenade = withGrenade;
		refreshScoreboard();
	}
	public boolean withGrenades(){
		return withGrenade;
	}

	private PointEvents pointEvents;
	private boolean withEvents = false;
	public void setWithPointEvents(boolean withPointEvents){
		if(withEvents == withPointEvents) return;
		if(pointEvents == null) pointEvents = new PointEvents(this);
		pointEvents.setEnabled(withPointEvents);
		broadcast(((withPointEvents)?"§aEnabled":"§cDisabled")+" §bpoint events");
		this.withEvents = withPointEvents;
		refreshScoreboard();
	}
	public boolean withPointEvents(){
		return withEvents;
	}
	

	private final HashMap<Player, Integer> playerPoints = new HashMap<>();
	private final HashMap<Player, LasertagColor> playerColor = new HashMap<>();
	public void setPlayerColor(Player p, LasertagColor color) {
		playerColor.put(p, color);
		if(scoreboard.board.getTeam(color.name()) != null) {
			Objects.requireNonNull(scoreboard.board.getTeam(color.name())).unregister();
		}
		Team t = scoreboard.board.registerNewTeam(color.name());
		t.setColor(color.getChatColor());
		t.addEntry(p.getName());
		refreshScoreboard();
	}
	public LasertagColor getPlayerColor(Player p) {
		return playerColor.get(p);
	}
	public Player getPlayerFromColor(LasertagColor color){
		for (Player p : players) {
			if(getPlayerColor(p).equals(color)) return p;
		}
		return null;
	}
	public void refreshSoloPlayerColors() {
		if (isSolo()) {
			for (Player p : players) {
				int ordinal = players.indexOf(p);
				if (ordinal > LasertagColor.values().length - 1) ordinal -= LasertagColor.values().length;
				playerColor.put(p, LasertagColor.values()[ordinal]);
				if(scoreboard.board.getTeam(LasertagColor.values()[ordinal].name()) != null) Objects.requireNonNull(scoreboard.board.getTeam(LasertagColor.values()[ordinal].name())).unregister();
				Team t = scoreboard.board.registerNewTeam(LasertagColor.values()[ordinal].name());
				t.setColor(LasertagColor.values()[ordinal].getChatColor());
				t.addEntry(p.getName());
			}
		} 
		refreshScoreboard();
	}
	public int getPlayerPoints(Player p) {
		playerPoints.putIfAbsent(p,0);
		return playerPoints.get(p);
	}

	public void addPoints(Player p, int points, String message) {
		playerPoints.put(p, playerPoints.get(p) + points);
		p.setLevel(playerPoints.get(p));
		if (!solo) {
			getPlayerTeam(p).addPoints(points);
			for(Player ap : getPlayerTeam(p).getPlayers()){
				if(ap != p) ap.playSound(ap.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 0);
			}
		}
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0);
		for(Player ap : players){
			ap.sendMessage(message);
		}
	}
	private List<Player> players = new ArrayList<>();
	public Player[] getPlayers() {
		return this.players.toArray(new Player[0]);
	}
	public boolean isInSession(Player p) {
		return players.contains(p);
	}
	public void addPlayer(Player p) {
		broadcast("§b"+p.getName()+" §ajoined the session!");
		setPlayerSession(p, this);
		players.add(p);
		playerPoints.put(p, 0);
		p.setLevel(0);
		playerVotes.put(p, null);
		sendMessage(p, "§aWelcome to the Game, §b"+p.getName()+"§r§a!");
		
		if (isTeams()) {
			if (p != owner) {
				generatePlayerTeam(p);
			}
		} else {
			refreshSoloPlayerColors();
		}
		setPlayerInv(p);
		
		refreshScoreboard();
		
		if(withMultiWeapons) Utils.runLater(()-> Inventories.openSecondaryWeaponChooserInv(p), 20);
	}
	public void banPlayer(Player p, Player admin) {
		bannedPlayers.add(p);
		removePlayer(p);
		broadcast("§b"+p.getName()+" §cwas kicked from §e"+admin.getName());
		sendMessage(p, "§b" + admin.getName() + " §ckicked you out of the session!");
	}
	public void leavePlayer(Player p) {
		removePlayer(p);
		broadcast("§b"+p.getName()+" §cleft the session");
		sendMessage(p, "§cYou left the session!");
	}
	public void removePlayer(Player p) {
		if(isSolo() && scoreboard.board.getTeam(getPlayerColor(p).name()) != null) Objects.requireNonNull(scoreboard.board.getTeam(getPlayerColor(p).name())).unregister();
		p.getInventory().clear();
		Lasertag.setPlayersLobbyInv(p);
		try {
			if(isTeams()) getPlayerTeam(p).removePlayer(p);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		players.remove(p);
		admins.remove(p);
		if(p == owner) {
			if(players.size() == 0) {
				close();
			} else if(admins.size() == 0) {
				admins.addAll(players);
				NAME_SESSION.put(code, null);
				owner = admins.get(0);
				sendMessage(owner, "§l§bYou were made the owner of this session!");
				code = owner.getName();
				NAME_SESSION.put(code, this);
				setPlayerInv(owner);
			} else {
				NAME_SESSION.put(code, null);
				owner = admins.get(0);
				sendMessage(owner, "§l§bYou were made the owner of this session!");
				code = owner.getName();
				NAME_SESSION.put(code, this);
				setPlayerInv(owner);
			}
		}
		if(isSolo() && waiting()) {
			try {
				for(Player ap : players) {
					int pColorIndex = players.indexOf(ap);
					if(pColorIndex > LasertagColor.values().length-1) pColorIndex -= LasertagColor.values().length-1;
					setPlayerColor(ap, LasertagColor.values()[pColorIndex]);
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} else {
			for(SessionTeam t : teams) {
				if(t.getPlayers().length == 1 && teams.indexOf(t) > 0) {
					for(int i = teams.indexOf(t)-1; i > 0; i--) {
						if(teams.get(i).getPlayers().length == 0) {
							Player tp = t.getPlayers()[0];
							t.removePlayer(tp);
							addPlayerToTeam(tp, teams.get(i));
						}
					}
				}
			}
		}
		try { p.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard()); } catch (NullPointerException e) {Minigames.warn("Error occured when trying to remove players scoreboard!");}
		scoreboard.refresh();
		setPlayerSession(p, null);
		Lasertag.setPlayersLobbyInv(p);
	}
	
	public HashMap<UUID, Pair> disconnectedPlayers = new HashMap<>();
	public void disconnectPlayer(Player p) {
		broadcast("§e"+p.getName()+" §cdisconnected!", p);
		int points = 0;
		if(tagging()) points = getPlayerPoints(p);
		disconnectedPlayers.put(p.getUniqueId(), new Pair(getPlayerColor(p), points));
		removePlayer(p);
	}
	
	public void reconnectPlayer(Player p) {
		if(disconnectedPlayers.get(p.getUniqueId()) != null) {
			LasertagColor color = (LasertagColor) disconnectedPlayers.get(p.getUniqueId()).get1();
			int points = (int) disconnectedPlayers.get(p.getUniqueId()).get2();
			disconnectedPlayers.put(p.getUniqueId(), null);
			PLAYER_SESSION.put(p, this);
			
			players.add(p);
			playerPoints.put(p, points);
			p.setLevel(points);
			playerVotes.put(p, null);

			if(isTeams()) {
				addPlayerToTeam(p, color);
			} else {
				setPlayerColor(p, color);
			}
			
			setPlayerInv(p);
			
			new BukkitRunnable() {
				@Override
				public void run() {
					p.sendMessage(StringUtils.repeat(" \n", 100));
					broadcast("§b"+p.getName()+" §aReturned to the session!", p);
					sendMessage(p, "§aWelcome back, §b"+p.getName()+"§r§a!");
				}
			}.runTaskLater(minigames, 10);
			
		}
	}

	
	
	private List<SessionTeam> teams = new ArrayList<>();
	public List<Player> hasTeamChooseInvOpen = new ArrayList<>();
	private int teamsAmount;
	
	public void addTeam(LasertagColor color, Player... players) {
		teams.add(new SessionTeam(this, color, players));
		for(Player p : players) {
			playerColor.put(p, color);
		}
	}
	public void addPlayerToTeam(Player p, LasertagColor name) {
		addPlayerToTeam(p, teams.get(name.ordinal()));
	}
	public void addPlayerToTeam(Player p, SessionTeam team) {
		if (getPlayerTeam(p) != team) {
			if(getPlayerTeam(p) != null) getPlayerTeam(p).removePlayer(p);
			team.addPlayer(p);
			playerColor.put(p, team.getLasertagColor());
			refreshScoreboard();
//			round.refreshPlayerTeams();
		}
	}
	public void generatePlayerTeam(Player p) {
		SessionTeam sTeam = teams.get(0);
		int lowestAmount = teams.get(0).getPlayers().length;
		for (SessionTeam team : teams) {
			if (team.getPlayers().length < lowestAmount) {
				lowestAmount = team.getPlayers().length;
				sTeam = team;
			}
		}
		addPlayerToTeam(p, sTeam);
	}
	public SessionTeam[] getTeams(){
		return teams.toArray(new SessionTeam[0]);
	}
	public LasertagColor getTeamColor(SessionTeam team) {
		return team.getLasertagColor();
	}
	public SessionTeam getTeamByColor(LasertagColor color){
		for(SessionTeam team : getTeams()){
			if(team.getLasertagColor() == color) return  team;
		}
		return null;
	}
	public int getTeamPoints(SessionTeam team) {
		return team.getPoints();
	}
	public SessionTeam getPlayerTeam(Player p) {
		return SessionTeam.getPlayerTeam(p);
	}
	public int getTeamsAmount() {
		return teamsAmount;
	}
	private boolean teamAmountSet = false;
	public void setTeamsAmount(int amount) {
		if (round.tagging()) return;
		if (!teamAmountSet) {
			teamAmountSet = true;

			for (int i = 0; i < amount; i++) {
				if (i == 0) teams.add(new SessionTeam(this, LasertagColor.Red, owner));
				else teams.add(new SessionTeam(this, LasertagColor.values()[i], new Player[] {}));
			}
		} else {
			if(amount < 2) {
				if (!solo) {
					solo = true;
					refreshSoloPlayerColors();
					for(SessionTeam t : teams) {
						for(Player p : t.getPlayers()) {
							try {
								t.removePlayer(p);
							} catch (IllegalStateException ignore) {
							}
						}
					}
					teams = new ArrayList<>();
					broadcast("§bUpdated mode to §dsolo!");
					for(Player p : players) {
						p.getInventory().clear();
						setPlayerInv(p);
					}
				}
			} else {
				if(solo) {
					solo = false;
					teams = new ArrayList<>();
					for (int i = 0; i < amount; i++) {
						teams.add(new SessionTeam(this, LasertagColor.values()[i], new Player[] {}));
					}
					for(Player p : players) {
						p.getInventory().clear();
						setPlayerInv(p);
						generatePlayerTeam(p);
					}
				} else if(amount > teamsAmount) {
					for(int i = amount-1; i < amount; i++) {
						teams.add(new SessionTeam(this, LasertagColor.values()[i], new Player[] {}));
					}
				} else if(amount < teamsAmount) {
					teams = new ArrayList<>();
					for (int i = 0; i < amount; i++) {
						if (i == 0) teams.add(new SessionTeam(this, LasertagColor.Red, owner));
						else teams.add(new SessionTeam(this, LasertagColor.values()[i], new Player[] {}));
					} 
					for(Player p : players) {
						p.getInventory().clear();
						setPlayerInv(p);
						generatePlayerTeam(p);
					}
				}
				broadcast("§bUpdated mode to §d"+amount+" teams!");
			}
		}
		teamsAmount = amount;
	}
	public boolean isTeamsAmountSet() {
		return teamAmountSet;
	}
	
	public boolean inSameTeam(Player p1, Player p2) {
		if (isTeams()) {
			for (SessionTeam team : getTeams()) {
				return (team.isInTeam(p1) && team.isInTeam(p2));
			}
		}
		return false;
	}

	public void attemptStart(Player p){
		if(withMultiweapons() && !isEveryBodyReady()) {
			sendMessage(p, "§cNot everybody is ready!");
			for(Player up : getNotReadyPlayers()) {
				Inventories.openSecondaryWeaponChooserInv(up);
			}
		} else {
			if(getPlayers().length > 1) {
				boolean enoughTeams = true;
				if(isTeams()) {
					boolean rearranged = false;
					for (SessionTeam sessionTeam : teams) {
						if(sessionTeam.getPlayers().length == 0) {
							rearranged = true;
							if(getTeams()[teamsAmount-2].getPlayers().length > 0){
								for (Player player : getTeams()[teamsAmount-2].getPlayers()) {
									addPlayerToTeam(player, getTeams()[teamsAmount-3]);
								}
							}
							setTeamsAmount(teamsAmount-1);
						}
					}
					if(rearranged){
						Session.sendMessage(p, "§cAutomatically removed empty teams, start again to confirm!");
						return;
					}

					int teamsWithPlayers = 0;
					for(SessionTeam team : getTeams()) {
						if(team.getPlayers().length > 0) teamsWithPlayers++;
					}
					if(teamsWithPlayers < 2) enoughTeams = false;
				}
				if(enoughTeams && !justStopped) {
					start(true);
				}
				else Session.sendMessage(p, "§cThere must be at least 2 teams with at least 1 player in it!");
			} else Session.sendMessage(p, "§cNot enough players!");
		}
	}

	
	public void refreshScoreboard() {
		scoreboard.refresh();
	}
	
	
	

	public void broadcast(String s, Player... excludedPlayers) {
		for(Player p : players) {
			boolean notExcluded = true;
			for(Player ep : excludedPlayers)
				if (ep == p) {
					notExcluded = false;
					break;
				}
			if(notExcluded) sendMessage(p, s);
		}
	}
	public void broadcastWithoutPrefix(String s, Player... excludedPlayers) {
		for(Player p : players) {
			boolean notExcluded = true;
			for(Player ep : excludedPlayers)
				if (ep == p) {
					notExcluded = false;
					break;
				}
			if(notExcluded) sendMessageWithoutPrefix(p, s);
		}
	}

	
	public void setAllPlayersInv() {
		for(Player p : players) setPlayerInv(p);
	}
	public void setPlayerInv(Player p) {
		if(tagging()) round.setPlayerGameInv(p);
		else setPlayerWaitingInv(p);
	}
	

	public void setAllPlayersWaitingInv() {
		for(Player p : players) {
			setPlayerWaitingInv(p);
		}
	}
	public void setPlayerWaitingInv(Player p) {
		Inventories.setPlayerSessionWaitingInv(p);
	}
	
	
	
	public Object getModValue(Mod m) {
		return modifiers.get(m);
	}
	
	public int getIntMod(Mod m) {
	 	return modifiers.getInt(m);
	}
    public double getDoubleMod(Mod m) {
    	return modifiers.getDouble(m);
    }
    public boolean getBooleanMod(Mod m) {
    	return modifiers.getBoolean(m);
    }
    
    public void setMod(Mod m, Object value) {
    	modifiers.set(m, value);
    	broadcast("§aThe modifier §b"+m.name().toLowerCase()+" §a was set to §e"+value.toString());
    }

    public void resetMod(Mod m){
		if(modifiers.get(m) != m.getOg()) broadcast("§aThe modifier §b"+m.name().toLowerCase()+" §a was resetset to §e"+m.getOg());
		modifiers.set(m, m.getOg());
	}
    
    
    
    private boolean withMultiWeapons = false;
    public boolean withMultiweapons() {
    	return withMultiWeapons;
    }
    
    private final HashMap<Player, Boolean> isPlayerReady = new HashMap<>();
    public boolean isPlayerReady(Player p) {
    	if(!withMultiWeapons) return true;
		isPlayerReady.putIfAbsent(p, false);
		return isPlayerReady.get(p);
    }
    public void setPlayerReady(Player p, boolean ready) {
    	isPlayerReady.put(p, ready);
    }
    public List<Player> getNotReadyPlayers(){
    	List<Player> list = new ArrayList<>();
    	for(Player p : players) {
    		if(!isPlayerReady(p)) list.add(p);
    	}
    	return list;
    }
    public boolean isEveryBodyReady() {
		for (Player p : players) {
			if (!isPlayerReady(p))
				return false;
		} 
		return true;
    }
    
    
    private final HashMap<UUID, Weapon> playersSecondaryWeapon = new HashMap<>();
    public Weapon getPlayerSecondaryWeapon(Player p) {
    	return playersSecondaryWeapon.get(p.getUniqueId());
    }
    public void setPlayerSecondaryWeapon(Player p, Weapon w) {
    	playersSecondaryWeapon.put(p.getUniqueId(), w);
    	setPlayerReady(p, true);
    	
    	boolean allReady = true;
		for(Player ap : getPlayers()) {
			if(!isPlayerReady(ap)) allReady = false;
		}
		
		if(allReady) {
			for(Player a : getAdmins()) {
				Session.sendMessage(a, "§aEverybody has chosen their secondary weapon!");
			}
		}
    }
    public void setWithMultiWeapons(boolean withMultiWeapons) {
    	if (waiting()) {
    		if(withMultiWeapons != this.withMultiWeapons) {
    			broadcast(((withMultiWeapons)?"§aEnabled":"§cDisabled")+" §bMultiweapons!");
				if(withMultiWeapons) {
					if(!this.withMultiWeapons) {
						this.withMultiWeapons = true;
						for(Player p : players) {
							setPlayerReady(p, false);
							Inventories.openSecondaryWeaponChooserInv(p);
							setPlayerInv(p);
						}
					}
				} else {
					this.withMultiWeapons = false;
					for (Player ap : players) {
						if(ap.getOpenInventory().getTopInventory().contains(Weapon.SNIPER.getType()) && ap.getOpenInventory().getTopInventory().contains(Weapon.SHOTGUN.getType())) ap.closeInventory();
					}
					setAllPlayersInv();
					playersSecondaryWeapon.clear();
				}
				refreshScoreboard();
			}
    	}
    }
	
	
	
	
	
	
	public void removePlayerSessinos() {
		PLAYER_SESSION.forEach((p, s) ->{
			if(s == this) PLAYER_SESSION.put(p, null);
		});
	}
	public void removeThisSession() {
		SESSIONS.remove(this);
	}

	private static final HashMap<OfflinePlayer, Session> PLAYER_SESSION = new HashMap<>();
	public static Session getPlayerSession(OfflinePlayer p) {
		return PLAYER_SESSION.get(p);
	}
	public static void setPlayerSession(OfflinePlayer p, Session s) {
		PLAYER_SESSION.put(p, s);
	}
	public static boolean isPlayerInSession(Player p){
		return getPlayerSession(p) != null;
	}
	
	private static final HashMap<String, Session> NAME_SESSION = new HashMap<>();
	public static Session getSessionFromName(String code) {
		return NAME_SESSION.get(code);
	}
	
	public static void sendMessage(Player p, String s) {
		p.sendMessage( "§o§7[§6Session§7] §r§a"+s);
	}
	public static void sendMessageWithoutPrefix(Player p, String s) {
		p.sendMessage( "§a"+s);
	}
	public static void sendMessage(String s,Player... players) {
		for(Player p : players){
			p.sendMessage( "§o§7[§6Session§7] §r§a"+s);
		}
	}
	public static void sendMessageWithoutPrefix(String s,Player... players) {
		for(Player p : players){
			p.sendMessage( "§a"+s);
		}
	}
	
	private static final List<Session> SESSIONS = new ArrayList<>();
	public static void closeAllSessions() {
		SESSIONS.forEach((Session::close));
	}
	public static void shutdownAllSessions() {
		SESSIONS.forEach((Session::shutdown));
	}
	public static Session[] getAllSessions() {
		return SESSIONS.toArray(new Session[0]);
	}
}