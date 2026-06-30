package fracture.mod.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelCustomArm extends ModelBase {

	//ACTIVE WIP
	
    private final ModelRenderer bipedRightArm;
    private final ModelRenderer bipedRightArmwear;
    private final ModelRenderer bipedLeftArm;
    private final ModelRenderer bipedLeftArmwear;

    private final ModelRenderer bipedRightArmSlim;
    private final ModelRenderer bipedRightArmwearSlim;
    private final ModelRenderer bipedLeftArmSlim;
    private final ModelRenderer bipedLeftArmwearSlim;

    public ModelCustomArm() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        // Steve arms
        this.bipedRightArm = new ModelRenderer(this, 40, 16);
        this.bipedRightArm.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4); 
        this.bipedRightArm.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
        this.bipedRightArmwear.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4, 0.25F);
        this.bipedRightArmwear.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedLeftArm = new ModelRenderer(this, 32, 48);
        this.bipedLeftArm.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4);
        this.bipedLeftArm.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
        this.bipedLeftArmwear.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4, 0.25F);
        this.bipedLeftArmwear.setRotationPoint(0.0F, 0.0F, 0.0F);

        // Alex arms
        this.bipedRightArmSlim = new ModelRenderer(this, 40, 16);
        this.bipedRightArmSlim.addBox(-1.5F, -6.0F, -2.0F, 3, 12, 4);
        this.bipedRightArmSlim.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedRightArmwearSlim = new ModelRenderer(this, 40, 32);
        this.bipedRightArmwearSlim.addBox(-1.5F, -6.0F, -2.0F, 3, 12, 4, 0.25F);
        this.bipedRightArmwearSlim.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedLeftArmSlim = new ModelRenderer(this, 32, 48);
        this.bipedLeftArmSlim.addBox(-1.5F, -6.0F, -2.0F, 3, 12, 4);
        this.bipedLeftArmSlim.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedLeftArmwearSlim = new ModelRenderer(this, 48, 48);
        this.bipedLeftArmwearSlim.addBox(-1.5F, -6.0F, -2.0F, 3, 12, 4, 0.25F);
        this.bipedLeftArmwearSlim.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    public void renderRightArm(boolean isSlim, float scale) {
        if (isSlim) {
            this.bipedRightArmSlim.render(scale);
            this.bipedRightArmwearSlim.render(scale);
        } else {
            this.bipedRightArm.render(scale);
            this.bipedRightArmwear.render(scale);
        }
    }

    public void renderLeftArm(boolean isSlim, float scale) {
        if (isSlim) {
            this.bipedLeftArmSlim.render(scale);
            this.bipedLeftArmwearSlim.render(scale);
        } else {
            this.bipedLeftArm.render(scale);
            this.bipedLeftArmwear.render(scale);
        }
    }
}