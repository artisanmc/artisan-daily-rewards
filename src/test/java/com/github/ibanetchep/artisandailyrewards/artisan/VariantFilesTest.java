package com.github.ibanetchep.artisandailyrewards.artisan;

import net.artisanmc.modules.api.template.TemplateDeclaration;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** The menus' variants the module ships: {@code textured} (resource pack) and {@code bedrock}. */
class VariantFilesTest {

    private static final Pattern OTHER_CLICK =
            Pattern.compile("^\\s*(right|shift_left|shift_right|middle|drop)\\s*:", Pattern.MULTILINE);

    /**
     * A variant's menu takes the inputs of the module's menu (Artisan leaves it out otherwise, and
     * its players silently get the module's), and the Bedrock versions only use the left click,
     * the only one Bedrock sends.
     */
    @Test
    void variantsKeepTheInputsAndBedrockOnlyTaps() {
        TemplateDeclaration files = ModuleFiles.template(getClass().getClassLoader());
        int variants = 0;
        for (String path : files.getPaths()) {
            if (!path.startsWith("variants/")) continue;
            variants++;
            String[] parts = path.split("/", 3);
            String text = new String(files.getFile(path), StandardCharsets.UTF_8);
            Map<String, Object> variant = new Yaml().load(text);
            assertNotNull(files.getFile(parts[2]), parts[2]);
            Map<String, Object> module = new Yaml().load(new String(files.getFile(parts[2]), StandardCharsets.UTF_8));
            assertEquals(module.get("inputs"), variant.get("inputs"), path);
            if (parts[1].equals("bedrock")) assertFalse(OTHER_CLICK.matcher(text).find(), path);
        }
        assertEquals(2, variants);
    }
}
