package dev.coder2195.silk_chests.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.coder2195.silk_chests.SilkChests;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Containers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.world.level.block.ShulkerBoxBlock.CONTENTS;

@Mixin(BlockBehaviour.class)
public class BlockBehaviorMixin {
  @Definition(id = "builder", local = @Local(type = net.minecraft.world.level.storage.loot.LootParams.Builder.class, argsOnly = true))
  @Expression("@(builder).?(?, ?)")
  @ModifyExpressionValue(method = "getDrops", at = @At("MIXINEXTRAS:EXPRESSION"))
  private LootParams.Builder addChestSilkTouch(LootParams.Builder builder) {
    BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
    if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
      var level = builder.getLevel();
      var tool = builder.getParameter(LootContextParams.TOOL).get(DataComponents.ENCHANTMENTS);
      var silkTouch = tool != null && level.registryAccess().registry(Registries.ENCHANTMENT).flatMap(registry -> registry.getHolder(Enchantments.SILK_TOUCH).map(tool::getLevel)).orElse(0) != 0;
      if (silkTouch)
        builder = builder.withDynamicDrop(CONTENTS, consumer -> {
          for (int i = 0; i < chestBlockEntity.getContainerSize(); i++) {
            consumer.accept(chestBlockEntity.getItem(i));
            SilkChests.LOGGER.info("Added item {} to loot table", chestBlockEntity.getItem(i));
          }
        });
      else Containers.dropContents(chestBlockEntity.getLevel(), chestBlockEntity.getBlockPos(), chestBlockEntity);
    }
    return builder;
  }
}