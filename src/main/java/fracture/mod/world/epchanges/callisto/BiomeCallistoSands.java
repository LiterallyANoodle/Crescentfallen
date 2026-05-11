package fracture.mod.world.epchanges.callisto;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BiomeCallistoSands extends Biome {
    public BiomeCallistoSands() {
        super(new BiomeProperties("Callisto Sands")
            .setBaseHeight(0.0F)
            .setHeightVariation(0.05F)
            .setRainDisabled());

        Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        this.topBlock = (stone != null) ? stone.getStateFromMeta(6) : Blocks.SAND.getDefaultState();
        this.fillerBlock = this.topBlock;
    }
}