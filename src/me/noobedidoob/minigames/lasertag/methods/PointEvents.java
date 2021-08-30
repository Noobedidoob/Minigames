package me.noobedidoob.minigames.lasertag.methods;

import me.noobedidoob.minigames.Minigames;
import me.noobedidoob.minigames.lasertag.session.Session;
import me.noobedidoob.minigames.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Random;

public class PointEvents {
    //TODO: Enable PointsEvents

    Session session;

    ArrayList<Mod> mods = new ArrayList<>();
    public PointEvents(Session session){
        this.session = session;
        for(Mod m : Mod.values()){
            if(m.inPointEvents) mods.add(m);
        }
    }


    boolean enabled;
    public void setEnabled(boolean enabled){
        if(enabled && this.enabled && this.running) stopEvents();
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
    boolean running;
    public boolean isRunning() {
        return running;
    }



    boolean coolingDown;
    private BukkitTask currentTimer;
    private BossBar bar;
    public void startEvents(){
        System.out.println("starting point events");
        if(enabled && !running){
            running = true;
            coolingDown = true;
            bar = Bukkit.createBossBar("§aNext Event in §r§b"+Utils.getTimeFormatFromLong((long) session.getIntMod(Mod.POINT_EVENT_FIRST_COUNTDOWN_TICKS)/20, Utils.TimeFormat.SECONDS),
                    BarColor.RED, BarStyle.SOLID);
            for(Player p : session.getPlayers()) bar.addPlayer(p);
            bar.setVisible(true);
            currentTimer = new BukkitRunnable(){
                int curr = session.getIntMod(Mod.POINT_EVENT_FIRST_COUNTDOWN_TICKS);
                @Override
                public void run() {
                    if (curr > 0) {
                        curr--;
                        bar.setProgress((float) curr / session.getIntMod(Mod.POINT_EVENT_FIRST_COUNTDOWN_TICKS));
                        bar.setTitle("§aNext Event in §r§b"+Utils.getTimeFormatFromLong((long) curr/20, Utils.TimeFormat.SECONDS));
                    } else {
                        cancel();
                        startRandomEvent();
                    }
                }
            }.runTaskTimer(session.minigames, 1, 1);
        } else if(!enabled) System.out.println("disabled");
    }

    public void stopEvents(){
        if(currentTimer != null & !currentTimer.isCancelled()) currentTimer.cancel();
        if(currentEvent != null) session.resetMod(currentEvent.mod);
        if(bar != null) bar.removeAll();
    }

    private Event currentEvent;
    private void startRandomEvent(){
        coolingDown = false;
        currentEvent = new Event(session, mods);
        bar.setColor(BarColor.GREEN);
        bar.setTitle("§2Current Event: §b"+currentEvent.name+currentEvent.value.toString());
        session.setMod(currentEvent.mod, currentEvent.value);
        currentTimer = new BukkitRunnable(){
            int curr = session.getIntMod(Mod.POINT_EVENT_DEFAULT_EVENT_LENGTH_TICKS);
            @Override
            public void run() {
                if(curr > 0){
                    curr--;
                    bar.setProgress((float) curr / session.getIntMod(Mod.POINT_EVENT_DEFAULT_EVENT_LENGTH_TICKS));
                } else {
                    cancel();
                    session.resetMod(currentEvent.mod);
                    currentTimer = new BukkitRunnable(){
                        int curr = session.getIntMod(Mod.POINT_EVENT_DEFAULT_COUNTDOWN_TICKS);
                        @Override
                        public void run() {
                            if (curr > 0) {
                                curr--;
                                bar.setProgress((float) curr / session.getIntMod(Mod.POINT_EVENT_DEFAULT_COUNTDOWN_TICKS));
                                bar.setTitle("§aNext Event in §r§b"+Utils.getTimeFormatFromLong((long) curr/20, Utils.TimeFormat.SECONDS));
                            } else {
                                cancel();
                                startRandomEvent();
                            }
                        }
                    }.runTaskTimer(session.minigames, 1, 1);
                }
            }
        }.runTaskTimer(session.minigames, 1 , 1);
    }


}

class Event{

    public final Mod mod;
    public final String name;
    public final int ticks;
    public final Object value;

    public Event(Session session, ArrayList<Mod> mods){
        Mod randomMod = mods.get(Utils.randomInt(0, mods.size()-1));
        System.out.println("new event: "+randomMod.getCommand());
        while ((randomMod.name().contains("MULTIWEAPONS") | randomMod.name().contains("MULTIPLIER")) & !session.withMultiweapons()) randomMod = mods.get(Utils.randomInt(0, mods.size()-1));
        while (randomMod.name().contains("NORMAL") & session.withMultiweapons()) randomMod = mods.get(Utils.randomInt(0, mods.size()-1));
        this.mod = randomMod;
        this.name = mod.eventName;

        System.out.println("instantiating "+mod.name()+" event");
        int max = (session.getTime(Utils.TimeFormat.SECONDS) < 60)?session.getTime(Utils.TimeFormat.SECONDS)*20:20*60;
        ticks = Utils.randomInt(Math.round(max/2f), max);
        Object value = mod.getOg();
        switch (mod.getValueType()){
            case INTEGER:
                value = Utils.randomInt((Integer) mod.eventChangeRange.get1(), (Integer) mod.eventChangeRange.get2());
                if(mod.name().contains("TICK")) value = ((int) value )/ 20;
                break;
            case DOUBLE:
                try {
                    value = Utils.randomDouble((Double) mod.eventChangeRange.get1(),(Double) mod.eventChangeRange.get2());
                } catch (Exception exception) {
                    try {
                        value = Utils.randomInt((Integer) mod.eventChangeRange.get1(), (Integer) mod.eventChangeRange.get2());
                    } catch (Exception e) {
                        value = mod.getOg();
                        Minigames.severe("ERROR occured while parsing");
                        e.printStackTrace();
                    }
                }
                break;
            case BOOLEAN:
                value = new Random().nextBoolean();
                break;
            default:
                break;
        }
        this.value = value;
    }
}
