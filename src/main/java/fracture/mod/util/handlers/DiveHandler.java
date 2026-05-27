package fracture.mod.util.handlers;

import fracture.mod.CFInfo;
import fracture.mod.CFMain;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = CFInfo.ID)

// THIS IS A SEPRATE VERION, A TESTING VERSION FOR CLIENT SIDE SYNCRONIZATION. IT IS A WORK IN PROGRESS

public class DiveHandler {

    private static final double DASH_STRENGTH = 1.1D;
    private static final double LEAP_STRENGTH = 0.5D;
    private static final float SATURATION_DRAIN = 2.0F;
    private static final int COOLDOWN_TICKS = 20; 

    private static final float SLIDE_START_SPEED = 1.2F;
    private static final float SLIDE_DECAY_RATE = 0.04F;
    private static final float SLIDE_HITBOX_HEIGHT = 0.6F;
    private static final float NORMAL_HITBOX_HEIGHT = 1.8F;

    private static Method setSizeMethod;

    static {
        try {
            setSizeMethod = ReflectionHelper.findMethod(Entity.class, "setSize", "func_70105_a", float.class, float.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum DiveState {
        NONE, DIVING, SLIDING
    }


    // Protects the local player from Server NBT overwrites during lag.
    @SideOnly(Side.CLIENT)
    public static class ClientPhysics {
        public static DiveState state = DiveState.NONE;
        public static int cooldown = 0;
        public static boolean canDive = true;
        public static double slideDirX, slideDirZ;
        public static float slideSpeed;
        public static double lastDiveDirX, lastDiveDirZ;
        public static float defaultStepHeight = 0.6F;
    }

    @SideOnly(Side.CLIENT)
    public static boolean performClientDive(EntityPlayerSP player, float forward, float strafe) {
        if (ClientPhysics.cooldown > 0) return false;

        if (!ClientPhysics.canDive && !player.onGround && !player.isInWater() && !player.isInLava()) return false;

        player.world.playSound(player.posX, player.posY, player.posZ,
                net.minecraft.init.SoundEvents.ENTITY_ENDERDRAGON_FLAP,
                net.minecraft.util.SoundCategory.PLAYERS, 0.3F, 1.0F, false);

        if (forward != 0 || strafe != 0) {
            float yaw = player.rotationYaw;
            double motionX = (strafe * Math.cos(Math.toRadians(yaw)) - forward * Math.sin(Math.toRadians(yaw)));
            double motionZ = (forward * Math.cos(Math.toRadians(yaw)) + strafe * Math.sin(Math.toRadians(yaw)));

            Vec3d moveVec = new Vec3d(motionX, 0, motionZ).normalize();
            player.motionX = moveVec.x * DASH_STRENGTH;
            player.motionZ = moveVec.z * DASH_STRENGTH;
            player.motionY += LEAP_STRENGTH;

            ClientPhysics.lastDiveDirX = moveVec.x;
            ClientPhysics.lastDiveDirZ = moveVec.z;
        }

        // Set LOCAL state
        ClientPhysics.cooldown = COOLDOWN_TICKS;
        ClientPhysics.canDive = false;
        ClientPhysics.state = DiveState.DIVING;
        
        player.getEntityData().setInteger("diveCooldown", COOLDOWN_TICKS);
        
        updatePlayerSize(player, 0.6F, SLIDE_HITBOX_HEIGHT);
        return true;
    }

    public static void performServerDiveStats(EntityPlayerMP player) {
        player.getEntityData().setInteger("diveCooldown", COOLDOWN_TICKS);
        player.getFoodStats().setFoodSaturationLevel(
                Math.max(0, player.getFoodStats().getSaturationLevel() - SATURATION_DRAIN)
        );
        player.getEntityData().setBoolean("canDive", false);
        int diveCount = player.getEntityData().getInteger("diveCount");
        player.getEntityData().setInteger("diveCount", diveCount + 1);
        
        setState(player, DiveState.DIVING);
        updatePlayerSize(player, 0.6F, SLIDE_HITBOX_HEIGHT);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        boolean isLocalPlayer = (player.world.isRemote && player instanceof EntityPlayerSP);

        if (isLocalPlayer) {
            if (ClientPhysics.cooldown > 0) ClientPhysics.cooldown--;
            
            if (player.onGround || player.isInWater() || player.isInLava()) {
                ClientPhysics.canDive = true;
            }
        } else {
        	// Handle other players
            int cd = player.getEntityData().getInteger("diveCooldown");
            if (cd > 0) player.getEntityData().setInteger("diveCooldown", cd - 1);
            if (player.onGround || player.isInWater() || player.isInLava()) {
                player.getEntityData().setBoolean("canDive", true);
            }
        }

        // Use local state for the client player, but NBT for everyone else
        DiveState state = isLocalPlayer ? ClientPhysics.state : getState(player);

        if (state == DiveState.DIVING) {
            updatePlayerSize(player, 0.6F, SLIDE_HITBOX_HEIGHT);

            if (isLocalPlayer) {
                if (!player.onGround && (Math.abs(player.motionX) > 0.01 || Math.abs(player.motionZ) > 0.01)) {
                     Vec3d currentMotion = new Vec3d(player.motionX, 0, player.motionZ).normalize();
                     ClientPhysics.lastDiveDirX = currentMotion.x;
                     ClientPhysics.lastDiveDirZ = currentMotion.z;
                }

                if (player.onGround) {
                    double dx = player.posX - player.prevPosX;
                    double dz = player.posZ - player.prevPosZ;
                    Vec3d slideDir = new Vec3d(dx, 0, dz);

                    if (slideDir.lengthSquared() < 1.0E-4D) {
                        if (Math.abs(ClientPhysics.lastDiveDirX) > 0.01 || Math.abs(ClientPhysics.lastDiveDirZ) > 0.01) {
                             slideDir = new Vec3d(ClientPhysics.lastDiveDirX, 0, ClientPhysics.lastDiveDirZ);
                        } else {
                            float yaw = player.rotationYaw;
                            slideDir = new Vec3d(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
                        }
                    }
                    slideDir = slideDir.normalize();
                    ClientPhysics.slideDirX = slideDir.x;
                    ClientPhysics.slideDirZ = slideDir.z;
                    ClientPhysics.slideSpeed = SLIDE_START_SPEED;
                    
                    // Store base step height and boost it for incline sliding
                    ClientPhysics.defaultStepHeight = player.stepHeight;
                    player.stepHeight = 1.25F; 

                    ClientPhysics.state = DiveState.SLIDING;
                }
            } else {
                if (player.onGround) setState(player, DiveState.SLIDING);
            }
        }

        // SLIDING
        if (state == DiveState.SLIDING) {
            updatePlayerSize(player, 0.6F, SLIDE_HITBOX_HEIGHT);

            if (player.isSneaking()) {
                stopSliding(player);
                return;
            }

            if (isLocalPlayer) {
                if (ClientPhysics.slideSpeed <= 0 || player.collidedHorizontally) {
                    stopSliding(player);
                    CFMain.NETWORK.sendToServer(new SlideCancelPacket()); 
                    return;
                }

                player.motionX = ClientPhysics.slideDirX * ClientPhysics.slideSpeed;
                player.motionZ = ClientPhysics.slideDirZ * ClientPhysics.slideSpeed;
                
                ClientPhysics.slideSpeed -= SLIDE_DECAY_RATE;
            }

            // Server handles particles for other clients to see
            if (!player.world.isRemote && player.ticksExisted % 2 == 0) {
                spawnSlideParticles(player);
            }
        }
    }

    public static void stopSliding(EntityPlayer player) {
        if (player.world.isRemote && player instanceof EntityPlayerSP) {
            player.motionX = 0;
            player.motionZ = 0;
            ClientPhysics.state = DiveState.NONE;
            // Reset Step Height back to normal
            player.stepHeight = ClientPhysics.defaultStepHeight;
        } else {
            setState(player, DiveState.NONE);
        }
        
        updatePlayerSize(player, 0.6F, NORMAL_HITBOX_HEIGHT);
        player.getEntityData().setInteger("diveCount", 0);
    }

    private static void updatePlayerSize(EntityPlayer player, float width, float height) {
        if (player.height == height && player.width == width) return; 
        try {
            if (setSizeMethod != null) setSizeMethod.invoke(player, width, height);
        } catch (Exception e) {}
    }

    private static void spawnSlideParticles(EntityPlayer player) {
        if (player.world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) player.world;
            worldServer.spawnParticle(
                EnumParticleTypes.BLOCK_DUST,
                player.posX, player.posY + 0.1, player.posZ, 
                3, 0.2, 0.0, 0.2, 0.05, 
                Block.getStateId(Blocks.DIRT.getDefaultState())
            );
        }
    }

    public static DiveState getState(EntityPlayer player) {
        String s = player.getEntityData().getString("diveState");
        if (s.isEmpty()) return DiveState.NONE; 
        try {
            return DiveState.valueOf(s);
        } catch (Exception e) {
            return DiveState.NONE;
        }
    }

    public static void setState(EntityPlayer player, DiveState state) {
        player.getEntityData().setString("diveState", state.name());
    }
}