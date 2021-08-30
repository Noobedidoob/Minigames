package me.noobedidoob.minigames.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import me.noobedidoob.minigames.Minigames;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

@SuppressWarnings("unused")
public class Utils {
	
	public static BukkitTask runLater(Runnable runnable, int delay){
		return new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskLater(Minigames.INSTANCE, delay);
	}
	public static BukkitTask runTimer(Runnable runnable, int delay, int interval){
		return new BukkitRunnable(){
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskTimer(Minigames.INSTANCE,delay,interval);
	}
	public static void runDefinedRepeater(Runnable r, int delay, int interval, int repeatAmount){
		new BukkitRunnable(){
			int times = 0;
			@Override
			public void run(){
				r.run();
				if(times++ == repeatAmount-1) cancel();
			}
		}.runTaskTimer(Minigames.INSTANCE,delay,interval);
	}
	public static void runDefinedRepeater(Runnable r, int delay, int interval, int repeatAmount, Runnable runWhenComplete){
		new BukkitRunnable(){
			int times = 0;
			@Override
			public void run(){
				r.run();
				if(times++ == repeatAmount-1) {
					cancel();
					runWhenComplete.run();
				}
			}
		}.runTaskTimer(Minigames.INSTANCE,delay,interval);
	}

	public static ItemStack getItemStack(Material material, String displayName, ItemFlag... flags) {
		return getItemStack(material, displayName, 1, flags);
	}
	public static ItemStack getItemStack(Material material, String displayName, int amount, ItemFlag... flags) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();
		if(meta == null) return null;
		if(displayName != null) meta.setDisplayName(displayName);
		meta.setUnbreakable(true);
		if(flags.length > 0) meta.addItemFlags(flags);
		else meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_UNBREAKABLE);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack getItemStack(Material material, String displayName, int amount, List<String> lore, ItemFlag... flags) {
		ItemStack item = new ItemStack(material,amount);
		ItemMeta meta = item.getItemMeta();
		if(meta == null) return null;
		if(displayName != null) meta.setDisplayName(displayName);
		meta.setUnbreakable(true);
		if(lore != null) meta.setLore(lore);
		if(flags.length > 0) meta.addItemFlags(flags);
		else meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}
	public static ItemStack getLeatherArmorItem(Material leatherArmorMaterial, String displayName, Color color) {
		return getLeatherArmorItem(leatherArmorMaterial, displayName, color, null, 1);
	}
	public static ItemStack getLeatherArmorItem(Material leatherArmorMaterial, String displayName, Color color, int amount) {
		return getLeatherArmorItem(leatherArmorMaterial, displayName, color, null, amount);
	}
	public static ItemStack getLeatherArmorItem(Material leatherArmorMaterial, String displayName, Color color, List<String> lore) {
		return getLeatherArmorItem(leatherArmorMaterial, displayName, color, lore, 1);
	}
	public static ItemStack getLeatherArmorItem(Material leatherArmorMaterial, String displayName, Color color, List<String> lore, int amount) {
		ItemStack item = new ItemStack(leatherArmorMaterial, amount);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		if(meta == null) return null;
		if(displayName != null) meta.setDisplayName(displayName);
		meta.setColor(color);
		if(lore != null) meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_UNBREAKABLE);
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack getPlayerSkullItem(Player p, String displayName){
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		if(skullMeta == null) return null;
		skullMeta.setDisplayName((displayName != null)?displayName:p.getName());
		skullMeta.setOwningPlayer(p);
		skull.setItemMeta(skullMeta);
		return skull;
	}
	public static ItemStack getPlayerSkullItem(Player p, String displayName, List<String> lore){
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		if(skullMeta == null) return null;
		skullMeta.setDisplayName((displayName != null)?displayName:p.getName());
		skullMeta.setOwningPlayer(p);
		if(lore != null) skullMeta.setLore(lore);
		skull.setItemMeta(skullMeta);
		return skull;
	}


