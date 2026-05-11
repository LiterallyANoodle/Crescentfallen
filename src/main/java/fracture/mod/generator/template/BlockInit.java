package fracture.mod.generator.template;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.javapoet.FieldSpec;

import fracture.mod.generator.GeneratorTarget;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

@GeneratorTarget
public class BlockInit {
	
	@Literal
	public static final List<Block> BLOCKS = new ArrayList<Block>();
	
	@Template
	public static FieldSpec metaBlockRegistration(JsonNode root) {
		
		String name = root.path("name").asText();
		String properName = name.substring(0, 1).toUpperCase() + name.substring(1);
		
		return FieldSpec.builder(Block.class, "BLOCK_" + name.toUpperCase())
		.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
		.initializer("new $T($S, $T.$L)", "BlockOf" + properName, "block_" + name, Material.class, root.path("material").asText().toUpperCase())
		.build();
	}
	
}
