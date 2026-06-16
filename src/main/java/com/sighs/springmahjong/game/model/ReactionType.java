package com.sighs.springmahjong.game.model;

public enum ReactionType {
    WIN(100),
    KONG(80),
    PUNG(60),
    CHI(40),
    PASS(0);

    private final int priority;

    ReactionType(int priority) { this.priority = priority; }
    public int priority() { return priority; }
}
