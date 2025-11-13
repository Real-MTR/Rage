package io.mtr.rage;

import io.mtr.rage.adapter.RageAdapter;
import io.mtr.rage.listener.RageListener;
import io.mtr.rage.manager.RageManager;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author MTR
 */

@Getter
public class Rage {

    private final JavaPlugin plugin;
    private final RageManager manager;
    private final RageAdapter adapter;

    public Rage(JavaPlugin plugin, RageAdapter adapter) {
        this.plugin = plugin;
        this.adapter = adapter;
        this.manager = new RageManager(this);

        plugin.getServer().getPluginManager().registerEvents(new RageListener(this), plugin);
    }

    /**
     * Running a task to update all players' scoreboard (Optional)
     */
    public void runTask(long delayTicks) {
        // Dont ask, just deal with it.
        long delay = delayTicks < 0 ? 0 : delayTicks;

        // We cannot use async tasks.
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                manager.update(player, adapter.getTitle(), adapter.getLines());
            }
        }, 0L, delay);
    }
}