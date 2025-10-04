package aio.manhunt;

import aio.manhunt.command.builder.CommandRegistry;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    Features:
        - Compass tracker -> player, portals, dimensions
        - Hud display -> blocks away, arrow
        - Glow -> same team when in range
        - Piglin loot table -> enderpearl droprate
        - Config -> json configuration
        - Commands -> enable/disable features
        - Hunger -> temporary hunger on spawn
        - Hunter delay -> timer for head start
        - Armour keepinven -> save hunters armour handicap
 */

public class Manhunt implements ModInitializer
{
	public static final String MOD_ID = "manhunt";
    public static final String COMMAND_PACKAGE = "aio.manhunt.command";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
    {
        ServerTickEvents.START_SERVER_TICK.register(this::OnServerTick);

        CommandRegistrationCallback.EVENT.register(CommandRegistry.getInstance()::build);

    }

    private void OnServerTick(MinecraftServer minecraftServer)
    {

    }
}

