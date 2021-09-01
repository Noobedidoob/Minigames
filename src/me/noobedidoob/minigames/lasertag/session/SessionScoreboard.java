package me.noobedidoob.minigames.lasertag.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.noobedidoob.minigames.utils.BaseSphere;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.noobedidoob.minigames.lasertag.Lasertag;
import me.noobedidoob.minigames.lasertag.methods.Mod;
import me.noobedidoob.minigames.utils.Map;
import me.noobedidoob.minigames.utils.Utils;
import me.noobedidoob.minigames.utils.Utils.TimeFormat;

public class SessionScoreboard {
	
	private final Session session;
	public SessionScoreboard(Session session) {
		this.session = session;
	}
	
	public Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
	private Objective obj;
	
	public void refresh() {
		if(obj != null) obj.unregister();
		obj = board.registerNewObjective("Scoreboard", "lasertag", "§b§lLasertag");
		obj.setDisplayName("§b§lLasertag");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);


		long time = session.getTime(TimeFormat.SECONDS);
		if(session.tagging()) time++;
		if(time < 3600) obj.getScore("§eTime:  §c§l"+Utils.getTimeFormatFromLongSmooth(time, TimeFormat.MINUTES)).setScore(0);
		else obj.getScore("§eTime:  §c§l"+Utils.getTimeFormatFromLongSmooth(time, TimeFormat.HOURS)).setScore(0);
		obj.getScore(" ").setScore(1);

		int i = 2;

		boolean withModes = session.withCaptureTheFlag() | session.withMultiweapons() | session.withGrenades() | session.withPointEvents();
		boolean withExtendedModes = Utils.TrueAmount(session.withMultiweapons(), session.withCaptureTheFlag(), session.withGrenades(), session.withPointEvents()) > 2 | (Utils.TrueAmount(session.withMultiweapons(), session.withCaptureTheFlag(), session.withGrenades(), session.withPointEvents()) == 2 & session.withCaptureTheFlag() && session.withMultiweapons());
		int lines = 3 + session.getPlayers().length + 1;
		if(session.isTeams()) lines += session.getTeamsAmount()*2;
		if(withModes) lines += withExtendedModes ? 3 : 2;
		if(session.votingMap()) lines += session.getVotedMapAmount();

		if(!session.isMapNull()){
			if(session.votingMap()) {
				if (lines <= 17 | (session.isTeams() && lines - session.getTeamsAmount() <= 17)) {
					for(int j = Map.MAPS.size(); j-- > 0;) {
						Map m = Map.MAPS.get(j);
						if(session.getMapVotes().get(m) == null) continue;
						if(session.getMapVotes().get(m) > 0 && j == Map.MAPS.size()-1) {
							obj.getScore("  §n§6"+m.getName()+": §7(§a"+session.getMapVotes().get(m)+"§7)").setScore(i++);
						}
						else if(session.getMapVotes().get(m) > 0) {
							obj.getScore("  §6"+m.getName()+": §7(§a"+session.getMapVotes().get(m)+"§7)").setScore(i++);
						}
					}
				} else lines -= session.getVotedMapAmount();
				obj.getScore("§eMap:  §o§aVoting...").setScore(i++);
			} else {
				obj.getScore("§eMap:  §r§b"+session.getMap().getName()).setScore(i++);
			}
//			if(session.votingMap() && votes > 0 && session.getTeamsAmount() < 3 && session.getPlayers().length < 10){
//				/*if(session.withCaptureTheFlag() | session.withMultiweapons() | session.withGrenades() | session.withPointEvents()) */obj.getScore("  ").setScore(i++);
//			} else {
//				if(session.isSolo() && !withModes) obj.getScore("  ").setScore(i++);
//			}
		}

