package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalendarFilesTest {

    private static void write(Path root, String path, String text) throws Exception {
        Path file = root.resolve(path);
        Files.createDirectories(file.getParent());
        Files.writeString(file, text);
    }

    private static Object yaml(Path file) throws Exception {
        return new Yaml(new SafeConstructor(new LoaderOptions())).load(Files.readString(file));
    }

    @Test
    void theOwnersFileWinsOverTheShippedOneYmlBeforeYaml(@TempDir Path tmp) throws Exception {
        File folder = tmp.toFile();
        assertNull(CalendarFiles.find(folder, CalendarFiles.SETTINGS));
        assertTrue(CalendarFiles.noneYet(folder));
        write(tmp, "originals/data/settings.yml", "a: 1\n");
        assertEquals(tmp.resolve("originals/data/settings.yml").toFile(), CalendarFiles.find(folder, CalendarFiles.SETTINGS));
        assertFalse(CalendarFiles.noneYet(folder));
        write(tmp, "data/settings.yaml", "a: 2\n");
        assertEquals("settings.yaml", CalendarFiles.find(folder, CalendarFiles.SETTINGS).getName());
        write(tmp, "data/settings.yml", "a: 3\n");
        assertEquals(tmp.resolve("data/settings.yml").toFile(), CalendarFiles.find(folder, CalendarFiles.SETTINGS));
    }

    @Test
    void theTwoFilesReadAsOneCalendarEachKeyFromItsOwnFile(@TempDir Path tmp) throws Exception {
        write(tmp, "data/settings.yml", "reset_time: \"06:00\"\ncycle_length: 99\nfreeze: { earn_every: 3 }\n");
        write(tmp, "originals/data/rewards.yml", "cycle_length: 7\nreset_time: \"12:00\"\n"
                + "days: { 7: { actions: [ { give_item: { material: DIAMOND } } ] } }\n");
        Map<String, Object> calendar = CalendarFiles.read(tmp.toFile());
        assertEquals("06:00", calendar.get("reset_time"));
        assertEquals(7, calendar.get("cycle_length"));
        CalendarSettings settings = CalendarSettings.read(calendar);
        assertEquals(7, settings.cycleLength());
        assertEquals(3, settings.freezeEarnEvery());
        assertNotNull(settings.reward(7).actions());
    }

    @Test
    void anEditedCalendarIsSplitOnceAndKeptAside(@TempDir Path tmp) throws Exception {
        File folder = tmp.toFile();
        File backup = tmp.resolve("backup").toFile();
        assertEquals(CalendarFiles.Migration.NONE, CalendarFiles.migrate(folder, backup));

        write(tmp, "data/calendar.yml", "reset_time: \"06:00\"\ncycle_length: 14\nauto_claim: \"on\"\n"
                + "default_day: { actions: [ { give_item: { material: BREAD, count: 8 } } ] }\n"
                + "days: { 14: { icon: { material: DIAMOND, title: { en: \"<aqua>Diamond\", fr: \"<aqua>Diamant\" } } } }\n");
        CalendarSettings before = CalendarSettings.read((Map<?, ?>) yaml(tmp.resolve("data/calendar.yml")));

        assertEquals(CalendarFiles.Migration.SPLIT, CalendarFiles.migrate(folder, backup));
        assertFalse(Files.exists(tmp.resolve("data/calendar.yml")));
        assertEquals(Map.of("reset_time", "06:00", "auto_claim", "on"), yaml(tmp.resolve("data/settings.yml")));
        assertEquals(List.of("cycle_length", "default_day", "days"),
                List.copyOf(((Map<?, ?>) yaml(tmp.resolve("data/rewards.yml"))).keySet()));
        assertEquals(before, CalendarSettings.read(CalendarFiles.read(folder)));
        assertEquals(1, backup.listFiles().length);

        assertEquals(CalendarFiles.Migration.NONE, CalendarFiles.migrate(folder, backup));
    }

    @Test
    void aCalendarBesideTheNewFilesIsLeftUnread(@TempDir Path tmp) throws Exception {
        write(tmp, "data/calendar.yaml", "cycle_length: 3\n");
        write(tmp, "data/rewards.yml", "cycle_length: 5\n");
        assertEquals(CalendarFiles.Migration.LEFT, CalendarFiles.migrate(tmp.toFile(), tmp.resolve("backup").toFile()));
        assertTrue(Files.exists(tmp.resolve("data/calendar.yaml")));
        assertEquals(5, CalendarFiles.read(tmp.toFile()).get("cycle_length"));
    }
}
