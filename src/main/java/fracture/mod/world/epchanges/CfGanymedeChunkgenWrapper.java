//package fracture.mod.world.epchanges;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.entity.EnumCreatureType;
//import net.minecraft.init.Blocks;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.World;
//import net.minecraft.world.biome.Biome;
//import net.minecraft.world.chunk.Chunk;
//import net.minecraft.world.gen.IChunkGenerator;
//import net.minecraftforge.fml.common.registry.ForgeRegistries;
//
//import javax.annotation.Nullable;
//import java.util.List;
//import java.util.Random;
//
//public class CfGanymedeChunkgenWrapper implements IChunkGenerator {
//
//    private final IChunkGenerator original;
//    private final Random rand;
//
//    // Config
//    private final IBlockState AIR = Blocks.AIR.getDefaultState();
//    private IBlockState ashBlockState; 
//
//    // Optimization: Reusable arrays for caching calculations
//    // This prevents creating new arrays 600 times a second
//    // Its not helping the worldlag, may end up doing something diffrent
//    private final double[] noiseX = new double[16];
//    private final double[] noiseZ = new double[16];
//    private final double[] noiseY = new double[256];
//    
//    private final double[] darkNoiseX = new double[16];
//    private final double[] darkNoiseZ = new double[16];
//    private final double[] darkNoiseY = new double[256];
//
//    public CfGanymedeChunkgenWrapper(IChunkGenerator original, long seed) {
//        this.original = original;
//        this.rand = new Random(seed);
//    }
//    
//    public CfGanymedeChunkgenWrapper(IChunkGenerator original) {
//        this(original, System.currentTimeMillis());
//    }
//
//    @Override
//    public Chunk generateChunk(int x, int z) {
//        // Let EP generate the base terrain
//        Chunk chunk = original.generateChunk(x, z);
//        
//        if (ashBlockState == null) {
//            Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "ash_rock"));
//            if (b != null) {
//                ashBlockState = b.getDefaultState();
//            } else {
//                ashBlockState = Blocks.OBSIDIAN.getDefaultState(); 
//            }
//        }
//
//        // Addition map
//        // Instead of calculating sin() 65,000 times inside the loop,
//        // calculate it once for each axis and store it.
//        
//        double realXBase = x * 16;
//        double realZBase = z * 16;
//        
//        // Holes
//        double holeScale = 0.05; // Lower = Larger, wider caves
//        
//        // Ash blocks
//        double darkScale = 0.03; 
//
//        // Fill X arrays
//        for (int i = 0; i < 16; i++) {
//            double rx = realXBase + i;
//            // Complex sine wave for Holes
//            noiseX[i] = Math.sin(rx * holeScale) + (0.5 * Math.sin(rx * holeScale * 2.1));
//            // Simple sine wave for Dark Blocks
//            darkNoiseX[i] = Math.sin(rx * darkScale);
//        }
//
//        // Fill Z arrays
//        for (int k = 0; k < 16; k++) {
//            double rz = realZBase + k;
//            noiseZ[k] = Math.sin(rz * holeScale) + (0.5 * Math.sin(rz * holeScale * 2.1));
//            darkNoiseZ[k] = Math.sin(rz * darkScale);
//        }
//
//        // Fill Y arrays
//        for (int y = 0; y < 140; y++) {
//            noiseY[y] = Math.sin(y * holeScale) + (0.5 * Math.sin(y * holeScale * 2.1));
//            darkNoiseY[y] = Math.sin(y * darkScale);
//        }
//
//        for (int i = 0; i < 16; i++) {
//            for (int k = 0; k < 16; k++) {
//                
//                // Replace bedrock
//                for(int y = 0; y < 5; y++) {
//                     if (chunk.getBlockState(i, y, k).getBlock() == Blocks.BEDROCK) {
//                         chunk.setBlockState(new BlockPos(i, y, k), AIR);
//                     }
//                }
//
//                for (int y = 5; y < 140; y++) {
//                    
//                    double holeVal = noiseX[i] + noiseY[y] + noiseZ[k];
//                    
//                    if ((holeVal / 2.0) > 0.4) {
//                         if (chunk.getBlockState(i, y, k) != AIR) {
//                             chunk.setBlockState(new BlockPos(i, y, k), AIR);
//                         }
//                         continue;
//                    }
//
//                    double darkVal = darkNoiseX[i] + darkNoiseY[y] + darkNoiseZ[k];
//                    
//                    if ((darkVal / 1.5) > 0.5) {
//                        IBlockState current = chunk.getBlockState(i, y, k);
//                        if (current != AIR && current.getBlock() != Blocks.BEDROCK) {
//                             chunk.setBlockState(new BlockPos(i, y, k), ashBlockState);
//                        }
//                    }
//                }
//            }
//        }
//        return chunk;
//    }
//
//    @Override public void populate(int x, int z) { original.populate(x, z); }
//    @Override public boolean generateStructures(Chunk c, int x, int z) { return original.generateStructures(c, x, z); }
//    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType t, BlockPos p) { return original.getPossibleCreatures(t, p); }
//    @Nullable @Override public BlockPos getNearestStructurePos(World w, String s, BlockPos p, boolean f) { return original.getNearestStructurePos(w, s, p, f); }
//    @Override public boolean isInsideStructure(World w, String s, BlockPos p) { return original.isInsideStructure(w, s, p); }
//    @Override public void recreateStructures(Chunk c, int x, int z) { original.recreateStructures(c, x, z); }
//}