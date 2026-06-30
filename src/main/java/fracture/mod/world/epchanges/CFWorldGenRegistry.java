package fracture.mod.world.epchanges;

import fracture.mod.world.epchanges.callisto.CfCallistoBiomeProvider;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class CFWorldGenRegistry {

    // Note: Can be made dynamic via config later
    private static final int IO_DIM_ID = -1500;
    private static final int EUROPA_DIM_ID = -1501;
    private static final int CALLISTO_DIM_ID = -1505;
    private static final int GANYMEDE_DIM_ID = -1506;
    private static final int OBERON_DIM_ID = -1509;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) return; // Only run on server logic

        int dimId = world.provider.getDimension();

        // --- GANYMEDE ---
        if (dimId == GANYMEDE_DIM_ID) {
            if (world instanceof WorldServer) {
                injectChunkGenerator((WorldServer) world, new CfGanymedeChunkgen(world, world.getSeed()), "Ganymede");
            }
        }

        // --- CALLISTO ---
        else if (dimId == CALLISTO_DIM_ID) {
            injectBiomeProvider(world, new CfCallistoBiomeProvider(world), "Callisto");
        }

        /* // --- FUTURE MOON INJECTIONS ---
        // Once you convert Io, Europa, and Oberon from ChunkEvent.Load listeners 
        // into proper IChunkGenerator classes (like CfGanymedeChunkgen), 
        // you will register them here.
        
        else if (dimId == IO_DIM_ID) {
             if (world instanceof WorldServer) {
                 injectChunkGenerator((WorldServer) world, new CfIoChunkGen(world, world.getSeed()), "Io");
             }
        }
        else if (dimId == EUROPA_DIM_ID) {
             if (world instanceof WorldServer) {
                 injectChunkGenerator((WorldServer) world, new CfEuropaChunkGen(world, world.getSeed()), "Europa");
             }
        }
        */
    }

    /**
     * Replaces the world's default chunk generator with a custom one.
     */
    private void injectChunkGenerator(WorldServer world, IChunkGenerator newGenerator, String planetName) {
        try {
            ChunkProviderServer provider = world.getChunkProvider();
            Field genField = ReflectionHelper.findField(ChunkProviderServer.class, "chunkGenerator", "field_186029_c");
            genField.setAccessible(true);

            IChunkGenerator currentGen = (IChunkGenerator) genField.get(provider);

            if (!currentGen.getClass().equals(newGenerator.getClass())) {
                System.out.println("[Crescentfallen] Injecting Custom ChunkGen into " + planetName);
                genField.set(provider, newGenerator);
            }

        } catch (Exception e) {
            System.err.println("[Crescentfallen] Failed to inject ChunkGenerator for " + planetName);
            e.printStackTrace();
        }
    }

    /**
     * Replaces the world's default biome provider with a custom one.
     */
    private void injectBiomeProvider(World world, net.minecraft.world.biome.BiomeProvider newProvider, String planetName) {
        try {
            Field providerField = ReflectionHelper.findField(WorldProvider.class, "biomeProvider", "field_76578_c");
            providerField.setAccessible(true);
            
            providerField.set(world.provider, newProvider);
            System.out.println("[Crescentfallen] Successfully isolated " + planetName + " BiomeProvider.");

        } catch (Exception e) {
            System.err.println("[Crescentfallen] Failed to inject BiomeProvider for " + planetName);
            e.printStackTrace();
        }
    }
}