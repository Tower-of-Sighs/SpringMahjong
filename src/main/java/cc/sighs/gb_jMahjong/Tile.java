package cc.sighs.gb_jMahjong;

public class Tile implements Comparable<Tile> {
    public static final int TILE_INVALID = 0;
    public static final int TILE_1m = 1;
    public static final int TILE_2m = 2;
    public static final int TILE_3m = 3;
    public static final int TILE_4m = 4;
    public static final int TILE_5m = 5;
    public static final int TILE_6m = 6;
    public static final int TILE_7m = 7;
    public static final int TILE_8m = 8;
    public static final int TILE_9m = 9;
    public static final int TILE_1s = 10;
    public static final int TILE_2s = 11;
    public static final int TILE_3s = 12;
    public static final int TILE_4s = 13;
    public static final int TILE_5s = 14;
    public static final int TILE_6s = 15;
    public static final int TILE_7s = 16;
    public static final int TILE_8s = 17;
    public static final int TILE_9s = 18;
    public static final int TILE_1p = 19;
    public static final int TILE_2p = 20;
    public static final int TILE_3p = 21;
    public static final int TILE_4p = 22;
    public static final int TILE_5p = 23;
    public static final int TILE_6p = 24;
    public static final int TILE_7p = 25;
    public static final int TILE_8p = 26;
    public static final int TILE_9p = 27;
    public static final int TILE_E = 28;
    public static final int TILE_S = 29;
    public static final int TILE_W = 30;
    public static final int TILE_N = 31;
    public static final int TILE_C = 32;
    public static final int TILE_F = 33;
    public static final int TILE_P = 34;
    public static final int TILE_MEI = 35;
    public static final int TILE_LAN = 36;
    public static final int TILE_ZHU = 37;
    public static final int TILE_JU = 38;
    public static final int TILE_CHU = 39;
    public static final int TILE_XIA = 40;
    public static final int TILE_QIU = 41;
    public static final int TILE_DONG = 42;
    public static final int TILE_BAIDA = 43;
    public static final int TILE_MAJIANG = 44;
    public static final int TILE_SIZE = 43;

    public static final int SUIT_INVALID = 0;
    public static final int SUIT_WAN = 1;
    public static final int SUIT_TIAO = 2;
    public static final int SUIT_BING = 3;
    public static final int SUIT_HUA = 4;
    public static final int SUIT_FENG = 5;
    public static final int SUIT_JIAN = 6;

    public static final int RANK_INVALID = 0;
    public static final int RANK_1 = 1;
    public static final int RANK_2 = 2;
    public static final int RANK_3 = 3;
    public static final int RANK_4 = 4;
    public static final int RANK_5 = 5;
    public static final int RANK_6 = 6;
    public static final int RANK_7 = 7;
    public static final int RANK_8 = 8;
    public static final int RANK_9 = 9;

    public static final char TILE_CHAR_INVALID = ' ';
    public static final char TILE_CHAR_WAN = 'm';
    public static final char TILE_CHAR_TIAO = 's';
    public static final char TILE_CHAR_BING = 'p';
    public static final char TILE_CHAR_E = 'E';
    public static final char TILE_CHAR_S = 'S';
    public static final char TILE_CHAR_W = 'W';
    public static final char TILE_CHAR_N = 'N';
    public static final char TILE_CHAR_C = 'C';
    public static final char TILE_CHAR_F = 'F';
    public static final char TILE_CHAR_P = 'P';
    public static final char TILE_CHAR_MEI = 'a';
    public static final char TILE_CHAR_LAN = 'b';
    public static final char TILE_CHAR_ZHU = 'c';
    public static final char TILE_CHAR_JU = 'd';
    public static final char TILE_CHAR_CHU = 'e';
    public static final char TILE_CHAR_XIA = 'f';
    public static final char TILE_CHAR_QIU = 'g';
    public static final char TILE_CHAR_DONG = 'h';

    private static long bit(int t) {
        return 1L << t;
    }

