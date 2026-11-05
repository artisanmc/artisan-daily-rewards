package com.github.ibanetchep.artisandailyrewards.artisan;

import com.github.ibanetchep.artisandailyrewards.command.DailyCommand;
import com.github.ibanetchep.artisandailyrewards.core.CalendarSettings;
import net.artisanmc.modules.api.model.ModelField;
import net.artisanmc.modules.api.template.TemplateDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleFilesTest {

    private static Object yaml(String path) throws IOException {
        try (InputStream in = ModuleFilesTest.class.getClassLoader().getResourceAsStream(ModuleFiles.ROOT + "/" + path)) {
            assertNotNull(in, path);
            return new Yaml(new SafeConstructor(new LoaderOptions())).load(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void thePathsAreTheShippedFiles() throws IOException, URISyntaxException {
        URL root = getClass().getClassLoader().getResource(ModuleFiles.ROOT);
        assertNotNull(root);
        Path dir = Path.of(root.toURI());
        Set<String> shipped = new TreeSet<>();
        try (Stream<Path> files = Files.walk(dir)) {
            files.filter(Files::isRegularFile).forEach(f -> shipped.add(dir.relativize(f).toString().replace('\\', '/')));
        }
        assertEquals(new TreeSet<>(ModuleFiles.PATHS), shipped);
        TemplateDeclaration template = ModuleFiles.template(getClass().getClassLoader());
        assertTrue(template.isSystem());
        assertEquals(ModuleFiles.PATHS, template.getPaths());
    }

    @Test
    void theShippedCalendarReadsAsTheDefaultsWithRewards() throws Exception {
        Map<Object, Object> calendar = new java.util.LinkedHashMap<>((Map<?, ?>) yaml("data/settings.yaml"));
        calendar.putAll((Map<?, ?>) yaml("data/rewards.yaml"));
        CalendarSettings shipped = CalendarSettings.read(calendar);
        CalendarSettings d = CalendarSettings.DEFAULTS;
        assertEquals(new CalendarSettings(d.resetTime(), d.cycleLength(), d.autoClaim(), d.minPlaytimeMinutes(),
                d.joinReminder(), d.freezeEarnEvery(), d.freezeMaxStock(), shipped.days(), shipped.defaultDay()), shipped);
        assertNotNull(shipped.defaultDay());
        for (int day = 1; day <= shipped.cycleLength(); day++) {
            assertNotNull(shipped.reward(day).icon(), "day " + day);
            assertNotNull(shipped.reward(day).actions(), "day " + day);
        }
    }

    @Test
    void eachModelNamesTheKeysOfItsFile() throws IOException {
        assertEquals(keys(SettingsModel.declaration().getFields()), keys("data/settings.yaml"));
        assertEquals(keys(RewardsModel.declaration().getFields()), keys("data/rewards.yaml"));
        assertEquals(CalendarFiles.REWARD_KEYS, keys("data/rewards.yaml"));
        assertEquals(Map.of("model", SettingsModel.ID), yaml("models/settings.yaml"));
        assertEquals(Map.of("model", RewardsModel.ID), yaml("models/rewards.yaml"));
        assertEquals(CalendarFiles.SETTINGS, SettingsModel.TABLE);
        assertEquals(CalendarFiles.REWARDS, RewardsModel.TABLE);
    }

    private static Set<String> keys(List<ModelField> fields) {
        return new TreeSet<>(fields.stream().map(ModelField::getName).toList());
    }

    private static Set<String> keys(String path) throws IOException {
        return new TreeSet<>(((Map<?, ?>) yaml(path)).keySet().stream().map(String::valueOf).toList());
    }

    @Test
    void theEditorBundleIsInTheJarBuiltAgainstTheDeclaredSdk() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(DailyRewardsModule.EDITOR_BUNDLE)) {
            assertNotNull(in, DailyRewardsModule.EDITOR_BUNDLE + ": run npm run build in editor/");
            String script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(script.contains(RewardsModel.COMPONENT), "the bundle names its view " + RewardsModel.COMPONENT);
        }
        String pkg = Files.readString(Path.of("editor/package.json"));
        Matcher sdk = Pattern.compile("artisan-editor-sdk-(\\d+\\.\\d+)\\.\\d+\\.tgz").matcher(pkg);
        assertTrue(sdk.find(), "editor/package.json takes the SDK from its archive");
        assertEquals(DailyRewardsModule.EDITOR_SDK, sdk.group(1));
    }

    @Test
    void theMenuReadsOnlyDeclaredSourcesAndFields() throws IOException {
        Map<?, ?> menu = (Map<?, ?>) yaml("menus/calendar.yaml");
        assertEquals("menu/v1", menu.get("schema"));
        assertEquals(DailyCommand.MENU, "daily:" + ModuleFiles.PATHS.stream()
                .filter(p -> p.startsWith("menus/")).findFirst().orElseThrow().replace("menus/", "").replace(".yaml", ""));

        CalendarData data = new CalendarData(null);
        Map<String, Set<String>> fields = Map.of(
                "d", names(data.calendar().getSchema()),
                "status[0]", names(data.status().getSchema()));
        String text;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(ModuleFiles.ROOT + "/menus/calendar.yaml")) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        List<String> unknown = new ArrayList<>();
        Matcher m = Pattern.compile("\\b(d|status\\[0])\\.([a-z_]+)").matcher(text);
        while (m.find()) {
            if (!fields.get(m.group(1)).contains(m.group(2))) unknown.add(m.group());
        }
        assertEquals(List.of(), unknown);
    }

    private static Set<String> names(List<net.artisanmc.modules.api.datasource.DataSourceField> schema) {
        Set<String> names = new TreeSet<>();
        schema.forEach(f -> names.add(f.getName()));
        return names;
    }

    @Test
    void theModuleFileIsWrittenOnlyWhenTheFolderHasNone(@TempDir Path tmp) throws IOException {
        assertTrue(ModuleFiles.writeModuleFile(tmp.toFile()));
        assertEquals(ModuleFiles.MODULE_FILE, Files.readString(tmp.resolve("artisan-module.yml")));
        assertFalse(ModuleFiles.writeModuleFile(tmp.toFile()));

        Path other = tmp.resolve("other");
        Files.createDirectories(other);
        Files.writeString(other.resolve("manifest.yaml"), "schema: module/v1\n");
        assertFalse(ModuleFiles.writeModuleFile(other.toFile()));
        assertFalse(Files.exists(other.resolve("artisan-module.yml")));
    }
}
