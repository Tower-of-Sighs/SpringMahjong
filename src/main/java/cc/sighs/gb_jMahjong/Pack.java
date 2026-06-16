package cc.sighs.gb_jMahjong;

import java.util.ArrayList;
import java.util.List;

public class Pack {
    public static final int PACK_TYPE_INVALID = 0;
    public static final int PACK_TYPE_SHUNZI = 1;
    public static final int PACK_TYPE_KEZI = 2;
    public static final int PACK_TYPE_GANG = 3;
    public static final int PACK_TYPE_JIANG = 4;
    public static final int PACK_TYPE_ZUHELONG = 5;

    public static final long[] ZUHELONG_BITMAP = {
            0L,
            bit(Tile.TILE_1m) | bit(Tile.TILE_4m) | bit(Tile.TILE_7m) |
                    bit(Tile.TILE_2s) | bit(Tile.TILE_5s) | bit(Tile.TILE_8s) |
                    bit(Tile.TILE_3p) | bit(Tile.TILE_6p) | bit(Tile.TILE_9p),
            bit(Tile.TILE_1m) | bit(Tile.TILE_4m) | bit(Tile.TILE_7m) |
                    bit(Tile.TILE_3s) | bit(Tile.TILE_6s) | bit(Tile.TILE_9s) |
                    bit(Tile.TILE_2p) | bit(Tile.TILE_5p) | bit(Tile.TILE_8p),
            bit(Tile.TILE_2m) | bit(Tile.TILE_5m) | bit(Tile.TILE_8m) |
                    bit(Tile.TILE_1s) | bit(Tile.TILE_4s) | bit(Tile.TILE_7s) |
                    bit(Tile.TILE_3p) | bit(Tile.TILE_6p) | bit(Tile.TILE_9p),
            bit(Tile.TILE_2m) | bit(Tile.TILE_5m) | bit(Tile.TILE_8m) |
                    bit(Tile.TILE_3s) | bit(Tile.TILE_6s) | bit(Tile.TILE_9s) |
                    bit(Tile.TILE_1p) | bit(Tile.TILE_4p) | bit(Tile.TILE_7p),
            bit(Tile.TILE_3m) | bit(Tile.TILE_6m) | bit(Tile.TILE_9m) |
                    bit(Tile.TILE_1s) | bit(Tile.TILE_4s) | bit(Tile.TILE_7s) |
                    bit(Tile.TILE_2p) | bit(Tile.TILE_5p) | bit(Tile.TILE_8p),
            bit(Tile.TILE_3m) | bit(Tile.TILE_6m) | bit(Tile.TILE_9m) |
                    bit(Tile.TILE_2s) | bit(Tile.TILE_5s) | bit(Tile.TILE_8s) |
                    bit(Tile.TILE_1p) | bit(Tile.TILE_4p) | bit(Tile.TILE_7p)
    };

    private static long bit(int t) {
        return 1L << t;
    }

    private int type;
    private Tile tile;
    private int zuhelongType;
    private int offer;

    public Pack() {
        this(PACK_TYPE_INVALID, new Tile(), 0, 0);
    }

    public Pack(int type, Tile tile) {
        this(type, tile, 0, 0);
    }

    public Pack(int type, Tile tile, int zuhelongType, int offer) {
        this.type = type;
        this.tile = tile;
        this.zuhelongType = zuhelongType;
        this.offer = offer;
    }

    public boolean isValid() {
        return type != PACK_TYPE_INVALID;
    }

    public int getType() {
        return type;
    }

    public Tile getMiddleTile() {
        return tile;
    }

    public int getZuhelongType() {
        return zuhelongType;
    }

    public long getZuhelongBitmap() {
        return ZUHELONG_BITMAP[zuhelongType];
    }

    public int getOffer() {
        return offer;
    }

    public boolean isAnshou() {
        return offer == 0 || offer == -1;
    }

    public boolean haveLastTile() {
        return offer < 0;
    }

    public boolean isShunzi() {
        return type == PACK_TYPE_SHUNZI;
    }

    public boolean isKezi() {
        return type == PACK_TYPE_KEZI;
    }

    public boolean isGang() {
        return type == PACK_TYPE_GANG;
    }

    public boolean isKeGang() {
        return isKezi() || isGang();
    }

    public boolean isJiang() {
        return type == PACK_TYPE_JIANG;
    }

    public boolean isZuhelong() {
        return type == PACK_TYPE_ZUHELONG;
    }

    public void setOffer(int offer) {
        this.offer = offer;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Tile> getAllTile() {
        List<Tile> ret = new ArrayList<>();
        switch (type) {
            case PACK_TYPE_SHUNZI:
                ret.add(tile.pred());
                ret.add(new Tile(tile));
                ret.add(tile.succ());
                break;
            case PACK_TYPE_GANG:
                ret.add(new Tile(tile));
            case PACK_TYPE_KEZI:
                ret.add(new Tile(tile));
            case PACK_TYPE_JIANG:
                ret.add(new Tile(tile));
                ret.add(new Tile(tile));
                break;
            case PACK_TYPE_ZUHELONG:
                for (int i = Tile.TILE_1m; i <= Tile.TILE_9s; i++) {
                    if ((bit(i) & getZuhelongBitmap()) != 0L) {
                        ret.add(new Tile(i));
                    }
                }
                break;
            default:
                break;
        }
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pack other)) {
            return false;
        }
        return type == other.type && tile.equals(other.tile);
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + tile.hashCode();
        return result;
    }
}

