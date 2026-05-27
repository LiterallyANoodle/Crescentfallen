package fracture.mod.util.handlers;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.init.Biomes;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import fracture.mod.init.BlockInit; // adjust import if needed
import fracture.mod.world.gen.AlphaRoseGenerator;

/**
 * Event handler that triggers AlphaRoseGenerator during chunk decoration in Dark Forests.
 */

	//Note: this is non functional. Fix later

public class FlowerSpawnHandler {

    private final AlphaRoseGenerator generator;
    // How frequently to try per Flower decorate pass (0.0 -> never, 1.0 -> always)
    private final double spawnChancePerChunk;

    public FlowerSpawnHandler() {
        this.generator = new AlphaRoseGenerator(2, 9); // define flowers per patch 
        this.spawnChancePerChunk = 1.00D; // 25% of chunks (adjust to taste)
    }

    /**
     * Register this handler via: MinecraftForge.EVENT_BUS.register(new FlowerSpawnHandler());
     */
    @SubscribeEvent
    public void onDecorateBiome(DecorateBiomeEvent.Decorate event) {
        // Only run during the FLOWERS decorate stage
        if (event.getType() != DecorateBiomeEvent.Decorate.EventType.FLOWERS) return;

        World world = event.getWorld();
        if (world.isRemote) return;

        // Only spawn in dimension 0
        if (world.provider.getDimension() != 0) return;

        BlockPos chunkPos = event.getPos(); // this is typically chunk corner (x,z = chunk*16)
        Random rand = event.getRand();

        // check biome at the chunk position (top y doesn't matter for identifying biome)
        Biome biome = world.getBiome(chunkPos);
        if (biome != Biomes.ROOFED_FOREST && biome != Biomes.MUTATED_ROOFED_FOREST) return;

        // Decide whether to spawn in this chunk
        if (rand.nextDouble() > this.spawnChancePerChunk) return;

        // Choose 1..N patches per success (adjust as you like)
        int patches = 1 + rand.nextInt(2); // 1-2 patches
        for (int p = 0; p < patches; p++) {
            // choose a random x/z within the chunk area (with +8 offset similar to vanilla decoration)
            int rx = chunkPos.getX() + rand.nextInt(16) + 8;
            int rz = chunkPos.getZ() + rand.nextInt(16) + 8;

            // find a good y - use top solid block
            int ry = world.getHeight(rx, rz);
            BlockPos spawnCenter = new BlockPos(rx, ry, rz);

            // run the generator (it will attempt to place 2-7 flowers around spawnCenter)
            this.generator.generate(world, rand, spawnCenter);
        }
    }
}