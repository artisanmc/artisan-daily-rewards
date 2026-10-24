package com.github.ibanetchep.artisandailyrewards;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CalendarFileTest {
    @Test
    void calendarIsFoundUnderEitherExtensionYmlFirst(@TempDir Path tmp) throws Exception {
        File folder = tmp.toFile();
        assertNull(DailyService.calendarFile(folder));
        Files.createDirectories(tmp.resolve("data"));
        Files.writeString(tmp.resolve("data/calendar.yaml"), "a: 1\n");
        assertEquals("calendar.yaml", DailyService.calendarFile(folder).getName());
        Files.writeString(tmp.resolve("data/calendar.yml"), "a: 2\n");
        assertEquals("calendar.yml", DailyService.calendarFile(folder).getName());
    }
}
