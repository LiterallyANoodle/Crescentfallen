package fracture.mod.util.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Client-side visual handler: spawns particles when the player's NBT state indicates SLIDING.
 */
public class SlideClientHandler {

    @SubscribeEvent
    public void onClientPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerSP)) return;
        EntityPlayerSP player = (EntityPlayerSP) event.getEntityLiving();

        // Spawn dust particles if player is sliding
        String state = player.getEntityData().getString("diveState");
        if (!"SLIDING".equals(state)) return;

        for (int i = 0; i < 3; i++) {
            double px = player.posX + (player.world.rand.nextDouble() - 0.5) * 0.6;
            double py = player.posY + 0.1;
            double pz = player.posZ + (player.world.rand.nextDouble() - 0.5) * 0.6;

            player.world.spawnParticle(
                EnumParticleTypes.BLOCK_DUST,
                px, py, pz,
                0.0, 0.05, 0.0,
                net.minecraft.block.Block.getStateId(Blocks.DIRT.getDefaultState())
            );
        }
    }
}