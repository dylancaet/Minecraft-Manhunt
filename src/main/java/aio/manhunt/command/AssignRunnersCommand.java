package aio.manhunt.command;

import aio.manhunt.command.builder.Command;
import aio.manhunt.tracker.TrackerHandler;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

@Command(
    name = "runners",
    permissionLevel = 1
)
public class AssignRunnersCommand
{
    public void add(CommandContext<ServerCommandSource> context, ServerPlayerEntity player)
    {
        TrackerHandler.getInstance().addTrackedPlayer(player);
    }

    public void remove(CommandContext<ServerCommandSource> context, ServerPlayerEntity player)
    {
        TrackerHandler.getInstance().removeTrackedPlayer(player);
    }
}