		if(withModes){
			String text = "§eModes: §b";
			int addon = 1;
			if(lines <= 15){
				obj.getScore("  ").setScore(i++);
				//§eModes: §bCapture the Flag,
				//         §bEvents, MultiWeapons, Grenades
				if(session.withCaptureTheFlag()) text += session.withPointEvents() && session.withMultiweapons() && session.withGrenades() ? "CTF" : "Capture the Flag";
				if(session.withPointEvents()) {
					if(!session.withCaptureTheFlag() | (session.withMultiweapons() && session.withGrenades())) text += (!session.withCaptureTheFlag() ? "":", ") + "Events";
					else {
						obj.getScore(text+",").setScore(i+1);
						text = "         §bEvents";
						addon = 2;
					}
				}
				if(session.withMultiweapons()) {
					String t = session.withCaptureTheFlag() & (session.withGrenades() | session.withPointEvents()) ? "MW" : "MultiWeapons";
					if(!session.withCaptureTheFlag() && session.withGrenades() && session.withPointEvents()) {
						text = "§eModes: §bMultiWeapons";
						obj.getScore(text+",").setScore(i+1);
						text = "         §bEvents";
					} else if(text.charAt(text.length()-1) == 'b'){
						text += t;
					} else if(((text + ", ")+t).length() > 40 | session.withCaptureTheFlag()) {
						obj.getScore(text+",").setScore(i+1);
						text = "         §b"+t;
						addon = 2;
					} else text += ", "+t;
				}
				if(session.withGrenades()) {
					if(text.charAt(text.length()-1) == 'b'){
						text += "Grenades";
					} else if((text + ", Grenades").length() > 40 | session.withMultiweapons() && !session.withPointEvents() && !session.withMultiweapons()) {
						obj.getScore(text + ",").setScore(i+1);
						text = "         §bGrenades";
						addon = 2;
					} else text += ", Grenades";
				}
			} else {
				lines--;
				if(lines <= 15) obj.getScore("  ").setScore(i++);
				else lines--;
				if(session.withCaptureTheFlag()) text += (!session.withMultiweapons() && (!session.withPointEvents() | !session.withGrenades())) ? "Capture the Flag" : "CTF";
				if(session.withPointEvents()) text += (!text.equals("§eModes: ") ? ", " : "") + "Events";
				if(session.withMultiweapons()) text += (!text.equals("§eModes: ") ? ", " : "") + (!session.withCaptureTheFlag() && (!session.withPointEvents() | !session.withGrenades()) ? "§bMultiWeapons" : "§bMW");
				if(session.withGrenades()) text += (!text.equals("§eModes: ") ? ", " : "") + "Grenades";
			}
			obj.getScore(text).setScore(i);
			i += addon;
			if(session.isSolo()) obj.getScore("    ").setScore(i++);
		}

