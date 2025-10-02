package aio.manhunt;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.impl.gametest.FabricGameTestModInitializer;
import net.fabricmc.fabric.impl.gametest.FabricGameTestRunner;
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
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
    {
        ServerTickEvents.START_SERVER_TICK.register(this::OnServerTick);
	}

    private void OnServerTick(MinecraftServer minecraftServer)
    {

    }
}

