package fracture.mod.events;

import fracture.mod.client.event.ClientTimerSyncHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.BiomeColorHelper;

public class FoliageColorTweaker {

	public static void register() {

		IBlockColor deadFloraHandler = (state, worldIn, pos, tintIndex) -> {
			if (worldIn == null || pos == null)
				return -1;

			int originalColor = -1;
			Block block = state.getBlock();

			if (block == Blocks.GRASS || block == Blocks.TALLGRASS || block == Blocks.DOUBLE_PLANT) {
				originalColor = BiomeColorHelper.getGrassColorAtPos(worldIn, pos);
			} else {
				originalColor = BiomeColorHelper.getFoliageColorAtPos(worldIn, pos);
			}

			long time = ClientTimerSyncHandler.getDisplayTime();

			if (time < 24000)
				return originalColor;

			// Progressively shift from 0.0 to 1.0 over the course of Day 2
			float percent = MathHelper.clamp((time - 24000) / 24000.0f, 0.0f, 1.0f);

			int targetR = 61;
			int targetG = 51;
			int targetB = 38;

			int origR = (originalColor >> 16) & 255;
			int origG = (originalColor >> 8) & 255;
			int origB = originalColor & 255;

			int finalR = (int) (origR + (targetR - origR) * percent);
			int finalG = (int) (origG + (targetG - origG) * percent);
			int finalB = (int) (origB + (targetB - origB) * percent);

			return (finalR << 16) | (finalG << 8) | finalB;
		};

		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(deadFloraHandler, Blocks.GRASS,
				Blocks.TALLGRASS, Blocks.DOUBLE_PLANT, Blocks.LEAVES, Blocks.LEAVES2, Blocks.VINE);
	}
}