package io.github.piscescup.fabricmc.teamcraft.datagen;

import com.ibm.icu.text.Transliterator;

import java.util.LinkedHashMap;
import java.util.Map;

/** Converts the canonical Simplified Chinese strings into regional Traditional Chinese. */
final class TraditionalChineseTranslations {
    private static final Transliterator TO_TRADITIONAL =
        Transliterator.getInstance("Simplified-Traditional");

    private static final Map<String, String> HONG_KONG_TERMS = terms(
        "服務器", "伺服器",
        "配置", "設定",
        "默認", "預設",
        "信息", "資訊",
        "創建", "建立",
        "添加", "新增",
        "保存", "儲存",
        "數據", "資料"
    );

    private static final Map<String, String> TAIWAN_TERMS = terms(
        "服務器", "伺服器",
        "配置", "設定",
        "默認", "預設",
        "信息", "資訊",
        "在線", "線上",
        "創建", "建立",
        "添加", "新增",
        "保存", "儲存",
        "數據", "資料",
        "鼠標", "滑鼠",
        "視頻", "影片"
    );

    private TraditionalChineseTranslations() {
    }

    static String hongKong(String simplified) {
        return regionalize(simplified, HONG_KONG_TERMS);
    }

    static String taiwan(String simplified) {
        return regionalize(simplified, TAIWAN_TERMS);
    }

    private static String regionalize(String simplified, Map<String, String> terms) {
        String result = TO_TRADITIONAL.transliterate(simplified);
        for (Map.Entry<String, String> term : terms.entrySet()) {
            result = result.replace(term.getKey(), term.getValue());
        }
        return result;
    }

    private static Map<String, String> terms(String... entries) {
        Map<String, String> terms = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            terms.put(entries[i], entries[i + 1]);
        }
        return Map.copyOf(terms);
    }
}
