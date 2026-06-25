package fracture.mod.world.epchanges.callisto;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BiomeCallistoGravelBed extends Biome {

    public BiomeCallistoGravelBed() {
        super(new BiomeProperties("Callisto Gravel Bed")
            .setBaseHeight(-0.5F)
            .setHeightVariation(0.05F)
            .setRainDisabled()
            .setTemperature(1.5F));

        Block gravel = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto_gravel"));
        this.topBlock = (gravel != null) ? gravel.getDefaultState() : Blocks.GRAVEL.getDefaultState();
        this.fillerBlock = this.topBlock;
    }
}