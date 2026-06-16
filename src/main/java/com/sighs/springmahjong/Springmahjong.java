package com.sighs.springmahjong;

import com.mojang.logging.LogUtils;
import com.sighs.springmahjong.block.MahjongChairBlock;
import com.sighs.springmahjong.block.MahjongTableBlock;
import com.sighs.springmahjong.block.MahjongTableEntity;
import com.sighs.springmahjong.block.TestMahjongTableBlock;
import com.sighs.springmahjong.entity.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.Set;
import java.util.function.Supplier;

@Mod(Springmahjong.MODID)
public class Springmahjong {
    public static final String MODID = "springmahjong";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    // Blocks — using registerBlock which properly creates blocks during registration
    public static final DeferredBlock<MahjongTableBlock> MAHJONG_TABLE =
        BLOCKS.registerBlock("mahjong_table", MahjongTableBlock::new,
            props -> props.strength(2.0f).noOcclusion());
    public static final DeferredBlock<TestMahjongTableBlock> TEST_MAHJONG_TABLE =
        BLOCKS.registerBlock("test_mahjong_table", TestMahjongTableBlock::new,
            props -> props.strength(2.0f).noOcclusion());
    public static final DeferredBlock<MahjongChairBlock> MAHJONG_CHAIR =
        BLOCKS.registerBlock("mahjong_chair", MahjongChairBlock::new,
            props -> props.strength(2.0f).noOcclusion().noCollision());
    public static final DeferredBlock<Block> MAHJONG_TILE =
        BLOCKS.registerSimpleBlock("mahjong_tile",
            props -> props.strength(0.1f).noOcclusion().noCollision());

    // Block items
    public static final DeferredItem<BlockItem> MAHJONG_TABLE_ITEM =
        ITEMS.registerSimpleBlockItem("mahjong_table", MAHJONG_TABLE);
    public static final DeferredItem<BlockItem> TEST_MAHJONG_TABLE_ITEM =
        ITEMS.registerSimpleBlockItem("test_mahjong_table", TEST_MAHJONG_TABLE);
    public static final DeferredItem<BlockItem> MAHJONG_CHAIR_ITEM =
        ITEMS.registerSimpleBlockItem("mahjong_chair", MAHJONG_CHAIR);

    // Block entity type
    public static final Supplier<BlockEntityType<MahjongTableEntity>> TABLE_ENTITY =
        BLOCK_ENTITIES.register("mahjong_table", () ->
            new BlockEntityType<>(MahjongTableEntity::new,
                Set.of(MAHJONG_TABLE.get(), TEST_MAHJONG_TABLE.get())));

    // Creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
        CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MODID))
            .icon(() -> MAHJONG_TABLE_ITEM.get().getDefaultInstance())
            .displayItems((params, output) -> {
                output.accept(MAHJONG_TABLE_ITEM.get());
                output.accept(TEST_MAHJONG_TABLE_ITEM.get());
                output.accept(MAHJONG_CHAIR_ITEM.get());
            })
            .build());

    public Springmahjong(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);

        modEventBus.addListener(this::onBlockEntityTypeAddBlocks);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void onBlockEntityTypeAddBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(TABLE_ENTITY.get(), MAHJONG_TABLE.get(), TEST_MAHJONG_TABLE.get());
    }
}
