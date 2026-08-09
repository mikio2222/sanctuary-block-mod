package net.sanctuaryblock.block.entity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.sanctuaryblock.block.ModBlocks;

public class ModBlockEntities {

	public static final BlockEntityType<SanctuaryBlockEntity> SANCTUARY_BE = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			Identifier.of("sanctuaryblock", "sanctuary_block_entity"),
			BlockEntityType.Builder.create(SanctuaryBlockEntity::new, ModBlocks.SANCTUARY_BLOCK).build()
	);

	public static void initialize() {
		// Triggers static field initialization above.
	}
}
