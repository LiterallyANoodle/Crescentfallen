//package fracture.mod.client.event;
//
//import net.minecraft.client.Minecraft;
//import net.minecraftforge.client.event.RenderGameOverlayEvent;
//import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
//import net.minecraftforge.fml.common.eventhandler.EventPriority;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import org.lwjgl.opengl.GL11;
//
//public class HidePressureScissor {
//
//	//Wip, not functioning
//	
//    public HidePressureScissor() {
//        System.out.println("[FRACTURE] HidePressureScissor event handler initialized!");
//    }
//
//    private boolean shouldHide() {
//        Minecraft mc = Minecraft.getMinecraft();
//        return mc.player != null && mc.player.isCreative();
//    }
//
//    //looking for it
//    private boolean isTargetEvent(ElementType type) {
//        return type == ElementType.ALL || 
//               type == ElementType.HELMET || 
//               type == ElementType.HOTBAR || 
//               type == ElementType.TEXT; 
//    }
//
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public void onPreStart(RenderGameOverlayEvent.Pre event) {
//        if (shouldHide() && isTargetEvent(event.getType())) {
//            GL11.glEnable(GL11.GL_SCISSOR_TEST);
//            GL11.glScissor(0, 0, 0, 0); 
//        }
//    }
//
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public void onPreEnd(RenderGameOverlayEvent.Pre event) {
//        if (shouldHide() && isTargetEvent(event.getType())) {
//            GL11.glDisable(GL11.GL_SCISSOR_TEST);
//        }
//    }
//
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public void onPostStart(RenderGameOverlayEvent.Post event) {
//        if (shouldHide() && isTargetEvent(event.getType())) {
//            GL11.glEnable(GL11.GL_SCISSOR_TEST);
//            GL11.glScissor(0, 0, 0, 0);
//        }
//    }
//
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public void onPostEnd(RenderGameOverlayEvent.Post event) {
//        if (shouldHide() && isTargetEvent(event.getType())) {
//            GL11.glDisable(GL11.GL_SCISSOR_TEST);
//        }
//    }
//}