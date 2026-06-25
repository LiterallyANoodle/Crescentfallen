package fracture.mod.objects.items;

import com.mrcrayfish.guns.item.ItemGun;
import com.mrcrayfish.guns.object.Gun;
import fracture.mod.CFMain;
import fracture.mod.init.ItemInit;
import fracture.mod.client.render.HybridGunRenderer;
import fracture.mod.util.IHasModel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ItemCustomGun extends ItemGun implements IHasModel, IAnimatable {

    private final Gun cfGun;
    private final int reloadDuration; 
    public AnimationFactory factory = new AnimationFactory(this);

    public ItemCustomGun(Gun gun, String name, int reloadDuration) {
        super(new ResourceLocation("fracture", name));
        this.cfGun = gun;
        this.reloadDuration = reloadDuration; 
        setTranslationKey(name);
        setCreativeTab(CFMain.CrescentfallenGuns);
        this.setTileEntityItemStackRenderer(new HybridGunRenderer());
        ItemInit.ITEMS.add(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ActionResult<ItemStack> result = super.onItemRightClick(worldIn, playerIn, handIn);
        if (worldIn.isRemote) {
            ItemStack stack = playerIn.getHeldItem(handIn);
            if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setLong("LastFiredTick", (long)playerIn.ticksExisted);
            stack.getTagCompound().removeTag("LastReloadTick");
        }
        return result;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 1, this::predicate));
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return PlayState.STOP;

        ItemStack stack = player.getHeldItemMainhand(); 
        if (stack.getItem() != this) return PlayState.STOP;

        NBTTagCompound tag = stack.getTagCompound();
        int currentTick = player.ticksExisted;
        String currentAnim = event.getController().getCurrentAnimation() != null ? 
                             event.getController().getCurrentAnimation().animationName : "none";

        // Logic for Reload
        if (tag != null && tag.hasKey("LastReloadTick")) {
            long lastReload = tag.getLong("LastReloadTick");
            if (currentTick - lastReload < (this.reloadDuration / 50)) {
                if (!currentAnim.equals("animation_reload")) {
                    event.getController().setAnimation(new AnimationBuilder().addAnimation("animation_reload", false));
                }
                return PlayState.CONTINUE;
            }
        }

        // Logic for Fire (Refined with 3-tick window for snappier response)
        if (tag != null && tag.hasKey("LastFiredTick")) {
             long lastFired = tag.getLong("LastFiredTick");
             if (currentTick - lastFired < 3) { // Window reduced from 5 to 3
                 if (!currentAnim.equals("animation_fire")) {
                     // Pass true here so automatic weapons repeat their recoil cycles smoothly
                     boolean isAuto = this.getGun().general.auto;
                     event.getController().setAnimation(new AnimationBuilder().addAnimation("animation_fire", isAuto));
                 }
                 return PlayState.CONTINUE;
             }
        }
        
        // Movement Logic
        if (player.moveForward != 0 || player.moveStrafing != 0) {
            if (!currentAnim.equals("animation_walk")) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation_walk", true));
            }
        } else {
            // Changes here
            if (!currentAnim.equals("animation_idle") && !currentAnim.equals("animation_fire")) {
                event.getController().setAnimation(new AnimationBuilder().addAnimation("animation_idle", true));
            }
        }
        return PlayState.CONTINUE;
    }

    @Override public AnimationFactory getFactory() { return this.factory; }
    @Override public Gun getGun() { return this.cfGun; }
    @Override public void registerModels() { CFMain.proxy.registerItemRenderer(this, 0, "inventory"); }
    @Override public int getMaxItemUseDuration(ItemStack stack) { return 72000; }
}