package com.github.ibanetchep.artisandailyrewards.core;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/** The icons of the rewards file, as the calendar and milestones menus show them. */
final class Icons {

    private Icons() {
    }

    /**
     * {@code icon} without a blank {@code title}: the editor's item form writes {@code title: ""}
     * when the field is left empty, and a menu item named "" shows no name at all. Without it the
     * item keeps its own name (the material's, or the one of the item it {@code extends}).
     */
    static @Nullable Object withoutBlankTitle(@Nullable Object icon) {
        if (!(icon instanceof Map<?, ?> map) || !map.containsKey("title") || !blank(map.get("title"))) return icon;
        Map<Object, Object> out = new LinkedHashMap<>(map);
        out.remove("title");
        return out;
    }

    /** null, a blank string, or a map of languages whose texts are all blank. */
    private static boolean blank(@Nullable Object title) {
        if (title == null) return true;
        if (title instanceof String s) return s.isBlank();
        if (title instanceof Map<?, ?> byLang) {
            for (Object text : byLang.values()) if (!blank(text)) return false;
            return true;
        }
        return false;
    }
}
