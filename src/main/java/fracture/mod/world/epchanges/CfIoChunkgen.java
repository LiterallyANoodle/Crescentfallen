package fracture.mod.world.epchanges;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CfIoChunkgen {

	// Note: go back later and have this draw from players config
    private static final int IO_DIM_ID = -1500;
    
    private IBlockState ashRockState = null;
    private Block targetIoBlock = null; 
    private IBlockState magmaState = null;
    
    // Biome IDs (Auto-detected)
    private int bioId_Io = -999;
    private int bioId_BurningPlains = -999;
    
    private boolean initialized = false;
    private final Random rand = new Random();

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        if (world.provider.getDimension() != IO_DIM_ID) return;

        if (!initialized) {
            initBlocks();
            initialized = true;
        }
        
        // If blocks or biomes are missing, partial logic can still be run, 
        // but checking target block is essential.
        if (ashRockState == null || targetIoBlock == null) return;

        Chunk chunk = event.getChunk();
        boolean modified = false;

        boolean[][] isCrackMap = new boolean[16][16];
        
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = chunk.x * 16 + x;
                int realZ = chunk.z * 16 + z;

                if (isCrackLocation(realX, realZ)) {
                    
                    Biome b = world.getBiome(new BlockPos(realX, 64, realZ));
                    int bId = Biome.getIdForBiome(b);
                    
                    if (bId == bioId_Io || bId == bioId_BurningPlains) {
                        isCrackMap[x][z] = true;
                    }
                }
            }
        }

        // Process storage
        for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
            if (storage != null) {
                int yBase = storage.getYLocation();

                // Skip scanning the deep underground (Performance)
                if (yBase < 16) continue; 

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        
                        boolean isCrack = isCrackMap[x][z];

                        for (int y = 0; y < 16; y++) {
                            int realY = yBase + y;

                            // Cracks/ravine gen
                            // cut from Y=25 (Deep) up to Y=75 (Surface)
                            if (isCrack && realY > 25 && realY < 75) {
                                IBlockState current = storage.get(x, y, z);
                                // Dont break bedrock or air
                                if (current.getBlock() != Blocks.BEDROCK && current.getBlock() != Blocks.AIR) {
                                    storage.set(x, y, z, Blocks.AIR.getDefaultState());
                                    modified = true;
                                    continue;
                                }
                            }

                            // Ash rock mountain replacement
                            // Replaces everything above Y=65
                            if (realY > 65) {
                                IBlockState current = storage.get(x, y, z);
                                
                                if (current.getBlock() == targetIoBlock) {
                                    
                                    if (rand.nextInt(200) == 0 && magmaState != null) {
                                        storage.set(x, y, z, magmaState);
                                    } else {
                                        storage.set(x, y, z, ashRockState);
                                    }
                                    modified = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (modified) {
            chunk.markDirty();
        }
    }

    private boolean isCrackLocation(int x, int z) {
        // Lower number = Larger, wider feature.
        double scaleLarge = 0.04; 
        
        // 0.1 creates smaller ripples for jagged edges.
        double scaleDetail = 0.1; 

        // declare val by using the large scale sine/cosine waves
        double val = Math.sin(x * scaleLarge) + Math.cos(z * scaleLarge);
        
        // Add detail
        val += 0.5 * Math.sin(x * scaleDetail + z * scaleDetail);
        
        return Math.abs(val) < 0.12;
    }

    private void initBlocks() {
        System.out.println("[Fracture] CFIoChunkgen: Initializing IO Features.");

        // Blocks
        Block ash = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "ash_rock"));
        if (ash != null) ashRockState = ash.getDefaultState();

        targetIoBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "io"));
        if (targetIoBlock != null) System.out.println("[Fracture] Target Block: extraplanets:io");

        Block magma = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "magma")); 
        if (magma == null) magma = Blocks.LAVA; 
        magmaState = magma.getDefaultState();

        // Biomes
        for (Biome b : ForgeRegistries.BIOMES) {
            String name = b.getBiomeName();
            if (name != null) {
                if (name.equals("Io")) {
                    bioId_Io = Biome.getIdForBiome(b);
                    System.out.println("[Fracture] Found Biome: 'Io' (ID: " + bioId_Io + ")");
                } 
                else if (name.equals("Io Burning Plains")) {
                    bioId_BurningPlains = Biome.getIdForBiome(b);
                    System.out.println("[Fracture] Found Biome: 'Io Burning Plains' (ID: " + bioId_BurningPlains + ")");
                }
            }
        }
    }
}