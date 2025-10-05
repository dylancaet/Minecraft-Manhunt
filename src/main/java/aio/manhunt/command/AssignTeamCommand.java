package aio.manhunt.command;

import aio.manhunt.command.builder.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Command(
    name = "runners",
    permissionLevel = 1
)
public class AssignTeamCommand
{
    public void add(CommandContext<ServerCommandSource> context, ServerPlayerEntity player)
    {
        context.getSource().sendFeedback(() -> Text.literal("Added player " + player.getName().getLiteralString()), false);
        player.sendMessage(Text.of("You've been added as a runner"));
    }

    public void remove(CommandContext<ServerCommandSource> context, Integer value) {}

    public void testString(CommandContext<ServerCommandSource> context, String value) {}

    public void boolSet(CommandContext<ServerCommandSource> context, Boolean value) {}

    public void start(CommandContext<ServerCommandSource> context)
    {
        context.getSource().getPlayer().sendMessage(Text.of("You started the game"), true);
    }
}
