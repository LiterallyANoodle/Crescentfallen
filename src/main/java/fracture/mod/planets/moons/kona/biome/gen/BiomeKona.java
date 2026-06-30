package fracture.mod.planets.moons.kona.biome.gen;

import fracture.mod.init.BlockInit;
import fracture.mod.planets.moons.kona.biome.KonaBiomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class BiomeKona extends KonaBiomes {

    public BiomeKona(BiomeProperties properties) {
        super(properties);
        this.topBlock = BlockInit.SURFACE_KONA.getDefaultState(); 
        this.fillerBlock = BlockInit.STONE_SUBSURFACE_KONA.getDefaultState(); 
        //this.setRegistryName("kona");
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
    }

    @Override
    public void registerTypes(Biome b) {
        BiomeDictionary.addTypes(b, BiomeDictionary.Type.COLD, BiomeDictionary.Type.DEAD, BiomeDictionary.Type.WASTELAND);
    }
}