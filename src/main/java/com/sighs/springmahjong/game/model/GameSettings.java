package com.sighs.springmahjong.game.model;

public record GameSettings(
    int initialScore,
    int maxRounds,
    boolean allowSelfDrawWin,
    boolean allowDiscardWin,
    int kongSupplementCount,
    int minFanToWin,
    TimeControl timeControl,
    boolean autoFillBots,
    boolean timedBotTurns
) {
    public static final GameSettings GUOBIAO = new GameSettings(
        0, 16, true, true, 14, 8,
        TimeControl.DEFAULT, true, false
    );

    public static final GameSettings DEBUG = new GameSettings(
        0, 99, true, true, 14, 0,
        TimeControl.NO_LIMIT, true, false
    );

    public static final GameSettings RENDER_TEST = new GameSettings(
        0, 99, true, true, 14, 0,
        new TimeControl(3_000, 3_000), true, true
    );

    public record TimeControl(long discardTimeMs, long reactionTimeMs) {
        public static final TimeControl DEFAULT = new TimeControl(15_000, 10_000);
        public static final TimeControl NO_LIMIT = new TimeControl(Long.MAX_VALUE, Long.MAX_VALUE);
    }
}
