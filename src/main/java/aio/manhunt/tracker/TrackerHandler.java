package aio.manhunt.tracker;

import aio.manhunt.Manhunt;
import aio.manhunt.event.EventHandler;
import aio.manhunt.event.EventType;
import lombok.Getter;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
            var distances = new ArrayList<String>();
            var colours = new ArrayList<String>();

            for (PlayerTracker trackedPlayer : trackers)
            {
                Optional<BlockPos> targetPos = trackedPlayer.getBlockTowardsPlayer(player);

                if (targetPos.isEmpty()) {
                    colours.add(Formatting.DARK_RED.toString());
                    distances.add("N/A");
                    continue;
                }

                float distance = (float) Math.sqrt(targetPos.get().getSquaredDistance(playerPos));
                colours.add(getFacingColor(player, targetPos.get()));
                distances.add(toSuperscript((int) Math.abs(Math.ceil(distance))));
            }

            StringBuilder renderedText = new StringBuilder();
            for (int i = 0; i < colours.size(); i++)
            {
                renderedText.append(colours.get(i)).append(distances.get(i)).append("   ");
            }

            player.sendMessage(Text.of(renderedText.toString()), true);
        }
    }

    public float getYawToTarget(ServerPlayerEntity player, BlockPos target) {
        Vec3d playerPos = player.getEntityPos();
        double dx = target.getX() + 0.5 - playerPos.x;
        double dz = target.getZ() + 0.5 - playerPos.z;
        return (float) (MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(-dx, dz))));
    }

    public String getFacingColor(ServerPlayerEntity player, BlockPos target) {
        float playerYaw = MathHelper.wrapDegrees(player.getYaw());
        float targetYaw = getYawToTarget(player, target);
        float diff = Math.abs(MathHelper.wrapDegrees(playerYaw - targetYaw));

        float facingFactor = 1.0f - (diff / 180.0f);

        String colour;
        String arrow = getArrowForAngle(diff, playerYaw, targetYaw);

        if (facingFactor > 0.95f) {
            colour = Formatting.DARK_GREEN.toString(); /* §a */
        } else if (facingFactor > 0.35f) {
            colour = Formatting.GOLD.toString(); /* §e */
        } else {
            colour = Formatting.DARK_RED.toString(); /* §c */
        }

        return colour + arrow;
    }

    private static String getArrowForAngle(double diff, float yaw, double targetAngle) {
        double rel = MathHelper.wrapDegrees(targetAngle - yaw);

        // 4-way directional breakdown
        if (rel >= -45 && rel < 45) return "▲";
        if (rel >= 45 && rel < 135) return "▶";
        if (rel >= -135 && rel < -45) return "◀";
        return "▼";
    }


    public static String toSuperscript(int number)
    {
        char[] SUPERSCRIPTS = {
                '⁰', '¹', '²', '³', '⁴', '⁵', '⁶', '⁷', '⁸', '⁹'
        };

        String str = String.valueOf(number);
        StringBuilder sb = new StringBuilder();

        for (char c : str.toCharArray()) {
            if (c == '-') {
                sb.append('⁻');
            } else if (Character.isDigit(c)) {
                sb.append(SUPERSCRIPTS[c - '0']);
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
