package com.sighs.springmahjong.game.model;

import javax.annotation.Nullable;

public record Reaction(
    ReactionType type,
    @Nullable MeldData data
) {
    public static Reaction pass() {
        return new Reaction(ReactionType.PASS, null);
    }

    public static Reaction of(ReactionType type) {
        return new Reaction(type, null);
    }

    public static Reaction of(ReactionType type, MeldData data) {
        return new Reaction(type, data);
    }
}
