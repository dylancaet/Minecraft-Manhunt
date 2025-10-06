package aio.manhunt.tracker;

import aio.manhunt.Manhunt;
import aio.manhunt.event.EventHandler;
import aio.manhunt.event.EventType;
import lombok.Getter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TrackerHandler
{
    private static TrackerHandler instance;

    @Getter private HashMap<String, PlayerTracker> playerTrackerMap;
    private Scoreboard scoreboard;
    private Team runners;

    private final EventHandler<MinecraftServer> onTickHandle = this::onTick;

    public TrackerHandler()
    {
        playerTrackerMap = new HashMap<String, PlayerTracker>();
        scoreboard = Manhunt.SERVER.getScoreboard();
        runners = createRunnersTeam();

        Manhunt.EVENTS.subscribe(EventType.SERVER_TICK, onTickHandle);
    }

    public static synchronized TrackerHandler getInstance()
    {
        if (instance == null)
            instance = new TrackerHandler();

        return instance;
    }

    public void addTrackedPlayer(ServerPlayerEntity player)
    {
        if (playerTrackerMap.containsKey(player.getName().getLiteralString()))
        {
            Manhunt.LOGGER.info(String.format("%s already being tracked.", player.getName().getLiteralString()));
            return;
        }

        assignRunner(player, true);

        Manhunt.LOGGER.info(String.format("%s added to be tracked.", player.getName().getLiteralString()));
    }

    public void removeTrackedPlayer(ServerPlayerEntity player)
    {
        assignRunner(player, false);
    }

    private void assignRunner(ServerPlayerEntity player, Boolean value)
    {
        String playerName = player.getName().getLiteralString();

        if (value)
        {
            playerTrackerMap.put(playerName, new PlayerTracker(player));
            scoreboard.addScoreHolderToTeam(playerName, runners);
        }
        else
        {
            if (runners.getPlayerList().contains(playerName)) {
                playerTrackerMap.get(playerName).dispose();
                scoreboard.removeScoreHolderFromTeam(playerName, runners);
                playerTrackerMap.remove(playerName);
            }
        }
    }

    private Team createRunnersTeam()
    {
        Team runners = scoreboard.getTeam("runners");

        if (runners != null)
            scoreboard.removeTeam(runners);

        runners = scoreboard.addTeam("runners");
        runners.setColor(Formatting.RED);
        runners.setShowFriendlyInvisibles(false);
        runners.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.ALWAYS);

        return runners;
    }

    private void onTick(MinecraftServer server)
    {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        List<PlayerTracker> trackers = playerTrackerMap.values().stream().toList();

        for (var player : players)
        {
            if (playerTrackerMap.containsKey(player.getName().getLiteralString()))
                continue;

            var playerPos = player.getBlockPos();
            var distances = new ArrayList<Float>();

            for (var tracker : trackers)
            {
                var currentDimension = player.getEntityWorld().getRegistryKey();

                if (currentDimension == tracker.getCurrentDimension())
                {
                    float distance = (float)Math.sqrt(tracker.getOverworldBlock().getSquaredDistance(playerPos));
                    distances.add(distance);
                }
            }

            player.sendMessage(Text.of(distances.stream().map(Object::toString).collect(Collectors.joining(", "))), true);
        }
    }
}
