package fracture.mod.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

//Note: right now, this causes lag. there needs to be testing to see whether 
//or not an older version is actually less resource intensive.
//when certain enemies come to fruition, this needs to be put on hold entirely, 
//then the destruction needs to be applied when the player is not being attacked or all the enemies are dead.
//half way through day 3, all leaves need to be removed, including snow, and the constant checking needs to stop entirely.
//Do not forget to add code that changes the foliage color to desaturate periodically.

//This is very much a work in progress.

@Mod.EventBusSubscriber
public class EcologicalCollapseHandler {

    private static int currentPulseRate = 5; 
    public static boolean emergencyThrottle = false;
    private static boolean wasFireTickEnabled = true;
    private static final java.util.List<Chunk> chunkSnapshot = new java.util.ArrayList<>();
    private static int snapshotIndex = 0;

    private static final java.util.Map<ChunkPos, Integer> chunkHits = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<ChunkPos, Long> chunkCooldowns = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int HITS_BEFORE_COOLDOWN = 3; // Chunk takes 3 rapid bites before the cap engages
    private static final long COOLDOWN_TICKS = 4800; // 4 minutes

    private static double getAverageTickTime(net.minecraft.server.MinecraftServer server) {
        double total = 0.0;
        for (long tickTime : server.tickTimeArray) {
            total += tickTime;
        }
        return (total / server.tickTimeArray.length) * 1.0E-6D;
    }

