package aio.manhunt.tracker;

import aio.manhunt.Manhunt;
import aio.manhunt.event.EventHandler;
import aio.manhunt.event.EventSystem;
import aio.manhunt.event.EventType;
import aio.manhunt.event.IDispose;
import lombok.Getter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

import java.time.LocalDateTime;

public class PlayerTracker implements IDispose
{
    private ServerPlayerEntity player;
    private String displayName;

    @Getter private Vec3d lastOverworldLocation;
    @Getter private Vec3d lastNetherLocation;
    @Getter private Vec3d lastEndLocation;

    private final EventHandler<MinecraftServer> onTickHandle = this::onTick; /* needed for a stable method handle */

    public PlayerTracker(ServerPlayerEntity player)
    {
        this.player = player;
        this.displayName = player.getDisplayName().getLiteralString();

        Manhunt.EVENTS.subscribe(EventType.SERVER_TICK, onTickHandle);
    }

    private void onTick(MinecraftServer minecraftServer)
    {
        
    }

    public void dispose()
    {
        Manhunt.EVENTS.unsubscribe(EventType.SERVER_TICK, onTickHandle);
    }
}
