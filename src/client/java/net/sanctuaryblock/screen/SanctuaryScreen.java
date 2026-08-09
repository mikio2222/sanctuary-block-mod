package net.sanctuaryblock.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.sanctuaryblock.block.SanctuaryBlock;
import net.sanctuaryblock.network.SanctuaryScreenHandler;
import net.sanctuaryblock.network.SanctuaryUpdateC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SanctuaryScreen extends HandledScreen<SanctuaryScreenHandler> {

	private final BlockPos sanctuaryPos;

	private boolean active;
	private boolean explosionProtection;
	private boolean blockPlacement;
	private boolean blockBreaking;
	private boolean mobSpawning;
	private int horizontalRadius;
	private int verticalRadius;

	private ButtonWidget activeButton;
	private ButtonWidget explosionButton;
	private ButtonWidget placementButton;
	private ButtonWidget breakingButton;
	private ButtonWidget spawningButton;
	private SliderWidget horizontalSlider;
	private SliderWidget verticalSlider;

	public SanctuaryScreen(SanctuaryScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.sanctuaryPos = handler.getSanctuaryPos();
		this.backgroundWidth = 256;
		this.backgroundHeight = 220;

		SanctuaryBlock.SanctuaryData data = SanctuaryBlock.getSanctuaryData(this.sanctuaryPos);
		this.active = data.active;
		this.explosionProtection = data.explosionProtection;
		this.blockPlacement = data.blockPlacement;
		this.blockBreaking = data.blockBreaking;
		this.mobSpawning = data.mobSpawning;
		this.horizontalRadius = data.horizontalRadius;
		this.verticalRadius = data.verticalRadius;
	}

	@Override
	protected void init() {
		super.init();
		int centerX = this.x + this.backgroundWidth / 2;
		int startY = this.y + 30;

		this.activeButton = ButtonWidget.builder(
				Text.literal(active ? "Active" : "Inactive").formatted(active ? Formatting.GREEN : Formatting.RED),
				button -> {
					active = !active;
					updateActiveButton();
					sendUpdate();
				}).dimensions(centerX - 50, startY, 100, 20).build();
		this.addDrawableChild(this.activeButton);

		this.explosionButton = ButtonWidget.builder(
				Text.literal("Explosions: " + (explosionProtection ? "Blocked" : "Allowed"))
						.formatted(explosionProtection ? Formatting.RED : Formatting.GREEN),
				button -> {
					explosionProtection = !explosionProtection;
					updateExplosionButton();
					sendUpdate();
				}).dimensions(centerX - 60, startY + 25, 120, 20).build();
		this.addDrawableChild(this.explosionButton);

		this.placementButton = ButtonWidget.builder(
				Text.literal("Placement: " + (blockPlacement ? "Blocked" : "Allowed"))
						.formatted(blockPlacement ? Formatting.RED : Formatting.GREEN),
				button -> {
					blockPlacement = !blockPlacement;
					updatePlacementButton();
					sendUpdate();
				}).dimensions(centerX - 60, startY + 50, 120, 20).build();
		this.addDrawableChild(this.placementButton);

		this.breakingButton = ButtonWidget.builder(
				Text.literal("Breaking: " + (blockBreaking ? "Blocked" : "Allowed"))
						.formatted(blockBreaking ? Formatting.RED : Formatting.GREEN),
				button -> {
					blockBreaking = !blockBreaking;
					updateBreakingButton();
					sendUpdate();
				}).dimensions(centerX - 60, startY + 75, 120, 20).build();
		this.addDrawableChild(this.breakingButton);

		this.spawningButton = ButtonWidget.builder(
				Text.literal("Mob Spawning: " + (mobSpawning ? "Blocked" : "Allowed"))
						.formatted(mobSpawning ? Formatting.RED : Formatting.GREEN),
				button -> {
					mobSpawning = !mobSpawning;
					updateSpawningButton();
					sendUpdate();
				}).dimensions(centerX - 65, startY + 100, 130, 20).build();
		this.addDrawableChild(this.spawningButton);

		this.horizontalSlider = new SliderWidget(centerX - 60, startY + 125, 120, 20,
				Text.literal("H-Radius: " + horizontalRadius), (horizontalRadius - 1) / 99.0) {
			@Override
			protected void updateMessage() {
				horizontalRadius = (int) Math.round(this.value * 99.0) + 1;
				this.setMessage(Text.literal("H-Radius: " + horizontalRadius));
			}

			@Override
			protected void applyValue() {
				sendUpdate();
			}
		};
		this.addDrawableChild(this.horizontalSlider);

		this.verticalSlider = new SliderWidget(centerX - 60, startY + 150, 120, 20,
				Text.literal("V-Radius: " + verticalRadius), (verticalRadius - 1) / 99.0) {
			@Override
			protected void updateMessage() {
				verticalRadius = (int) Math.round(this.value * 99.0) + 1;
				this.setMessage(Text.literal("V-Radius: " + verticalRadius));
			}

			@Override
			protected void applyValue() {
				sendUpdate();
			}
		};
		this.addDrawableChild(this.verticalSlider);
	}

	private void updateActiveButton() {
		activeButton.setMessage(Text.literal(active ? "Active" : "Inactive").formatted(active ? Formatting.GREEN : Formatting.RED));
	}

	private void updateExplosionButton() {
		explosionButton.setMessage(Text.literal("Explosions: " + (explosionProtection ? "Blocked" : "Allowed"))
				.formatted(explosionProtection ? Formatting.RED : Formatting.GREEN));
	}

	private void updatePlacementButton() {
		placementButton.setMessage(Text.literal("Placement: " + (blockPlacement ? "Blocked" : "Allowed"))
				.formatted(blockPlacement ? Formatting.RED : Formatting.GREEN));
	}

	private void updateBreakingButton() {
		breakingButton.setMessage(Text.literal("Breaking: " + (blockBreaking ? "Blocked" : "Allowed"))
				.formatted(blockBreaking ? Formatting.RED : Formatting.GREEN));
	}

	private void updateSpawningButton() {
		spawningButton.setMessage(Text.literal("Mob Spawning: " + (mobSpawning ? "Blocked" : "Allowed"))
				.formatted(mobSpawning ? Formatting.RED : Formatting.GREEN));
	}

	private void sendUpdate() {
		ClientPlayNetworking.send(new SanctuaryUpdateC2SPayload(
				sanctuaryPos, active, explosionProtection, blockPlacement, blockBreaking, mobSpawning,
				horizontalRadius, verticalRadius));

		SanctuaryBlock.SanctuaryData data = SanctuaryBlock.getSanctuaryData(sanctuaryPos);
		data.active = active;
		data.explosionProtection = explosionProtection;
		data.blockPlacement = blockPlacement;
		data.blockBreaking = blockBreaking;
		data.mobSpawning = mobSpawning;
		data.horizontalRadius = horizontalRadius;
		data.verticalRadius = verticalRadius;
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		context.fill(this.x, this.y, this.x + this.backgroundWidth, this.y + this.backgroundHeight, 0x88000000);
		context.drawBorder(this.x, this.y, this.backgroundWidth, this.backgroundHeight, 0xFFFFFFFF);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		Text title = Text.literal("Sanctuary Configuration").formatted(Formatting.GOLD);
		int titleWidth = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, this.x + (this.backgroundWidth - titleWidth) / 2, this.y + 10, 0xFFFFFF, false);
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
	}
}
