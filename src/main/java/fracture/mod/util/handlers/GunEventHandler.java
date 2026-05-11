package fracture.mod.util.handlers;

import fracture.mod.objects.items.ItemCustomGun;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class GunEventHandler {

    private boolean wasClicking = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.currentScreen != null) {
            wasClicking = false;
            return;
        }

        boolean isClicking = Mouse.isButtonDown(0);
        if (isClicking) {
            if (!wasClicking) handleFire(false); 
            else handleFire(true);
        }
        wasClicking = isClicking;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.player != null && mc.player.getHeldItemMainhand().getItem() instanceof ItemCustomGun) {
                setTag(mc.player.getHeldItemMainhand(), "LastReloadTick", (long)mc.player.ticksExisted);
            }
        }
    }
    
    // Note: make sure key name matches item predicate
    
    private void handleFire(boolean isHolding) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack stack = mc.player.getHeldItemMainhand();

        if (stack.getItem() instanceof ItemCustomGun) {
            ItemCustomGun gunItem = (ItemCustomGun) stack.getItem();
            if (isHolding && !gunItem.getGun().general.auto) return; 

            long currentTick = mc.player.ticksExisted;
            setTag(stack, "LastFiredTick", currentTick);
            
            if(stack.hasTagCompound()) {
                stack.getTagCompound().removeTag("LastReloadTick");
            }
        }
    }

    // compilation error
    private void setTag(ItemStack stack, String key, long value) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setLong(key, value);
    }
}