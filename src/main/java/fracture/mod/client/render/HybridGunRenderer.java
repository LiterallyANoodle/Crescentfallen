package fracture.mod.client.render;

import fracture.mod.objects.items.ItemCustomGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import net.minecraft.client.renderer.BufferBuilder;

import java.nio.FloatBuffer;

public class HybridGunRenderer extends GeoItemRenderer<ItemCustomGun> {

	//ACTIVE WIP
	
    private static final float TRANSLATE_X =  0.0F;
    private static final float TRANSLATE_Y =  0.0F;
    private static final float TRANSLATE_Z = -1.5F;
    private static final float SCALE       =  0.5F;

    private static final float F = 1.0F / 16.0F; // 1 Blockbench unit = 1/16th block
    private static final float RAD_TO_DEG = 57.295776F;

    // --- DEBUG SYSTEM ---
    private static final boolean DEBUG_MODE = true; 
    private int debugTickCounter = 0;

    // --- MATRIX CAPTURE BUFFERS ---
    private final FloatBuffer savedMatrixRight = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer savedMatrixLeft  = BufferUtils.createFloatBuffer(16);
    private boolean hasRight = false;
    private boolean hasLeft  = false;

    // --- CUSTOM ARM RENDERER ---
    private static final ModelCustomArm armModel = new ModelCustomArm();

    public HybridGunRenderer() {
        super(new ModelCustomGun());
    }

    @Override
    public void renderEarly(ItemCustomGun animatable, float partialTicks,
                            float red, float green, float blue, float alpha) {
        super.renderEarly(animatable, partialTicks, red, green, blue, alpha);
        GlStateManager.translate(TRANSLATE_X, TRANSLATE_Y, TRANSLATE_Z);
        GlStateManager.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public void renderRecursively(BufferBuilder builder, GeoBone bone,
                                  float red, float green, float blue, float alpha) {
        String name = bone.getName();

        if (name.equals("hr") || name.equals("hl")) {
            boolean isRight = name.equals("hr");
            GlStateManager.pushMatrix();

            GlStateManager.translate(bone.rotationPointX * F, bone.rotationPointY * F, bone.rotationPointZ * F);
            
            GlStateManager.translate(bone.getPositionX() * F, bone.getPositionY() * F, bone.getPositionZ() * F);
            
            if (bone.getRotationZ() != 0.0F) GlStateManager.rotate(bone.getRotationZ() * RAD_TO_DEG, 0.0F, 0.0F, 1.0F);
            if (bone.getRotationY() != 0.0F) GlStateManager.rotate(bone.getRotationY() * RAD_TO_DEG, 0.0F, 1.0F, 0.0F);
            if (bone.getRotationX() != 0.0F) GlStateManager.rotate(bone.getRotationX() * RAD_TO_DEG, 1.0F, 0.0F, 0.0F);
            
            if (bone.getScaleX() != 1.0F || bone.getScaleY() != 1.0F || bone.getScaleZ() != 1.0F) {
                GlStateManager.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
            }

            if (bone.childCubes != null && !bone.childCubes.isEmpty()) {
                software.bernie.geckolib3.geo.render.built.GeoCube cube = bone.childCubes.get(0);
                
                GlStateManager.translate((cube.pivot.getX() - bone.getPivotX()) * F, 
                                         (cube.pivot.getY() - bone.getPivotY()) * F, 
                                         (cube.pivot.getZ() - bone.getPivotZ()) * F);

                float rotX = cube.rotation.getX() * RAD_TO_DEG;
                float rotY = cube.rotation.getY() * RAD_TO_DEG;
                float rotZ = cube.rotation.getZ() * RAD_TO_DEG;

                if (rotZ != 0.0F) GlStateManager.rotate(rotZ, 0.0F, 0.0F, 1.0F);
                if (rotY != 0.0F) GlStateManager.rotate(rotY, 0.0F, 1.0F, 0.0F);
                if (rotX != 0.0F) GlStateManager.rotate(rotX, 1.0F, 0.0F, 0.0F);

                float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
                float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

                for (software.bernie.geckolib3.geo.render.built.GeoQuad quad : cube.quads) {
                    for (software.bernie.geckolib3.geo.render.built.GeoVertex vertex : quad.vertices) {
                        float vx = vertex.position.getX();
                        float vy = vertex.position.getY();
                        float vz = vertex.position.getZ();
                        if (vx < minX) minX = vx;
                        if (vy < minY) minY = vy;
                        if (vz < minZ) minZ = vz;
                        if (vx > maxX) maxX = vx;
                        if (vy > maxY) maxY = vy;
                        if (vz > maxZ) maxZ = vz;
                    }
                }

                float centerX = (minX + maxX) / 2.0F;
                float centerY = (minY + maxY) / 2.0F;
                float centerZ = (minZ + maxZ) / 2.0F;
                GlStateManager.translate(centerX * F, centerY * F, centerZ * F);

                if ((maxX - minX) > (maxY - minY)) {
                    GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
                }
            }

            if (isRight) {
                savedMatrixRight.clear();
                GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, savedMatrixRight);
                hasRight = true;
            } else {
                savedMatrixLeft.clear();
                GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, savedMatrixLeft);
                hasLeft = true;
            }

            GlStateManager.popMatrix();
            return; 
        }

        super.renderRecursively(builder, bone, red, green, blue, alpha);
    }
    
    @Override
    public void renderByItem(ItemStack itemStack) {
        hasRight = false;
        hasLeft  = false;

        super.renderByItem(itemStack);
        
        debugTickCounter++;

        AbstractClientPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return;

        boolean isSlim = player.getSkinType().equals("slim");
        Minecraft.getMinecraft().renderEngine.bindTexture(player.getLocationSkin());
        GlStateManager.disableCull();

        // --- RENDER RIGHT ARM ---
        if (hasRight) {
            GlStateManager.pushMatrix();
            GL11.glLoadMatrix(savedMatrixRight);   


            GlStateManager.scale(-1.0F, -1.0F, 1.0F);

            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F); // Pitch correction (Flips Right-Side Up)
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F); // Yaw correction (Faces Forward)
            
            armModel.renderRightArm(isSlim, F);
            GlStateManager.popMatrix();
        }

        // --- RENDER LEFT ARM ---
        if (hasLeft) {
            GlStateManager.pushMatrix();
            GL11.glLoadMatrix(savedMatrixLeft);

            GlStateManager.scale(-1.0F, -1.0F, 1.0F);

            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F); // Pitch correction (Flips Right-Side Up)
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F); // Yaw correction (Faces Forward)
            
            armModel.renderLeftArm(isSlim, F);
            GlStateManager.popMatrix();
        }

        GlStateManager.enableCull();
    }
}