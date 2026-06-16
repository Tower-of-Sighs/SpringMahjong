package cc.sighs.gb_jMahjong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handtiles {
    public static final int WIND_E = Tile.TILE_E;
    public static final int WIND_S = Tile.TILE_S;
    public static final int WIND_W = Tile.TILE_W;
    public static final int WIND_N = Tile.TILE_N;

    public final List<Pack> fulu = new ArrayList<>();
    public final List<Tile> lipai = new ArrayList<>();
    public final List<Tile> huapai = new ArrayList<>();
    public final Map<Integer, Integer> fuluTable = new HashMap<>();
    public final Map<Integer, Integer> lipaiTable = new HashMap<>();
    public final Map<Integer, Integer> huapaiTable = new HashMap<>();

    private int quanfeng;
    private int menfeng;
    private int zimo;
    private int juezhang;
    private int haidi;
    private int gang;

    public long fuluBitmap() {
        long bitmap = 0L;
        for (Pack p : fulu) {
            Tile mt = p.getMiddleTile();
            switch (p.getType()) {
                case Pack.PACK_TYPE_SHUNZI:
                    bitmap |= mt.getBitmap() | mt.pred().getBitmap() | mt.succ().getBitmap();
                    break;
                case Pack.PACK_TYPE_KEZI:
                case Pack.PACK_TYPE_GANG:
                case Pack.PACK_TYPE_JIANG:
                    bitmap |= mt.getBitmap();
                    break;
                case Pack.PACK_TYPE_ZUHELONG:
                default:
                    break;
            }
        }
        return bitmap;
    }

    public long lipaiBitmap() {
        long bitmap = 0L;
        for (Tile t : lipai) {
            bitmap |= t.getBitmap();
        }
        return bitmap;
    }

    public int lipaiTileCount(Tile tile) {
        return lipaiTable.getOrDefault(tile.getId(), 0);
    }

    public int fuluTileCount(Tile tile) {
        return fuluTable.getOrDefault(tile.getId(), 0);
    }

    public int handTileCount(Tile tile) {
        return lipaiTileCount(tile) + fuluTileCount(tile);
    }

    public int huapaiCount() {
        int cnt = 0;
        for (int i = Tile.TILE_MEI; i <= Tile.TILE_DONG; i++) {
            cnt += huapaiTable.getOrDefault(i, 0);
        }
        return cnt;
    }

    public String handtilesToString() {
        StringBuilder ret = new StringBuilder();
        for (Pack p : fulu) {
            Tile middleTile = p.getMiddleTile();
            List<Tile> v = p.getAllTile();
            ret.append('[');
            for (Tile t : v) {
                ret.append(t.tileChar());
            }
            if (middleTile.isShu()) {
                ret.append(middleTile.suitChar());
            }
            if (p.getOffer() != 0) {
                ret.append(',');
                ret.append(p.getOffer());
            }
            ret.append(']');
        }
        int flagFirstNumberedTile = 1;
        for (int i = 0; i < lipai.size(); i++) {
            if (flagFirstNumberedTile == 0) {
                boolean isFourteenth = i + 1 + fulu.size() * 3 == 14;
                Tile current = lipai.get(i);
                Tile last = lipai.get(i - 1);
                if (isFourteenth || !current.isShu() || current.suit() != last.suit()) {
                    ret.append(last.suitChar());
                }
            }
            Tile t = lipai.get(i);
            flagFirstNumberedTile = t.isShu() ? 0 : 1;
            ret.append(t.tileChar());
        }
        if (flagFirstNumberedTile == 0) {
            ret.append(getLastLipai().suitChar());
        }
        ret.append('|');
        ret.append(new Tile(getQuanfeng()).tileChar());
        ret.append(new Tile(getMenfeng()).tileChar());
        ret.append(isZimo());
        ret.append(isJuezhang());
        ret.append(isHaidi());
        ret.append(isGang());
        ret.append('|');
        for (Tile t : huapai) {
            ret.append(t.tileChar());
        }
        return ret.toString();
    }

    public int stringToHandtiles(String original) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);
            if (c != ' ') {
                sb.append(c);
            }
        }
        String s = sb.toString();
        Pattern pattern = Pattern.compile("(\\[([1-9]{3,4}[msp]|[ESWNCFP]{3,4})(,[123567])?]|([ESWNCFPa-h]|[1-9]+[msp]))+(\\|([ESWN]{2}[01]{4})(\\|([a-h]{0,8}|[0-8]))?)?");
        Matcher matcher = pattern.matcher(s);
        if (!matcher.matches()) {
            return -1;
        }
        clearAndSetDefault();
        Map<Character, Integer> mp = new HashMap<>();
        mp.put(Tile.TILE_CHAR_WAN, Tile.TILE_1m);
        mp.put(Tile.TILE_CHAR_TIAO, Tile.TILE_1s);
        mp.put(Tile.TILE_CHAR_BING, Tile.TILE_1p);
        mp.put(Tile.TILE_CHAR_E, Tile.TILE_E);
        mp.put(Tile.TILE_CHAR_S, Tile.TILE_S);
        mp.put(Tile.TILE_CHAR_W, Tile.TILE_W);
        mp.put(Tile.TILE_CHAR_N, Tile.TILE_N);
        mp.put(Tile.TILE_CHAR_C, Tile.TILE_C);
        mp.put(Tile.TILE_CHAR_F, Tile.TILE_F);
        mp.put(Tile.TILE_CHAR_P, Tile.TILE_P);
        mp.put(Tile.TILE_CHAR_MEI, Tile.TILE_MEI);
        int part = 0;
        int isFulu = 0;
        int handleOffer = 0;
        int offer = 0;
        StringBuilder nums = new StringBuilder();
        StringBuilder chars = new StringBuilder();
        char charSuit = 0;
        for (int idx = 0; idx < s.length(); idx++) {
            char c = s.charAt(idx);
            switch (c) {
                case '[':
                    isFulu = 1;
                    break;
                case ']': {
                    boolean isChars;
                    int tileCode;
                    if (!nums.isEmpty()) {
                        isChars = false;
                        tileCode = mp.get(charSuit) - 1 + (nums.charAt(1) - '0');
                    } else {
                        isChars = true;
                        tileCode = mp.get(chars.charAt(1));
                    }
                    String tiles = isChars ? chars.toString() : nums.toString();
                    Pack p = new Pack(Pack.PACK_TYPE_INVALID, new Tile(tileCode));
                    if (tiles.length() == 3) {
                        if (handleOffer == 0) {
                            offer = 1;
                        }
                        if (offer > 3) {
                            return -2;
                        }
                        if (!isChars
                                && tiles.charAt(1) == tiles.charAt(0) + 1
                                && tiles.charAt(1) == tiles.charAt(2) - 1) {
                            p.setType(Pack.PACK_TYPE_SHUNZI);
                        } else if (tiles.charAt(1) == tiles.charAt(0)
                                && tiles.charAt(1) == tiles.charAt(2)) {
                            p.setType(Pack.PACK_TYPE_KEZI);
                        } else {
                            return -3;
                        }
                    } else if (tiles.length() == 4) {
                        if (handleOffer == 0) {
                            offer = 0;
                        }
                        if (tiles.charAt(1) == tiles.charAt(0)
                                && tiles.charAt(1) == tiles.charAt(2)
                                && tiles.charAt(1) == tiles.charAt(3)) {
                            p.setType(Pack.PACK_TYPE_GANG);
                        } else {
                            return -4;
                        }
                    }
                    p.setOffer(offer);
                    fulu.add(p);
                    isFulu = 0;
                    handleOffer = 0;
                    nums.setLength(0);
                    chars.setLength(0);
                    charSuit = 0;
                    break;
                }
                case ',':
                    handleOffer = 1;
                    break;
                default:
                    if (c >= '0' && c <= '9') {
                        if (part == 0) {
                            if (isFulu == 1) {
                                if (handleOffer == 0) {
                                    nums.append(c);
                                } else {
                                    offer = c - '0';
                                }
                            } else {
                                nums.append(c);
                            }
                        } else if (part == 1) {
                            nums.append(c);
                        } else if (part == 2) {
                            int cnt = c - '0';
                            for (int i = 0; i < cnt; i++) {
                                huapai.add(new Tile(mp.get(Tile.TILE_CHAR_MEI) + i));
                            }
                        }
                    } else if (c == Tile.TILE_CHAR_E
                            || c == Tile.TILE_CHAR_S
                            || c == Tile.TILE_CHAR_W
                            || c == Tile.TILE_CHAR_N
                            || c == Tile.TILE_CHAR_C
                            || c == Tile.TILE_CHAR_F
                            || c == Tile.TILE_CHAR_P) {
                        if (part == 0) {
                            if (isFulu == 1) {
                                chars.append(c);
                                charSuit = 'z';
                            } else {
                                lipai.add(new Tile(mp.get(c)));
                            }
                        } else if (part == 1) {
                            chars.append(c);
                        }
                    } else if (c == Tile.TILE_CHAR_WAN
                            || c == Tile.TILE_CHAR_TIAO
                            || c == Tile.TILE_CHAR_BING) {
                        if (isFulu == 1) {
                            charSuit = c;
                        } else {
                            Integer base = mp.get(c);
                            for (int i = 0; i < nums.length(); i++) {
                                int code = base - 1 + (nums.charAt(i) - '0');
                                lipai.add(new Tile(code));
                            }
                            nums.setLength(0);
                        }
                    } else if (c == '|') {
                        part++;
                    } else if (c >= Tile.TILE_CHAR_MEI && c <= Tile.TILE_CHAR_DONG) {
                        if (part == 0) {
                            int code = mp.get(Tile.TILE_CHAR_MEI) + (c - Tile.TILE_CHAR_MEI);
                            lipai.add(new Tile(code));
                        } else if (part == 2) {
                            int code = mp.get(Tile.TILE_CHAR_MEI) + (c - Tile.TILE_CHAR_MEI);
                            huapai.add(new Tile(code));
                        }
                    } else {
                        return -999;
                    }
                    break;
            }
        }
        if (part >= 1) {
            setQuanfeng(mp.get(chars.charAt(0)));
            setMenfeng(mp.get(chars.charAt(1)));
            setZimo(nums.charAt(0) - '0');
            setJuezhang(nums.charAt(1) - '0');
            setHaidi(nums.charAt(2) - '0');
            setGang(nums.charAt(3) - '0');
        }
        int total = fulu.size() * 3 + lipai.size();
        if (total == 13) {
            lipai.add(new Tile(Tile.TILE_INVALID));
        } else if (total != 14) {
            return -5;
        }
        if (generateTable() != 0) {
            return -6;
        }
        if (isZimo() == 1) {
            lastLipai().setZimo();
        } else {
            lastLipai().setChonghu();
        }
        if (isGang() == 1) {
            if (isZimo() == 1) {
                boolean hasGang = false;
                for (Pack p : fulu) {
                    if (p.isGang()) {
                        hasGang = true;
                        break;
                    }
                }
                if (!hasGang) {
                    return -7;
                }
            } else {
                if (isHaidi() == 1 || handTileCount(getLastLipai()) > 1) {
                    return -7;
                }
            }
        }
        if (isJuezhang() == 1) {
            if (lipaiTileCount(getLastLipai()) > 1) {
                return -7;
            }
        }
        sortLipaiWithoutLastOne();
        return 0;
    }

    public int generateTable() {
        fuluTable.clear();
        lipaiTable.clear();
        huapaiTable.clear();
        for (Pack p : fulu) {
            List<Tile> v = p.getAllTile();
            for (Tile t : v) {
                fuluTable.merge(t.getId(), 1, Integer::sum);
            }
        }
        for (Tile t : lipai) {
            lipaiTable.merge(t.getId(), 1, Integer::sum);
        }
        for (Tile t : huapai) {
            huapaiTable.merge(t.getId(), 1, Integer::sum);
        }
        for (int i = Tile.TILE_1m; i < Tile.TILE_SIZE; i++) {
            int cnt = fuluTable.getOrDefault(i, 0) + lipaiTable.getOrDefault(i, 0);
            if (cnt > 4) {
                return -1;
            }
        }
        for (int i = Tile.TILE_MEI; i <= Tile.TILE_DONG; i++) {
            int cnt = lipaiTable.getOrDefault(i, 0) + huapaiTable.getOrDefault(i, 0);
            if (cnt > 1) {
                return -1;
            }
        }
        return 0;
    }

    private void clearAndSetDefault() {
        fulu.clear();
        lipai.clear();
        huapai.clear();
        fuluTable.clear();
        lipaiTable.clear();
        huapaiTable.clear();
        setQuanfeng(WIND_E);
        setMenfeng(WIND_E);
        setZimo(0);
        setJuezhang(0);
        setHaidi(0);
        setGang(0);
    }

    public void drawTile(Tile tile) {
        setLastLipai(tile);
        lastLipai().setZimo();
    }

    public void setTile(Tile tile) {
        setLastLipai(tile);
        lastLipai().setChonghu();
    }

    public Tile discardTile() {
        Tile tile = new Tile(getLastLipai().getId());
        setLastLipai(new Tile(Tile.TILE_INVALID));
        return tile;
    }

    public void sortLipaiWithoutLastOne() {
        if (lipai.isEmpty()) {
            return;
        }
        List<Tile> sub = lipai.subList(0, lipai.size() - 1);
        Collections.sort(sub);
    }

    public void sortLipaiAll() {
        Collections.sort(lipai);
    }

    public int getQuanfeng() {
        return quanfeng;
    }

    public void setQuanfeng(int quanfeng) {
        this.quanfeng = quanfeng;
    }

    public int getMenfeng() {
        return menfeng;
    }

    public void setMenfeng(int menfeng) {
        this.menfeng = menfeng;
    }

    public int isZimo() {
        return zimo;
    }

    public void setZimo(int zimo) {
        this.zimo = zimo;
    }

    public int isJuezhang() {
        return juezhang;
    }

    public void setJuezhang(int juezhang) {
        this.juezhang = juezhang;
    }

    public int isHaidi() {
        return haidi;
    }

    public void setHaidi(int haidi) {
        this.haidi = haidi;
    }

    public int isGang() {
        return gang;
    }

    public void setGang(int gang) {
        this.gang = gang;
    }

    public int isMenqing() {
        for (Pack p : fulu) {
            if (!p.isAnshou()) {
                return 0;
            }
        }
        return 1;
    }

    public int isTotallyFulu() {
        if (fulu.size() != 4) {
            return 0;
        }
        for (Pack p : fulu) {
            if (p.isAnshou()) {
                return 0;
            }
        }
        return 1;
    }

    public int noFulu() {
        return fulu.isEmpty() ? 1 : 0;
    }

    public void setLastLipai(Tile t) {
        if (lipai.isEmpty()) {
            lipai.add(t);
            lipaiTable.merge(t.getId(), 1, Integer::sum);
            return;
        }
        Tile last = lastLipai();
        int lastId = last.getId();
        lipaiTable.put(lastId, lipaiTable.getOrDefault(lastId, 1) - 1);
        lipai.set(lipai.size() - 1, t);
        lipaiTable.merge(t.getId(), 1, Integer::sum);
    }

    public Tile lastLipai() {
        return lipai.get(lipai.size() - 1);
    }

    public Tile getLastLipai() {
        return lipai.get(lipai.size() - 1);
    }
}

