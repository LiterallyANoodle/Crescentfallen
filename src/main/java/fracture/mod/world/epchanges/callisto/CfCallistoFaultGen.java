//package fracture.mod.world.epchanges.callisto;
//
//import java.util.Random;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.init.Blocks;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//import net.minecraft.world.chunk.IChunkProvider;
//import net.minecraft.world.gen.IChunkGenerator;
//import net.minecraftforge.fml.common.IWorldGenerator;
//import net.minecraftforge.fml.common.registry.ForgeRegistries;
//
//public class CfCallistoFaultGen implements IWorldGenerator {
//
//    private static final int CALLISTO_DIM_ID = -1505;
//    private IBlockState stoneState;
//
//    @Override
//    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
//        if (world.provider.getDimension() != CALLISTO_DIM_ID) return;
//        if (stoneState == null) initBlocks();
//
//        if (random.nextInt(15) == 0) {
//            int x = chunkX * 16;
//            int z = chunkZ * 16;
//            generateFault(world, x, z, random);
//        }
//    }
//
//    private void generateFault(World world, int startX, int startZ, Random rand) {
//        // ... (Same logic as previous response, just higher chance above)
//        double angle = rand.nextDouble() * Math.PI * 2.0D;
//        float width = 2.0F + rand.nextFloat() * 2.0F; 
//        int length = 50 + rand.nextInt(50); 
//
//        double x = startX;
//        double z = startZ;
//
//        for (int i = 0; i < length; i++) {
//            x += Math.cos(angle) * 1.5;
//            z += Math.sin(angle) * 1.5;
//            angle += (rand.nextDouble() - 0.5) * 0.2; 
//
//            BlockPos pos = new BlockPos(x, 100, z);
//            for (int dx = -(int)width; dx <= width; dx++) {
//                for (int dz = -(int)width; dz <= width; dz++) {
//                    BlockPos target = world.getTopSolidOrLiquidBlock(pos.add(dx, 0, dz)).down();
//                    if (target.getY() > 20) {
//                        int depth = 10 + rand.nextInt(10);
//                        for (int d = 0; d < depth; d++) {
//                            world.setBlockState(target.down(d), Blocks.AIR.getDefaultState(), 2);
//                        }
//                        // Cliff Logic
//                        if (dx > 0) {
//                            world.setBlockState(target.up(), stoneState, 2);
//                            world.setBlockState(target.up(2), stoneState, 2);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void initBlocks() {
//        net.minecraft.block.Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
//        stoneState = (stone != null) ? stone.getDefaultState() : Blocks.STONE.getDefaultState();
//    }
//}