package fracture.mod.events;

import fracture.mod.network.NetworkHandler;
import fracture.mod.network.PacketBombardment;
import fracture.mod.util.CustomExplosion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class BombardmentHandler {

    private static final int STRIKE_CHANCE = 100;
    private static final double TARGET_RADIUS = 48.0D; 

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START || event.player.world.isRemote) {
            return;
        }

        EntityPlayer player = event.player;
        World world = player.world;

        if (world.provider.getDimension() != 0) {
            return;
        }


        long currentTime = world.getWorldTime();
        if (currentTime < 25000 || currentTime >= 48000) {
            return;
        }

        Random rand = world.rand;

        if (rand.nextInt(STRIKE_CHANCE) != 0) {
            return;
        }

        double targetX = player.posX;
        double targetZ = player.posZ;
        boolean validTargetFound = false;

        if (rand.nextFloat() < 0.60F) {
            AxisAlignedBB searchBox = player.getEntityBoundingBox().grow(TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);
            List<EntityLivingBase> potentialTargets = world.getEntitiesWithinAABB(
                    EntityLivingBase.class, 
                    searchBox,
                    entity -> entity instanceof EntityAnimal || entity instanceof EntityVillager
            );

            if (!potentialTargets.isEmpty()) {
                EntityLivingBase chosenTarget = potentialTargets.get(rand.nextInt(potentialTargets.size()));
                targetX = chosenTarget.posX;
                targetZ = chosenTarget.posZ;
                validTargetFound = true;
            }
        }

        if (!validTargetFound) {
            double dist = 10.0D + (rand.nextDouble() * (TARGET_RADIUS - 10.0D));
            double angle = rand.nextDouble() * Math.PI * 2;
            
            targetX = player.posX + (Math.cos(angle) * dist);
            targetZ = player.posZ + (Math.sin(angle) * dist);
        }
        
        double aimconeX = (rand.nextDouble() * 10.0D) - 5.0D;
        double aimconeZ = (rand.nextDouble() * 10.0D) - 5.0D;

        double finalX = targetX + aimconeX;
        double finalZ = targetZ + aimconeZ;
        
        BlockPos strikePos = world.getHeight(new BlockPos(finalX, 0, finalZ));
        double finalY = strikePos.getY();

        CustomExplosion.triggerExplosion(
                world,
                null,
                finalX,
                finalY,
                finalZ,
                5.0F,
                1.0F,
                true,
                false, 
                CustomExplosion.ExplosionType.DEFAULT
        );

        double originX = finalX + ((rand.nextDouble() * 40.0D) - 20.0D);
        double originY = 255.0D;
        double originZ = finalZ + ((rand.nextDouble() * 40.0D) - 20.0D);

        NetworkRegistry.TargetPoint targetPoint = 
                new NetworkRegistry.TargetPoint(
                        world.provider.getDimension(), 
                        finalX, finalY, finalZ, 
                        128.0D
                );

        NetworkHandler.INSTANCE.sendToAllAround(
                new PacketBombardment(finalX, finalY, finalZ, originX, originY, originZ), 
                targetPoint
        );
    }
}