    public static final long TILE_TYPE_BITMAP_WAN =
            bit(TILE_1m) | bit(TILE_2m) | bit(TILE_3m) | bit(TILE_4m) | bit(TILE_5m) | bit(TILE_6m) | bit(TILE_7m) | bit(TILE_8m) | bit(TILE_9m);
    public static final long TILE_TYPE_BITMAP_TIAO =
            bit(TILE_1s) | bit(TILE_2s) | bit(TILE_3s) | bit(TILE_4s) | bit(TILE_5s) | bit(TILE_6s) | bit(TILE_7s) | bit(TILE_8s) | bit(TILE_9s);
    public static final long TILE_TYPE_BITMAP_BING =
            bit(TILE_1p) | bit(TILE_2p) | bit(TILE_3p) | bit(TILE_4p) | bit(TILE_5p) | bit(TILE_6p) | bit(TILE_7p) | bit(TILE_8p) | bit(TILE_9p);
    public static final long TILE_TYPE_BITMAP_SHU =
            TILE_TYPE_BITMAP_WAN | TILE_TYPE_BITMAP_TIAO | TILE_TYPE_BITMAP_BING;
    public static final long TILE_TYPE_BITMAP_FENG =
            bit(TILE_E) | bit(TILE_S) | bit(TILE_W) | bit(TILE_N);
    public static final long TILE_TYPE_BITMAP_JIAN =
            bit(TILE_C) | bit(TILE_F) | bit(TILE_P);
    public static final long TILE_TYPE_BITMAP_ZI =
            TILE_TYPE_BITMAP_FENG | TILE_TYPE_BITMAP_JIAN;
    public static final long TILE_TYPE_BITMAP_MEANINGFUL =
            TILE_TYPE_BITMAP_SHU | TILE_TYPE_BITMAP_ZI;
    public static final long TILE_TYPE_BITMAP_YAOJIU =
            TILE_TYPE_BITMAP_ZI | bit(TILE_1m) | bit(TILE_9m) | bit(TILE_1s) | bit(TILE_9s) | bit(TILE_1p) | bit(TILE_9p);
    public static final long TILE_TYPE_BITMAP_LV =
            bit(TILE_2s) | bit(TILE_3s) | bit(TILE_4s) | bit(TILE_6s) | bit(TILE_8s) | bit(TILE_F);
    public static final long TILE_TYPE_BITMAP_QUANDA =
            bit(TILE_7m) | bit(TILE_8m) | bit(TILE_9m) |
                    bit(TILE_7s) | bit(TILE_8s) | bit(TILE_9s) |
                    bit(TILE_7p) | bit(TILE_8p) | bit(TILE_9p);
    public static final long TILE_TYPE_BITMAP_QUANZHONG =
            bit(TILE_4m) | bit(TILE_5m) | bit(TILE_6m) |
                    bit(TILE_4s) | bit(TILE_5s) | bit(TILE_6s) |
                    bit(TILE_4p) | bit(TILE_5p) | bit(TILE_6p);
    public static final long TILE_TYPE_BITMAP_QUANXIAO =
            bit(TILE_1m) | bit(TILE_2m) | bit(TILE_3m) |
                    bit(TILE_1s) | bit(TILE_2s) | bit(TILE_3s) |
                    bit(TILE_1p) | bit(TILE_2p) | bit(TILE_3p);
    public static final long TILE_TYPE_BITMAP_DAYUWU =
            TILE_TYPE_BITMAP_QUANDA | bit(TILE_6m) | bit(TILE_6s) | bit(TILE_6p);
    public static final long TILE_TYPE_BITMAP_XIAOYUWU =
            TILE_TYPE_BITMAP_QUANXIAO | bit(TILE_4m) | bit(TILE_4s) | bit(TILE_4p);
    public static final long TILE_TYPE_BITMAP_TUIBUDAO =
            bit(TILE_2s) | bit(TILE_4s) | bit(TILE_5s) | bit(TILE_6s) | bit(TILE_8s) | bit(TILE_9s) |
                    bit(TILE_1p) | bit(TILE_2p) | bit(TILE_3p) | bit(TILE_4p) | bit(TILE_5p) | bit(TILE_8p) | bit(TILE_9p) | bit(TILE_P);

    private static final String[] TILES_UTF8 = {
            "",
            "🀇", "🀈", "🀉", "🀊", "🀋", "🀌", "🀍", "🀎", "🀏",
            "🀐", "🀑", "🀒", "🀓", "🀔", "🀕", "🀖", "🀗", "🀘",
            "🀙", "🀚", "🀛", "🀜", "🀝", "🀞", "🀟", "🀠", "🀡",
            "🀀", "🀁", "🀂", "🀃",
            "🀄", "🀅", "🀆",
            "🀢", "🀣", "🀤", "🀥", "🀦", "🀧", "🀨", "🀩",
            "🀪", "🀫"
    };

    private static final int[] TILES_SUIT = {
            SUIT_INVALID,
            SUIT_WAN, SUIT_WAN, SUIT_WAN, SUIT_WAN, SUIT_WAN, SUIT_WAN, SUIT_WAN, SUIT_WAN, SUIT_WAN,
            SUIT_TIAO, SUIT_TIAO, SUIT_TIAO, SUIT_TIAO, SUIT_TIAO, SUIT_TIAO, SUIT_TIAO, SUIT_TIAO, SUIT_TIAO,
            SUIT_BING, SUIT_BING, SUIT_BING, SUIT_BING, SUIT_BING, SUIT_BING, SUIT_BING, SUIT_BING, SUIT_BING,
            SUIT_FENG, SUIT_FENG, SUIT_FENG, SUIT_FENG,
            SUIT_JIAN, SUIT_JIAN, SUIT_JIAN,
            SUIT_HUA, SUIT_HUA, SUIT_HUA, SUIT_HUA, SUIT_HUA, SUIT_HUA, SUIT_HUA, SUIT_HUA,
            SUIT_INVALID, SUIT_INVALID
    };

