package fracture.mod.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import fracture.mod.CFMain;
import fracture.mod.client.particle.ParticleBlastDebris;
import fracture.mod.client.particle.ParticleExplosion;
import fracture.mod.client.particle.ParticleShockwave;
import fracture.mod.client.particle.ParticleSmoke;
import fracture.mod.client.particle.ParticleSpark;
import fracture.mod.client.vhandlers.ScreenShakeManager;
import fracture.mod.init.CFSounds;
import fracture.mod.network.PacketSpawnExplosionParticles;

public class CustomExplosion {

    public enum ExplosionType {
        DEFAULT,
        HOLY
    }

    private final World world;
    private final Entity exploder;
    private final double x;
    private final double y;
    private final double z;
    private final float radius;

    private float knockbackMultiplier;
    private boolean damagesBlocks;
    private boolean causesScreenShake;
    private ExplosionType type;

    public CustomExplosion(World world, Entity exploder, double x, double y, double z, float radius) {
        this.world = world;
        this.exploder = exploder;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        
        this.knockbackMultiplier = 0.0F;
        this.damagesBlocks = false;
        this.causesScreenShake = false;
        this.type = ExplosionType.DEFAULT;
    }

    public CustomExplosion setKnockbackMultiplier(float multiplier) {
        this.knockbackMultiplier = multiplier;
        return this;
    }

    public CustomExplosion setDamagesBlocks(boolean damagesBlocks) {
        this.damagesBlocks = damagesBlocks;
        return this;
    }

    public CustomExplosion setCausesScreenShake(boolean causesScreenShake) {
        this.causesScreenShake = causesScreenShake;
        return this;
    }

    public CustomExplosion setType(ExplosionType type) {
        this.type = type;
        return this;
    }

    public void doEntityDamage() {
        AxisAlignedBB aabb = new AxisAlignedBB(this.x - this.radius, this.y - this.radius, this.z - this.radius, this.x + this.radius, this.y + this.radius, this.z + this.radius);
        List<Entity> targets = this.world.getEntitiesWithinAABB(Entity.class, aabb);

        for (Entity entity : targets) {
            if (entity == this.exploder || entity.isImmuneToExplosions()) continue;

            double distSq = entity.getDistanceSq(this.x, this.y, this.z);
            double radiusSq = this.radius * this.radius;

            if (distSq <= radiusSq) {
                double distance = Math.sqrt(distSq);
                double damageScale = 1.0D - (distance / this.radius);
                float damageAmount = (float)((damageScale * damageScale + damageScale) / 2.0D * 7.0D * this.radius + 1.0D);

                DamageSource source = getDamageSource();
                entity.attackEntityFrom(source, damageAmount);

                if (this.knockbackMultiplier > 0.0F) {
                    double dX = entity.posX - this.x;
                    double dY = entity.posY + (double)entity.getEyeHeight() - this.y;
                    double dZ = entity.posZ - this.z;
                    double vectorLength = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

                    if (vectorLength != 0.0D) {
                        dX /= vectorLength;
                        dY /= vectorLength;
                        dZ /= vectorLength;

                        double knockbackPower = damageScale * this.knockbackMultiplier;
                        entity.motionX += dX * knockbackPower;
                        entity.motionY += dY * knockbackPower;
                        entity.motionZ += dZ * knockbackPower;
                        entity.velocityChanged = true;
                    }
                }
            }
        }
    }

