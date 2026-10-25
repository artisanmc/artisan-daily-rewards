package com.github.ibanetchep.artisandailyrewards.artisan;

import net.artisanmc.modules.api.template.TemplateDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * The files the plugin ships for its folder {@code plugins/Artisan/modules/daily/}: the calendar
 * menu, the calendar and its model, a system template Artisan keeps there
 * ({@code TemplateDeclaration.system()}). A missing file is written again, an untouched one
 * follows each new version, an edited one stays the admin's until Reset in the editor, and a
 * file already on disk before counts as edited. Artisan writes them under {@code .yml}, whatever
 * {@link #ROOT} names them.
 */
public final class ModuleFiles {

    static final String ROOT = "module";
    static final String TEMPLATE = "daily_files";
    /** Raise it whenever a file of {@link #PATHS} changes: untouched copies follow, edited ones merge. */
    static final int VERSION = 1;

    static final List<String> PATHS = List.of(
            "data/calendar.yaml",
            "menus/calendar.yaml",
            "models/calendar.yaml");

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
