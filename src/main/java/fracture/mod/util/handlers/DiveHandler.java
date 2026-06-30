package fracture.mod.util.handlers;

import fracture.mod.CFInfo;
import fracture.mod.CFMain;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
public class DiveHandler {

    private static final double DASH_STRENGTH = 1.1D;
    private static final double LEAP_STRENGTH = 0.5D;
    private static final float SATURATION_DRAIN = 2.0F;
    private static final int COOLDOWN_TICKS = 20; 

    private static final float SLIDE_START_SPEED = 1.2F;
    private static final float SLIDE_DECAY_RATE = 0.04F;
    private static final float SLIDE_HITBOX_HEIGHT = 0.5F;
    private static final float CROUCH_HITBOX_HEIGHT = 0.9F;  
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
        NONE, DIVING, SLIDING, CROUCHING
    }

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
                SoundEvents.ENTITY_ENDERDRAGON_FLAP,
                SoundCategory.PLAYERS, 0.3F, 1.0F, false);

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

        ClientPhysics.cooldown = COOLDOWN_TICKS;
        ClientPhysics.canDive = false;
        ClientPhysics.state = DiveState.DIVING;
        
        player.getEntityData().setInteger("diveCooldown", COOLDOWN_TICKS);
        updatePlayerSize(player, 0.6F, SLIDE_HITBOX_HEIGHT);
        return true;
    }

    public static void performServerDiveStats(EntityPlayerMP player) {
        player.getEntityData().setInteger("diveCooldown", COOLDOWN_TICKS);
        player.getFoodStats().setFoodSaturationLevel(Math.max(0, player.getFoodStats().getSaturationLevel() - SATURATION_DRAIN));
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
                ClientPhysics.cooldown = 0; 
            }

            // CROUCH & SPRINT-SLIDE TRANSITION ATTEMPT
            if (ClientPhysics.state == DiveState.NONE && player.onGround) {
                if (KeybindHandler.crouchKey.isKeyDown()) {
                    if (player.isSprinting()) {
                        double speedX = player.motionX;
                        double speedZ = player.motionZ;
                        double totalSpeed = Math.sqrt(speedX * speedX + speedZ * speedZ);

                        if (totalSpeed > 0.05D) {
                            ClientPhysics.slideDirX = speedX / totalSpeed;
                            ClientPhysics.slideDirZ = speedZ / totalSpeed;
                        } else {
                            float yaw = player.rotationYaw;
                            ClientPhysics.slideDirX = -Math.sin(Math.toRadians(yaw));
                            ClientPhysics.slideDirZ = Math.cos(Math.toRadians(yaw));
                        }

                        ClientPhysics.slideSpeed = SLIDE_START_SPEED;
                        ClientPhysics.defaultStepHeight = player.stepHeight;
                        player.stepHeight = 1.25F; 
                        ClientPhysics.state = DiveState.SLIDING;
                        
                    } else {
                        ClientPhysics.state = DiveState.CROUCHING;
                    }
                }
            }
        } else {
            int cd = player.getEntityData().getInteger("diveCooldown");
            if (cd > 0) player.getEntityData().setInteger("diveCooldown", cd - 1);
            
            if (player.onGround || player.isInWater() || player.isInLava()) {
                player.getEntityData().setBoolean("canDive", true);
                player.getEntityData().setInteger("diveCooldown", 0);
            }
        }

        DiveState state = isLocalPlayer ? ClientPhysics.state : getState(player);

        // DIVING STATE
        if (state == DiveState.DIVING) {
            updatePlayerSize(player, 0.6F, SLIDE_HITBOX_HEIGHT);
            if (isLocalPlayer) {
                if (!player.onGround && (Math.abs(player.motionX) > 0.01 || Math.abs(player.motionZ) > 0.01)) {
                     Vec3d currentMotion = new Vec3d(player.motionX, 0, player.motionZ).normalize();
                     ClientPhysics.lastDiveDirX = currentMotion.x;
                     ClientPhysics.lastDiveDirZ = currentMotion.z;
                }
                if (player.onGround) {
                    Vec3d slideDir = Vec3d.ZERO;
                    if (Math.abs(ClientPhysics.lastDiveDirX) > 0.01 || Math.abs(ClientPhysics.lastDiveDirZ) > 0.01) {
                        slideDir = new Vec3d(ClientPhysics.lastDiveDirX, 0, ClientPhysics.lastDiveDirZ);
                    } else {
                        double dx = player.posX - player.prevPosX;
                        double dz = player.posZ - player.prevPosZ;
                        slideDir = new Vec3d(dx, 0, dz);
                        if (slideDir.lengthSquared() < 1.0E-4D) {
                            float yaw = player.rotationYaw;
                            slideDir = new Vec3d(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
                        }
                    }
                    
                    slideDir = slideDir.normalize();
                    ClientPhysics.slideDirX = slideDir.x;
                    ClientPhysics.slideDirZ = slideDir.z;
                    ClientPhysics.slideSpeed = SLIDE_START_SPEED;
                    
                    ClientPhysics.defaultStepHeight = player.stepHeight;
                    player.stepHeight = 1.25F; 
                    ClientPhysics.state = DiveState.SLIDING;
                }
            } else {
                if (player.onGround) setState(player, DiveState.SLIDING);
            }
        }

        // SLIDING STATE
        if (state == DiveState.SLIDING) {
            updatePlayerSize(player, 0.6F, SLIDE_HITBOX_HEIGHT);

            if (player.isSneaking()) {
                stopSliding(player);
                return;
            }

            if (isLocalPlayer) {
                if (ClientPhysics.slideSpeed <= 0 || player.collidedHorizontally) {
                    if (KeybindHandler.crouchKey.isKeyDown()) {
                        ClientPhysics.state = DiveState.CROUCHING;
                        player.stepHeight = ClientPhysics.defaultStepHeight;
                    } else {
                        stopSliding(player);
                        CFMain.NETWORK.sendToServer(new SlideCancelPacket()); 
                    }
                    return;
                }

                player.motionX = ClientPhysics.slideDirX * ClientPhysics.slideSpeed;
                player.motionZ = ClientPhysics.slideDirZ * ClientPhysics.slideSpeed;
                ClientPhysics.slideSpeed -= SLIDE_DECAY_RATE;
            }

            if (!player.world.isRemote && player.ticksExisted % 2 == 0) {
                spawnSlideParticles(player);
            }
        }

        // CROUCHING STATE HANDLING
        if (state == DiveState.CROUCHING) {
            updatePlayerSize(player, 0.6F, CROUCH_HITBOX_HEIGHT);

            if (isLocalPlayer) {
                EntityPlayerSP localPlayer = (EntityPlayerSP) player;
                localPlayer.movementInput.moveForward *= 0.5F;
                localPlayer.movementInput.moveStrafe *= 0.5F;

                if (!KeybindHandler.crouchKey.isKeyDown() && canUncrouch(player)) {
                    ClientPhysics.state = DiveState.NONE;
                    updatePlayerSize(player, 0.6F, NORMAL_HITBOX_HEIGHT);
                }
            }
        }
    }

    /**
     * Confirms whether a player has enough spatial block clearance directly above them to stand up. Not working currently.
     */
    private static boolean canUncrouch(EntityPlayer player) {
        AxisAlignedBB box = player.getEntityBoundingBox();
        AxisAlignedBB standingBox = new AxisAlignedBB(
            box.minX, 
            box.minY, 
            box.minZ, 
            box.maxX, 
            box.minY + NORMAL_HITBOX_HEIGHT, 
            box.maxZ
        );
        return player.world.getCollisionBoxes(player, standingBox).isEmpty();
    }

    public static void stopSliding(EntityPlayer player) {
        if (player.world.isRemote && player instanceof EntityPlayerSP) {
            player.motionX = 0;
            player.motionZ = 0;
            ClientPhysics.state = DiveState.NONE;
            ClientPhysics.lastDiveDirX = 0;
            ClientPhysics.lastDiveDirZ = 0;
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
            
            int x = MathHelper.floor(player.posX);
            int y = MathHelper.floor(player.getEntityBoundingBox().minY - 0.2D);
            int z = MathHelper.floor(player.posZ);
            BlockPos pos = new BlockPos(x, y, z);
            
            IBlockState state = worldServer.getBlockState(pos);
            
            if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
                worldServer.spawnParticle(
                    EnumParticleTypes.BLOCK_DUST,
                    player.posX, player.posY + 0.1, player.posZ, 
                    3, 0.2, 0.0, 0.2, 0.05, 
                    Block.getStateId(state)
                );
            }
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