		if(session.isSolo()) {
			obj.getScore("        ").setScore(i++);

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
			List<Player> sortedInRanks = new ArrayList<>();
			for(int c = 0; c <= maxScore; c++) {
				if(playersInSorted.get(c) != null) {
					List<Player> rankList = playersInSorted.get(c);
					sortedInRanks.addAll(rankList);
				}
			}

			for(Player p : sortedInRanks) {
				int pp = session.getPlayerPoints(p);
				obj.getScore(((playersInSorted.get(maxScore).contains(p))?"§n":"")+session.getPlayerColor(p).getChatColor()+p.getName()+" §7(§a"+pp+"§7)  ").setScore(i++);
			}

		} else {

			HashMap<Integer, List<SessionTeam>> teamsInSorted = new HashMap<>();
			int maxTeamScore = 0;
			for(SessionTeam team : session.getTeams()) {
				List<SessionTeam> newList = new ArrayList<>();
				if (teamsInSorted.get(session.getTeamPoints(team)) != null) {
					newList = teamsInSorted.get(session.getTeamPoints(team));
				}
				newList.add(team);
				teamsInSorted.put(session.getTeamPoints(team), newList);
				if(session.getTeamPoints(team) > maxTeamScore) maxTeamScore = session.getTeamPoints(team);
			}
			List<SessionTeam> teamsSortedInRanks = new ArrayList<>();
			for(int c = 0; c <= maxTeamScore; c++) {
				if(teamsInSorted.get(c) != null) {
					List<SessionTeam> rankTeamsList = teamsInSorted.get(c);
					teamsSortedInRanks.addAll(rankTeamsList);
				}
			}

			boolean first = true;
			StringBuilder spaces = new StringBuilder("    ");
			for(int j = 0; j++ < i;) spaces.append(" ");
			obj.getScore(spaces.toString()).setScore(i++);

			for(SessionTeam team : teamsSortedInRanks) {
				try {
					if(lines <= 15){
						if(!first){
							StringBuilder spaces2 = new StringBuilder("    ");
							for(int j = 0; j++ < i;) spaces2.append(" ");
							obj.getScore(spaces2.toString()).setScore(i++);
						}

						HashMap<Integer, List<Player>> playersInSorted = new HashMap<>();
						int maxScore = 0;
						for(Player p : team.getPlayers()) {
							List<Player> newList = new ArrayList<>();
							if (playersInSorted.get(session.getPlayerPoints(p)) != null) {
								newList = playersInSorted.get(session.getPlayerPoints(p));
							}
							newList.add(p);
							playersInSorted.put(session.getPlayerPoints(p), newList);
							if(session.getPlayerPoints(p) > maxScore) maxScore = session.getPlayerPoints(p);
						}
						List<Player> sortedInRanks = new ArrayList<>();
						for(int c = 0; c <= maxScore; c++) {
							if(playersInSorted.get(c) != null) {
								List<Player> rankList = playersInSorted.get(c);
								sortedInRanks.addAll(rankList);
							}
						}

						for(Player p : sortedInRanks) {
							int points = session.getPlayerPoints(p);
							obj.getScore("   "+session.getPlayerColor(p).getChatColor()+p.getName()+" §7(§a"+points+"§7)  ").setScore(i++);
//							obj.getScore("   "+session.getPlayerColor(p).getChatColor()+p.getName()+" §7(§a"+session.getPlayerPoints(p)+"§7)  ").setScore(i++);
						}
					}
					obj.getScore(session.getTeamColor(team).getChatColor()+session.getTeamColor(team).name()+" Team §7(§a"+session.getTeamPoints(team)+"§7)  ").setScore(i++);
				} catch (Exception e) {
					e.printStackTrace();
				}
				first = false;
			}
		}
		obj.getScore("   ").setScore(i);
//		if(i > 14) {
//			i++;
//			if(time < 3600) obj.getScore("§eTime:  §c§l"+Utils.getTimeFormatFromLong(time, TimeFormat.MINUTES)).setScore(i++);
//			else obj.getScore("§eTime:  §c§l"+Utils.getTimeFormatFromLong(time, TimeFormat.HOURS)).setScore(i++);
//			obj.getScore(" ").setScore(i);
//		}
		for(Player p : session.getPlayers()) {
			p.setScoreboard(board);
			if(session.getBooleanMod(Mod.HIGHLIGHT_PLAYERS) && session.tagging()) p.setGlowing(true);
			if (session.tagging()) {
				if(Lasertag.isPlayerProtected(p)) BaseSphere.drawPlayerProtectionSphere(p);
				session.getMap().checkPlayerPosition(p);
			}
		}
	}
	
	public String[] getMapNamesSorted() {
		Map longest = Map.MAPS.get(0);
		
		for(Map m : Map.MAPS) {
			if(m.getName().length() > longest.getName().length()) longest = m;
		}
		
		List<String> list = new ArrayList<>();
		for(Map m : Map.MAPS) {
			int difference = longest.getName().length() - m.getName().length();
			
			StringBuilder name = new StringBuilder("§6" + m.getName() + ":");
			while(difference > 0) {
				name.append(" ");
				difference--;
			}
			list.add("  §6"+name+" §a"+session.getMapVotes().get(m));
		}
		return list.toArray(new String[0]);
	}
}