    public void doBlockDamage() {
        if (!this.damagesBlocks) return;

        int minX = (int) Math.floor(this.x - this.radius);
        int maxX = (int) Math.floor(this.x + this.radius);
        int minY = (int) Math.floor(this.y - this.radius);
        int maxY = (int) Math.floor(this.y + this.radius);
        int minZ = (int) Math.floor(this.z - this.radius);
        int maxZ = (int) Math.floor(this.z + this.radius);

        double radiusSq = this.radius * this.radius;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Explosion dummyExplosion = new Explosion(this.world, this.exploder, this.x, this.y, this.z, this.radius, false, true);
        float dropChance = 1.0F / this.radius;

        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    double dx = (bx + 0.5D) - this.x;
                    double dy = (by + 0.5D) - this.y;
                    double dz = (bz + 0.5D) - this.z;
                    double distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq <= radiusSq) {
                        pos.setPos(bx, by, bz);
                        IBlockState state = this.world.getBlockState(pos);
                        Block block = state.getBlock();

                        if (block.isAir(state, this.world, pos)) continue;

                        double distance = Math.sqrt(distSq);
                        float blastPower = this.radius * (1.0F - (float)(distance / this.radius));
                        float scaledPower = blastPower * 7.0F; 
                        float resistance = block.getExplosionResistance(this.world, pos, this.exploder, dummyExplosion);

                        if (scaledPower > resistance) {
                            if (block.canDropFromExplosion(dummyExplosion)) {
                                block.dropBlockAsItemWithChance(this.world, pos, state, dropChance, 0);
                            }
                            block.onBlockExploded(this.world, pos, dummyExplosion);
                        }
                    }
                }
            }
        }
    }

    private DamageSource getDamageSource() {
        if (this.type == ExplosionType.HOLY) return DamageSource.MAGIC; 
        if (this.exploder instanceof EntityLivingBase) return new EntityDamageSource("explosion", this.exploder).setExplosion();
        return new DamageSource("explosion").setExplosion();
    }

    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
        if (!this.world.isRemote) return;

        Minecraft mc = Minecraft.getMinecraft();

        float randomizedPitch = 0.8F + (this.world.rand.nextFloat() * 0.4F); 

        this.world.playSound(
                this.x, this.y, this.z, 
                CFSounds.CF_EXPLODE, 
                SoundCategory.BLOCKS, 
                6.0F,            
                randomizedPitch, 
                false
        );

        if (this.causesScreenShake && mc.player != null) {
            double dist = mc.player.getDistance(this.x, this.y, this.z);
            double shakeRadius = this.radius * 3.0D; 
            
            if (dist <= shakeRadius) {
                float intensity = (float) (1.0D - (dist / shakeRadius)) * 5.0F; 
                ScreenShakeManager.addShake(intensity);
            }
        }

        // F3+B Debug Bounding Sphere(make this better)
        if (mc.getRenderManager().isDebugBoundingBox()) {
            for (int i = 0; i < 40; i++) {
                double dX = (this.world.rand.nextDouble() - 0.5D) * 2.0D;
                double dY = (this.world.rand.nextDouble() - 0.5D) * 2.0D;
                double dZ = (this.world.rand.nextDouble() - 0.5D) * 2.0D;
                double mag = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
                
                if (mag != 0) {
                    dX = (dX / mag) * this.radius;
                    dY = (dY / mag) * this.radius;
                    dZ = (dZ / mag) * this.radius;
                    this.world.spawnParticle(EnumParticleTypes.BARRIER, this.x + dX, this.y + dY, this.z + dZ, 0, 0, 0);
                }
            }
        }

        mc.effectRenderer.addEffect(new ParticleExplosion(this.world, this.x, this.y, this.z));
        mc.effectRenderer.addEffect(new ParticleSpark(this.world, this.x, this.y, this.z));
        mc.effectRenderer.addEffect(new ParticleShockwave(this.world, this.x, this.y + 0.5D, this.z));

        int smokeCount = 8 + this.world.rand.nextInt(5);
        for (int i = 0; i < smokeCount; i++) {
            double vx = (this.world.rand.nextDouble() - 0.5D) * 3.0D;
            double vy = (this.world.rand.nextDouble() - 0.5D) * 3.0D;
            double vz = (this.world.rand.nextDouble() - 0.5D) * 3.0D;
            mc.effectRenderer.addEffect(new ParticleSmoke(this.world, this.x, this.y, this.z, vx, vy, vz));
        }

        BlockPos posBelow = new BlockPos(Math.floor(this.x), Math.floor(this.y) - 1, Math.floor(this.z));
        IBlockState groundState = this.world.getBlockState(posBelow);

        if (!groundState.getBlock().isAir(groundState, this.world, posBelow)) {
            int debrisCount = 40 + this.world.rand.nextInt(20);
            for (int i = 0; i < debrisCount; i++) {
                double vx = (this.world.rand.nextDouble() - 0.5D) * 3.0D;
                double vy = this.world.rand.nextDouble() * 2.0D + 0.5D; 
                double vz = (this.world.rand.nextDouble() - 0.5D) * 3.0D;
                mc.effectRenderer.addEffect(new ParticleBlastDebris(this.world, this.x, this.y, this.z, vx, vy, vz, groundState));
            }
        }
    }


    public static void triggerExplosion(World world, Entity exploder, double x, double y, double z, float radius, float knockback, boolean damagesBlocks, boolean causesScreenShake, ExplosionType type) {
        if (!world.isRemote) {
            CustomExplosion explosion = new CustomExplosion(world, exploder, x, y, z, radius)
                    .setKnockbackMultiplier(knockback)
                    .setDamagesBlocks(damagesBlocks)
                    .setCausesScreenShake(causesScreenShake)
                    .setType(type);

            explosion.doEntityDamage();
            explosion.doBlockDamage();

            NetworkRegistry.TargetPoint point = 
                    new NetworkRegistry.TargetPoint(
                            world.provider.getDimension(), x, y, z, 64.0D);

            CFMain.NETWORK.sendToAllAround(
                    new PacketSpawnExplosionParticles(x, y, z, radius, causesScreenShake), point);
        }
    }
}