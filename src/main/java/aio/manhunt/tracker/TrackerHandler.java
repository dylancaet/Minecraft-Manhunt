package aio.manhunt.tracker;

import aio.manhunt.Manhunt;
import lombok.Getter;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Optional;

public class TrackerHandler
{
    private static TrackerHandler instance;

    @Getter private HashMap<String, PlayerTracker> playerTrackerMap;
    private Scoreboard scoreboard;
    private Team runners;

    public TrackerHandler()
    {
        playerTrackerMap = new HashMap<String, PlayerTracker>();
        scoreboard = Manhunt.SERVER.getScoreboard();
        runners = createRunnersTeam();
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


}
