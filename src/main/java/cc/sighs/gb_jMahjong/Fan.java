package cc.sighs.gb_jMahjong;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Fan {
    public enum FanType {
        FAN_INVALID,
        FAN_DASIXI,
        FAN_DASANYUAN,
        FAN_LVYISE,
        FAN_JIULIANBAODENG,
        FAN_SIGANG,
        FAN_LIANQIDUI,
        FAN_SHISANYAO,
        FAN_QINGYAOJIU,
        FAN_XIAOSIXI,
        FAN_XIAOSANYUAN,
        FAN_ZIYISE,
        FAN_SIANKE,
        FAN_YISESHUANGLONGHUI,
        FAN_YISESITONGSHUN,
        FAN_YISESIJIEGAO,
        FAN_YISESIBUGAO,
        FAN_SANGANG,
        FAN_HUNYAOJIU,
        FAN_QIDUI,
        FAN_QIXINGBUKAO,
        FAN_QUANSHUANGKE,
        FAN_QINGYISE,
        FAN_YISESANTONGSHUN,
        FAN_YISESANJIEGAO,
        FAN_QUANDA,
        FAN_QUANZHONG,
        FAN_QUANXIAO,
        FAN_QINGLONG,
        FAN_SANSESHUANGLONGHUI,
        FAN_YISESANBUGAO,
        FAN_QUANDAIWU,
        FAN_SANTONGKE,
        FAN_SANANKE,
        FAN_QUANBUKAO,
        FAN_ZUHELONG,
        FAN_DAYUWU,
        FAN_XIAOYUWU,
        FAN_SANFENGKE,
        FAN_HUALONG,
        FAN_TUIBUDAO,
        FAN_SANSESANTONGSHUN,
        FAN_SANSESANJIEGAO,
        FAN_WUFANHU,
        FAN_MIAOSHOUHUICHUN,
        FAN_HAIDILAOYUE,
        FAN_GANGSHANGKAIHUA,
        FAN_QIANGGANGHU,
        FAN_PENGPENGHU,
        FAN_HUNYISE,
        FAN_SANSESANBUGAO,
        FAN_WUMENQI,
        FAN_QUANQIUREN,
        FAN_SHUANGANGANG,
        FAN_SHUANGJIANKE,
        FAN_QUANDAIYAO,
        FAN_BUQIUREN,
        FAN_SHUANGMINGGANG,
        FAN_HUJUEZHANG,
        FAN_JIANKE,
        FAN_QUANFENGKE,
        FAN_MENFENGKE,
        FAN_MENQIANQING,
        FAN_PINGHU,
        FAN_SIGUIYI,
        FAN_SHUANGTONGKE,
        FAN_SHUANGANKE,
        FAN_ANGANG,
        FAN_DUANYAO,
        FAN_YIBANGAO,
        FAN_XIXIANGFENG,
        FAN_LIANLIU,
        FAN_LAOSHAOFU,
        FAN_YAOJIUKE,
        FAN_MINGGANG,
        FAN_QUEYIMEN,
        FAN_WUZI,
        FAN_BIANZHANG,
        FAN_KANZHANG,
        FAN_DANDIAOJIANG,
        FAN_ZIMO,
        FAN_HUAPAI,
        FAN_MINGANGANG
    }

    public static final int[] FAN_SCORE = {
            0,
            88, 88, 88, 88, 88, 88, 88,
            64, 64, 64, 64, 64, 64,
            48, 48,
            32, 32, 32,
            24, 24, 24, 24, 24, 24, 24, 24, 24,
            16, 16, 16, 16, 16, 16,
            12, 12, 12, 12, 12,
            8, 8, 8, 8, 8, 8, 8, 8, 8,
            6, 6, 6, 6, 6, 6, 6,
            4, 4, 4, 4,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            5
    };

    public static final String[] FAN_NAME = {
            "无效番种",
            "大四喜", "大三元", "绿一色", "九莲宝灯", "四杠", "连七对", "十三幺",
            "清幺九", "小四喜", "小三元", "字一色", "四暗刻", "一色双龙会",
            "一色四同顺", "一色四节高",
            "一色四步高", "三杠", "混幺九",
            "七对", "七星不靠", "全双刻", "清一色", "一色三同顺", "一色三节高", "全大", "全中", "全小",
            "清龙", "三色双龙会", "一色三步高", "全带五", "三同刻", "三暗刻",
            "全不靠", "组合龙", "大于五", "小于五", "三风刻",
            "花龙", "推不倒", "三色三同顺", "三色三节高", "无番和", "妙手回春", "海底捞月", "杠上开花", "抢杠和",
            "碰碰和", "混一色", "三色三步高", "五门齐", "全求人", "双暗杠", "双箭刻",
            "全带幺", "不求人", "双明杠", "和绝张",
            "箭刻", "圈风刻", "门风刻", "门前清", "平和", "四归一", "双同刻", "双暗刻", "暗杠", "断幺",
            "一般高", "喜相逢", "连六", "老少副", "幺九刻", "明杠", "缺一门", "无字", "边张", "坎张", "单钓将", "自摸", "花牌",
            "明暗杠"
    };

    @SuppressWarnings("unchecked")
    public final List<List<Integer>>[] fanTable = new ArrayList[FanType.values().length];
    @SuppressWarnings("unchecked")
    public final List<List<Integer>>[] fanTableRes = new ArrayList[FanType.values().length];
    @SuppressWarnings("unchecked")
    public final List<List<Integer>>[] excludedFanTable = new ArrayList[FanType.values().length];
    public final List<Pack> fanPacks = new ArrayList<>();
    public final List<Pack> fanPacksRes = new ArrayList<>();
    public int totFan;
    public int totFanRes;

    private int _bitCount(long n) {
        int c = 0;
        long v = n;
        while (v != 0) {
            v &= (v - 1);
            c++;
        }
        return c;
    }

    private int _judgeZuhelong(long bitmap) {
        for (int i = 1; i <= 6; i++) {
            long z = Pack.ZUHELONG_BITMAP[i];
            if ((z & bitmap) == z) {
                return i;
            }
        }
        return 0;
    }

    private int _judgePartOfZuhelong(long bitmap) {
        long v = bitmap & Tile.TILE_TYPE_BITMAP_SHU;
        for (int i = 1; i <= 6; i++) {
            long z = Pack.ZUHELONG_BITMAP[i];
            if ((z | v) == z) {
                return 1;
            }
        }
        return 0;
    }

    private int _judge2SameOrAdjacent(Tile a, Tile b) {
        if (a.isShu() && a.suit() == b.suit()) {
            return (a.equals(b.pred()) || a.equals(b)) ? 1 : 0;
        } else if (a.isZi()) {
            return a.equals(b) ? 1 : 0;
        }
        return 0;
    }

    private int _judge3MakePack(Tile a, Tile b, Tile c) {
        if (a.isShu() && b.suit() == a.suit() && b.suit() == c.suit() && b.equals(a.succ()) && b.equals(c.pred())) {
            return Pack.PACK_TYPE_SHUNZI;
        } else if (b.equals(a) && b.equals(c)) {
            return Pack.PACK_TYPE_KEZI;
        }
        return 0;
    }

    private int _judge2MakePack(Tile a, Tile b) {
        return b.equals(a) ? Pack.PACK_TYPE_JIANG : 0;
    }

    private long _packsHashcode(Handtiles ht, List<Pack> packs) {
        long h = 0L;
        for (int i = ht.fulu.size(); i < packs.size(); i++) {
            Pack p = packs.get(i);
            h = ((h << 7) | p.getMiddleTile().getId());
            h = ((h << 3) | p.getType());
            h = (h << 1) | (p.haveLastTile() ? 1L : 0L);
        }
        return h;
    }

    private int _dfsRecursive(Handtiles ht, List<Tile> sortedLipai, int mianziCnt, int duiziCnt, int[] vis, List<Pack> packs, int flagCountFan, Pack zuhelongPack, Set<Long> st) {
        int ret = 0;
        if (mianziCnt == 0 && duiziCnt == 0) {
            if (flagCountFan != 0) {
                _countBasicFan(ht, packs, zuhelongPack);
                fanPacks.clear();
                fanPacks.addAll(packs);
                _getMaxFan();
            }
            return 1;
        }
        int startPos = -1;
        int n = sortedLipai.size();
        for (int i = 0; i < n; i++) {
            if (vis[i] == 0) {
                startPos = i;
                break;
            }
        }
        if (startPos == -1) {
            return 0;
        }
        for (int i = startPos + 1; i < n; i++) {
            if (vis[i] != 0) {
                continue;
            }
            if (_judge2SameOrAdjacent(sortedLipai.get(startPos), sortedLipai.get(i)) == 0) {
                break;
            }
            if (duiziCnt != 0 && _judge2MakePack(sortedLipai.get(startPos), sortedLipai.get(i)) != 0) {
                vis[startPos] = 1;
                vis[i] = 1;
                int offer = -(sortedLipai.get(startPos).getDrawFlag() + sortedLipai.get(i).getDrawFlag());
                packs.add(new Pack(Pack.PACK_TYPE_JIANG, sortedLipai.get(i), 0, offer));
                long hashcode = _packsHashcode(ht, packs);
                if (!st.contains(hashcode)) {
                    st.add(hashcode);
                    ret |= _dfsRecursive(ht, sortedLipai, mianziCnt, duiziCnt - 1, vis, packs, flagCountFan, zuhelongPack, st);
                    if (flagCountFan == 0 && ret != 0) {
                        return 1;
                    }
                }
                packs.remove(packs.size() - 1);
                vis[startPos] = 0;
                vis[i] = 0;
            }
            if (mianziCnt != 0) {
                for (int j = i + 1; j < n; j++) {
                    if (vis[j] != 0) {
                        continue;
                    }
                    if (_judge2SameOrAdjacent(sortedLipai.get(i), sortedLipai.get(j)) == 0) {
                        break;
                    }
                    int type = _judge3MakePack(sortedLipai.get(startPos), sortedLipai.get(i), sortedLipai.get(j));
                    if (type != 0) {
                        vis[startPos] = 1;
                        vis[i] = 1;
                        vis[j] = 1;
                        int offer = -(sortedLipai.get(startPos).getDrawFlag()
                                + sortedLipai.get(i).getDrawFlag()
                                + sortedLipai.get(j).getDrawFlag());
                        packs.add(new Pack(type, sortedLipai.get(i), 0, offer));
                        long hashcode = _packsHashcode(ht, packs);
                        if (!st.contains(hashcode)) {
                            st.add(hashcode);
                            ret |= _dfsRecursive(ht, sortedLipai, mianziCnt - 1, duiziCnt, vis, packs, flagCountFan, zuhelongPack, st);
                            if (flagCountFan == 0 && ret != 0) {
                                return 1;
                            }
                        }
                        packs.remove(packs.size() - 1);
                        vis[startPos] = 0;
                        vis[i] = 0;
                        vis[j] = 0;
                    }
                }
            }
        }
        return ret;
    }

    private int _dfs(Handtiles ht, List<Tile> sortedLipai, int mianziCnt, int duiziCnt, List<Pack> packs, int flagCountFan, Pack zuhelongPack) {
        Set<Long> st = new HashSet<>();
        int[] vis = new int[14];
        return _dfsRecursive(ht, sortedLipai, mianziCnt, duiziCnt, vis, packs, flagCountFan, zuhelongPack, st);
    }

    private FanType _judgeCompleteSpecialHu(Handtiles ht) {
        if (ht.noFulu() == 0) {
            return FanType.FAN_INVALID;
        }
        long bitmap = ht.lipaiBitmap();
        int cnt = _bitCount(bitmap);
        long meaningful = Tile.TILE_TYPE_BITMAP_MEANINGFUL;
        long yaojiu = Tile.TILE_TYPE_BITMAP_YAOJIU;
        if ((bitmap & yaojiu) == bitmap && cnt == 13) {
            return FanType.FAN_SHISANYAO;
        }
        if (_judgePartOfZuhelong(bitmap) == 1 && (bitmap & meaningful) == bitmap && cnt == 14) {
            long zi = Tile.TILE_TYPE_BITMAP_ZI;
            if ((bitmap & zi) == zi) {
                return FanType.FAN_QIXINGBUKAO;
            } else {
                return FanType.FAN_QUANBUKAO;
            }
        }
        return FanType.FAN_INVALID;
    }

    private FanType _judgeQidui(Handtiles ht) {
        if (ht.noFulu() == 0) {
            return FanType.FAN_INVALID;
        }
        List<Tile> sortedLipai = new ArrayList<>(ht.lipai);
        sortedLipai.sort(null);
        List<Pack> packs = new ArrayList<>();
        int ret = _dfs(ht, sortedLipai, 0, 7, packs, 0, new Pack());
        if (ret != 0) {
            int flag = 1;
            for (int i = 1; i < packs.size(); i++) {
                Tile prev = packs.get(i - 1).getMiddleTile();
                Tile curr = packs.get(i).getMiddleTile();
                if (!(prev.succ().equals(curr) && prev.suit() == curr.suit())) {
                    flag = 0;
                    break;
                }
            }
            if (flag == 0) {
                return FanType.FAN_QIDUI;
            } else {
                return FanType.FAN_LIANQIDUI;
            }
        }
        return FanType.FAN_INVALID;
    }

    private int _judgeBasicHu(Handtiles ht) {
        List<Tile> sortedLipai = new ArrayList<>(ht.lipai);
        sortedLipai.sort(null);
        List<Pack> packs = new ArrayList<>(ht.fulu);
        return _dfs(ht, sortedLipai, 4 - ht.fulu.size(), 1, packs, 0, new Pack());
    }

    private int _judgeZuhelongBasicHu(Handtiles ht) {
        List<Tile> sortedLipai = new ArrayList<>();
        int zuhelongType = _judgeZuhelong(ht.lipaiBitmap());
        long zuhelongBitmap = zuhelongType == 0 ? 0L : Pack.ZUHELONG_BITMAP[zuhelongType];
        if (zuhelongBitmap != 0L) {
            long bitmapTemp = zuhelongBitmap;
            for (Tile t : ht.lipai) {
                long b = t.getBitmap();
                if ((b & bitmapTemp) != 0L) {
                    bitmapTemp ^= b;
                } else {
                    sortedLipai.add(t);
                }
            }
            sortedLipai.sort(null);
            List<Pack> packs = new ArrayList<>(ht.fulu);
            return _dfs(ht, sortedLipai, 1 - ht.fulu.size(), 1, packs, 0, new Pack());
        }
        return 0;
    }

    public Fan() {
        for (int i = 0; i < fanTable.length; i++) {
            fanTable[i] = new ArrayList<>();
            fanTableRes[i] = new ArrayList<>();
            excludedFanTable[i] = new ArrayList<>();
        }
    }

    private void _addFan(FanType f, List<Integer> v) {
        fanTable[f.ordinal()].add(v);
    }

    private void _excludeFan(FanType f, List<Integer> v) {
        excludedFanTable[f.ordinal()].add(v);
    }

    private boolean _hasFan(FanType f) {
        return !fanTable[f.ordinal()].isEmpty();
    }

    private boolean _hasExcludedFan(FanType f) {
        return !excludedFanTable[f.ordinal()].isEmpty();
    }

    private void _clearTable() {
        for (int i = 1; i < fanTable.length; i++) {
            fanTable[i].clear();
            excludedFanTable[i].clear();
        }
    }

    private void _clearResult() {
        totFanRes = 0;
    }

    private void _clear() {
        _clearTable();
        _clearResult();
    }

    private void _fanTableExclude() {
        for (int i = 1; i < fanTable.length; i++) {
            List<List<Integer>> table = fanTable[i];
            List<List<Integer>> excluded = excludedFanTable[i];
            if (!table.isEmpty() && !excluded.isEmpty()) {
                List<List<Integer>> keep = new ArrayList<>();
                int excludedSize = excluded.size();
                int tableSize = table.size();
                int[] vis = new int[excludedSize];
                int[] res = new int[tableSize];
                for (int j = 0; j < excludedSize; j++) {
                    if (vis[j] != 0) {
                        continue;
                    }
                    List<Integer> ev = excluded.get(j);
                    for (int k = 0; k < tableSize; k++) {
                        if (res[k] != 0) {
                            continue;
                        }
                        if (table.get(k).equals(ev)) {
                            res[k] = 1;
                            break;
                        }
                    }
                    vis[j] = 1;
                }
                for (int k = 0; k < tableSize; k++) {
                    if (res[k] == 0) {
                        keep.add(table.get(k));
                    }
                }
                fanTable[i] = keep;
            }
        }
    }

    private void _fanTableCount() {
        int cnt = 0;
        for (int i = 1; i < fanTable.length; i++) {
            cnt += fanTable[i].size() * FAN_SCORE[i];
        }
        if (cnt == 0) {
            _addFan(FanType.FAN_WUFANHU, new ArrayList<>());
            cnt = FAN_SCORE[FanType.FAN_WUFANHU.ordinal()];
        }
        totFan = cnt;
    }

    private void _excludeYaojiuke(List<Pack> packs) {
        for (int i = 0; i < packs.size(); i++) {
            Pack p = packs.get(i);
            int rank = p.getMiddleTile().rank();
            int isZi = p.getMiddleTile().isZi() ? 1 : 0;
            if (p.isKeGang() && (rank == Tile.RANK_1 || rank == Tile.RANK_9 || isZi == 1)) {
                List<Integer> v = new ArrayList<>();
                v.add(i);
                _excludeFan(FanType.FAN_YAOJIUKE, v);
            }
        }
    }

    private void _countOverallAttrFan(Handtiles ht, List<Pack> packs, Pack zuhelongPack) {
        long bitmap = ht.lipaiBitmap() | ht.fuluBitmap();
        if (zuhelongPack.getZuhelongType() == 0) {
            if ((bitmap & Tile.TILE_TYPE_BITMAP_LV) == bitmap) {
                _addFan(FanType.FAN_LVYISE, new ArrayList<>());
                _excludeFan(FanType.FAN_HUNYISE, new ArrayList<>());
            }
            if (ht.noFulu() != 0) {
                java.util.Map<Integer, Integer> tileTable = new HashMap<>(ht.lipaiTable);
                Tile last = ht.getLastLipai();
                int lastId = last.getId();
                tileTable.put(lastId, tileTable.getOrDefault(lastId, 1) - 1);
                int st;
                if (tileTable.getOrDefault(Tile.TILE_1m, 0) != 0) {
                    st = Tile.TILE_1m;
                } else if (tileTable.getOrDefault(Tile.TILE_1s, 0) != 0) {
                    st = Tile.TILE_1s;
                } else {
                    st = Tile.TILE_1p;
                }
                int flag = 1;
                for (int i = 2; i <= 8; i++) {
                    if (tileTable.getOrDefault(st - 1 + i, 0) != 1) {
                        flag = 0;
                        break;
                    }
                }
                if (tileTable.getOrDefault(st, 0) != 3 || tileTable.getOrDefault(st + 8, 0) != 3) {
                    flag = 0;
                }
                tileTable.put(lastId, tileTable.getOrDefault(lastId, 0) + 1);
                if (flag != 0) {
                    _addFan(FanType.FAN_JIULIANBAODENG, new ArrayList<>());
                    _excludeFan(FanType.FAN_QINGYISE, new ArrayList<>());
                    _excludeFan(FanType.FAN_BUQIUREN, new ArrayList<>());
                    _excludeFan(FanType.FAN_MENQIANQING, new ArrayList<>());
                    _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
                    for (int i = 0; i < packs.size(); i++) {
                        Pack p = packs.get(i);
                        if (p.isKezi() && p.getMiddleTile().isYaojiu()) {
                            List<Integer> v = new ArrayList<>();
                            v.add(i);
                            _excludeFan(FanType.FAN_YAOJIUKE, v);
                            break;
                        }
                    }
                }
            }
            if ((bitmap & (Tile.TILE_TYPE_BITMAP_YAOJIU & ~Tile.TILE_TYPE_BITMAP_ZI)) == bitmap) {
                _addFan(FanType.FAN_QINGYAOJIU, new ArrayList<>());
                _excludeFan(FanType.FAN_PENGPENGHU, new ArrayList<>());
                _excludeFan(FanType.FAN_QUANDAIYAO, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
                for (int i = 0; i < packs.size(); i++) {
                    for (int j = i + 1; j < packs.size(); j++) {
                        Pack pi = packs.get(i);
                        Pack pj = packs.get(j);
                        if (pi.isKeGang() && pj.isKeGang() && pi.getMiddleTile().rank() == pj.getMiddleTile().rank()) {
                            List<Integer> v = new ArrayList<>();
                            v.add(i);
                            v.add(j);
                            _excludeFan(FanType.FAN_SHUANGTONGKE, v);
                        }
                    }
                }
                _excludeYaojiuke(packs);
            }
            if ((bitmap & Tile.TILE_TYPE_BITMAP_ZI) == bitmap) {
                _addFan(FanType.FAN_ZIYISE, new ArrayList<>());
                _excludeFan(FanType.FAN_PENGPENGHU, new ArrayList<>());
                _excludeFan(FanType.FAN_QUANDAIYAO, new ArrayList<>());
                _excludeYaojiuke(packs);
            }
            if ((bitmap & Tile.TILE_TYPE_BITMAP_YAOJIU & ~Tile.TILE_TYPE_BITMAP_ZI) != 0L
                    && (bitmap & Tile.TILE_TYPE_BITMAP_ZI) != 0L
                    && (bitmap & Tile.TILE_TYPE_BITMAP_YAOJIU) == bitmap) {
                _addFan(FanType.FAN_HUNYAOJIU, new ArrayList<>());
                _excludeFan(FanType.FAN_PENGPENGHU, new ArrayList<>());
                _excludeFan(FanType.FAN_QUANDAIYAO, new ArrayList<>());
                _excludeYaojiuke(packs);
            }
            if (packs.size() == 5) {
                int flag = 1;
                for (Pack p : packs) {
                    Tile mt = p.getMiddleTile();
                    boolean ok = (p.isKeGang() || p.isJiang()) && mt.isShu() && mt.rank() % 2 == 0;
                    if (!ok) {
                        flag = 0;
                        break;
                    }
                }
                if (flag != 0) {
                    _addFan(FanType.FAN_QUANSHUANGKE, new ArrayList<>());
                    _excludeFan(FanType.FAN_PENGPENGHU, new ArrayList<>());
                    _excludeFan(FanType.FAN_DUANYAO, new ArrayList<>());
                    _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
                }
            }
            if ((bitmap & Tile.TILE_TYPE_BITMAP_WAN) == bitmap
                    || (bitmap & Tile.TILE_TYPE_BITMAP_TIAO) == bitmap
                    || (bitmap & Tile.TILE_TYPE_BITMAP_BING) == bitmap) {
                _addFan(FanType.FAN_QINGYISE, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
            }
            if ((bitmap & Tile.TILE_TYPE_BITMAP_QUANDA) == bitmap) {
                _addFan(FanType.FAN_QUANDA, new ArrayList<>());
                _excludeFan(FanType.FAN_DAYUWU, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
            }
            if ((bitmap & Tile.TILE_TYPE_BITMAP_QUANZHONG) == bitmap) {
                _addFan(FanType.FAN_QUANZHONG, new ArrayList<>());
                _excludeFan(FanType.FAN_DUANYAO, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
            }
            if ((bitmap & Tile.TILE_TYPE_BITMAP_QUANXIAO) == bitmap) {
                _addFan(FanType.FAN_QUANXIAO, new ArrayList<>());
                _excludeFan(FanType.FAN_XIAOYUWU, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
            }
            if (packs.size() == 5) {
                int flag = 1;
                for (Pack p : packs) {
                    Tile mt = p.getMiddleTile();
                    int rank = mt.rank();
                    boolean ok = (p.isShunzi() && rank >= Tile.RANK_4 && rank <= Tile.RANK_6)
                            || ((p.isKeGang() || p.isJiang()) && rank == Tile.RANK_5);
                    if (!ok) {
                        flag = 0;
                        break;
                    }
                }
                if (flag != 0) {
                    _addFan(FanType.FAN_QUANDAIWU, new ArrayList<>());
                    _excludeFan(FanType.FAN_DUANYAO, new ArrayList<>());
                    _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
                }
            }
            if ((bitmap & Tile.TILE_TYPE_BITMAP_DAYUWU) == bitmap) {
                _addFan(FanType.FAN_DAYUWU, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
            }
            if ((bitmap & Tile.TILE_TYPE_BITMAP_XIAOYUWU) == bitmap) {
                _addFan(FanType.FAN_XIAOYUWU, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
            }
            if ((bitmap & Tile.TILE_TYPE_BITMAP_TUIBUDAO) == bitmap) {
                _addFan(FanType.FAN_TUIBUDAO, new ArrayList<>());
                _excludeFan(FanType.FAN_QUEYIMEN, new ArrayList<>());
            }
            if (packs.size() == 5) {
                int flag = 1;
                for (Pack p : packs) {
                    if (!(p.isKeGang() || p.isJiang())) {
                        flag = 0;
                        break;
                    }
                }
                if (flag != 0) {
                    _addFan(FanType.FAN_PENGPENGHU, new ArrayList<>());
                }
            }
            long bitmapNoZi = bitmap & ~Tile.TILE_TYPE_BITMAP_ZI;
            if ((bitmap & Tile.TILE_TYPE_BITMAP_ZI) != 0L
                    && (bitmap & Tile.TILE_TYPE_BITMAP_SHU) != 0L
                    && ((bitmapNoZi & Tile.TILE_TYPE_BITMAP_WAN) == bitmapNoZi
                    || (bitmapNoZi & Tile.TILE_TYPE_BITMAP_TIAO) == bitmapNoZi
                    || (bitmapNoZi & Tile.TILE_TYPE_BITMAP_BING) == bitmapNoZi)) {
                _addFan(FanType.FAN_HUNYISE, new ArrayList<>());
            }
            if (packs.size() == 5) {
                int flag = 1;
                for (Pack p : packs) {
                    Tile mt = p.getMiddleTile();
                    int rank = mt.rank();
                    int isYaojiu = mt.isYaojiu() ? 1 : 0;
                    boolean ok = (p.isShunzi() && (rank == Tile.RANK_2 || rank == Tile.RANK_8))
                            || ((p.isKeGang() || p.isJiang()) && isYaojiu == 1);
                    if (!ok) {
                        flag = 0;
                        break;
                    }
                }
                if (flag != 0) {
                    _addFan(FanType.FAN_QUANDAIYAO, new ArrayList<>());
                }
            }
            if ((bitmap & ~Tile.TILE_TYPE_BITMAP_YAOJIU) == bitmap) {
                _addFan(FanType.FAN_DUANYAO, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
            }
            int suitCnt = ((bitmap & Tile.TILE_TYPE_BITMAP_WAN) != 0L ? 1 : 0)
                    + ((bitmap & Tile.TILE_TYPE_BITMAP_TIAO) != 0L ? 1 : 0)
                    + ((bitmap & Tile.TILE_TYPE_BITMAP_BING) != 0L ? 1 : 0);
            if (suitCnt == 2) {
                _addFan(FanType.FAN_QUEYIMEN, new ArrayList<>());
            }
        }
        int colorCnt = ((bitmap & Tile.TILE_TYPE_BITMAP_WAN) != 0L ? 1 : 0)
                + ((bitmap & Tile.TILE_TYPE_BITMAP_TIAO) != 0L ? 1 : 0)
                + ((bitmap & Tile.TILE_TYPE_BITMAP_BING) != 0L ? 1 : 0)
                + ((bitmap & Tile.TILE_TYPE_BITMAP_FENG) != 0L ? 1 : 0)
                + ((bitmap & Tile.TILE_TYPE_BITMAP_JIAN) != 0L ? 1 : 0);
        if (colorCnt == 5) {
            _addFan(FanType.FAN_WUMENQI, new ArrayList<>());
        }
        if (packs.size() != 7 && !packs.isEmpty()) {
            boolean ok = true;
            for (Pack p : packs) {
                if (!(p.isShunzi() || (p.isJiang() && p.getMiddleTile().isShu()))) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                _addFan(FanType.FAN_PINGHU, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
            }
        }
        for (int i = Tile.TILE_1m; i < Tile.TILE_SIZE; i++) {
            boolean hasGang = false;
            for (Pack p : packs) {
                if (p.isGang() && p.getMiddleTile().getId() == i) {
                    hasGang = true;
                    break;
                }
            }
            if (hasGang) {
                continue;
            }
            if (ht.handTileCount(new Tile(i)) == 4) {
                _addFan(FanType.FAN_SIGUIYI, new ArrayList<>());
            }
        }
        if ((bitmap & ~Tile.TILE_TYPE_BITMAP_ZI) == bitmap) {
            _addFan(FanType.FAN_WUZI, new ArrayList<>());
        }
    }

    private void _countKeGangFan(Handtiles ht, List<Pack> packs) {
        List<Integer> angang = new ArrayList<>();
        List<Integer> minggang = new ArrayList<>();
        List<Integer> anke = new ArrayList<>();
        for (int i = 0; i < packs.size(); i++) {
            Pack p = packs.get(i);
            if (p.isGang()) {
                if (p.isAnshou()) {
                    angang.add(i);
                } else {
                    minggang.add(i);
                }
            } else if (p.isKezi() && p.isAnshou()) {
                anke.add(i);
            }
        }
        int key = angang.size() * 100 + minggang.size() * 10 + anke.size();
        switch (key) {
            case 400:
                _addFan(FanType.FAN_SIGANG, new ArrayList<>(angang));
                _addFan(FanType.FAN_SIANKE, new ArrayList<>(angang));
                break;
            case 310: {
                List<Integer> v = new ArrayList<>();
                v.add(angang.get(0));
                v.add(angang.get(1));
                v.add(angang.get(2));
                v.add(minggang.get(0));
                _addFan(FanType.FAN_SIGANG, v);
                _addFan(FanType.FAN_SANANKE, new ArrayList<>(angang));
                break;
            }
            case 220: {
                List<Integer> v = new ArrayList<>();
                v.add(angang.get(0));
                v.add(angang.get(1));
                v.add(minggang.get(0));
                v.add(minggang.get(1));
                _addFan(FanType.FAN_SIGANG, v);
                _addFan(FanType.FAN_SHUANGANKE, new ArrayList<>(angang));
                break;
            }
            case 130: {
                List<Integer> v = new ArrayList<>();
                v.add(angang.get(0));
                v.add(minggang.get(0));
                v.add(minggang.get(1));
                v.add(minggang.get(2));
                _addFan(FanType.FAN_SIGANG, v);
                break;
            }
            case 301:
                _addFan(FanType.FAN_SANGANG, new ArrayList<>(angang));
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(angang.get(1));
                    v.add(angang.get(2));
                    v.add(anke.get(0));
                    _addFan(FanType.FAN_SIANKE, v);
                }
                break;
            case 300:
                _addFan(FanType.FAN_SANGANG, new ArrayList<>(angang));
                _addFan(FanType.FAN_SANANKE, new ArrayList<>(angang));
                break;
            case 211: {
                List<Integer> v1 = new ArrayList<>();
                v1.add(angang.get(0));
                v1.add(angang.get(1));
                v1.add(minggang.get(0));
                _addFan(FanType.FAN_SANGANG, v1);
                List<Integer> v2 = new ArrayList<>();
                v2.add(angang.get(0));
                v2.add(angang.get(1));
                v2.add(anke.get(0));
                _addFan(FanType.FAN_SANANKE, v2);
                break;
            }
            case 210: {
                List<Integer> v1 = new ArrayList<>();
                v1.add(angang.get(0));
                v1.add(angang.get(1));
                v1.add(minggang.get(0));
                _addFan(FanType.FAN_SANGANG, v1);
                List<Integer> v2 = new ArrayList<>();
                v2.add(angang.get(0));
                v2.add(angang.get(1));
                _addFan(FanType.FAN_SHUANGANKE, v2);
                break;
            }
            case 121: {
                List<Integer> v1 = new ArrayList<>();
                v1.add(angang.get(0));
                v1.add(minggang.get(0));
                v1.add(minggang.get(1));
                _addFan(FanType.FAN_SANGANG, v1);
                List<Integer> v2 = new ArrayList<>();
                v2.add(angang.get(0));
                v2.add(anke.get(0));
                _addFan(FanType.FAN_SHUANGANKE, v2);
                break;
            }
            case 120: {
                List<Integer> v = new ArrayList<>();
                v.add(angang.get(0));
                v.add(minggang.get(0));
                v.add(minggang.get(1));
                _addFan(FanType.FAN_SANGANG, v);
                break;
            }
            case 202:
                _addFan(FanType.FAN_SHUANGANGANG, new ArrayList<>(angang));
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(angang.get(1));
                    v.add(anke.get(0));
                    v.add(anke.get(1));
                    _addFan(FanType.FAN_SIANKE, v);
                }
                break;
            case 201:
                _addFan(FanType.FAN_SHUANGANGANG, new ArrayList<>(angang));
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(angang.get(1));
                    v.add(anke.get(0));
                    _addFan(FanType.FAN_SANANKE, v);
                }
                break;
            case 112:
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(minggang.get(0));
                    _addFan(FanType.FAN_MINGANGANG, v);
                }
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(anke.get(0));
                    v.add(anke.get(1));
                    _addFan(FanType.FAN_SANANKE, v);
                }
                break;
            case 111:
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(minggang.get(0));
                    _addFan(FanType.FAN_MINGANGANG, v);
                }
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(anke.get(0));
                    _addFan(FanType.FAN_SHUANGANKE, v);
                }
                break;
            case 22:
                _addFan(FanType.FAN_SHUANGMINGGANG, new ArrayList<>(minggang));
                _addFan(FanType.FAN_SHUANGANKE, new ArrayList<>(anke));
                break;
            case 103:
                _addFan(FanType.FAN_ANGANG, new ArrayList<>(angang));
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(anke.get(0));
                    v.add(anke.get(1));
                    v.add(anke.get(2));
                    _addFan(FanType.FAN_SIANKE, v);
                }
                break;
            case 102:
                _addFan(FanType.FAN_ANGANG, new ArrayList<>(angang));
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(anke.get(0));
                    v.add(anke.get(1));
                    _addFan(FanType.FAN_SANANKE, v);
                }
                break;
            case 101:
                _addFan(FanType.FAN_ANGANG, new ArrayList<>(angang));
                {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(anke.get(0));
                    _addFan(FanType.FAN_SHUANGANKE, v);
                }
                break;
            case 13:
                _addFan(FanType.FAN_MINGGANG, new ArrayList<>(minggang));
                _addFan(FanType.FAN_SANANKE, new ArrayList<>(anke));
                break;
            case 12:
                _addFan(FanType.FAN_MINGGANG, new ArrayList<>(minggang));
                _addFan(FanType.FAN_SHUANGANKE, new ArrayList<>(anke));
                break;
            default: {
                int cntAngang = angang.size();
                int cntMinggang = minggang.size();
                int cntAnke = anke.size();
                if (cntMinggang == 4) {
                    _addFan(FanType.FAN_SIGANG, new ArrayList<>(minggang));
                } else if (cntAnke == 4) {
                    _addFan(FanType.FAN_SIANKE, new ArrayList<>(anke));
                } else if (cntMinggang == 3) {
                    _addFan(FanType.FAN_SANGANG, new ArrayList<>(minggang));
                } else if (cntAnke == 3) {
                    _addFan(FanType.FAN_SANANKE, new ArrayList<>(anke));
                } else if (cntAngang == 2) {
                    _addFan(FanType.FAN_SHUANGANGANG, new ArrayList<>(angang));
                } else if (cntMinggang == 2) {
                    _addFan(FanType.FAN_SHUANGMINGGANG, new ArrayList<>(minggang));
                } else if (cntAnke == 2) {
                    _addFan(FanType.FAN_SHUANGANKE, new ArrayList<>(anke));
                } else if (cntMinggang == 1 && cntAngang == 1) {
                    List<Integer> v = new ArrayList<>();
                    v.add(angang.get(0));
                    v.add(minggang.get(0));
                    _addFan(FanType.FAN_MINGANGANG, v);
                } else if (cntAngang == 1) {
                    _addFan(FanType.FAN_ANGANG, new ArrayList<>(angang));
                } else if (cntMinggang == 1) {
                    _addFan(FanType.FAN_MINGGANG, new ArrayList<>(minggang));
                }
                break;
            }
        }
        if (_hasFan(FanType.FAN_SIGANG)) {
            _excludeFan(FanType.FAN_PENGPENGHU, new ArrayList<>());
            for (int i = 0; i < packs.size(); i++) {
                if (packs.get(i).isJiang()) {
                    List<Integer> v = new ArrayList<>();
                    v.add(i);
                    _excludeFan(FanType.FAN_DANDIAOJIANG, v);
                    break;
                }
            }
        }
        if (_hasFan(FanType.FAN_SHUANGANGANG)) {
            if (!fanTable[FanType.FAN_SHUANGANGANG.ordinal()].isEmpty()) {
                List<Integer> v = new ArrayList<>(fanTable[FanType.FAN_SHUANGANGANG.ordinal()].get(0));
                _excludeFan(FanType.FAN_SHUANGANKE, v);
            }
        }
        if (_hasFan(FanType.FAN_SIANKE)) {
            _excludeFan(FanType.FAN_PENGPENGHU, new ArrayList<>());
            _excludeFan(FanType.FAN_BUQIUREN, new ArrayList<>());
            _excludeFan(FanType.FAN_MENQIANQING, new ArrayList<>());
        }
    }

    private void _countAssociatedCombinationFan(Handtiles ht, List<Pack> packs) {
        class AssocEntry {
            FanType type;
            List<Integer> indices;

            AssocEntry(FanType type, List<Integer> indices) {
                this.type = type;
                this.indices = indices;
            }
        }
        List<AssocEntry> e = new ArrayList<>();
        List<Integer> shunziId = new ArrayList<>();
        List<Integer> kegangId = new ArrayList<>();
        List<Integer> jiangId = new ArrayList<>();
        for (int i = 0; i < packs.size(); i++) {
            Pack p = packs.get(i);
            if (p.isShunzi()) {
                shunziId.add(i);
            } else if (p.isKeGang()) {
                kegangId.add(i);
            } else if (p.isJiang()) {
                jiangId.add(i);
            }
        }
        List<Integer> fengKegang = new ArrayList<>();
        List<Integer> fengJiang = new ArrayList<>();
        for (int i = 0; i < packs.size(); i++) {
            Tile mt = packs.get(i).getMiddleTile();
            if (mt.isFeng()) {
                if (packs.get(i).isKeGang()) {
                    fengKegang.add(i);
                } else {
                    fengJiang.add(i);
                }
            }
        }
        if (fengKegang.size() == 4) {
            e.add(new AssocEntry(FanType.FAN_DASIXI, new ArrayList<>(fengKegang)));
        }
        if (fengKegang.size() == 3 && fengJiang.size() == 1) {
            List<Integer> v = new ArrayList<>();
            v.add(fengKegang.get(0));
            v.add(fengKegang.get(1));
            v.add(fengKegang.get(2));
            v.add(fengJiang.get(0));
            e.add(new AssocEntry(FanType.FAN_XIAOSIXI, v));
        }
        if (fengKegang.size() == 3) {
            List<Integer> v = new ArrayList<>();
            v.add(fengKegang.get(0));
            v.add(fengKegang.get(1));
            v.add(fengKegang.get(2));
            e.add(new AssocEntry(FanType.FAN_SANFENGKE, v));
        }
        List<Integer> jianKegang = new ArrayList<>();
        List<Integer> jianJiang = new ArrayList<>();
        for (int i = 0; i < packs.size(); i++) {
            Tile mt = packs.get(i).getMiddleTile();
            if (mt.isJian()) {
                if (packs.get(i).isKeGang()) {
                    jianKegang.add(i);
                } else {
                    jianJiang.add(i);
                }
            }
        }
        if (jianKegang.size() == 3) {
            e.add(new AssocEntry(FanType.FAN_DASANYUAN, new ArrayList<>(jianKegang)));
        }
        if (jianKegang.size() == 2 && jianJiang.size() == 1) {
            List<Integer> v = new ArrayList<>();
            v.add(jianKegang.get(0));
            v.add(jianKegang.get(1));
            v.add(jianJiang.get(0));
            e.add(new AssocEntry(FanType.FAN_XIAOSANYUAN, v));
        }
        if (jianKegang.size() == 2) {
            List<Integer> v = new ArrayList<>();
            v.add(jianKegang.get(0));
            v.add(jianKegang.get(1));
            e.add(new AssocEntry(FanType.FAN_SHUANGJIANKE, v));
        }
        List<Integer> shunzi123 = new ArrayList<>();
        List<Integer> shunzi789 = new ArrayList<>();
        for (int id : shunziId) {
            int rank = packs.get(id).getMiddleTile().rank();
            if (rank == Tile.RANK_2) {
                shunzi123.add(id);
            } else if (rank == Tile.RANK_8) {
                shunzi789.add(id);
            }
        }
        if (shunzi123.size() == 2 && shunzi789.size() == 2 && jiangId.size() == 1 && packs.get(jiangId.get(0)).getMiddleTile().rank() == 5) {
            int suit123_1 = packs.get(shunzi123.get(0)).getMiddleTile().suit();
            int suit123_2 = packs.get(shunzi123.get(1)).getMiddleTile().suit();
            int suit789_1 = packs.get(shunzi789.get(0)).getMiddleTile().suit();
            int suit789_2 = packs.get(shunzi789.get(1)).getMiddleTile().suit();
            int suitJiang = packs.get(jiangId.get(0)).getMiddleTile().suit();
            if (suit123_1 == suit123_2 && suit123_1 == suit789_1 && suit123_1 == suit789_2 && suit123_1 == suitJiang) {
                List<Integer> v = new ArrayList<>();
                v.add(shunzi123.get(0));
                v.add(shunzi123.get(1));
                v.add(shunzi789.get(0));
                v.add(shunzi789.get(1));
                v.add(jiangId.get(0));
                e.add(new AssocEntry(FanType.FAN_YISESHUANGLONGHUI, v));
            } else if (((suit123_1 == suit789_1 && suit123_2 == suit789_2) || (suit123_1 == suit789_2 && suit123_2 == suit789_1))
                    && suit123_1 != suit123_2 && suit123_1 != suitJiang && suit123_2 != suitJiang) {
                List<Integer> v = new ArrayList<>();
                v.add(shunzi123.get(0));
                v.add(shunzi123.get(1));
                v.add(shunzi789.get(0));
                v.add(shunzi789.get(1));
                v.add(jiangId.get(0));
                e.add(new AssocEntry(FanType.FAN_SANSESHUANGLONGHUI, v));
            }
        }
        for (int i = 0; i < shunziId.size(); i++) {
            for (int j = i + 1; j < shunziId.size(); j++) {
                if (packs.get(shunziId.get(i)).equals(packs.get(shunziId.get(j)))) {
                    List<Integer> vYG = new ArrayList<>();
                    vYG.add(shunziId.get(i));
                    vYG.add(shunziId.get(j));
                    e.add(new AssocEntry(FanType.FAN_YIBANGAO, vYG));
                } else {
                    continue;
                }
                for (int k = j + 1; k < shunziId.size(); k++) {
                    if (packs.get(shunziId.get(j)).equals(packs.get(shunziId.get(k)))) {
                        List<Integer> v3 = new ArrayList<>();
                        v3.add(shunziId.get(i));
                        v3.add(shunziId.get(j));
                        v3.add(shunziId.get(k));
                        e.add(new AssocEntry(FanType.FAN_YISESANTONGSHUN, v3));
                    } else {
                        continue;
                    }
                    for (int l = k + 1; l < shunziId.size(); l++) {
                        if (packs.get(shunziId.get(k)).equals(packs.get(shunziId.get(l)))) {
                            List<Integer> v4 = new ArrayList<>();
                            v4.add(shunziId.get(i));
                            v4.add(shunziId.get(j));
                            v4.add(shunziId.get(k));
                            v4.add(shunziId.get(l));
                            e.add(new AssocEntry(FanType.FAN_YISESITONGSHUN, v4));
                        }
                    }
                }
            }
        }
        List<Integer> sortedKegang = new ArrayList<>();
        List<int[]> kegangTemp = new ArrayList<>();
        for (int id : kegangId) {
            Tile mt = packs.get(id).getMiddleTile();
            if (mt.isShu()) {
                kegangTemp.add(new int[]{mt.rank(), id});
            }
        }
        kegangTemp.sort((a, b) -> Integer.compare(a[0], b[0]));
        for (int[] pr : kegangTemp) {
            sortedKegang.add(pr[1]);
        }
        for (int i = 0; i < sortedKegang.size(); i++) {
            for (int j = i + 1; j < sortedKegang.size(); j++) {
                Tile ti = packs.get(sortedKegang.get(i)).getMiddleTile();
                Tile tj = packs.get(sortedKegang.get(j)).getMiddleTile();
                if (ti.rank() != tj.rank() - 1) {
                    continue;
                }
                for (int k = j + 1; k < sortedKegang.size(); k++) {
                    Tile tk = packs.get(sortedKegang.get(k)).getMiddleTile();
                    if (tj.rank() != tk.rank() - 1) {
                        continue;
                    }
                    if (ti.suit() != tj.suit() && ti.suit() != tk.suit() && tj.suit() != tk.suit()) {
                        List<Integer> v = new ArrayList<>();
                        v.add(sortedKegang.get(i));
                        v.add(sortedKegang.get(j));
                        v.add(sortedKegang.get(k));
                        e.add(new AssocEntry(FanType.FAN_SANSESANJIEGAO, v));
                    } else if (ti.suit() == tj.suit() && ti.suit() == tk.suit()) {
                        List<Integer> v = new ArrayList<>();
                        v.add(sortedKegang.get(i));
                        v.add(sortedKegang.get(j));
                        v.add(sortedKegang.get(k));
                        e.add(new AssocEntry(FanType.FAN_YISESANJIEGAO, v));
                    } else {
                        continue;
                    }
                    for (int l = k + 1; l < sortedKegang.size(); l++) {
                        Tile tl = packs.get(sortedKegang.get(l)).getMiddleTile();
                        if (ti.suit() == tj.suit() && ti.suit() == tk.suit() && ti.suit() == tl.suit()) {
                            List<Integer> v4 = new ArrayList<>();
                            v4.add(sortedKegang.get(i));
                            v4.add(sortedKegang.get(j));
                            v4.add(sortedKegang.get(k));
                            v4.add(sortedKegang.get(l));
                            e.add(new AssocEntry(FanType.FAN_YISESIJIEGAO, v4));
                        }
                    }
                }
            }
        }
        List<Integer> sortedShunzi = new ArrayList<>();
        List<int[]> shunziTemp = new ArrayList<>();
        for (int id : shunziId) {
            shunziTemp.add(new int[]{packs.get(id).getMiddleTile().rank(), id});
        }
        shunziTemp.sort((a, b) -> Integer.compare(a[0], b[0]));
        for (int[] pr : shunziTemp) {
            sortedShunzi.add(pr[1]);
        }
        for (int i = 0; i < sortedShunzi.size(); i++) {
            for (int j = i + 1; j < sortedShunzi.size(); j++) {
                Tile ti = packs.get(sortedShunzi.get(i)).getMiddleTile();
                Tile tj = packs.get(sortedShunzi.get(j)).getMiddleTile();
                int step1 = tj.rank() - ti.rank();
                if ((step1 != 1 && step1 != 2) || ti.suit() != tj.suit()) {
                    continue;
                }
                for (int k = j + 1; k < sortedShunzi.size(); k++) {
                    Tile tk = packs.get(sortedShunzi.get(k)).getMiddleTile();
                    int step2 = tk.rank() - tj.rank();
                    if ((step2 != 1 && step2 != 2) || tj.suit() != tk.suit()) {
                        continue;
                    }
                    if (step1 == step2) {
                        List<Integer> v3 = new ArrayList<>();
                        v3.add(sortedShunzi.get(i));
                        v3.add(sortedShunzi.get(j));
                        v3.add(sortedShunzi.get(k));
                        e.add(new AssocEntry(FanType.FAN_YISESANBUGAO, v3));
                    }
                    for (int l = k + 1; l < sortedShunzi.size(); l++) {
                        Tile tl = packs.get(sortedShunzi.get(l)).getMiddleTile();
                        int step3 = tl.rank() - tk.rank();
                        if ((step3 != 1 && step3 != 2) || tk.suit() != tl.suit()) {
                            continue;
                        }
                        if (step1 == step2 && step1 == step3) {
                            List<Integer> v4 = new ArrayList<>();
                            v4.add(sortedShunzi.get(i));
                            v4.add(sortedShunzi.get(j));
                            v4.add(sortedShunzi.get(k));
                            v4.add(sortedShunzi.get(l));
                            e.add(new AssocEntry(FanType.FAN_YISESIBUGAO, v4));
                        }
                    }
                }
            }
        }
        for (int i = 0; i < sortedShunzi.size(); i++) {
            for (int j = i + 1; j < sortedShunzi.size(); j++) {
                Tile ti = packs.get(sortedShunzi.get(i)).getMiddleTile();
                Tile tj = packs.get(sortedShunzi.get(j)).getMiddleTile();
                if (tj.rank() - ti.rank() != 1 || ti.suit() == tj.suit()) {
                    continue;
                }
                for (int k = j + 1; k < sortedShunzi.size(); k++) {
                    Tile tk = packs.get(sortedShunzi.get(k)).getMiddleTile();
                    if (tk.rank() - tj.rank() != 1 || ti.suit() == tk.suit() || tj.suit() == tk.suit()) {
                        continue;
                    }
                    List<Integer> v = new ArrayList<>();
                    v.add(sortedShunzi.get(i));
                    v.add(sortedShunzi.get(j));
                    v.add(sortedShunzi.get(k));
                    e.add(new AssocEntry(FanType.FAN_SANSESANBUGAO, v));
                }
            }
        }
        List<Integer>[] rank = new List[10];
        for (int i = 0; i < rank.length; i++) {
            rank[i] = new ArrayList<>();
        }
        for (int id : shunziId) {
            int r = packs.get(id).getMiddleTile().rank();
            if (r >= 2 && r <= 8) {
                rank[r].add(id);
            }
        }
        if (!rank[2].isEmpty() && !rank[5].isEmpty() && !rank[8].isEmpty()) {
            for (int id2 : rank[2]) {
                for (int id5 : rank[5]) {
                    for (int id8 : rank[8]) {
                        int suit1 = packs.get(id2).getMiddleTile().suit();
                        int suit2 = packs.get(id5).getMiddleTile().suit();
                        int suit3 = packs.get(id8).getMiddleTile().suit();
                        if (suit1 == suit2 && suit1 == suit3) {
                            List<Integer> v = new ArrayList<>();
                            v.add(id2);
                            v.add(id5);
                            v.add(id8);
                            e.add(new AssocEntry(FanType.FAN_QINGLONG, v));
                        }
                        if (suit1 != suit2 && suit1 != suit3 && suit2 != suit3) {
                            List<Integer> v = new ArrayList<>();
                            v.add(id2);
                            v.add(id5);
                            v.add(id8);
                            e.add(new AssocEntry(FanType.FAN_HUALONG, v));
                        }
                    }
                }
            }
        }
        for (int i = 0; i < kegangId.size(); i++) {
            for (int j = i + 1; j < kegangId.size(); j++) {
                Tile ti = packs.get(kegangId.get(i)).getMiddleTile();
                Tile tj = packs.get(kegangId.get(j)).getMiddleTile();
                if (ti.isShu() && ti.rank() == tj.rank()) {
                    List<Integer> v2 = new ArrayList<>();
                    v2.add(kegangId.get(i));
                    v2.add(kegangId.get(j));
                    e.add(new AssocEntry(FanType.FAN_SHUANGTONGKE, v2));
                } else {
                    continue;
                }
                for (int k = j + 1; k < kegangId.size(); k++) {
                    Tile tk = packs.get(kegangId.get(k)).getMiddleTile();
                    if (tj.rank() == tk.rank()) {
                        List<Integer> v3 = new ArrayList<>();
                        v3.add(kegangId.get(i));
                        v3.add(kegangId.get(j));
                        v3.add(kegangId.get(k));
                        e.add(new AssocEntry(FanType.FAN_SANTONGKE, v3));
                    } else {
                        continue;
                    }
                }
            }
        }
        for (int i = 0; i < shunziId.size(); i++) {
            for (int j = i + 1; j < shunziId.size(); j++) {
                Tile ti = packs.get(shunziId.get(i)).getMiddleTile();
                Tile tj = packs.get(shunziId.get(j)).getMiddleTile();
                if (ti.rank() != tj.rank() || ti.suit() == tj.suit()) {
                    continue;
                }
                for (int k = j + 1; k < shunziId.size(); k++) {
                    Tile tk = packs.get(shunziId.get(k)).getMiddleTile();
                    if (tj.rank() == tk.rank() && ti.suit() != tk.suit() && tj.suit() != tk.suit()) {
                        List<Integer> v = new ArrayList<>();
                        v.add(shunziId.get(i));
                        v.add(shunziId.get(j));
                        v.add(shunziId.get(k));
                        e.add(new AssocEntry(FanType.FAN_SANSESANTONGSHUN, v));
                    }
                }
            }
        }
        for (int i = 0; i < shunziId.size(); i++) {
            for (int j = i + 1; j < shunziId.size(); j++) {
                Tile ti = packs.get(shunziId.get(i)).getMiddleTile();
                Tile tj = packs.get(shunziId.get(j)).getMiddleTile();
                if (ti.suit() != tj.suit()) {
                    if (ti.rank() == tj.rank()) {
                        List<Integer> v = new ArrayList<>();
                        v.add(shunziId.get(i));
                        v.add(shunziId.get(j));
                        e.add(new AssocEntry(FanType.FAN_XIXIANGFENG, v));
                    }
                } else {
                    if (ti.rank() == tj.rank() + 3 || ti.rank() == tj.rank() - 3) {
                        List<Integer> v = new ArrayList<>();
                        v.add(shunziId.get(i));
                        v.add(shunziId.get(j));
                        e.add(new AssocEntry(FanType.FAN_LIANLIU, v));
                    } else if (ti.rank() == tj.rank() + 6 || ti.rank() == tj.rank() - 6) {
                        List<Integer> v = new ArrayList<>();
                        v.add(shunziId.get(i));
                        v.add(shunziId.get(j));
                        e.add(new AssocEntry(FanType.FAN_LAOSHAOFU, v));
                    }
                }
            }
        }
        class Status {
            int[] parent = new int[5];
            List<Integer> eid = new ArrayList<>();
            int fanCnt;

            Status() {
                for (int i = 0; i < 5; i++) {
                    parent[i] = i;
                }
                fanCnt = 0;
            }

            Status(Status other) {
                System.arraycopy(other.parent, 0, parent, 0, 5);
                eid.addAll(other.eid);
                fanCnt = other.fanCnt;
            }

            int find(int x) {
                if (parent[x] == x) {
                    return x;
                }
                parent[x] = find(parent[x]);
                return parent[x];
            }

            void uni(int a, int b) {
                a = find(a);
                b = find(b);
                if (a == b) {
                    return;
                }
                if (a < b) {
                    parent[b] = a;
                } else {
                    parent[a] = b;
                }
            }

            boolean uniFan(int id, List<AssocEntry> es) {
                List<Integer> v = es.get(id).indices;
                for (int i = 0; i < v.size(); i++) {
                    for (int j = i + 1; j < v.size(); j++) {
                        if (find(v.get(i)) == find(v.get(j))) {
                            return false;
                        }
                    }
                }
                for (int i = 1; i < v.size(); i++) {
                    uni(v.get(i), v.get(i - 1));
                }
                eid.add(id);
                fanCnt += FAN_SCORE[es.get(id).type.ordinal()];
                return true;
            }

            int hash() {
                int ret = 0;
                for (int i = 0; i < 5; i++) {
                    ret *= 5;
                    ret += find(i);
                }
                return ret;
            }
        }
        Map<Integer, Integer> mp = new HashMap<>();
        int bestFan = 0;
        Status bestStatus = new Status();
        Queue<Status> q = new ArrayDeque<>();
        q.add(new Status());
        while (!q.isEmpty()) {
            Status sFront = q.poll();
            for (int i = 0; i < e.size(); i++) {
                Status s = new Status(sFront);
                if (s.uniFan(i, e)) {
                    int h = s.hash();
                    int old = mp.getOrDefault(h, 0);
                    if (s.fanCnt > old) {
                        q.add(s);
                        mp.put(h, s.fanCnt);
                        if (s.fanCnt > bestFan) {
                            bestFan = s.fanCnt;
                            bestStatus = s;
                        }
                    }
                }
            }
        }
        for (int id : bestStatus.eid) {
            AssocEntry entry = e.get(id);
            _addFan(entry.type, entry.indices);
            switch (entry.type) {
                case FAN_DASIXI:
                    _excludeFan(FanType.FAN_PENGPENGHU, new ArrayList<>());
                    for (int idx : entry.indices) {
                        Tile mt = packs.get(idx).getMiddleTile();
                        if (mt.isFeng()) {
                            List<Integer> v = new ArrayList<>();
                            v.add(idx);
                            _excludeFan(FanType.FAN_QUANFENGKE, v);
                            _excludeFan(FanType.FAN_MENFENGKE, v);
                            _excludeFan(FanType.FAN_YAOJIUKE, v);
                        }
                    }
                    break;
                case FAN_DASANYUAN:
                case FAN_XIAOSANYUAN:
                case FAN_SHUANGJIANKE:
                    for (int idx : entry.indices) {
                        Tile mt = packs.get(idx).getMiddleTile();
                        if (mt.isJian()) {
                            List<Integer> v = new ArrayList<>();
                            v.add(idx);
                            _excludeFan(FanType.FAN_JIANKE, v);
                            _excludeFan(FanType.FAN_YAOJIUKE, v);
                        }
                    }
                    break;
                case FAN_XIAOSIXI:
                case FAN_SANFENGKE:
                    for (int idx : entry.indices) {
                        Tile mt = packs.get(idx).getMiddleTile();
                        if (mt.isFeng()) {
                            List<Integer> v = new ArrayList<>();
                            v.add(idx);
                            _excludeFan(FanType.FAN_YAOJIUKE, v);
                        }
                    }
                    break;
                case FAN_YISESHUANGLONGHUI:
                    _excludeFan(FanType.FAN_QINGYISE, new ArrayList<>());
                    _excludeFan(FanType.FAN_PINGHU, new ArrayList<>());
                    _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
                    break;
                case FAN_YISESITONGSHUN:
                    _excludeFan(FanType.FAN_SIGUIYI, new ArrayList<>());
                    _excludeFan(FanType.FAN_SIGUIYI, new ArrayList<>());
                    _excludeFan(FanType.FAN_SIGUIYI, new ArrayList<>());
                    break;
                case FAN_YISESIJIEGAO:
                    _excludeFan(FanType.FAN_PENGPENGHU, new ArrayList<>());
                    break;
                case FAN_SANSESHUANGLONGHUI:
                    _excludeFan(FanType.FAN_PINGHU, new ArrayList<>());
                    break;
                default:
                    break;
            }
        }
    }

    private void _countSinglePackFan(Handtiles ht, List<Pack> packs) {
        for (int i = 0; i < packs.size(); i++) {
            Pack p = packs.get(i);
            if (p.isKeGang()) {
                Tile mt = p.getMiddleTile();
                if (mt.isJian()) {
                    List<Integer> v = new ArrayList<>();
                    v.add(i);
                    _addFan(FanType.FAN_JIANKE, v);
                    _excludeFan(FanType.FAN_YAOJIUKE, v);
                }
                if (mt.getId() == ht.getQuanfeng()) {
                    List<Integer> v = new ArrayList<>();
                    v.add(i);
                    _addFan(FanType.FAN_QUANFENGKE, v);
                    _excludeFan(FanType.FAN_YAOJIUKE, v);
                }
                if (mt.getId() == ht.getMenfeng()) {
                    List<Integer> v = new ArrayList<>();
                    v.add(i);
                    _addFan(FanType.FAN_MENFENGKE, v);
                    _excludeFan(FanType.FAN_YAOJIUKE, v);
                }
                if (mt.isYaojiu()) {
                    List<Integer> v = new ArrayList<>();
                    v.add(i);
                    _addFan(FanType.FAN_YAOJIUKE, v);
                }
            }
        }
    }

    private void _countWinModeFan(Handtiles ht, List<Pack> packs, Pack zuhelongPack, List<Tile> ting) {
        if (ht.isHaidi() == 1 && ht.isZimo() == 1) {
            _addFan(FanType.FAN_MIAOSHOUHUICHUN, new ArrayList<>());
            _excludeFan(FanType.FAN_ZIMO, new ArrayList<>());
        }
        if (ht.isHaidi() == 1 && ht.isZimo() == 0) {
            _addFan(FanType.FAN_HAIDILAOYUE, new ArrayList<>());
        }
        if (ht.isGang() == 1 && ht.isZimo() == 1) {
            _addFan(FanType.FAN_GANGSHANGKAIHUA, new ArrayList<>());
            _excludeFan(FanType.FAN_ZIMO, new ArrayList<>());
        }
        if (ht.isGang() == 1 && ht.isZimo() == 0) {
            _addFan(FanType.FAN_QIANGGANGHU, new ArrayList<>());
            _excludeFan(FanType.FAN_HUJUEZHANG, new ArrayList<>());
        }
        if (ht.isTotallyFulu() == 1 && ht.isZimo() == 0) {
            _addFan(FanType.FAN_QUANQIUREN, new ArrayList<>());
            int jiangIdx = -1;
            for (int i = 0; i < packs.size(); i++) {
                if (packs.get(i).isJiang()) {
                    jiangIdx = i;
                    break;
                }
            }
            if (jiangIdx >= 0) {
                List<Integer> v = new ArrayList<>();
                v.add(jiangIdx);
                _excludeFan(FanType.FAN_DANDIAOJIANG, v);
            }
        }
        if (ht.isMenqing() == 1 && ht.isZimo() == 1) {
            _addFan(FanType.FAN_BUQIUREN, new ArrayList<>());
            _excludeFan(FanType.FAN_MENQIANQING, new ArrayList<>());
            _excludeFan(FanType.FAN_ZIMO, new ArrayList<>());
        }
        if (ht.isJuezhang() == 1) {
            _addFan(FanType.FAN_HUJUEZHANG, new ArrayList<>());
            int jiangIdx = -1;
            for (int i = 0; i < packs.size(); i++) {
                if (packs.get(i).isJiang()) {
                    jiangIdx = i;
                    break;
                }
            }
            if (jiangIdx >= 0) {
                List<Integer> v = new ArrayList<>();
                v.add(jiangIdx);
                _excludeFan(FanType.FAN_DANDIAOJIANG, v);
            }
        }
        if (ht.isMenqing() == 1) {
            _addFan(FanType.FAN_MENQIANQING, new ArrayList<>());
        }
        if (ting.size() == 1 && (zuhelongPack.getZuhelongBitmap() & ht.getLastLipai().getBitmap()) == 0L) {
            for (int i = 0; i < packs.size(); i++) {
                Pack p = packs.get(i);
                Tile t = p.getMiddleTile();
                if (p.haveLastTile()) {
                    if (p.isJiang()) {
                        List<Integer> v = new ArrayList<>();
                        v.add(i);
                        _addFan(FanType.FAN_DANDIAOJIANG, v);
                    } else if (p.isShunzi()) {
                        int rank = t.rank();
                        int lastRank = ht.getLastLipai().rank();
                        if ((rank == Tile.RANK_2 && lastRank == Tile.RANK_3)
                                || (rank == Tile.RANK_8 && lastRank == Tile.RANK_7)) {
                            List<Integer> v = new ArrayList<>();
                            v.add(i);
                            _addFan(FanType.FAN_BIANZHANG, v);
                        } else if (rank == lastRank) {
                            List<Integer> v = new ArrayList<>();
                            v.add(i);
                            _addFan(FanType.FAN_KANZHANG, v);
                        }
                    }
                    break;
                }
            }
        }
        if (ht.isZimo() == 1) {
            _addFan(FanType.FAN_ZIMO, new ArrayList<>());
            if ((_hasFan(FanType.FAN_JIULIANBAODENG) || _hasFan(FanType.FAN_SIANKE))
                    && !_hasFan(FanType.FAN_MIAOSHOUHUICHUN)
                    && !_hasFan(FanType.FAN_GANGSHANGKAIHUA)) {
                excludedFanTable[FanType.FAN_ZIMO.ordinal()].clear();
            }
        }
    }

    private void _countBasicFan(Handtiles ht, List<Pack> packs, Pack zuhelongPack) {
        _countOverallAttrFan(ht, packs, zuhelongPack);
        _countKeGangFan(ht, packs);
        _countAssociatedCombinationFan(ht, packs);
        _countSinglePackFan(ht, packs);
        _countWinModeFan(ht, packs, zuhelongPack, calcTing(ht));
    }

    private void _getMaxFan() {
        _fanTableExclude();
        _fanTableCount();
        if (totFan > totFanRes) {
            totFanRes = totFan;
            for (int i = 1; i < fanTable.length; i++) {
                fanTableRes[i].clear();
                fanTableRes[i].addAll(fanTable[i]);
            }
            fanPacksRes.clear();
            fanPacksRes.addAll(fanPacks);
        }
        _clearTable();
    }

    public int judgeHu(Handtiles ht) {
        if (_judgeCompleteSpecialHu(ht) != FanType.FAN_INVALID) {
            return 1;
        }
        if (_judgeQidui(ht) != FanType.FAN_INVALID) {
            return 1;
        }
        if (_judgeBasicHu(ht) != 0) {
            return 1;
        }
        if (_judgeZuhelongBasicHu(ht) != 0) {
            return 1;
        }
        return 0;
    }

    public int judgeHuTile(Handtiles ht, Tile t) {
        ht.setTile(t);
        int ret = judgeHu(ht);
        ht.setTile(new Tile(Tile.TILE_INVALID));
        return ret;
    }

    public List<Tile> calcTing(Handtiles ht) {
        Handtiles temp = ht;
        List<Tile> ting = new ArrayList<>();
        for (int i = 1; i < Tile.TILE_SIZE; i++) {
            Tile tile = new Tile(i);
            temp.setTile(tile);
            if (judgeHu(temp) != 0 && temp.handTileCount(tile) != 5) {
                ting.add(tile);
            }
        }
        temp.setTile(new Tile(Tile.TILE_INVALID));
        return ting;
    }

    public void countFan(Handtiles ht) {
        _clear();
        FanType f;
        f = _judgeCompleteSpecialHu(ht);
        int flagQuanbukao = 0;
        if (f != FanType.FAN_INVALID) {
            flagQuanbukao = 1;
            _addFan(f, new ArrayList<>());
            _countWinModeFan(ht, new ArrayList<>(), new Pack(), new ArrayList<>());
            int zuhelongType = _judgeZuhelong(ht.lipaiBitmap());
            if (zuhelongType != 0) {
                fanPacks.clear();
                fanPacks.add(new Pack(Pack.PACK_TYPE_ZUHELONG, new Tile(), zuhelongType, 0));
                List<Integer> v = new ArrayList<>();
                v.add(0);
                _addFan(FanType.FAN_ZUHELONG, v);
            }
            _excludeFan(FanType.FAN_BUQIUREN, new ArrayList<>());
            _excludeFan(FanType.FAN_MENQIANQING, new ArrayList<>());
            if (ht.isZimo() == 1) {
                int idx = FanType.FAN_ZIMO.ordinal();
                fanTable[idx].clear();
                excludedFanTable[idx].clear();
                fanTable[idx].add(new ArrayList<>());
            }
            _getMaxFan();
        }
        f = _judgeQidui(ht);
        if (f != FanType.FAN_INVALID) {
            _addFan(f, new ArrayList<>());
            _countOverallAttrFan(ht, new ArrayList<>(), new Pack());
            _countWinModeFan(ht, new ArrayList<>(), new Pack(), new ArrayList<>());
            _excludeFan(FanType.FAN_BUQIUREN, new ArrayList<>());
            _excludeFan(FanType.FAN_MENQIANQING, new ArrayList<>());
            if (f == FanType.FAN_LIANQIDUI) {
                _excludeFan(FanType.FAN_QINGYISE, new ArrayList<>());
                _excludeFan(FanType.FAN_WUZI, new ArrayList<>());
            }
            if (ht.isZimo() == 1) {
                int idx = FanType.FAN_ZIMO.ordinal();
                fanTable[idx].clear();
                excludedFanTable[idx].clear();
                fanTable[idx].add(new ArrayList<>());
            }
            _getMaxFan();
        }
        List<Tile> sortedLipai = new ArrayList<>();
        int zuhelongType = _judgeZuhelong(ht.lipaiBitmap());
        long zuhelongBitmap = zuhelongType == 0 ? 0L : Pack.ZUHELONG_BITMAP[zuhelongType];
        if (zuhelongBitmap != 0L) {
            long bitmapTemp = zuhelongBitmap;
            for (Tile t : ht.lipai) {
                long b = t.getBitmap();
                if ((b & bitmapTemp) != 0L) {
                    bitmapTemp ^= b;
                } else {
                    sortedLipai.add(t);
                }
            }
        } else {
            sortedLipai.addAll(ht.lipai);
        }
        sortedLipai.sort(null);
        List<Pack> packs = new ArrayList<>(ht.fulu);
        if (zuhelongBitmap != 0L && flagQuanbukao == 0) {
            _dfs(ht, sortedLipai, 1 - ht.fulu.size(), 1, packs, 1, new Pack(Pack.PACK_TYPE_ZUHELONG, new Tile(), zuhelongType, 0));
            int wufanIdx = FanType.FAN_WUFANHU.ordinal();
            if (totFanRes == FAN_SCORE[wufanIdx] && fanTableRes[wufanIdx].size() == 1) {
                fanTableRes[wufanIdx].clear();
                totFanRes = 0;
            }
            fanPacksRes.add(new Pack(Pack.PACK_TYPE_ZUHELONG, new Tile(), zuhelongType, 0));
            List<Integer> v = new ArrayList<>();
            v.add(fanPacksRes.size() - 1);
            fanTableRes[FanType.FAN_ZUHELONG.ordinal()].add(v);
            totFanRes += FAN_SCORE[FanType.FAN_ZUHELONG.ordinal()];
        } else {
            _dfs(ht, sortedLipai, 4 - ht.fulu.size(), 1, packs, 1, new Pack());
        }
        int cntHua = ht.huapaiCount();
        for (int i = 0; i < cntHua; i++) {
            fanTableRes[FanType.FAN_HUAPAI.ordinal()].add(new ArrayList<>());
        }
        totFanRes += cntHua;
    }
}
