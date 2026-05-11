package fracture.mod.client.render;

import fracture.mod.objects.items.ItemCustomGun;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = "fracture", value = Side.CLIENT)
public class GunRenderHandler {
    
    private static final HybridGunRenderer renderer = new HybridGunRenderer();

    @SubscribeEvent
    public static void onRenderHand(RenderSpecificHandEvent event) {
        Item item = event.getItemStack().getItem();

        if (item instanceof ItemCustomGun) {
            event.setCanceled(true); // Cancel MrCrayfish / Vanilla rendering

            GlStateManager.pushMatrix();
            //GlStateManager.translate(0.5, -0.5, -0.5); 
            
            renderer.renderByItem(event.getItemStack());
            
            GlStateManager.popMatrix();
        }
    }
}