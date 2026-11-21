package dev.aurum.economy.client.render;

import dev.aurum.economy.block.CoinPedestalBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

/**
 * Предмет на витрине парит над постаментом, плавно покачивается
 * и вращается вокруг оси — как настоящий экспонат.
 */
public class CoinPedestalRenderer implements BlockEntityRenderer<CoinPedestalBlockEntity> {
	private final ItemRenderer itemRenderer;

	public CoinPedestalRenderer(BlockEntityRendererFactory.Context ctx) {
		this.itemRenderer = ctx.getItemRenderer();
	}

	@Override
	public void render(CoinPedestalBlockEntity entity, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light, int overlay) {
		ItemStack stack = entity.getStack();
		World world = entity.getWorld();
		if (stack.isEmpty() || world == null) return;

		float time = world.getTime() + tickDelta;
		float bob = (float) Math.sin(time / 9.0f) * 0.06f;

		matrices.push();
		matrices.translate(0.5, 1.12 + bob + 0.2, 0.5);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 4.0f));
		matrices.scale(0.85f, 0.85f, 0.85f);

		int lightAbove = WorldRenderer.getLightmapCoordinates(world, entity.getPos().up());
		itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, lightAbove,
			OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, world, (int) entity.getPos().asLong());
		matrices.pop();
	}
}
