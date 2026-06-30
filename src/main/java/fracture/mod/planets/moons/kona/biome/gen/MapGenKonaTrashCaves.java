package fracture.mod.planets.moons.kona.biome.gen;

import fracture.mod.init.BlockInit;
import fracture.mod.planets.moons.kona.biome.KonaBiomes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenCaves;

public class MapGenKonaTrashCaves extends MapGenCaves {

    private final IBlockState trashPile = BlockInit.TRASH_PILE.getDefaultState();
    private final IBlockState surfaceStone = BlockInit.SURFACE_KONA.getDefaultState();
    private final IBlockState konaStone = BlockInit.STONE_KONA.getDefaultState();

    @Override
    protected boolean canReplaceBlock(IBlockState state, IBlockState stateUp) {
        return state.getBlock() == trashPile.getBlock() || 
               state.getBlock() == surfaceStone.getBlock() || 
               state.getBlock() == konaStone.getBlock();
    }

    @Override
    protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, IBlockState state, IBlockState up) {
        Biome currentBiome = this.world.getBiome(new BlockPos(chunkX * 16 + x, 0, chunkZ * 16 + z));
        
        if (currentBiome != KonaBiomes.BiomeKonaTrashHeap) {
            return; 
        }

        data.setBlockState(x, y, z, Blocks.AIR.getDefaultState());
    }
}