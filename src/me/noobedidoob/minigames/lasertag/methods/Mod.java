package me.noobedidoob.minigames.lasertag.methods;

import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.utils.Pair;
import me.noobedidoob.minigames.utils.Utils.ValueType;

import java.util.Locale;

public enum Mod{
    POINTS(1, true, 1, 5, "Points: ","Normal amount of points a player gets"),
    SHOT_KILL_EXTRA_POINTS(0, true,-1, 5, "Shot kill points: ", "Extra points when a player shot normal"),
    SNIPER_KILL_EXTRA_POINTS(1, true, -1, 10, "Sniper kill points: ", "Extra points when killing with snipe-shot"),
    HEADSHOT_EXTRA_POINTS(1, true, -1, 10, "Headshot kill points: ", "Extra ponts when killing with headshot"),
    BACKSTAB_EXTRA_POINTS(2, true, -1, 10, "Backstab kill points: ", "Extra points when backstabbing"),
    GRENADE_KILL_EXTRA_POINTS(1, true, -1, 10, "Grenade kill points: ", "Extra points when killing with an grenade"),
    PVP_KILL_EXTRA_POINTS(0, true, -1, 5, "Melee kill points: ", "Extra ponts when killed at melee"),
    STREAK_EXTRA_POINTS(2, true, -1, 10, "Streak kill points: ", "Extra points when having a streak"),
    STREAK_SHUTDOWN_EXTRA_POINTS(2, true, 2, 10, "Streak shutdown points: ", "Extra points when shutting down a streak"),
    MULTIKILLS_EXTRA_POINTS(3, true, 3, 10, "Multiple kills points: ", "Extra points when killing multiple players at once"),
    CAPTURE_THE_FLAG_POINTS(15, true, -1, 30, "Flag capture points: ", "Points a player gets when delivering an enemys flag", true),

    MINIMAL_SNIPE_DISTANCE(25,  "Minimal distance of a shot to be a sniper shot", true),

    MINIMAL_KILLS_FOR_STREAK(5, "Minimal kill amount required for a streak", true),

    SPAWNPROTECTION_SECONDS(10, "Seconds a player is protected after spawning", true),

    WIDTH_ADDON(0D, true, -0.5D, 1, "Width addon: ", "Addon to the width of a players hitbox"),
    HEIGHT_ADDON(0d, true, -0.5D, 1, "Height addon: ", "ddon to the height of a players hitbox", true),

    SHOOT_THROUGH_BLOCKS(false, true, false, true, "Shoot trough blocks: ", "Shoot through blocks"),
    HIGHLIGHT_PLAYERS(false, true, false, true, "Glowing players: ", "Making players glow and more visible", true),

    GRENADE_EFFECT_RADUIS(2,true, 1, 5,"Grenade radius: ", "Radius in wich a player is affected by the grenade explosion"),
    GRENADE_MAX_DETONATION_COUNTDOWN(10,"Max selectable seconds for the detonation cooldown"),
    GRENADE_MIN_DETONATION_COUNTDOWN(1,"Min selectable seconds for the detonation cooldown", true),

    LASERGUN_NORMAL_COOLDOWN_TICKS(12, true, 1, 30, "Lasergun cooldown: ", "Ticks (20 ticks = 1 second) a lasergun takes to cool down"),
    LASERGUN_MULTIWEAPONS_COOLDOWN_TICKS(2, true, 1, 12, "Lasergun cooldown: ", "Ticks a lasergun takes to cool down when playing with multiple weapons"),
    SNIPER_COOLDOWN_TICKS(100, true, 10, 100, "Sniper cooldown: ", "Ticks a sniperrifle takes to cool down"),
    SHOTGUN_COOLDOWN_TICKS(40, true, 5, 40, "Shotgun cooldown: ", "Ticks a shotgun takes to cool down"),
    GRENADE_COOLDOWN_TICKS(400, true, 10, 100, "Grenade cooldown: ", "Ticks a grenade takes to cool down"),
    SNIPER_AMMO(3, true, 1, 10,  "Sniper ammo: ", "Maximal sniper ammo", true),

    LASERGUN_NORMAL_DAMAGE(100, true, 50 , 100, "Lasergun damage: ", "Normal lasergun shot damage"),
    LASERGUN_MULTIWEAPONS_DAMAGE(7, true, 7, 15, "Lasergun damage: ", "lasergun shot damage when playing with multiple weapons"),
    LASERGUN_PVP_DAMAGE(8, true, 8, 20, "Lasergun melee damage: ", "Lasergun melee damage (only without multiweapons)"),
    SHOTGUN_DAMAGE(11, true, 5, 20, "Shotgun damage: ", "Shotgun shot damage"),
    SNIPER_DAMAGE(100, true, 10, 20, "Sniper damage: ", "Sniper shot damage"),
    DAGGER_DAMAGE(13, true, 5, 20, "Dagger damage: ", "Stabber melee damage"),
    GRENADE_DAMAGE(15, true, 5, 20, "Grenade damage: ", "Grenade explosion damage", true),