	public static void removeItemFromPlayer(Player p, ItemStack i) {
		ItemStack[] ogInv = p.getInventory().getContents();
		List<ItemStack> isList = new ArrayList<>();
		for(ItemStack is : ogInv) {
			if(is != i) isList.add(i);
		}
		ItemStack[] newInv = new ItemStack[isList.size()];
		newInv = isList.toArray(newInv);
		p.getInventory().clear();
		p.getInventory().setContents(newInv);

	}

	public static String getTimeFormatFromLong(long seconds, TimeFormat type) {
		long millis = seconds*1000;
		SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
		if(type == TimeFormat.HOURS) dateFormat = new SimpleDateFormat("HH:mm:ss");
		else if(type == TimeFormat.SECONDS) dateFormat = new SimpleDateFormat("ss");
		TimeZone tz = TimeZone.getTimeZone("MESZ");
		dateFormat.setTimeZone(tz);
		return dateFormat.format(new Date(millis));
	}

	public enum TimeFormat{
		SECONDS,
		MINUTES,
		HOURS;

		public static TimeFormat getFromString(String s) {
			String format = s.substring(0, 1).toUpperCase();
			for(TimeFormat tf : TimeFormat.values()) {
				if(tf.name().substring(0, 1).equalsIgnoreCase(format)) {
					return tf;
				}
			}
			return TimeFormat.MINUTES;
		}
	}

	public static boolean contains(String string, String... strings) {
		for(String s : strings) {
			if(string.toUpperCase().contains(s.toUpperCase())) return true;
		}
		return false;
	}

	public static boolean isBetween(double min, double value, double max) {
		return value < max && value > min;
	}
	
	public static int randomInt(int min, int max) {
		return (int) Math.round((Math.random() * ((max - min) + 1)) + min);
	}
	public static double randomDouble(double min, double max) {
		return (Math.random() * ((max - min) + 1)) + min;
	}
	
	public static boolean isNumericOnly(String s) {
		return s.matches("\\d+");
	}
	public static boolean isAlphabeticOnly(String s) {
		char[] chars = s.toCharArray();
	    for (char c : chars) {
	        if(!Character.isLetter(c)) {
	            return false;
	        }
	    }
	    return true;
	}

	public static Location getPlayerBackLocation(Player p){
		Location backLoc = p.getLocation().add(0,1,0).add(p.getLocation().getDirection().multiply(-1));

		Vector dir = p.getLocation().getDirection().multiply(-1);
		Location backLocation = p.getLocation().add(dir);
		backLocation.setY(p.getLocation().getY()+1);
		return backLocation;
	}

	public static boolean isPlayerBehindOtherPlayer(Player player1, Player player2){
		return player1.getLocation().add(0,1,0).distance(getPlayerBackLocation(player2)) <= 0.5;
	}

	public static ArrayList<Location> getRay(Location a, double distance, double space){
		ArrayList<Location> list = new ArrayList<>();

		Vector direction = a.getDirection();
		direction.multiply(space);
		for(double d = 0; d < distance; d += space){
			Location loc = a.add(direction);
			list.add(loc.clone());
		}
		return list;
	}

	public enum ValueType{
		INTEGER("integer (full number)"),
		DOUBLE("fractional number"),
		BOOLEAN("true/false");

		public final String description;
		ValueType(String description){
			this.description = description;
		}

		public String getName(){
			return this.name().charAt(0)+this.name().substring(1).toLowerCase();
		}
	}

	public static boolean IsOneTrue(boolean... vals){
		boolean state = false;
		for (boolean v: vals) {
			if(!v) continue;
			if(!state) state = true;
			if(state) state = false;
		}
		return state;
	}
	public static int TrueAmount(boolean... vals){
		int i = 0;
		for (boolean v: vals) {
			if(v) i++;
		}
		return i;
	}

}
