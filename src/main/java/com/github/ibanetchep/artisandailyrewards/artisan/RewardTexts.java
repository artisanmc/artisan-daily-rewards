package com.github.ibanetchep.artisandailyrewards.artisan;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The icon texts of {@code data/rewards.yml} in every language. The editor keeps an icon's
 * title and lore inline in the default language and the other languages in
 * {@code lang/<lang>.yml}, at the text's address ({@code sources.rewards.default_day.icon.title},
 * {@code sources.rewards.days.7.icon.title}, {@code sources.rewards.milestones_2.icon.lore}...,
 * as Artisan's {@code SourceLocalizer} reads a config source). The calendar rows carry the icon
 * as it is, so each text becomes a {@code {lang: text}} map the menu localizes itself; a blank
 * text is left out, so a language without one falls back to another.
 * Pure: no Bukkit, tested on its own.
 */
final class RewardTexts {

    static final String BASE = "sources.rewards";

    private RewardTexts() {
    }

    /**
     * {@code calendar} with the icon texts of {@code default_day}, {@code days} and
     * {@code milestones} as per-language maps. {@code translations}: language → dotted key →
     * text, the lang files flattened; {@code defaultLang}: the language of the inline texts.
     */
    static Map<String, Object> inline(Map<String, Object> calendar, Map<String, Map<String, String>> translations,
                                      String defaultLang) {
        if (translations.isEmpty()) return calendar;
        Map<String, Object> out = new LinkedHashMap<>(calendar);
        if (out.get("default_day") instanceof Map<?, ?> day) {
            out.put("default_day", reward(day, BASE + ".default_day", translations, defaultLang));
        }
        if (out.get("days") instanceof Map<?, ?> days) {
            Map<Object, Object> each = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : days.entrySet()) {
                String address = BASE + ".days." + String.valueOf(e.getKey()).trim().toLowerCase(Locale.ROOT);
                each.put(e.getKey(), e.getValue() instanceof Map<?, ?> d ? reward(d, address, translations, defaultLang) : e.getValue());
            }
            out.put("days", each);
        }
        if (out.get("milestones") instanceof List<?> list) {
            List<Object> each = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                String address = BASE + ".milestones" + (i == 0 ? "" : "_" + (i + 1));
                Object m = list.get(i);
                each.add(m instanceof Map<?, ?> map ? reward(map, address, translations, defaultLang) : m);
            }
            out.put("milestones", each);
        }
        return out;
    }

    private static Map<Object, Object> reward(Map<?, ?> reward, String address,
                                              Map<String, Map<String, String>> translations, String defaultLang) {
        Map<Object, Object> out = new LinkedHashMap<>(reward);
        if (!(reward.get("icon") instanceof Map<?, ?> icon)) return out;
        Map<Object, Object> texts = new LinkedHashMap<>(icon);
        String base = address + ".icon";
        if (icon.containsKey("title") || has(translations, base + ".title")) {
            Object title = title(icon.get("title"), base + ".title", translations, defaultLang);
            if (title == null) texts.remove("title");
            else texts.put("title", title);
        }
        Object lore = lore(icon.get("lore"), base + ".lore", translations, defaultLang);
        if (lore != null) texts.put("lore", lore);
        out.put("icon", texts);
        return out;
    }

    /** The title in every language, a map; the value as it is when it is already one; null for none. */
    private static @Nullable Object title(@Nullable Object inline, String address,
                                          Map<String, Map<String, String>> translations, String defaultLang) {
        if (inline instanceof Map<?, ?>) return inline;
        Map<String, String> byLang = new LinkedHashMap<>();
        if (inline != null && !String.valueOf(inline).isBlank()) byLang.put(defaultLang, String.valueOf(inline));
        for (Map.Entry<String, Map<String, String>> lang : translations.entrySet()) {
            String text = lang.getValue().get(address);
            if (text != null && !text.isBlank()) byLang.putIfAbsent(lang.getKey(), text);
        }
        return byLang.isEmpty() ? null : byLang;
    }

    /**
     * The lore in every language, {@code {lang: [lines]}}, when a lang file holds lines of it;
     * null to keep it as it is. Line i sits at {@code lore}, then {@code lore_2}, {@code lore_3}...
     */
    private static @Nullable Object lore(@Nullable Object inline, String address,
                                         Map<String, Map<String, String>> translations, String defaultLang) {
        if (inline instanceof Map<?, ?>) return null;
        List<String> lines = new ArrayList<>();
        if (inline instanceof List<?> list) for (Object line : list) lines.add(line == null ? "" : String.valueOf(line));
        else if (inline != null) lines.add(String.valueOf(inline));
        Map<String, Object> byLang = new LinkedHashMap<>();
        if (!lines.isEmpty()) byLang.put(defaultLang, lines);
        boolean translated = false;
        for (Map.Entry<String, Map<String, String>> lang : translations.entrySet()) {
            if (lang.getKey().equals(defaultLang)) continue;
            List<String> own = new ArrayList<>();
            for (int i = 0; ; i++) {
                String text = lang.getValue().get(address + (i == 0 ? "" : "_" + (i + 1)));
                if (text == null) {
                    if (i < lines.size()) { own.add(lines.get(i)); continue; }
                    break;
                }
                own.add(text);
                translated = true;
            }
            if (!own.isEmpty()) byLang.put(lang.getKey(), own);
        }
        return translated ? byLang : null;
    }

    private static boolean has(Map<String, Map<String, String>> translations, String key) {
        for (Map<String, String> lang : translations.values()) if (lang.containsKey(key)) return true;
        return false;
    }

    /** A lang file's nested sections as dotted keys, {@code a: {b: x}} → {@code a.b = x}. */
    static Map<String, String> flatten(Map<?, ?> section) {
        Map<String, String> out = new LinkedHashMap<>();
        flatten("", section, out);
        return out;
    }

    private static void flatten(String prefix, Map<?, ?> section, Map<String, String> out) {
        for (Map.Entry<?, ?> e : section.entrySet()) {
            String key = prefix + e.getKey();
            if (e.getValue() instanceof Map<?, ?> nested) flatten(key + ".", nested, out);
            else if (e.getValue() != null) out.put(key, String.valueOf(e.getValue()));
        }
    }
}
