package fracture.mod.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("cf_channel");

    private static int packetId = 0;

    public static void init() {
        
        INSTANCE.registerMessage(PacketSpawnExplosionParticles.Handler.class, PacketSpawnExplosionParticles.class, packetId++, Side.CLIENT);
        
        INSTANCE.registerMessage(PacketBombardment.Handler.class, PacketBombardment.class, packetId++, Side.CLIENT);
    }
}