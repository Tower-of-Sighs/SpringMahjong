package com.sighs.springmahjong;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Springmahjong.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MIN_FAN_TO_WIN = BUILDER
        .comment("Minimum fan required to win (璧峰拰鐣? 鍥芥爣榛樿 8)")
        .defineInRange("minFanToWin", 8, 0, 88);

    private static final ModConfigSpec.IntValue DISCARD_TIME_SECONDS = BUILDER
        .comment("Time limit for discarding in seconds (0 = no limit)")
        .defineInRange("discardTimeSeconds", 15, 0, 120);

    private static final ModConfigSpec.IntValue REACTION_TIME_SECONDS = BUILDER
        .comment("Time limit for reaction in seconds (0 = no limit)")
        .defineInRange("reactionTimeSeconds", 10, 0, 120);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int minFanToWin;
    public static int discardTimeSeconds;
    public static int reactionTimeSeconds;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        minFanToWin = MIN_FAN_TO_WIN.get();
        discardTimeSeconds = DISCARD_TIME_SECONDS.get();
        reactionTimeSeconds = REACTION_TIME_SECONDS.get();
    }
}

