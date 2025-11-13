package io.mtr.rage.listener;

import io.mtr.rage.Rage;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit events for the SidebarManager.
 * Automatically creates and removes sidebars when players connect or disconnect.
 *
 * @author MTR
 */

@RequiredArgsConstructor
public class RageListener implements Listener {

    private final Rage instance;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        instance.getManager().update(
                event.getPlayer(),
                instance.getAdapter().getTitle(),
                instance.getAdapter().getLines()
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        instance.getManager().remove(event.getPlayer());
    }
}