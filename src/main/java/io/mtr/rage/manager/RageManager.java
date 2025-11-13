package io.mtr.rage.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.ObjectiveMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.RenderType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore.Action;
import io.mtr.rage.Rage;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author MTR
 */

@RequiredArgsConstructor
public class RageManager {

    private final Rage instance;

    private String getObjectiveName(Player player) {
        return "ragesb_" + player.getUniqueId().toString().replace("-", "");
    }

    private final Map<UUID, String> lastTitle = new HashMap<>();
    private final Map<UUID, List<String>> lastLines = new HashMap<>();

    private void sendCreateObjective(Player viewer, String objectiveName, String title) {
        WrapperPlayServerScoreboardObjective create = new WrapperPlayServerScoreboardObjective(
                objectiveName,
                ObjectiveMode.CREATE,
                Component.text(title),
                RenderType.INTEGER
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, create);

        WrapperPlayServerDisplayScoreboard display = new WrapperPlayServerDisplayScoreboard(1, objectiveName);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, display);
    }

    private void sendUpdateObjective(Player viewer, String objectiveName, String title) {
        WrapperPlayServerScoreboardObjective update = new WrapperPlayServerScoreboardObjective(
                objectiveName,
                ObjectiveMode.UPDATE,
                Component.text(title),
                RenderType.INTEGER
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, update);
    }

    private void sendRemoveObjective(Player viewer, String objectiveName) {
        WrapperPlayServerScoreboardObjective remove = new WrapperPlayServerScoreboardObjective(
                objectiveName,
                ObjectiveMode.REMOVE,
                Component.empty(),
                null
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, remove);

        WrapperPlayServerDisplayScoreboard clear = new WrapperPlayServerDisplayScoreboard(1, "");
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, clear);
    }

    private void sendScores(Player viewer, String objectiveName, List<String> lines) {
        lines = makeUnique(lines);

        List<String> old = lastLines.getOrDefault(viewer.getUniqueId(), Collections.emptyList());
        if (old.equals(lines)) return;

        for (String oldLine : old) {
            if (!lines.contains(oldLine)) {
                WrapperPlayServerUpdateScore rem = new WrapperPlayServerUpdateScore(
                        oldLine,
                        Action.REMOVE_ITEM,
                        objectiveName,
                        Optional.empty()
                );
                PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, rem);
            }
        }

        int score = lines.size();
        for (String line : lines) {
            WrapperPlayServerUpdateScore add = new WrapperPlayServerUpdateScore(
                    line,
                    Action.CREATE_OR_UPDATE_ITEM,
                    objectiveName,
                    Optional.of(score--)
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, add);
        }

        lastLines.put(viewer.getUniqueId(), new ArrayList<>(lines));
    }

    private List<String> makeUnique(List<String> input) {
        Set<String> used = new HashSet<>();
        List<String> result = new ArrayList<>();

        for (String line : input) {
            String unique = line;

            while (!used.add(unique)) {
                unique += "§r";
            }

            result.add(unique);
        }
        return result;
    }

    /**
     * Update (or create) a sidebar for the given player (viewer).
     * This manager keeps sidebars per-viewer (unique objective name), which
     * avoids cross-player collisions when different viewers should see different content.
     *
     * @param viewer who will see the sidebar
     * @param title  title text
     * @param lines  lines (top -> bottom). Max lines depend on client (use <= 15)
     */
    public void update(Player viewer, String title, List<String> lines) {
        String objectiveName = getObjectiveName(viewer);

        boolean newObjective = !lastTitle.containsKey(viewer.getUniqueId());
        String prevTitle = lastTitle.get(viewer.getUniqueId());

        if (newObjective) {
            sendCreateObjective(viewer, objectiveName, title);
        } else if (!Objects.equals(prevTitle, title)) {
            sendUpdateObjective(viewer, objectiveName, title);
        }

        sendScores(viewer, objectiveName, lines);
        lastTitle.put(viewer.getUniqueId(), title);
    }

    /**
     * Remove the sidebar from the given player.
     */
    public void remove(Player viewer) {
        String objectiveName = getObjectiveName(viewer);
        sendRemoveObjective(viewer, objectiveName);

        lastTitle.remove(viewer.getUniqueId());
        lastLines.remove(viewer.getUniqueId());
    }

    /**
     * Remove sidebars for all online players.
     */
    public void clearAll() {
        for (Player player : instance.getPlugin().getServer().getOnlinePlayers()) {
            remove(player);
        }
    }
}