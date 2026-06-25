package fracture.mod.planets.moons.kona.biome.gen;

import java.util.Random;

import fracture.mod.client.particle.ParticleKonaFilth;
import fracture.mod.planets.moons.kona.biome.KonaBiomes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TrashHeapAtmosphereHandler {

    public static float fogTransition = 0.0F;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == Side.SERVER || event.phase == TickEvent.Phase.END) return;
        
        EntityPlayer player = event.player;
        World world = player.world;

        boolean inTrashHeap = world.getBiome(player.getPosition()) == KonaBiomes.BiomeKonaTrashHeap;
        if (inTrashHeap) {
            if (fogTransition < 1.0F) fogTransition += 0.02F;
            if (fogTransition > 1.0F) fogTransition = 1.0F;
        } else {
            if (fogTransition > 0.0F) fogTransition -= 0.02F;
            if (fogTransition < 0.0F) fogTransition = 0.0F;
        }

        Random rand = world.rand;
        
        for (int i = 0; i < 3; i++) { 
            double px = player.posX + (rand.nextDouble() - 0.5D) * 64.0D;
            double pz = player.posZ + (rand.nextDouble() - 0.5D) * 64.0D;
            double py = player.posY + (rand.nextDouble() * 32.0D) - 8.0D;
            
            BlockPos spawnPos = new BlockPos(px, py, pz);
            
            if (world.getBiome(spawnPos) == KonaBiomes.BiomeKonaTrashHeap) {
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleKonaFilth(world, px, py, pz, 0, 0, 0));
            }
        }
    }

    
    // WIP
    @SubscribeEvent
    public void onRenderFog(EntityViewRenderEvent.RenderFogEvent event) {
        if (fogTransition > 0.0F) {
            float farPlane = event.getFarPlaneDistance();
            
            float defaultStart = farPlane * 0.75F;
            float defaultEnd = farPlane;
            
            float targetStart = farPlane * 0.05F; 
            float targetEnd = 32.0F + (farPlane * 0.15F); 
            
            float currentStart = defaultStart + ((targetStart - defaultStart) * fogTransition);
            float currentEnd = defaultEnd + ((targetEnd - defaultEnd) * fogTransition);
            
            GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
            GlStateManager.setFogStart(currentStart);
            GlStateManager.setFogEnd(currentEnd);
        }
    }

    @SubscribeEvent
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        Entity entity = event.getEntity();
        World world = entity.world;
        Biome biome = world.getBiome(entity.getPosition());
        
        if (biome == KonaBiomes.BiomeKona || fogTransition > 0.0F) {
            float dayTime = world.getCelestialAngle(1.0F);
            float dayFactor = Math.max(0.0F, MathHelper.cos(dayTime * (float)Math.PI * 2.0F));
            
            float nightR = 0.01F; float nightG = 0.01F; float nightB = 0.03F;
            
            float baseDayR = 0.02F; float baseDayG = 0.03F; float baseDayB = 0.06F;
            
            float trashDayR = 0.12F; float trashDayG = 0.15F; float trashDayB = 0.08F;

            float currentR = nightR + ((baseDayR - nightR) * dayFactor);
            float currentG = nightG + ((baseDayG - nightG) * dayFactor);
            float currentB = nightB + ((baseDayB - nightB) * dayFactor);
            
            float targetR = nightR + ((trashDayR - nightR) * dayFactor);
            float targetG = nightG + ((trashDayG - nightG) * dayFactor);
            float targetB = nightB + ((trashDayB - nightB) * dayFactor);

            event.setRed(currentR + ((targetR - currentR) * fogTransition));
            event.setGreen(currentG + ((targetG - currentG) * fogTransition));
            event.setBlue(currentB + ((targetB - currentB) * fogTransition));
        }
    }
}