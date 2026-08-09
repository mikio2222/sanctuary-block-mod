package net.sanctuaryblock.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ModBlocks {

	public static final Block SANCTUARY_BLOCK = register(
			"sanctuary_block",
			settings -> new SanctuaryBlock(settings.mapColor(MapColor.PALE_PURPLE).strength(3.0f, 6.0f)
					.sounds(net.minecraft.sound.BlockSoundGroup.AMETHYST)
					.nonOpaque()
					.pistonBehavior(net.minecraft.block.piston.PistonBehavior.BLOCK)),
			true
	);

	private static Block register(String path, java.util.function.Function<AbstractBlock.Settings, Block> factory, boolean withItem) {
		RegistryKey<Block> blockKey = keyOf(path);
		Block block = factory.apply(AbstractBlock.Settings.create().registryKey(blockKey));
		Registry.register(Registries.BLOCK, blockKey, block);

		if (withItem) {
			RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("sanctuaryblock", path));
			BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey());
			Registry.register(Registries.ITEM, itemKey, blockItem);
		}

		return block;
	}

	private static RegistryKey<Block> keyOf(String path) {
		return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("sanctuaryblock", path));
	}

	public static void initialize() {
		// Add the Sanctuary Block to the vanilla "Functional Blocks" creative tab.
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries ->
				entries.add(SANCTUARY_BLOCK.asItem()));
	}
}
