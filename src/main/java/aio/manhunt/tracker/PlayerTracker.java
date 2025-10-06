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

public class PlayerTracker implements IDispose
{
    private ServerPlayerEntity player;
    private String displayName;

    @Getter private BlockPos overworldBlock;
    @Getter private BlockPos netherBlock;
    @Getter private BlockPos endBlock;
    @Getter private RegistryKey<World> currentDimension;

    private final EventHandler<MinecraftServer> onTickHandle = this::onTick; /* needed for a stable method handle */

    public PlayerTracker(ServerPlayerEntity player)
    {
        this.player = player;
        this.displayName = player.getName().getLiteralString();

        Manhunt.EVENTS.subscribe(EventType.SERVER_TICK, onTickHandle);
    }

    private void onTick(MinecraftServer minecraftServer)
    {
        if (!isValid())
            return;

        RegistryKey<World> dimensionKey = player.getEntityWorld().getRegistryKey();

        BlockPos origin = player.getBlockPos();

        if (dimensionKey == World.OVERWORLD) {
            overworldBlock = origin;
            currentDimension = World.OVERWORLD;
        } else if (dimensionKey == World.NETHER) {
            netherBlock = origin;
            currentDimension = World.NETHER;
        } else if (dimensionKey == World.END) {
            endBlock = origin;
            currentDimension = World.END;
        }

    }

    private Boolean isValid()
    {
        Boolean valid = true;

        if (player == null) valid = false;
        else if (player.isRemoved()) valid = false;
        else if (player.networkHandler == null || !player.networkHandler.isConnectionOpen()) valid = false;

        if (!valid)
            player = Manhunt.SERVER.getPlayerManager().getPlayer(displayName);

        return player != null;
    }

    public void dispose()
    {
        Manhunt.EVENTS.unsubscribe(EventType.SERVER_TICK, onTickHandle);
    }
}
