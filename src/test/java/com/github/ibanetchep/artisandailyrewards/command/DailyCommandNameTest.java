package com.github.ibanetchep.artisandailyrewards.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DailyCommandNameTest {

    @TempDir
    Path moduleDir;

    @Test
    void defaultsToDailyrewards() {
        assertEquals("dailyrewards", DailyCommand.nameIn(moduleDir.toFile()));
    }

    @Test
    void followsTheAdminsRename() throws Exception {
        Files.createDirectories(moduleDir.resolve("commands"));
        Files.writeString(moduleDir.resolve("commands/dailyrewards.yml"), "schema: command/v1\nname: calendrier\n");
        assertEquals("calendrier", DailyCommand.nameIn(moduleDir.toFile()));
    }

    @Test
    void ignoresAFileWithoutName() throws Exception {
        Files.createDirectories(moduleDir.resolve("commands"));
        Files.writeString(moduleDir.resolve("commands/dailyrewards.yml"), "schema: command/v1\npermission: daily.use\n");
        assertEquals("dailyrewards", DailyCommand.nameIn(new File(moduleDir.toString())));
    }
}
