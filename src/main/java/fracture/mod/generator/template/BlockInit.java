package fracture.mod.generator.template;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.FieldSpec;

import fracture.mod.generator.ItemRegistrarTarget;
import fracture.mod.init.BlockOfCenturium;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

@ItemRegistrarTarget
public class BlockInit {
	
	public static final List<Block> BLOCKS = new ArrayList<Block>();
	
	public static final Block BLOCK_CENTURIUM = new BlockOfCenturium("block_centurium", Material.IRON);
	
	@Template
	public FieldSpec metaBlockRegistration = FieldSpec.builder(Block.class, "$T")
		.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
		.initializer("new $T($S, $T)")
		.build();
	
}
