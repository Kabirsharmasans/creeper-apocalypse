package com.creeperapocalypse.init;

import com.creeperapocalypse.CreeperApocalypse;
import com.creeperapocalypse.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ModItems {

    public static class CustomSpawnEggItem extends Item {
        private final EntityType<?> type;
        private final int primaryColor;
        private final int secondaryColor;

        public CustomSpawnEggItem(EntityType<?> type, int primaryColor, int secondaryColor, Settings settings) {
            super(settings);
            this.type = type;
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
        }

        @Override
        public ActionResult useOnBlock(ItemUsageContext context) {
            World world = context.getWorld();
            if (!(world instanceof ServerWorld)) {
                return ActionResult.SUCCESS;
            }

            ItemStack itemStack = context.getStack();
            BlockPos blockPos = context.getBlockPos();
            Direction direction = context.getSide();
            BlockState blockState = world.getBlockState(blockPos);

            BlockPos spawnPos;
            if (blockState.getCollisionShape(world, blockPos).isEmpty()) {
                spawnPos = blockPos;
            } else {
                spawnPos = blockPos.offset(direction);
            }

            EntityType<?> entityType = this.type;
            if (entityType.spawnFromItemStack((ServerWorld)world, itemStack, context.getPlayer(), spawnPos, SpawnReason.MOB_SUMMONED, true, !blockPos.equals(spawnPos) && direction == Direction.UP) != null) {
                itemStack.decrement(1);
                world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
            }

            return ActionResult.CONSUME;
        }

        public int getPrimaryColor() { return primaryColor; }
        public int getSecondaryColor() { return secondaryColor; }
    }

    // Spawn Eggs
    public static final Item MINI_CREEPER_SPAWN_EGG = registerSpawnEgg("mini_creeper_spawn_egg", ModEntities.MINI_CREEPER, 0x0DA70B, 0x44AAFF);
    public static final Item GIANT_CREEPER_SPAWN_EGG = registerSpawnEgg("giant_creeper_spawn_egg", ModEntities.GIANT_CREEPER, 0x0DA70B, 0xFF0000);
    public static final Item SPIDER_CREEPER_SPAWN_EGG = registerSpawnEgg("spider_creeper_spawn_egg", ModEntities.SPIDER_CREEPER, 0x0DA70B, 0x333333);
    public static final Item NINJA_CREEPER_SPAWN_EGG = registerSpawnEgg("ninja_creeper_spawn_egg", ModEntities.NINJA_CREEPER, 0x111111, 0x000033);
    public static final Item RAINBOW_CREEPER_SPAWN_EGG = registerSpawnEgg("rainbow_creeper_spawn_egg", ModEntities.RAINBOW_CREEPER, 0xFFFFFF, 0xFF00FF);
    public static final Item BOUNCY_CREEPER_SPAWN_EGG = registerSpawnEgg("bouncy_creeper_spawn_egg", ModEntities.BOUNCY_CREEPER, 0x0DA70B, 0x00FF00);
    public static final Item JOCKEY_CREEPER_SPAWN_EGG = registerSpawnEgg("jockey_creeper_spawn_egg", ModEntities.JOCKEY_CREEPER, 0x0DA70B, 0x8B4513);
    public static final Item HAPPY_CREEPER_SPAWN_EGG = registerSpawnEgg("happy_creeper_spawn_egg", ModEntities.HAPPY_CREEPER, 0xFF69B4, 0xFFFFFF);
    public static final Item LIGHTNING_CREEPER_SPAWN_EGG = registerSpawnEgg("lightning_creeper_spawn_egg", ModEntities.LIGHTNING_CREEPER, 0x0DA70B, 0xFFFF00);

    private static Item registerSpawnEgg(String name, EntityType<? extends MobEntity> type, int primaryColor, int secondaryColor) {
        Identifier id = Identifier.of(CreeperApocalypse.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        // Important: Must set registry key in settings to avoid Item id not set error
        return Registry.register(Registries.ITEM, id,
                new CustomSpawnEggItem(type, primaryColor, secondaryColor, new Item.Settings().registryKey(key)));
    }

    public static void register() {
        CreeperApocalypse.LOGGER.info("Registering mod items and spawn eggs...");

        try {
            // Add to creative tab
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
                content.add(MINI_CREEPER_SPAWN_EGG);
                content.add(GIANT_CREEPER_SPAWN_EGG);
                content.add(SPIDER_CREEPER_SPAWN_EGG);
                content.add(NINJA_CREEPER_SPAWN_EGG);
                content.add(RAINBOW_CREEPER_SPAWN_EGG);
                content.add(BOUNCY_CREEPER_SPAWN_EGG);
                content.add(JOCKEY_CREEPER_SPAWN_EGG);
                content.add(HAPPY_CREEPER_SPAWN_EGG);
                content.add(LIGHTNING_CREEPER_SPAWN_EGG);
            });
        } catch (Exception e) {
             CreeperApocalypse.LOGGER.error("Failed to register spawn eggs to creative tab", e);
        }
    }
}

