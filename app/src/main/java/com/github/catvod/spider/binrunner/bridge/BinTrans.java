package com.github.catvod.spider.binrunner.bridge;

import com.github.catvod.spider.binrunner.util.BinLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * 简繁转换桥接类
 * 提供简体中文和繁体中文之间的转换功能
 */
public class BinTrans {

    // 简体到繁体映射表
    private static final Map<Character, Character> SIMPLE_TO_TRADITIONAL = new HashMap<>();
    // 繁体到简体映射表
    private static final Map<Character, Character> TRADITIONAL_TO_SIMPLE = new HashMap<>();

    // 初始化映射表（常用字）
    static {
        initMappings();
    }

    /**
     * 初始化简繁映射表
     */
    private static void initMappings() {
        // 常用简繁对照
        String simple = "爱宝备笔标变别布参仓产长尝车齿冲出处传辞词达当导灯敌电东独断对队尔发范飞凤妇该干刚钢高格宫关观广归柜国过海号贺红后华话还汇会护机际几记济纪极佳价坚见将进经精竞举决绝军开颗课扩来乐类离历礼丽联怜练临陆录绿罗论马买麦满门面庙难拟鸟宁农区齐亲轻请穷确热认荣入杀伤烧设圣师时视寿书输孙态谈体条铁听同头图团维伟无武务物西习细戏系现线向象协写行学选压亚严言验养药业医艺义应营印拥优犹游语欲远约运杂灾赞脏择泽张账照这争证职执质种众专装状资总组";
        String traditional = "愛寶備筆標變別佈參倉產長嘗車齒沖出處傳辭詞達當導燈敵電東獨斷對隊爾發範飛鳳婦該乾剛鋼高格宮關觀廣歸櫃國過海號賀紅後華話還匯會護機際幾記濟紀極佳價堅見將進經精競舉決絕軍開顆課擴來樂類離歷禮麗聯憐練臨陸錄綠羅論馬買麥滿門面廟難擬鳥寧農區齊親輕請窮確熱認榮入殺傷燒設聖師時視壽書輸孫態談體條鐵聽同頭圖團維偉無武務物西習細戲繫現線向象協寫行學選壓亞嚴言驗養藥業醫藝義應營印擁優猶遊語慾遠約運雜災讚臟擇澤張賬照這爭證職執質種眾專裝狀資總組";

        for (int i = 0; i < simple.length() && i < traditional.length(); i++) {
            SIMPLE_TO_TRADITIONAL.put(simple.charAt(i), traditional.charAt(i));
            TRADITIONAL_TO_SIMPLE.put(traditional.charAt(i), simple.charAt(i));
        }
    }

    /**
     * 简体转繁体
     * @param text 简体中文文本
     * @return 繁体中文文本
     */
    public static String toTraditional(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        try {
            StringBuilder result = new StringBuilder();
            for (char c : text.toCharArray()) {
                Character tc = SIMPLE_TO_TRADITIONAL.get(c);
                result.append(tc != null ? tc : c);
            }
            return result.toString();
        } catch (Exception e) {
            BinLogger.e("简体转繁体失败", e);
            return text;
        }
    }

    /**
     * 繁体转简体
     * @param text 繁体中文文本
     * @return 简体中文文本
     */
    public static String toSimplified(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        try {
            StringBuilder result = new StringBuilder();
            for (char c : text.toCharArray()) {
                Character sc = TRADITIONAL_TO_SIMPLE.get(c);
                result.append(sc != null ? sc : c);
            }
            return result.toString();
        } catch (Exception e) {
            BinLogger.e("繁体转简体失败", e);
            return text;
        }
    }

    /**
     * 简体转繁体的别名方法
     */
    public static String s2t(String text) {
        return toTraditional(text);
    }

    /**
     * 繁体转简体的别名方法
     */
    public static String t2s(String text) {
        return toSimplified(text);
    }
}
