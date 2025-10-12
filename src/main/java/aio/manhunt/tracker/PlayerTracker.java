package aio.manhunt.tracker;

import aio.manhunt.Manhunt;
import aio.manhunt.event.EventHandler;
import aio.manhunt.event.EventType;
import aio.manhunt.event.IDispose;
import lombok.Getter;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class PlayerTracker implements IDispose
{
    private ServerPlayerEntity trackedPlayer;
    private String displayName;

    @Getter private BlockPos lastOverworldBlock;
    @Getter private BlockPos lastNetherBlock;
    @Getter private BlockPos lastEndBlock;
    @Getter private RegistryKey<World> trackedDimension;

    private final EventHandler<MinecraftServer> onTickHandle = this::onTick; /* needed for a stable method handle */

    public PlayerTracker(ServerPlayerEntity player)
    {
        this.trackedPlayer = player;
        this.displayName = player.getName().getLiteralString();

        Manhunt.EVENTS.subscribe(EventType.SERVER_TICK, onTickHandle);
    }

    private void onTick(MinecraftServer minecraftServer)
    {
        if (!isValid())
            return;

        updatePos();
    }

    /*
        Returns the last block tracked player was at to reach the player.
        i.e. if the target is in the nether & the player is in the overworld, the compass will point the last overworld location
        (which should be the nether portal)

        May return 0, 0, 0 coords in instances where player goes to dimensions trackedPlayer hasn't visited.
    */
    public Optional<BlockPos> getBlockTowardsPlayer(ServerPlayerEntity player)
    {
        Optional<BlockPos> targetPos = Optional.empty();

        if (!isValid())
            return targetPos;

        var playerDimension = player.getEntityWorld().getRegistryKey();

        if (playerDimension == World.OVERWORLD && trackedDimension != World.OVERWORLD)
            targetPos = Optional.ofNullable(lastOverworldBlock);
        else if (playerDimension == World.NETHER && trackedDimension != World.NETHER)
            targetPos = Optional.ofNullable(lastNetherBlock);
        else if (playerDimension == World.END && trackedDimension != World.END)
            targetPos = Optional.ofNullable(lastEndBlock);
        else
            targetPos = Optional.ofNullable(trackedPlayer.getBlockPos());

        return targetPos;
    }


    private Boolean isValid()
    {
        Boolean valid = true;

        if (trackedPlayer == null) valid = false;
        else if (trackedPlayer.isRemoved()) valid = false;
        else if (trackedPlayer.networkHandler == null || !trackedPlayer.networkHandler.isConnectionOpen()) valid = false;

        if (!valid)
            trackedPlayer = Manhunt.SERVER.getPlayerManager().getPlayer(displayName);

        return trackedPlayer != null;
    }

    private void updatePos()
    {
        RegistryKey<World> dimensionKey = trackedPlayer.getEntityWorld().getRegistryKey();

        BlockPos origin = trackedPlayer.getBlockPos();

        if (dimensionKey == World.OVERWORLD) {
            lastOverworldBlock = origin;
            trackedDimension = World.OVERWORLD;
        } else if (dimensionKey == World.NETHER) {
            lastNetherBlock = origin;
            trackedDimension = World.NETHER;
        } else if (dimensionKey == World.END) {
            lastEndBlock = origin;
            trackedDimension = World.END;
        }
    }

    public void dispose()
    {
        Manhunt.EVENTS.unsubscribe(EventType.SERVER_TICK, onTickHandle);
    }
}
