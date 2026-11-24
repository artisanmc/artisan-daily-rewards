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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
                d.notifyOnUnlock(), d.freezeEarnEvery(), d.freezeMaxStock(), shipped.days(), shipped.defaultDay(),
                shipped.milestones()), shipped);
        assertNotNull(shipped.defaultDay());
        // Going on must always pay more than starting over: the last milestone repeats.
        assertFalse(shipped.milestones().isEmpty());
        assertTrue(shipped.milestones().get(shipped.milestones().size() - 1).repeats());
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
        List<String> unknown = new ArrayList<>();
        for (String path : List.of("menus/calendar.yaml", "variants/textured/menus/calendar.yaml")) {
            Matcher m = Pattern.compile("\\b(d|status\\[0])\\.([a-z_]+)").matcher(text(path));
            while (m.find()) {
                if (!fields.get(m.group(1)).contains(m.group(2))) unknown.add(path + ": " + m.group());
            }
        }
        assertEquals(List.of(), unknown);
    }

    private static String text(String path) throws IOException {
        try (InputStream in = ModuleFilesTest.class.getClassLoader().getResourceAsStream(ModuleFiles.ROOT + "/" + path)) {
            assertNotNull(in, path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void theTexturedMenusTakeTheSameInputsAndShippedImages() throws IOException {
        Map<?, ?> rules = (Map<?, ?>) yaml("variants.yml");
        assertEquals("variants/v1", rules.get("schema"));
        assertEquals(List.of("textured"), rules.get("variants"));
        // Which player sees it is the server's (Artisan's menu variants): no display rules of our own.
        assertFalse(rules.containsKey("show"));

        Set<String> images = new TreeSet<>();
        for (String path : ModuleFiles.PATHS) {
            Matcher a = Pattern.compile("^assets/(textures|glyphs)/(\\w+)\\.png$").matcher(path);
            if (a.find()) {
                images.add(a.group(2));
                assertTrue(ModuleFiles.PATHS.contains("assets/" + a.group(1) + "/" + a.group(2) + ".yml"), path);
            }
        }
        for (String menu : List.of("calendar", "milestones")) {
            Map<?, ?> module = (Map<?, ?>) yaml("menus/" + menu + ".yaml");
            Map<?, ?> textured = (Map<?, ?>) yaml("variants/textured/menus/" + menu + ".yaml");
            assertEquals(module.get("inputs"), textured.get("inputs"), menu + ": a variant takes the menu's inputs");
            assertEquals(module.get("size"), textured.get("size"), menu);
            assertEquals(false, textured.get("vanilla_texture"), menu);
            Matcher m = Pattern.compile("asset: (\\w+)").matcher(text("variants/textured/menus/" + menu + ".yaml"));
            int used = 0;
            while (m.find()) {
                assertTrue(images.contains(m.group(1)), menu + " uses " + m.group(1) + ", not shipped");
                used++;
            }
            assertTrue(used > 0, menu);
        }
    }

    @Test
    void everyTitleTextareaCarriesTheFieldsTheSchemaRequires() throws IOException {
        // menu/v1: a textarea layer requires x, y, width, line_height and alignment (the editor needs them all).
        for (String path : List.of("menus/calendar.yaml", "menus/milestones.yaml",
                "variants/textured/menus/calendar.yaml", "variants/textured/menus/milestones.yaml")) {
            Map<?, ?> menu = (Map<?, ?>) yaml(path);
            Object title = menu.get("title");
            if (!(title instanceof List<?> layers)) continue;
            for (Object layer : layers) {
                Map<?, ?> l = (Map<?, ?>) layer;
                if (!"textarea".equals(l.get("kind"))) continue;
                for (String field : List.of("x", "y", "width", "line_height", "alignment"))
                    assertTrue(l.containsKey(field), path + ": a textarea without " + field + " (" + l.get("value") + ")");
            }
        }
    }

    @Test
    void aCentredTitleTextIsPlacedByItsCentre() throws IOException {
        // Artisan centres a textarea's lines on its x: the window's middle is 88 (176 px wide).
        for (String menu : List.of("calendar", "milestones")) {
            Map<?, ?> textured = (Map<?, ?>) yaml("variants/textured/menus/" + menu + ".yaml");
            for (Object layer : (List<?>) textured.get("title")) {
                Map<?, ?> l = (Map<?, ?>) layer;
                if ("center".equals(l.get("alignment"))) assertEquals(88, l.get("x"), menu + ": " + l.get("value"));
            }
        }
    }

    @Test
    void aDayToClaimLooksNeitherClaimedNorToCome() throws IOException {
        Map<?, ?> menu = (Map<?, ?>) yaml("menus/calendar.yaml");
        Map<?, ?> days = ((List<?>) menu.get("elements")).stream().map(e -> (Map<?, ?>) e)
                .filter(e -> "days".equals(e.get("id"))).findFirst().orElseThrow();
        Map<String, Object> lookOf = new java.util.LinkedHashMap<>();
        for (Object o : (List<?>) days.get("states")) {
            Map<?, ?> state = (Map<?, ?>) o;
            String when = ((List<?>) state.get("if")).stream()
                    .map(c -> String.valueOf(((Map<?, ?>) c).get("expression"))).reduce((a, b) -> a + " && " + b).orElseThrow();
            lookOf.putIfAbsent(when, state.containsKey("item") ? state.get("item") : state.get("material"));
        }
        Object toClaim = lookOf.get("d.is_available");
        assertEquals(toClaim, lookOf.get("d.is_available && d.is_today"));
        assertEquals(lookOf.get("d.is_claimed"), lookOf.get("d.is_today"), "today claimed looks like a day claimed");
        Object toCome = days.get("item");
        for (Object other : List.of(lookOf.get("d.is_claimed"), toCome, lookOf.get("d.is_milestone"))) {
            assertNotEquals(toClaim, other);
        }
        assertNotEquals(lookOf.get("d.is_claimed"), toCome);
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
