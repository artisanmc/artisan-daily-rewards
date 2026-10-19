package com.github.ibanetchep.artisandailyrewards.listener;

import com.github.ibanetchep.artisandailyrewards.DailyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Loads a player's streak on join (then claims or reminds), forgets it on quit. */
public final class PlayerConnectionListener implements Listener {

    private final DailyService service;

    public PlayerConnectionListener(DailyService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        service.join(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        service.quit(event.getPlayer());
    }
}
