package fracture.mod.client.render;

import fracture.mod.objects.items.ItemCustomGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound; // Required for Manual ID
import net.minecraft.util.EnumHandSide;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import net.minecraft.client.renderer.BufferBuilder;

public class HybridGunRenderer extends GeoItemRenderer<ItemCustomGun> {

    public HybridGunRenderer() {
        super(new ModelCustomGun());
    }

    @Override
    public void renderRecursively(BufferBuilder builder, GeoBone bone, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("hr") || bone.getName().equals("hl")) {
            return; // Skip drawing cubes
        }
        super.renderRecursively(builder, bone, red, green, blue, alpha);
    }

    @Override
    public void renderByItem(ItemStack itemStack) {
        AbstractClientPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            super.renderByItem(itemStack);
            return;
        }

        ItemCustomGun animatable = (ItemCustomGun) itemStack.getItem();
        GeoModel model = this.getGeoModelProvider().getModel(this.getGeoModelProvider().getModelLocation(animatable));
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();

        int stackID = getOrCreateGeckoID(itemStack); 
        animatable.getFactory().getOrCreateAnimationData(stackID);

        GlStateManager.pushMatrix();

        GlStateManager.translate(0.1F, 0.1F, -1.5F); 
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F); 
        
        Minecraft.getMinecraft().renderEngine.bindTexture(this.getGeoModelProvider().getTextureLocation(animatable));

        super.render(model, animatable, partialTicks, 1.0F, 1.0F, 1.0F, 1.0F);
        
        GeoBone rootBone = getBoneRecursive(model, "root");
        GeoBone armsBone = getBoneRecursive(model, "Arms");
        GeoBone rightArmBone = getBoneRecursive(model, "hr");
        GeoBone leftArmBone = getBoneRecursive(model, "hl");

        if (rootBone != null && armsBone != null) {
            if (rightArmBone != null) renderVanillaArm(player, rootBone, armsBone, rightArmBone, EnumHandSide.RIGHT);
            if (leftArmBone != null) renderVanillaArm(player, rootBone, armsBone, leftArmBone, EnumHandSide.LEFT);
        }

        GlStateManager.popMatrix();
    }

    // Workaround for older GeckoLib versions missing guaranteeIDForStack
    private int getOrCreateGeckoID(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("GeckoID")) {
            int id = Minecraft.getMinecraft().player.ticksExisted + (int)(Math.random() * 10000);
            tag.setInteger("GeckoID", id);
        }
        return tag.getInteger("GeckoID");
    }

    private void renderVanillaArm(AbstractClientPlayer player, GeoBone root, GeoBone arms, GeoBone hand, EnumHandSide side) {
        GlStateManager.pushMatrix();
        applyBoneTransform(root);
        applyBoneTransform(arms);
        applyBoneTransform(hand);

        float f = 0.0625F; 
        if (side == EnumHandSide.RIGHT) GlStateManager.translate(-5.0F * f, 2.0F * f, 1.0F * f); 
        else GlStateManager.translate(5.0F * f, 2.0F * f, 1.0F * f);

        RenderPlayer renderPlayer = Minecraft.getMinecraft().getRenderManager().getSkinMap().get(player.getSkinType());
        Minecraft.getMinecraft().renderEngine.bindTexture(player.getLocationSkin());
        
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F); 
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F); 
        
        if (side == EnumHandSide.RIGHT) renderPlayer.renderRightArm(player);
        else renderPlayer.renderLeftArm(player);
        
        GlStateManager.popMatrix();
    }

    private void applyBoneTransform(GeoBone bone) {
        float f = 0.0625F; 
        GlStateManager.translate((bone.rotationPointX + bone.getPositionX()) * f, 
                                 (bone.rotationPointY + bone.getPositionY()) * f, 
                                 (bone.rotationPointZ + bone.getPositionZ()) * f);
        GlStateManager.rotate((float)Math.toDegrees(bone.getRotationX()), 1, 0, 0);
        GlStateManager.rotate((float)Math.toDegrees(bone.getRotationY()), 0, 1, 0);
        GlStateManager.rotate((float)Math.toDegrees(bone.getRotationZ()), 0, 0, 1);
        GlStateManager.translate(-bone.rotationPointX * f, -bone.rotationPointY * f, -bone.rotationPointZ * f);
    }

    private GeoBone getBoneRecursive(GeoModel model, String boneName) {
        if (model == null || model.topLevelBones == null) return null;
        for (GeoBone bone : model.topLevelBones) {
            GeoBone result = searchBoneTree(bone, boneName);
            if (result != null) return result;
        }
        return null;
    }

    private GeoBone searchBoneTree(GeoBone currentBone, String targetName) {
        if (currentBone.name.equals(targetName)) return currentBone;
        if (currentBone.childBones != null) {
            for (GeoBone child : currentBone.childBones) {
                GeoBone result = searchBoneTree(child, targetName);
                if (result != null) return result;
            }
        }
        return null;
    }
}