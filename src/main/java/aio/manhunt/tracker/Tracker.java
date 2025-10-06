package aio.manhunt.tracker;

import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Vector;

public class Tracker
{
    @Getter private Vec3d lastOverworldLocation;
    @Getter private Vec3d lastNetherLocation;
    @Getter private Vec3d lastEndLocation;


}
