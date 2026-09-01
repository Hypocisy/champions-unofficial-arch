package top.theillusivec4.champions.common.client.screen.editor.validate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import top.theillusivec4.champions.common.archetype.ChampionArchetype;
import top.theillusivec4.champions.common.data.ModifierSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Live validation for the raw-JSON editor view: syntax errors (with line numbers),
 * codec-level semantic errors and a required/optional key cheat-sheet per tab.
 */
public final class JsonValidator {

    private static final Pattern LINE_COL = Pattern.compile("line (\\d+)(?: column (\\d+))?");

    private JsonValidator() {}

    /** @return status lines rendered under the JSON editor (max ~5 lines). */
    public static List<String> validate(String text, boolean archetype, boolean modifier) {
        List<String> out = new ArrayList<>();
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            out.add("§8(empty — entry will be an empty object)");
            return out;
        }

        // 1. Syntax
        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(t);
        } catch (JsonSyntaxException e) {
            String msg = e.getMessage() == null ? "invalid JSON" : e.getMessage();
            Matcher m = LINE_COL.matcher(msg);
            String where = m.find() ? "  §e[@" + m.group(1)
                    + (m.group(2) != null ? ":" + m.group(2) : "") + "]" : "";
            out.add("§c✗ " + firstLine(msg) + where);
            return out;
        }
        if (!parsed.isJsonObject()) {
            out.add("§c✗ top-level JSON must be an object");
            return out;
        }
        JsonObject obj = parsed.getAsJsonObject();

        // 2. Semantic (codec parse) — only for tabs that have one
        if (archetype) {
            var result = ChampionArchetype.CODEC.parse(JsonOps.INSTANCE, obj);
            var err = result.error();
            if (err.isPresent()) {
                out.add("§c✗ " + firstLine(err.get().message()));
            } else {
                out.add("§a✓ valid archetype");
            }
            hintKeys(obj, out,
                    List.of("affix_pools"),
                    List.of("id", "tier_range", "weight", "entity_filter", "phases"));
        } else if (modifier) {
            var result = ModifierSetting.MAP_CODEC.codec().parse(JsonOps.INSTANCE, obj);
            var err = result.error();
            if (err.isPresent()) {
                out.add("§c✗ " + firstLine(err.get().message()));
            } else {
                out.add("§a✓ valid modifier");
            }
            hintKeys(obj, out,
                    List.of("attributeType", "enable", "modifier"),
                    List.of("conditions"));
        } else {
            // tier: manual check
            if (!obj.has("level")) out.add("§c✗ missing 'level'");
            else if (!obj.get("level").isJsonPrimitive()
                    || !isInt(obj.get("level").getAsString()))
                out.add("§c✗ 'level' must be an integer");
            else out.add("§a✓ valid tier");
            hintKeys(obj, out, List.of("level"), List.of("display"));
        }
        return out;
    }

    private static void hintKeys(JsonObject obj, List<String> out,
                                 List<String> required, List<String> optional) {
        StringBuilder missing = new StringBuilder();
        for (String r : required) {
            if (!obj.has(r)) {
                if (!missing.isEmpty()) missing.append(", ");
                missing.append(r);
            }
        }
        if (!missing.isEmpty()) out.add("§emissing required: " + missing);

        StringBuilder hints = new StringBuilder("§8keys: ");
        for (int i = 0; i < required.size(); i++) {
            if (i > 0) hints.append(" ");
            hints.append(required.get(i)).append("*");
        }
        for (String o : optional) hints.append(" ").append(o);
        out.add(hints.toString());
    }

    private static boolean isInt(String s) {
        try { Integer.parseInt(s.trim()); return true; }
        catch (Exception e) { return false; }
    }

    private static String firstLine(String s) {
        int cut = s.indexOf('\n');
        String line = cut > 0 ? s.substring(0, cut) : s;
        return line.length() > 90 ? line.substring(0, 90) + "…" : line;
    }
}
