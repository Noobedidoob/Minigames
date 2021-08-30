package me.noobedidoob.minigames.utils;

import me.noobedidoob.minigames.lasertag.Lasertag.LasertagColor;
import me.noobedidoob.minigames.lasertag.session.Session;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Map {
	
	public static List<Map> MAPS = new ArrayList<>();
	
	private String name;
	
	private final Area area;
	
	private final World world;
	
	private boolean randomSpawn;
	private boolean baseSpawn;
	private boolean captureTheFlag;
	
	private int baseAmount = 0;
	
	private final HashMap<LasertagColor, Coordinate> teamSpawnCoords = new HashMap<>();
	private final HashMap<LasertagColor, Boolean> hasColor = new HashMap<>();
	private final List<Coordinate> baseCoords = new ArrayList<>();
	private final HashMap<Coordinate, LasertagColor> baseColor = new HashMap<>();
	private final HashMap<LasertagColor, BaseSphere> baseSphere = new HashMap<>();
	private final HashMap<LasertagColor, Coordinate> baseFlagCoord = new HashMap<>();
	private final HashMap<LasertagColor, Flag> baseFlag = new HashMap<>();
	
	private int protectionRaduis;
	
	public Map(String name, Area area, World world) {
		MAPS.add(this);
		this.name = name.substring(0, 1).toUpperCase()+name.substring(1);

		for (Entity e : world.getNearbyEntities(new Coordinate(area.getMaxX()-(area.getWidthX()/2f), area.getMaxY()-(area.getHeight()/2f), area.getMaxZ()-(area.getWidthZ()/2f)).getLocation(), area.getWidthX()+4, area.getHeight()+4, area.getWidthZ()+4)) {
			if(area.isInside(e.getLocation()) && e.getType() == EntityType.ARMOR_STAND) e.remove();
		}

		this.area = area;
		this.world = world;
		for(LasertagColor color : LasertagColor.values()) hasColor.put(color, false);
		
		NAME_MAPS.put(name.toLowerCase(), this);
	}
	

	public Area getArea() {
		return area;
	}

	
	

	public void setWithRandomSpawn(boolean value) {
		this.randomSpawn = value;
	}
	public boolean withRandomSpawn() {
		return this.randomSpawn;
	}

	public void setWithBaseSpawn(boolean value) {
		this.baseSpawn = value;
	}
	public boolean withBaseSpawn() {
		return this.baseSpawn;
	}

	public void setWithCaptureTheFlag(boolean ctf){
		this.captureTheFlag = ctf;
	}
	public boolean withCaptureTheFlag(){
		return captureTheFlag;
	}

	public void setTeamSpawnCoords(LasertagColor color, Coordinate coordinate, Coordinate flagCoord) {
		this.teamSpawnCoords.put(color, coordinate);
        if(captureTheFlag && flagCoord != null) {
        	baseFlag.put(color,new Flag(flagCoord.getLocation(),color));
        	baseFlagCoord.put(color,flagCoord);
		}
		this.hasColor.put(color, true);
		int amount = 0;
		for(LasertagColor c : LasertagColor.values()) {
			if(this.hasColor(c)) amount++;
		}
		this.baseAmount = amount;
		
		baseCoords.add(coordinate);
		baseColor.put(coordinate, color);
	}
	public Coordinate[] getBaseCoords() {
		return baseCoords.toArray(new Coordinate[0]);
	}
	public boolean hasColor(LasertagColor color) {
		return this.hasColor.get(color);
	}
	public Coordinate getTeamSpawnCoord(LasertagColor color) {
		if(hasColor(color)) return teamSpawnCoords.get(color);
		else return null;
	}
	public void drawBaseSphere(LasertagColor color, Player... players) {
		baseSphere.get(color).draw(players);
	}
	public int getBaseAmount() {
		return this.baseAmount;
	}


	public LasertagColor getBaseColor(Coordinate baseCoord) {
		return baseColor.get(baseCoord);
	}
	public BaseSphere getBaseSphere(LasertagColor color) {
		return baseSphere.get(color);
	}
	public Flag getBaseFlag(LasertagColor color) {
		return baseFlag.get(color);
	}

	public Coordinate getBaseFlagCoord(LasertagColor color){
		if (captureTheFlag) {
			return baseFlagCoord.get(color);
		}
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
	
	
	
	public int getProtectionRaduis() {
		return protectionRaduis;
	}
	public void setProtectionRaduis(int protectionRaduis) {
		this.protectionRaduis = protectionRaduis;
		
		for(Coordinate coord : baseCoords) {
			baseSphere.put(baseColor.get(coord), new BaseSphere(coord, protectionRaduis, baseColor.get(coord).getColor(), area));
		}
	}

    public Coordinate getBaseCoord(LasertagColor color){
	    for(Coordinate coordinate : baseCoords){
	        if(baseColor.get(coordinate) == color) return coordinate;
        }
	    return null;
    }

	public Location getRandomSpawnLocation() {
		int x = (int) (Math.random()*(((area.getMaxX()-1)-(area.getMinX()+1))+1))+area.getMinX();
		int z = (int) (Math.random()*(((area.getMaxZ()-1)-(area.getMinZ()+1))+1))+area.getMinZ();
		for(int i = area.getMinY(); i < area.getMaxY(); i++) {
			if(world.getBlockAt(x, i, z).getType().isAir() && world.getBlockAt(x, i+1, z).getType().isAir()) return new Location(world, x, i, z);
		}
		return getRandomSpawnLocation();
	}
	
	
	public Location getTeamSpawnLoc(LasertagColor color) {
		if(hasColor(color)) return teamSpawnCoords.get(color).getLocation();
		else return null;
	}
	
	
	private boolean enabled = true;
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isEnabled() {
		return enabled;
	}


	public void enableCTF(Session session, List<LasertagColor> colors){
		if(captureTheFlag) {
			removeAllArmorStands();
			baseFlag.forEach((lasertagColor, flag) -> {
				if(colors.contains(lasertagColor)) flag.enable(session);
			});
		}
	}
	public void disableCTF(){
		if(captureTheFlag) {
			removeAllArmorStands();
			baseFlag.forEach((lasertagColor, flag) -> flag.disable());
		}
	}
	private void removeAllArmorStands(){
		for(Entity e : world.getNearbyEntities(new Location(world, area.getMaxX()-area.getWidthX()/2d, area.getMaxY()-area.getHeight()/2d, area.getMaxZ()-area.getWidthZ()/2d), area.getWidthX()+4, area.getHeight()+4, area.getWidthZ()+4)){
			if(e instanceof ArmorStand) {
				((ArmorStand) e).getEquipment().clear();
				e.remove();
			}
		}
	}

	
	private static final HashMap<String, Map> NAME_MAPS = new HashMap<>();
	public static Map getMapByName(String name) {
		return NAME_MAPS.get(name.toLowerCase());
	}

	private final HashMap<Player, Boolean> playerCoolingDownFromParticleEffect = new HashMap<>();
	public boolean checkLocPlayerShootingFrom(Player p){
		Session session = Session.getPlayerSession(p);
		if(session == null) return true;
		if((session.isSolo() && !session.getMap().withRandomSpawn()) | (session.isTeams() && session.getMap().withBaseSpawn())) {
			for(Coordinate coord : baseCoords){
				if(baseCoords.indexOf(coord) < ((session.isSolo())?session.getPlayers().length:session.getTeamsAmount()) && p.getLocation().distance(coord.getLocation()) < protectionRaduis){
					playerCoolingDownFromParticleEffect.putIfAbsent(p,false);
					if(!playerCoolingDownFromParticleEffect.get(p)) {
						baseSphere.get(baseColor.get(coord)).draw(p);
						p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+""+ChatColor.BOLD+"You can't shoot while in a base!"));
						playerCoolingDownFromParticleEffect.put(p, true);
						Utils.runLater(() -> playerCoolingDownFromParticleEffect.put(p, false), 25);
					}
					return true;
				}
			}
		}
		return false;
	}
	public boolean checkPlayerLaserLoc(Location loc, Player p){
		Session session = Session.getPlayerSession(p); if(session == null) return true;
		if((session.isSolo() && !session.getMap().withRandomSpawn()) | (session.isTeams() && session.getMap().withBaseSpawn())) {
			for (Coordinate coord : baseCoords) {
				if (baseCoords.indexOf(coord) < ((session.isSolo()) ? session.getPlayers().length : session.getTeamsAmount()) && loc.distance(coord.getLocation()) < protectionRaduis) {
					playerCoolingDownFromParticleEffect.putIfAbsent(p,false);
					if(!playerCoolingDownFromParticleEffect.get(p)) {
						baseSphere.get(baseColor.get(coord)).draw(p);
						p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "" + ChatColor.BOLD + "You can't shoot into a base!"));
						playerCoolingDownFromParticleEffect.put(p,true);
						Utils.runLater(()->playerCoolingDownFromParticleEffect.put(p,false), 25);
					}
					return true;
				}
			}
		}
		return false;
	}

	public void checkPlayerPosition(Player p){
		Session session = Session.getPlayerSession(p); if(session == null) return;
		if(session.withCaptureTheFlag()) return;
		if((session.isSolo() && !session.getMap().withRandomSpawn()) | (session.isTeams() && session.getMap().withBaseSpawn())) {
			for(Coordinate coord : baseCoords){
				if(baseColor.get(coord) != session.getPlayerColor(p) && baseCoords.indexOf(coord) < ((session.isSolo())?session.getPlayers().length:session.getTeamsAmount()) && p.getLocation().distance(coord.getLocation()) < protectionRaduis){
					p.damage(4);
					baseSphere.get(baseColor.get(coord)).draw(p);
					p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED+""+ChatColor.BOLD+"You can't shoot into a base!"));
				}
			}
		}
	}
}
