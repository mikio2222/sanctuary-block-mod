package net.sanctuaryblock.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ModBlocks {

	private static final String MOD_ID = "sanctuaryblock";

	public static final Block SANCTUARY_BLOCK = registerBlock(
			"sanctuary_block",
			new SanctuaryBlock(AbstractBlock.Settings.create()
					.mapColor(MapColor.PALE_PURPLE)
					.strength(3.0f, 6.0f)
					.sounds(net.minecraft.sound.BlockSoundGroup.AMETHYST_BLOCK)
					.nonOpaque()
					.pistonBehavior(net.minecraft.block.piston.PistonBehavior.BLOCK))
	);

	private static Block registerBlock(String name, Block block) {
		registerBlockItem(name, block);
		return Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, name), block);
	}

	private static Item registerBlockItem(String name, Block block) {
		return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name),
				new BlockItem(block, new Item.Settings()));
	}

	public static void initialize() {
		// Add the Sanctuary Block to the vanilla "Functional Blocks" creative tab.
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries ->
				entries.add(SANCTUARY_BLOCK.asItem()));
	}
}