    private static final int[] TILES_RANK = {
            RANK_INVALID,
            RANK_1, RANK_2, RANK_3, RANK_4, RANK_5, RANK_6, RANK_7, RANK_8, RANK_9,
            RANK_1, RANK_2, RANK_3, RANK_4, RANK_5, RANK_6, RANK_7, RANK_8, RANK_9,
            RANK_1, RANK_2, RANK_3, RANK_4, RANK_5, RANK_6, RANK_7, RANK_8, RANK_9,
            RANK_INVALID, RANK_INVALID, RANK_INVALID, RANK_INVALID,
            RANK_INVALID, RANK_INVALID, RANK_INVALID,
            RANK_INVALID, RANK_INVALID, RANK_INVALID, RANK_INVALID, RANK_INVALID, RANK_INVALID, RANK_INVALID, RANK_INVALID,
            RANK_INVALID, RANK_INVALID
    };

    private static final char[] TILES_SUIT_CHAR = {
            TILE_CHAR_INVALID,
            TILE_CHAR_WAN, TILE_CHAR_WAN, TILE_CHAR_WAN, TILE_CHAR_WAN, TILE_CHAR_WAN, TILE_CHAR_WAN, TILE_CHAR_WAN, TILE_CHAR_WAN, TILE_CHAR_WAN,
            TILE_CHAR_TIAO, TILE_CHAR_TIAO, TILE_CHAR_TIAO, TILE_CHAR_TIAO, TILE_CHAR_TIAO, TILE_CHAR_TIAO, TILE_CHAR_TIAO, TILE_CHAR_TIAO, TILE_CHAR_TIAO,
            TILE_CHAR_BING, TILE_CHAR_BING, TILE_CHAR_BING, TILE_CHAR_BING, TILE_CHAR_BING, TILE_CHAR_BING, TILE_CHAR_BING, TILE_CHAR_BING, TILE_CHAR_BING,
            TILE_CHAR_E, TILE_CHAR_S, TILE_CHAR_W, TILE_CHAR_N,
            TILE_CHAR_C, TILE_CHAR_F, TILE_CHAR_P,
            TILE_CHAR_MEI, TILE_CHAR_LAN, TILE_CHAR_ZHU, TILE_CHAR_JU, TILE_CHAR_CHU, TILE_CHAR_XIA, TILE_CHAR_QIU, TILE_CHAR_DONG,
            TILE_CHAR_INVALID, TILE_CHAR_INVALID
    };

    private final int id;
    private int drawFlag;

    public Tile() {
        this(TILE_INVALID, 0);
    }

    public Tile(int id) {
        this(id, 0);
    }

    public Tile(int id, int drawFlag) {
        this.id = id;
        this.drawFlag = drawFlag;
    }

    public Tile(Tile other) {
        this.id = other.id;
        this.drawFlag = other.drawFlag;
    }

    public int getId() {
        return id;
    }

    public int getDrawFlag() {
        return drawFlag;
    }

    public void setZimo() {
        drawFlag = 1;
    }

    public void setChonghu() {
        drawFlag = 2;
    }

    public void resetDrawFlag() {
        drawFlag = 0;
    }

    public boolean isZimo() {
        return drawFlag == 1;
    }

    public boolean isChonghu() {
        return drawFlag == 2;
    }

    public Tile pred() {
        return new Tile(id - 1, drawFlag);
    }

    public Tile succ() {
        return new Tile(id + 1, drawFlag);
    }

    public Tile getTileUsingOffset(int offset) {
        return new Tile(id + offset, drawFlag);
    }

    public int suit() {
        return TILES_SUIT[id];
    }

    public int rank() {
        return TILES_RANK[id];
    }

    public boolean isShu() {
        long bitmap = getBitmap();
        return (bitmap & TILE_TYPE_BITMAP_SHU) == bitmap;
    }

    public boolean isZi() {
        long bitmap = getBitmap();
        return (bitmap & TILE_TYPE_BITMAP_ZI) == bitmap;
    }

    public boolean isFeng() {
        long bitmap = getBitmap();
        return (bitmap & TILE_TYPE_BITMAP_FENG) == bitmap;
    }

    public boolean isJian() {
        long bitmap = getBitmap();
        return (bitmap & TILE_TYPE_BITMAP_JIAN) == bitmap;
    }

    public boolean isYaojiu() {
        long bitmap = getBitmap();
        return (bitmap & TILE_TYPE_BITMAP_YAOJIU) == bitmap;
    }

    public boolean isHua() {
        return suit() == SUIT_HUA;
    }

    public String utf8() {
        return TILES_UTF8[id];
    }

    public char rankChar() {
        return (char) ('0' + rank());
    }

    public char suitChar() {
        return TILES_SUIT_CHAR[id];
    }

    public char tileChar() {
        return isShu() ? rankChar() : suitChar();
    }

    public long getBitmap() {
        return 1L << id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Tile other)) {
            return false;
        }
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(Tile o) {
        return Integer.compare(this.id, o.id);
    }
}

