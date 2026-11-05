package com.github.ibanetchep.artisandailyrewards.artisan;

import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The calendar's two files in the module's folder, each edited by its own screen of the editor:
 * <ul>
 *   <li>{@code data/settings.yml}: reset time, auto-claim, playtime, reminder, jokers (Settings);</li>
 *   <li>{@code data/rewards.yml}: the cycle length and the reward of each day (Rewards).</li>
 * </ul>
 * Each is the owner's copy when there is one, else the shipped one Artisan keeps under
 * {@code originals/}. Before them, everything was in {@code data/calendar.yml}: {@link #migrate}
 * splits an edited one once. Pure: no Bukkit, tested on its own.
 */
public final class CalendarFiles {

    public static final String SETTINGS = "data/settings";
    public static final String REWARDS = "data/rewards";
    /** The single file before the split. */
    static final String LEGACY = "data/calendar";
    /** Where Artisan keeps the shipped files the owner has not changed. */
    static final String ORIGINALS = "originals";

    /** The keys of {@code data/rewards.yml}; every other key of the calendar is a setting. */
    static final Set<String> REWARD_KEYS = Set.of("cycle_length", "days", "default_day");

    private CalendarFiles() {
    }

    /**
     * The file Artisan loads for {@code stem} ({@code data/settings}): the owner's
     * {@code .yml}, else {@code .yaml}, else the shipped one under {@code originals/}; null when
     * none exists yet (a first start, before Artisan wrote the shipped files).
     */
    public static @Nullable File find(File folder, String stem) {
        File own = ownerFile(folder, stem);
        if (own != null) return own;
        return ownerFile(new File(folder, ORIGINALS), stem);
    }

    /** {@code <stem>.yml}, else {@code <stem>.yaml}, in {@code dir}; null for neither. */
    static @Nullable File ownerFile(File dir, String stem) {
        File yml = new File(dir, stem + ".yml");
        if (yml.isFile()) return yml;
        File yaml = new File(dir, stem + ".yaml");
        return yaml.isFile() ? yaml : null;
    }

    /**
     * The settings and the rewards as one calendar, the shape {@code CalendarSettings.read}
     * takes. A missing file adds nothing (its keys keep their defaults); a reward key found in
     * the settings file, or the reverse, is ignored, so each key has one place.
     */
    public static Map<String, Object> read(File folder) throws IOException {
        Map<String, Object> calendar = new LinkedHashMap<>();
        copy(load(find(folder, SETTINGS)), calendar, false);
        copy(load(find(folder, REWARDS)), calendar, true);
        return calendar;
    }

    /** True when neither file exists yet, owner's or shipped. */
    public static boolean noneYet(File folder) {
        return find(folder, SETTINGS) == null && find(folder, REWARDS) == null;
    }

    private static void copy(Map<?, ?> from, Map<String, Object> into, boolean rewards) {
        for (Map.Entry<?, ?> e : from.entrySet()) {
            String key = String.valueOf(e.getKey());
            if (REWARD_KEYS.contains(key) != rewards) continue;
            into.put(key, e.getValue());
        }
    }

    private static Map<?, ?> load(@Nullable File file) throws IOException {
        if (file == null) return Map.of();
        Object root = new Yaml(new SafeConstructor(new LoaderOptions()))
                .load(Files.readString(file.toPath(), StandardCharsets.UTF_8));
        if (root == null) return Map.of();
        if (!(root instanceof Map<?, ?> map)) throw new IOException(file.getPath() + ": expected a YAML section");
        return map;
    }

    /** What {@link #migrate} did. */
    public enum Migration {
        /** No owner's {@code data/calendar} file: nothing to split. */
        NONE,
        /** Split into the two files; the old one is copied to the backup folder and removed. */
        SPLIT,
        /** The two files already exist beside the old one: it is left as is, unread. */
        LEFT
    }

    /**
     * Splits the owner's {@code data/calendar.yml} (or {@code .yaml}) into
     * {@code data/settings.yml} and {@code data/rewards.yml}, then copies
     * it to {@code backupDir} and removes it. Runs before Artisan syncs the shipped files, so
     * both new files count as the owner's edits and keep their values. A shipped calendar the
     * owner never changed lives under {@code originals/}: Artisan drops it on its own, nothing
     * to do. When an owner's settings or rewards file already exists, nothing is touched.
     */
    public static Migration migrate(File folder, File backupDir) throws IOException {
        File legacy = ownerFile(folder, LEGACY);
        if (legacy == null) return Migration.NONE;
        if (ownerFile(folder, SETTINGS) != null || ownerFile(folder, REWARDS) != null) return Migration.LEFT;

        Map<?, ?> calendar = load(legacy);
        Map<String, Object> settings = new LinkedHashMap<>();
        Map<String, Object> rewards = new LinkedHashMap<>();
        copy(calendar, settings, false);
        copy(calendar, rewards, true);

        write(new File(folder, SETTINGS + ".yml"), settings);
        write(new File(folder, REWARDS + ".yml"), rewards);
        Files.createDirectories(backupDir.toPath());
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Files.copy(legacy.toPath(), new File(backupDir, "calendar-" + stamp + ".yml").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        Files.delete(legacy.toPath());
        return Migration.SPLIT;
    }

    /** Written beside, then moved over: never a half-written file. */
    private static void write(File target, Map<String, Object> content) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setAllowUnicode(true);
        options.setWidth(120);
        String text = new Yaml(new SafeConstructor(new LoaderOptions()), new Representer(options), options).dump(content);
        Files.createDirectories(target.getParentFile().toPath());
        File tmp = new File(target.getParentFile(), "." + target.getName() + ".tmp");
        Files.writeString(tmp.toPath(), text, StandardCharsets.UTF_8);
        Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

}