    /**
     * Prevents memory leaks by clearing cooldown data when chunks are unloaded from RAM.
     */
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getWorld().isRemote) return;
        ChunkPos pos = event.getChunk().getPos();
        chunkHits.remove(pos);
        chunkCooldowns.remove(pos);
    }

    /**
     * Spaced Batch Sweeper 
     * Dynamically shifts pulse rates based on server health.
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || event.phase == TickEvent.Phase.END || event.world.provider.getDimension() != 0) return;

        WorldServer worldServer = (WorldServer) event.world;
        long time = event.world.getWorldTime();

        // Lag failsafe monitor (1 srt, 20 t)
        if (time % 20 == 0) {
            double mspt = getAverageTickTime(worldServer.getMinecraftServer());
            
            // throttle
            if (mspt > 45.0 && !emergencyThrottle) {
                currentPulseRate = 50; 
                emergencyThrottle = true;
                
                wasFireTickEnabled = worldServer.getGameRules().getBoolean("doFireTick");
                worldServer.getGameRules().setOrCreateGameRule("doFireTick", "false");
                System.out.println("[Crescentfallen] High MSPT (" + Math.round(mspt) + "ms). Engaging failsafes.");
            } 
            else if (mspt < 35.0 && emergencyThrottle) {
                currentPulseRate = 5;
                emergencyThrottle = false;
                
                worldServer.getGameRules().setOrCreateGameRule("doFireTick", String.valueOf(wasFireTickEnabled));
                System.out.println("[Crescentfallen] MSPT stabilized. Resuming normal destruction rates.");
            }
        }

        if (time % currentPulseRate != 0) return;

        int phase = (time >= 48000) ? 2 : (time >= 24000) ? 1 : 0;
        if (phase == 0) return;

        java.util.List<Chunk> loadedChunks = new java.util.ArrayList<>(worldServer.getChunkProvider().getLoadedChunks());
        if (loadedChunks.isEmpty()) return;

        if (snapshotIndex >= loadedChunks.size()) {
            snapshotIndex = 0; 
        }
        
        Chunk chunk = loadedChunks.get(snapshotIndex++);
        if (chunk != null && chunk.isLoaded()) {
            ChunkPos pos = chunk.getPos();
            
            long cdEnd = chunkCooldowns.getOrDefault(pos, 0L);
            if (time < cdEnd) return; 

            boolean modified = applyPhaseDestruction(chunk, phase, false);
            
            if (modified) {
                int hits = chunkHits.getOrDefault(pos, 0) + 1;
                if (hits >= HITS_BEFORE_COOLDOWN) {
                    chunkCooldowns.put(pos, time + COOLDOWN_TICKS);
                } else {
                    chunkHits.put(pos, hits);
                }

                PlayerChunkMapEntry entry = worldServer.getPlayerChunkMap().getEntry(chunk.x, chunk.z);
                if (entry != null) {
                    entry.sendPacket(new SPacketChunkData(chunk, 65535));
                }
            } else {
                // If the sweeper found absolutely nothing left to destroy in this chunk,
                // automatically cap it and put it on a 4-minute cooldown to save CPU.
                chunkCooldowns.put(pos, time + COOLDOWN_TICKS);
                chunkHits.put(pos, HITS_BEFORE_COOLDOWN);
            }
        }
    }
    

    /**
     * Intercepts chunks loaded from disk 
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;

        long time = event.getWorld().getWorldTime();
        int phase = (time >= 48000) ? 2 : (time >= 24000) ? 1 : 0;

        if (phase > 0) {
            applyPhaseDestruction(event.getChunk(), phase, true); 
        }
    }

    /**
     * Intercepts FRESHLY GENERATED chunks exactly after vanilla populators (trees, grass) finish planting.
     */
    @SubscribeEvent
    public static void onChunkPopulated(PopulateChunkEvent.Post event) {
        if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;

        long time = event.getWorld().getWorldTime();
        int phase = (time >= 48000) ? 2 : (time >= 24000) ? 1 : 0;

        if (phase > 0) {
            Chunk chunk = event.getWorld().getChunk(event.getChunkX(), event.getChunkZ());
            // isInstant = true (Wreck the freshly generated forest instantly)
            applyPhaseDestruction(chunk, phase, true);
        }
    }

    @SubscribeEvent
    public static void onAnimalSpawn(net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn event) {
        if (event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) return;

        long time = event.getWorld().getWorldTime();
        int phase = (time >= 48000) ? 2 : (time >= 24000) ? 1 : 0;
        if (phase == 0) return;

        if (event.getEntityLiving() instanceof net.minecraft.entity.passive.EntityAnimal) {
            net.minecraft.entity.passive.EntityAnimal animal = (net.minecraft.entity.passive.EntityAnimal) event.getEntityLiving();
            java.util.concurrent.ThreadLocalRandom rand = java.util.concurrent.ThreadLocalRandom.current();

            int nearbySameType = event.getWorld().getEntitiesWithinAABB(
                animal.getClass(), 
                animal.getEntityBoundingBox().grow(16.0D, 8.0D, 16.0D)
            ).size();

            if (phase == 1) { 
                // --- DAY 2 ---
                
                int maxNearby = rand.nextInt(3) + 1;
                if (nearbySameType > maxNearby) {
                    event.setResult(net.minecraftforge.fml.common.eventhandler.Event.Result.DENY);
                    return;
                }
                
                if (rand.nextDouble() < 0.15) {
                    animal.setFire(10000);
                }
            } 
            else if (phase == 2) { 
                // --- DAY 3 ---
                
                if (rand.nextDouble() < 0.60) {
                    event.setResult(net.minecraftforge.fml.common.eventhandler.Event.Result.DENY);
                    return;
                }

                double roll = rand.nextDouble();
                int maxNearby = 0; 
                
                if (roll < 0.70) maxNearby = 0;      
                else if (roll < 0.90) maxNearby = 1; 
                else maxNearby = 2;                 
                
                if (nearbySameType > maxNearby) {
                    event.setResult(net.minecraftforge.fml.common.eventhandler.Event.Result.DENY);
                    return;
                }

                if (rand.nextDouble() < 0.30) {
                    animal.setFire(10000);
                }
            }
        }
    }
    
    public static boolean applyPhaseDestruction(Chunk chunk, int phase, boolean isInstant) {
        boolean chunkModified = false;
        ExtendedBlockStorage[] storageArrays = chunk.getBlockStorageArray();
        
        IBlockState airState = Blocks.AIR.getDefaultState();
        IBlockState fireState = Blocks.FIRE.getDefaultState();
        IBlockState dirtState = Blocks.DIRT.getDefaultState();
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (ExtendedBlockStorage storage : storageArrays) {
            if (storage != Chunk.NULL_BLOCK_STORAGE && !storage.isEmpty()) {
                int storageY = storage.getYLocation();

                if (storageY + 15 < 37 || storageY > 200) continue;

                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        
                        int worldY = y + storageY;
                        
                        if (worldY < 37 || worldY > 200) continue;

                        for (int z = 0; z < 16; z++) {
                            
                            IBlockState state = storage.get(x, y, z);
                            Block block = state.getBlock();

                            if (phase == 1) {
                                if (block == Blocks.SNOW_LAYER) {
                                    if (isInstant || rand.nextDouble() < 0.35) { 
                                        storage.set(x, y, z, airState);
                                        chunkModified = true;
                                    }
                                } 
                                else if (block == Blocks.DOUBLE_PLANT || block == Blocks.TALLGRASS) {
                                    double chance = isInstant ? 0.80 : 0.25; 
                                    if (rand.nextDouble() < chance) {
                                        storage.set(x, y, z, airState);
                                        chunkModified = true;
                                        
                                        if (block == Blocks.DOUBLE_PLANT) {
                                            if (worldY < 255 && chunk.getBlockState(x, worldY + 1, z).getBlock() == Blocks.DOUBLE_PLANT) {
                                                chunk.setBlockState(new net.minecraft.util.math.BlockPos(x, worldY + 1, z), airState);
                                            }
                                            if (worldY > 0 && chunk.getBlockState(x, worldY - 1, z).getBlock() == Blocks.DOUBLE_PLANT) {
                                                chunk.setBlockState(new net.minecraft.util.math.BlockPos(x, worldY - 1, z), airState);
                                            }
                                        }
                                    }
                                }
                                else if (block instanceof net.minecraft.block.BlockLeaves) {
                                    boolean isExposed = (worldY >= 255 || worldY <= 0 ||
                                                         chunk.getBlockState(x, worldY + 1, z).getBlock() == Blocks.AIR ||
                                                         chunk.getBlockState(x, worldY - 1, z).getBlock() == Blocks.AIR);
                                    
                                    double targetChance = isExposed ? 0.80 : 0.15;
                                    double actualChance = isInstant ? targetChance : (targetChance * 0.30);
                                    
                                    if (rand.nextDouble() < actualChance) {
                                        storage.set(x, y, z, airState);
                                        chunkModified = true;
                                    }
                                }
                                else if (block == Blocks.BROWN_MUSHROOM_BLOCK || block == Blocks.RED_MUSHROOM_BLOCK) {
                                    double chance = isInstant ? 0.90 : 0.40; 
                                    if (rand.nextDouble() < chance) {
                                        storage.set(x, y, z, airState);
                                        chunkModified = true;
                                    }
                                }
                                else if (block == Blocks.GRASS) {
                                    double chance = isInstant ? 0.40 : 0.10; 
                                    if (rand.nextDouble() < chance) {
                                        storage.set(x, y, z, dirtState);
                                        chunkModified = true;
                                    }
                                }
                                else if (block == Blocks.AIR) {
                                    if (worldY > 0) {
                                        Block blockBelow = chunk.getBlockState(x, worldY - 1, z).getBlock();
                                        if (blockBelow == Blocks.DIRT || blockBelow == Blocks.STONE || blockBelow == Blocks.GRASS) {
                                            int worldX = (chunk.x * 16) + x;
                                            int worldZ = (chunk.z * 16) + z;
                                            
                                            double clusterNoise = Math.sin(worldX * 0.15) * Math.cos(worldZ * 0.15);
                                            if (clusterNoise > 0.80) { 
                                                double baseChance = emergencyThrottle ? 0.001 : 0.05;
                                                double chance = isInstant ? 0.30 : baseChance; 
                                                
                                                if (rand.nextDouble() < chance) {
                                                    storage.set(x, y, z, fireState);
                                                    chunkModified = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            } 
                            else if (phase == 2) {
                                if (block instanceof net.minecraft.block.BlockLeaves || 
                                    block == Blocks.YELLOW_FLOWER || block == Blocks.RED_FLOWER) {
                                    
                                    double chance = isInstant ? 1.0 : 0.40; 
                                    if (rand.nextDouble() < chance) {
                                        storage.set(x, y, z, airState);
                                        chunkModified = true;
                                    }
                                }
                                else if (block == Blocks.DOUBLE_PLANT || block == Blocks.TALLGRASS) {
                                    double chance = isInstant ? 1.0 : 0.40;
                                    if (rand.nextDouble() < chance) {
                                        storage.set(x, y, z, airState);
                                        chunkModified = true;
                                        
                                        if (block == Blocks.DOUBLE_PLANT) {
                                            if (worldY < 255 && chunk.getBlockState(x, worldY + 1, z).getBlock() == Blocks.DOUBLE_PLANT) {
                                                chunk.setBlockState(new BlockPos(x, worldY + 1, z), airState);
                                            }
                                            if (worldY > 0 && chunk.getBlockState(x, worldY - 1, z).getBlock() == Blocks.DOUBLE_PLANT) {
                                                chunk.setBlockState(new BlockPos(x, worldY - 1, z), airState);
                                            }
                                        }
                                    }
                                }
                                else if (block == Blocks.GRASS) {
                                    int worldX = (chunk.x * 16) + x;
                                    int worldZ = (chunk.z * 16) + z;
                                    
                                    double noise = Math.sin(worldX * 0.3) + Math.cos(worldZ * 0.3);
                                    boolean survive = (rand.nextDouble() < 0.15) && (noise > 0.5);
                                    
                                    double chance = isInstant ? 1.0 : 0.25; 
                                    if (!survive && rand.nextDouble() < chance) {
                                        storage.set(x, y, z, dirtState);
                                        chunkModified = true;
                                    }
                                }
                                else if (block == Blocks.AIR) {
                                    double baseChance = emergencyThrottle ? 0.0001 : 0.002;
                                    double chance = isInstant ? 0.01 : baseChance; 
                                    
                                    if (rand.nextDouble() < chance) { 
                                        if (worldY > 0) {
                                            Block blockBelow = chunk.getBlockState(x, worldY - 1, z).getBlock();
                                            if (blockBelow == Blocks.DIRT || blockBelow == Blocks.STONE || blockBelow == Blocks.GRASS || blockBelow == Blocks.LOG || blockBelow == Blocks.LOG2) {
                                                storage.set(x, y, z, fireState);
                                                chunkModified = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (chunkModified) {
            // Tell Forge this chunk has been altered and needs to be saved to disk
            chunk.markDirty();
        }
        
        return chunkModified;
    }
}