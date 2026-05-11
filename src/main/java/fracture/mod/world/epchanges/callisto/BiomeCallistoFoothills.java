package fracture.mod.world.epchanges.callisto;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BiomeCallistoFoothills extends Biome {
    public BiomeCallistoFoothills() {
        super(new BiomeProperties("Callisto Foothills")
            .setBaseHeight(0.6F)
            .setHeightVariation(0.2F)
            .setRainDisabled());

        Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        this.topBlock = (stone != null) ? stone.getDefaultState() : Blocks.STONE.getDefaultState();
        this.fillerBlock = this.topBlock;
    }
}