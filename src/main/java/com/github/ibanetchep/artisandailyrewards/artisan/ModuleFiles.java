package com.github.ibanetchep.artisandailyrewards.artisan;

import net.artisanmc.modules.api.template.TemplateDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * The files the plugin ships for its folder {@code plugins/ArtisanDailyRewards/artisan_module/}: the calendar
 * menu and the milestones menu, the settings, the rewards and their models, the {@code textured} variant of
 * both menus with its images (players with the resource pack, {@code variants.yml}), a system template Artisan keeps there
 * ({@code TemplateDeclaration.system()}). A missing file is written again, an untouched one
 * follows each new version, an edited one stays the admin's until Reset in the editor, and a
 * file already on disk before counts as edited. Artisan writes them under {@code .yml}, whatever
 * {@link #ROOT} names them.
 */
public final class ModuleFiles {

    static final String ROOT = "module";
    static final String TEMPLATE = "daily_files";
    /** Raise it whenever a file of {@link #PATHS} changes: untouched copies follow, edited ones merge. */
    static final int VERSION = 18;

    /** Version 2 split {@code data/calendar} into these two files ({@link CalendarFiles#migrate}). */
    static final List<String> PATHS = List.of(
            CalendarFiles.REWARDS + ".yaml",
            CalendarFiles.SETTINGS + ".yaml",
            "menus/calendar.yaml",
            "menus/milestones.yaml",
            "models/rewards.yaml",
            "models/settings.yaml",
            // The "textured" variant: Java players with the resource pack (variants.yml).
            "variants.yml",
            "variants/textured/menus/calendar.yaml",
            "variants/textured/menus/milestones.yaml",
            "assets/glyphs/daily_background.png",
            "assets/glyphs/daily_background.yml",
            "assets/textures/daily_back.png",
            "assets/textures/daily_back.yml",
            "assets/textures/daily_claim.png",
            "assets/textures/daily_claim.yml",
            "assets/textures/daily_claim_off.png",
            "assets/textures/daily_claim_off.yml",
            "assets/textures/daily_close.png",
            "assets/textures/daily_close.yml",
            "assets/textures/daily_day_before.png",
            "assets/textures/daily_day_before.yml",
            "assets/textures/daily_day_claim.png",
            "assets/textures/daily_day_claim.yml",
            "assets/textures/daily_day_claimed.png",
            "assets/textures/daily_day_claimed.yml",
            "assets/textures/daily_day_freeze.png",
            "assets/textures/daily_day_freeze.yml",
            "assets/textures/daily_day_missed.png",
            "assets/textures/daily_day_missed.yml",
            "assets/textures/daily_day_today_claimed.png",
            "assets/textures/daily_day_today_claimed.yml",
            "assets/textures/daily_freezes.png",
            "assets/textures/daily_freezes.yml",
            "assets/textures/daily_milestones.png",
            "assets/textures/daily_milestones.yml",
            "assets/textures/daily_next.png",
            "assets/textures/daily_next.yml",
            "assets/textures/daily_next_off.png",
            "assets/textures/daily_next_off.yml",
            "assets/textures/daily_prev.png",
            "assets/textures/daily_prev.yml",
            "assets/textures/daily_prev_off.png",
            "assets/textures/daily_prev_off.yml");

    private ModuleFiles() {
    }

    static TemplateDeclaration template(ClassLoader loader) {
        return TemplateDeclaration.fromResources(loader, ROOT, TEMPLATE, VERSION, PATHS)
                .title("Daily rewards")
                .system()
                .build();
    }

    /** The module file under each name Artisan reads, the first one it looks for first. */
    private static final List<String> MODULE_FILE_NAMES = List.of("artisan-module.yml", "manifest.yml", "manifest.yaml");

    static final String MODULE_FILE = """
            schema: module/v1
            default_lang: en
            languages:
              - en
              - fr
            """;

    /**
     * The folder's languages ({@code artisan-module.yml}), which a template never ships: written
     * when the folder has no module file under any name, so the editor offers both languages of
     * the shipped texts. True when written.
     */
    static boolean writeModuleFile(File folder) throws IOException {
        for (String name : MODULE_FILE_NAMES) {
            if (new File(folder, name).exists()) return false;
        }
        Files.createDirectories(folder.toPath());
        File target = new File(folder, MODULE_FILE_NAMES.get(0));
        File tmp = new File(folder, "." + target.getName() + ".tmp");
        Files.writeString(tmp.toPath(), MODULE_FILE, StandardCharsets.UTF_8);
        Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
        return true;
    }
}
