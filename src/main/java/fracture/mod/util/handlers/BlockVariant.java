package fracture.mod.util.handlers;

import fracture.mod.generator.GeneratorTarget;
import net.minecraft.util.IStringSerializable;


@GeneratorTarget
public class BlockVariant {
	
	public static enum BlockVariantEnum implements IStringSerializable {
		// METALS
		copper(0, "copper"),
		TIN(1, "tin");
		//IRON(2, "iron");
		//TERRAMINIUM(3, "terraminium"),
		//PLATINUM(4, "platinum");
		// URANIUM(5, "uranium"),

		
		// GEMS
		// DIAMOND(6, "diamond"),
		// TANZ(7, "tanz"),
		// EMERALD(8, "emerald"),
		// COAL(9, "coal"),
		// PLUTONIUM(10, "pluto"),

		// OTHER(h is seritonium, o is oil)
		// HSHALE(11, "hshale"),
		// OSHALE(12, "oshale"),
		// ALUMINUM(14, "aluminum"),
		// CENT(13, "centurium");

		private static final BlockVariantEnum[] META_LOOKUP = new BlockVariantEnum[values().length];
		private final int variantId;
		private final String name, unlocalizedName;

		private BlockVariantEnum(int variantId, String name) {
			this(variantId, name, name);
		}

		private BlockVariantEnum(int meta, String name, String unlocalizedName) {
			this.variantId = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;

		}

		// The first constructor is referring to second constructor, and is setting the
		// unlocalized name to the same string as the normal name.

		@Override
		public String getName() {
			return this.name;
		}

		public int getMeta() {
			return this.variantId;
		}

		public String getUnlocalizedName() {
			return this.unlocalizedName;
		}

		@Override
		public String toString() {
			return this.name();
		}

		public static BlockVariantEnum byMetadata(int variantId) {
			return META_LOOKUP[variantId];
		}

		static {
			for (BlockVariantEnum enumtype : values()) {
				META_LOOKUP[enumtype.getMeta()] = enumtype;
			}
		}
	}
}
