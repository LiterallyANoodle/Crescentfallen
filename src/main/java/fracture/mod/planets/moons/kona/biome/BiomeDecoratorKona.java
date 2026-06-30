package fracture.mod.planets.moons.kona.biome;

import java.util.Random;

import fracture.mod.planets.moons.kona.biome.gen.WorldGenKonaRustSpikes;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class BiomeDecoratorKona extends BiomeDecoratorSpace {

    private World currentWorld;
    private final WorldGenKonaRustSpikes rustSpikeGenerator;

    public BiomeDecoratorKona() {
        this.rustSpikeGenerator = new WorldGenKonaRustSpikes();
    }

    @Override
    protected void setCurrentWorld(World world) {
        this.currentWorld = world;
    }

    @Override
    protected World getCurrentWorld() {
        return this.currentWorld;
    }

    @Override
    protected void decorate() {
        World world = this.getCurrentWorld();
        Random rand = this.rand;
        
        int chunkX = this.posX;
        int chunkZ = this.posZ;

        BlockPos chunkCenter = new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8);
        Biome biome = world.getBiomeProvider().getBiome(chunkCenter);

        if (biome == KonaBiomes.BiomeKonaTrashHeap) {
            
            for (int i = 0; i < 3; i++) {
                if (rand.nextInt(100) < 50) { 
                    
                    int x = chunkX * 16 + rand.nextInt(16) + 8;
                    int z = chunkZ * 16 + rand.nextInt(16) + 8;
                    
                    int y = 255;
                    while (y > 0 && world.isAirBlock(new BlockPos(x, y, z))) {
                        y--;
                    }

                    if (y > 10) { 
                        this.rustSpikeGenerator.generate(world, rand, new BlockPos(x, y, z));
                    }
                }
            }
        }
    }
}