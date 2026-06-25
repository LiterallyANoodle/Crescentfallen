package fracture.mod.world.epchanges.callisto;

//import fracture.mod.world.biomes.BiomeCallistoBlackDesert;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CfCallistoStructureGen implements IWorldGenerator {

    private static final int CALLISTO_DIM_ID = -1505;
    private IBlockState driedOilState;
    private IBlockState shaleOilState;
    private boolean initialized = false;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != CALLISTO_DIM_ID) return;
        if (!initialized) initBlocks();

        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        BlockPos surfacePos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        Biome biome = world.getBiome(surfacePos);

        if (biome instanceof BiomeCallistoBlackDesert) {
            int density = 2 + random.nextInt(3); // 2-4 per chunk
            for (int i = 0; i < density; i++) {
                int sx = chunkX * 16 + random.nextInt(16);
                int sz = chunkZ * 16 + random.nextInt(16);
                BlockPos pos = world.getHeight(new BlockPos(sx, 0, sz));
                
//                if (pos.getY() > 50 && world.getBlockState(pos.down()).isFullBlock()) {
//                    generateSpire(world, pos, random);
                }
            }
        }
    

//    private void generateSpire(World world, BlockPos pos, Random rand) {
//        int height = 15 + rand.nextInt(20);
//        int radius = 2 + rand.nextInt(2);
//
//        for (int y = 0; y < height; y++) {
//            int currentRadius = radius - (y / 12);
//            if (currentRadius < 1) currentRadius = 1;
//
//            for (int dx = -currentRadius; dx <= currentRadius; dx++) {
//                for (int dz = -currentRadius; dz <= currentRadius; dz++) {
//                    if (dx*dx + dz*dz <= currentRadius*currentRadius + 0.5) {
//                        
//                        // This prevents perfect stripes and allows double-layers
//                        double noise = Math.sin((pos.getY() + y) * 0.4);
//                        
//                        IBlockState state = (noise > 0.2) ? driedOilState : shaleOilState;
//                        world.setBlockState(pos.add(dx, y, dz), state, 2);
//                    }

    private void initBlocks() {
        net.minecraft.block.Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        if (stone != null) {
            driedOilState = stone.getStateFromMeta(6);
            shaleOilState = stone.getStateFromMeta(7);
        }
        initialized = true;
    }
}