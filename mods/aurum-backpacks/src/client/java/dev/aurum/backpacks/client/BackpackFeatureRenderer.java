package dev.aurum.backpacks.client;

import dev.aurum.backpacks.BackpackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class BackpackFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    public BackpackFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!entity.hasCustomName() && entity instanceof net.minecraft.client.network.AbstractClientPlayerEntity player) {
            ItemStack backpack = ItemStack.EMPTY;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (stack.getItem() instanceof BackpackItem) {
                    backpack = stack;
                    break;
                }
            }
            if (!backpack.isEmpty()) {
                matrices.push();
                
                // chest attachment
                if (player.isSneaking()) {
                    matrices.translate(0.0F, 0.2F, 0.0F);
                    matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(28.64789F));
                }
                matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
                matrices.translate(0.0F, 0.4F, -0.25F);
                matrices.scale(0.65f, 0.65f, 0.65f);

                ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
                itemRenderer.renderItem(backpack, ModelTransformationMode.FIXED, light, net.minecraft.client.render.OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 0);
                
                matrices.pop();
            }
        }
    }
}
