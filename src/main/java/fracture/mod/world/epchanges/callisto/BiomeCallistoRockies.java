package fracture.mod.world.epchanges.callisto;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BiomeCallistoRockies extends Biome {

    public BiomeCallistoRockies() {
        super(new BiomeProperties("Callisto Rockies")
            .setBaseHeight(1.5F)
            .setHeightVariation(0.5F)
            .setRainDisabled());

        Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        this.topBlock = (stone != null) ? stone.getDefaultState() : Blocks.STONE.getDefaultState();
        this.fillerBlock = this.topBlock;
    }
}