package com.sighs.springmahjong.entity;

import com.sighs.springmahjong.Springmahjong;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, Springmahjong.MODID);

    public static final Supplier<EntityType<SeatEntity>> SEAT =
        ENTITY_TYPES.register("seat", () -> EntityType.Builder
            .<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Springmahjong.MODID, "seat"))));

    public static void register() {
        // DeferredRegister registers during the proper registry event
    }
}
