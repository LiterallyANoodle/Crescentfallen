package fracture.mod.world.epchanges;

import fracture.mod.client.sky.GanymedeSkyProvider;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public class CfGanymedeWorldProvider extends WorldProvider {

    @Override
    public void init() {
        this.hasSkyLight = true;

        // Biome finder
        Biome targetBiome = ForgeRegistries.BIOMES.getValue(new ResourceLocation("extraplanets", "ganymede"));

        if (targetBiome == null) {
            for (Biome b : ForgeRegistries.BIOMES) {
                if (b.getRegistryName().getPath().contains("ganymede")) {
                    targetBiome = b;
                    System.out.println("[Fracture] Auto-resolved Ganymede biome to: " + b.getRegistryName());
                    break;
                }
            }
        }

        // Fallback
        if (targetBiome == null) {
            System.out.println("[Fracture] ERROR: Biome not found. Resulting to fallback.");
            targetBiome = Biomes.DESERT;
        }

        this.biomeProvider = new BiomeProviderSingle(targetBiome);

        if (this.world.isRemote) {
            this.setSkyRenderer(new GanymedeSkyProvider());
        }
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new CfGanymedeChunkgen(this.world, this.world.getSeed());
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionType.getById(-1506); 
    }
}