    HEADSHOT_DAMAGE_MULTILIER(1.3d, true, 0.5D, 5D, "Headshot damage multiplier: ", "Extra damage when hitting the head"),
    SNIPE_SHOT_DAMAGE_MULTIPLIER(1.3d, true, 0.5D, 5D, "Sniper shot damage multiplier: ", "Extra damage when sniping", true),


    POINT_EVENT_FIRST_EVENT_DELAY_TICKS(60*20, "Countdown after the point events start fot the first time"),
    POINT_EVENT_DEFAULT_LENGTH_TICKS(20*45, "How long a point event lasts"),
    POINT_EVENT_NEXT_EVENT_DELAY_TICKS(20*45, "Countdown between the point events"),
    POINT_EVENT_ONLY_POINTS(true, "If events shall only change points");


    private Object ogValue;
    private final String description;
    private final ValueType valueType;

    public final boolean inPointEvents ;
    public final Pair eventChangeRange;
    public final String eventName;

    public final boolean spaceAfter;

    Mod(Object value, String description) {
        this.ogValue = value;
        this.description = description;
        if(value instanceof Double) valueType = ValueType.DOUBLE;
        else if(value instanceof Integer) valueType = ValueType.INTEGER;
        else valueType = ValueType.BOOLEAN;

        this.inPointEvents = false;
        this.eventChangeRange = new Pair(null, null);
        this.eventName = "";

        this.spaceAfter = false;
    }
    Mod(Object value, String description, boolean spaceAfter) {
        this.ogValue = value;
        this.description = description;
        if(value instanceof Double) valueType = ValueType.DOUBLE;
        else if(value instanceof Integer) valueType = ValueType.INTEGER;
        else valueType = ValueType.BOOLEAN;

        this.inPointEvents = false;
        this.eventChangeRange = new Pair(null, null);
        this.eventName = "";

        this.spaceAfter = spaceAfter;
    }

    Mod(Object value, boolean inPointEvents, Object min, Object max, String eventName, String description) {
        this.ogValue = value;
        this.description = description;
        if(value instanceof Double) valueType = ValueType.DOUBLE;
        else if(value instanceof Integer) valueType = ValueType.INTEGER;
        else valueType = ValueType.BOOLEAN;

        this.inPointEvents = inPointEvents;
        this.eventChangeRange = new Pair(min, max);
        this.eventName = eventName;

        this.spaceAfter = false;
    }
    Mod(Object value, boolean inPointEvents, Object min, Object max, String eventName, String description, boolean spaceAfter) {
        this.ogValue = value;
        this.description = description;
        if(value instanceof Double) valueType = ValueType.DOUBLE;
        else if(value instanceof Integer) valueType = ValueType.INTEGER;
        else valueType = ValueType.BOOLEAN;

        this.inPointEvents = inPointEvents;
        this.eventChangeRange = new Pair(min, max);
        this.eventName = eventName;

        this.spaceAfter = spaceAfter;
    }



    public Object getOg() {
        return ogValue;
    }
    public int getOgInt() {
        try {
            return (int) ogValue;
        } catch (Exception e) {
            return 0;
        }
    }
    public double getOgDouble() {
        try {
            return (double) ogValue;
        } catch (Exception e) {
            return 0;
        }
    }
    public boolean getOgBoolean() {
        try {
            return (boolean) ogValue;
        } catch (Exception e) {
            return false;
        }
    }

    public void setOgValue(Object value) {
        if(value == null) return;
        if(value.getClass() == this.getOg().getClass() | (value instanceof Integer && ogValue instanceof Double)) {
            ogValue = value;
        } else {
            System.out.println("Error at setting mod! Value types are not the same!");
        }
    }


    public String getDescription() {
        return description;
    }
    public ValueType getValueType() {
        return valueType;
    }
    public String getCommand(){
        StringBuilder n = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : name().toLowerCase().toCharArray()) {
            if(c != '_') n.append(nextUpperCase ? Character.toString((c)).toUpperCase() : c);
            nextUpperCase = (c == '_');
        }
        return n.toString();
    }

//    public String getName(){
//        return this.name().charAt(0)+this.name().substring(1).toLowerCase()
//    }

    public static Mod getMod(String name) {
        for(Mod m : Mod.values()) {
            if(m.name().equalsIgnoreCase(name.replace(" ", "").replace("-", "_"))) return m;
        }
        StringBuilder n = new StringBuilder();
        for (char c : name.toCharArray()) n.append(Character.isLowerCase(c) ? c : "_"+c);
        for(Mod m : Mod.values()) {
            if(m.name().equalsIgnoreCase(n.toString().replace(" ", ""))) return m;
        }

        return null;
    }



    public static void registerMods(Minigames minigames) {
        for(Mod m : Mod.values()) {
            String configModName = "Lasertag.mods."+m.name().toLowerCase().replace("_", "-");
            if(minigames.getConfig().contains(configModName)) m.setOgValue(minigames.getConfig().get(configModName));
            else minigames.getConfig().set(configModName,m.ogValue);
        }
    }


}