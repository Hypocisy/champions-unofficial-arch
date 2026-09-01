package top.theillusivec4.champions.common.client.screen.editor.pane;

import com.google.gson.JsonObject;
import top.theillusivec4.champions.common.client.screen.editor.filter.FilterEditor;
import top.theillusivec4.champions.common.client.screen.editor.json.JsonPathOps;
import top.theillusivec4.champions.common.client.screen.editor.picker.PickerSources;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder;
import top.theillusivec4.champions.common.client.screen.editor.widget.FormBuilder.CycleOption;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Archetype tab — full structured editing of {@code champions/archetype} files:
 * base fields, entity filter tree, affix pools (with candidates) and phases
 * (with conditions and effects).
 */
public final class ArchetypePane implements EditorPane {

    private static final List<CycleOption> CONDITION_TYPES = List.of(
            new CycleOption("health_percent",  "health_percent"),
            new CycleOption("time_elapsed",    "time_elapsed"),
            new CycleOption("affix_triggered", "affix_triggered"));

    private static final List<CycleOption> EFFECT_TYPES = List.of(
            new CycleOption("add_affix",     "add_affix"),
            new CycleOption("add_attribute", "add_attribute"),
            new CycleOption("add_mob_effect","add_mob_effect"));

    private static final List<CycleOption> OPERATIONS = List.of(
            new CycleOption("add_value",             "add_value"),
            new CycleOption("add_multiplied_base",   "add_multiplied_base"),
            new CycleOption("add_multiplied_total",  "add_multiplied_total"));

    @Override
    public String newEntryPrefix() { return "champions:new_archetype"; }

    @Override
    public void buildForm(FormBuilder fb, String selectedId) {
        JsonObject root = fb.root();

        // ── Base ───────────────────────────────────────────────────────────────
        fb.header("Archetype");
        fb.textRaw("id", "id", selectedId);
        fb.text("weight", "weight", "10");
        fb.header("Tier Range");
        fb.text("min", "tier_range.min", "1");
        fb.text("max", "tier_range.max", "5");

        // ── Entity filter ──────────────────────────────────────────────────────
        fb.header("Entity Filter");
        FilterEditor.build(fb, "entity_filter", 0);

        // ── Affix pools ────────────────────────────────────────────────────────
        fb.header("Affix Pools");
        String poolsPath = "affix_pools";
        int poolCount = JsonPathOps.size(root, poolsPath);
        for (int i = 0; i < poolCount; i++) {
            String poolPath = JsonPathOps.index(poolsPath, i);
            fb.headerWithRemove("Pool " + (i + 1), 1,
                    () -> { JsonPathOps.remove(root, poolPath); fb.rebuild(); });
            fb.text("tier min", JsonPathOps.child(poolPath, "tier_range.min"), "1", 2);
            fb.text("tier max", JsonPathOps.child(poolPath, "tier_range.max"), "5", 2);
            fb.text("min count", JsonPathOps.child(poolPath, "min_count"), "1", 2);
            fb.text("max count", JsonPathOps.child(poolPath, "max_count"), "1", 2);

            fb.header("Candidates", 2);
            String candsPath = JsonPathOps.child(poolPath, "candidates");
            int candCount = JsonPathOps.size(root, candsPath);
            for (int j = 0; j < candCount; j++) {
                String candPath = JsonPathOps.index(candsPath, j);
                fb.headerWithRemove("Candidate " + (j + 1), 2,
                        () -> { JsonPathOps.remove(root, candPath); fb.rebuild(); });
                buildCandidate(fb, candPath);
            }
            fb.action("+ Add candidate", 2, () -> {
                JsonObject c = new JsonObject();
                c.addProperty("affix", "champions:lively");
                c.addProperty("weight", 10);
                JsonPathOps.append(root, candsPath, c);
                fb.rebuild();
            });
        }
        fb.action("+ Add pool", 1, () -> {
            JsonObject pool = new JsonObject();
            pool.add("tier_range", range(1, 5));
            pool.addProperty("min_count", 1);
            pool.addProperty("max_count", 1);
            pool.add("candidates", new com.google.gson.JsonArray());
            JsonPathOps.append(root, poolsPath, pool);
            fb.rebuild();
        });

        // ── Phases ─────────────────────────────────────────────────────────────
        fb.header("Phases");
        String phasesPath = "phases";
        int phaseCount = JsonPathOps.size(root, phasesPath);
        for (int i = 0; i < phaseCount; i++) {
            String phasePath = JsonPathOps.index(phasesPath, i);
            String phaseId = JsonPathOps.str(root, JsonPathOps.child(phasePath, "id"),
                    "phase_" + (i + 1));
            fb.headerWithRemove("Phase: " + shortId(phaseId), 1,
                    () -> { JsonPathOps.remove(root, phasePath); fb.rebuild(); });
            fb.textRaw("id", JsonPathOps.child(phasePath, "id"),
                    "champions:phase_" + (i + 1), 2);
            fb.toggle("repeatable", JsonPathOps.child(phasePath, "repeatable"), false, 2);

            fb.header("Condition", 2);
            buildCondition(fb, JsonPathOps.child(phasePath, "condition"));

            fb.header("Effects", 2);
            String effectsPath = JsonPathOps.child(phasePath, "effects");
            int fxCount = JsonPathOps.size(root, effectsPath);
            for (int j = 0; j < fxCount; j++) {
                String fxPath = JsonPathOps.index(effectsPath, j);
                fb.headerWithRemove("Effect " + (j + 1), 2,
                        () -> { JsonPathOps.remove(root, fxPath); fb.rebuild(); });
                buildEffect(fb, fxPath);
            }
            fb.action("+ Add effect", 2, () -> {
                JsonPathOps.append(root, effectsPath, defaultEffect("add_affix"));
                fb.rebuild();
            });
        }
        fb.action("+ Add phase", 1, () -> {
            JsonObject phase = new JsonObject();
            phase.addProperty("id", "champions:new_phase");
            phase.add("condition", defaultCondition("health_percent"));
            phase.add("effects", new com.google.gson.JsonArray());
            JsonPathOps.append(root, phasesPath, phase);
            fb.rebuild();
        });
    }

    // ── Candidate (WeightedAffix) ──────────────────────────────────────────────

    private static void buildCandidate(FormBuilder fb, String candPath) {
        JsonObject root = fb.root();
        String affixPath = JsonPathOps.child(candPath, "affix");
        String current = JsonPathOps.str(root, affixPath, "champions:lively");

        fb.textRaw("affix", affixPath, current, 3);
        fb.action("Pick affix…  §8" + shortId(current), 3, () ->
                fb.openPicker("Affixes", PickerSources.affixes(), false,
                        Set.of(current), ids -> {
                            if (!ids.isEmpty()) {
                                JsonPathOps.setString(root, affixPath, ids.iterator().next());
                                fb.rebuild();
                            }
                        }));
        fb.text("weight", JsonPathOps.child(candPath, "weight"), "10", 3);
        fb.text("min strength", JsonPathOps.child(candPath, "min_strength"), "1", 3);
        fb.text("max strength", JsonPathOps.child(candPath, "max_strength"), "1", 3);
    }

    // ── Phase condition ────────────────────────────────────────────────────────

    private static void buildCondition(FormBuilder fb, String condPath) {
        JsonObject root = fb.root();
        String type = JsonPathOps.str(root, JsonPathOps.child(condPath, "type"),
                "health_percent");

        fb.cycle("type", JsonPathOps.child(condPath, "type"), type, CONDITION_TYPES, 3,
                newType -> {
                    JsonPathOps.set(root, condPath, defaultCondition(newType));
                    fb.rebuild();
                });

        switch (type) {
            case "health_percent" ->
                    fb.text("below", JsonPathOps.child(condPath, "below"), "0.5", 3);
            case "time_elapsed" ->
                    fb.text("seconds", JsonPathOps.child(condPath, "seconds"), "30", 3);
            case "affix_triggered" -> {
                String affixPath = JsonPathOps.child(condPath, "affix");
                String current = JsonPathOps.str(root, affixPath, "champions:adaptable");
                fb.textRaw("affix", affixPath, current, 3);
                fb.action("Pick affix…  §8" + shortId(current), 3, () ->
                        fb.openPicker("Affixes", PickerSources.affixes(), false,
                                Set.of(current), ids -> {
                                    if (!ids.isEmpty()) {
                                        JsonPathOps.setString(root, affixPath,
                                                ids.iterator().next());
                                        fb.rebuild();
                                    }
                                }));
                fb.text("count", JsonPathOps.child(condPath, "count"), "3", 3);
            }
            default -> fb.hint("§cunknown condition type", 3);
        }
    }

    // ── Phase effect ───────────────────────────────────────────────────────────

    private static void buildEffect(FormBuilder fb, String fxPath) {
        JsonObject root = fb.root();
        String type = JsonPathOps.str(root, JsonPathOps.child(fxPath, "type"), "add_affix");

        fb.cycle("type", JsonPathOps.child(fxPath, "type"), type, EFFECT_TYPES, 3,
                newType -> {
                    JsonPathOps.set(root, fxPath, defaultEffect(newType));
                    fb.rebuild();
                });

        switch (type) {
            case "add_affix" -> {
                String affixPath = JsonPathOps.child(fxPath, "affix");
                String current = JsonPathOps.str(root, affixPath, "champions:enkindling");
                fb.textRaw("affix", affixPath, current, 3);
                fb.action("Pick affix…  §8" + shortId(current), 3, () ->
                        fb.openPicker("Affixes", PickerSources.affixes(), false,
                                Set.of(current), ids -> {
                                    if (!ids.isEmpty()) {
                                        JsonPathOps.setString(root, affixPath,
                                                ids.iterator().next());
                                        fb.rebuild();
                                    }
                                }));
                fb.text("strength", JsonPathOps.child(fxPath, "strength"), "1", 3);
            }
            case "add_attribute" -> {
                fb.textWithPick("attribute", JsonPathOps.child(fxPath, "attribute"),
                        "minecraft:generic.movement_speed", PickerSources.attributes());
                fb.text("amount", JsonPathOps.child(fxPath, "amount"), "0.3", 3);
                fb.cycle("operation", JsonPathOps.child(fxPath, "operation"),
                        "add_value", OPERATIONS, 3, null);
            }
            case "add_mob_effect" -> {
                fb.textWithPick("effect", JsonPathOps.child(fxPath, "effect"),
                        "minecraft:speed", PickerSources.mobEffects());
                fb.text("amplifier", JsonPathOps.child(fxPath, "amplifier"), "0", 3);
                fb.toggle("infinite", JsonPathOps.child(fxPath, "infinite"), true, 3);
                fb.text("duration_ticks", JsonPathOps.child(fxPath, "duration_ticks"),
                        "200", 3);
            }
            default -> fb.hint("§cunknown effect type", 3);
        }
    }

    // ── Defaults ───────────────────────────────────────────────────────────────

    private static JsonObject defaultCondition(String type) {
        JsonObject o = new JsonObject();
        o.addProperty("type", type);
        switch (type) {
            case "health_percent" -> o.addProperty("below", 0.5f);
            case "time_elapsed" -> o.addProperty("seconds", 30);
            case "affix_triggered" -> {
                o.addProperty("affix", "champions:adaptable");
                o.addProperty("count", 3);
            }
        }
        return o;
    }

    private static JsonObject defaultEffect(String type) {
        JsonObject o = new JsonObject();
        o.addProperty("type", type);
        switch (type) {
            case "add_affix" -> {
                o.addProperty("affix", "champions:enkindling");
                o.addProperty("strength", 1);
            }
            case "add_attribute" -> {
                o.addProperty("attribute", "minecraft:generic.movement_speed");
                o.addProperty("amount", 0.3);
                o.addProperty("operation", "add_value");
            }
            case "add_mob_effect" -> {
                o.addProperty("effect", "minecraft:speed");
                o.addProperty("amplifier", 0);
                o.addProperty("infinite", true);
            }
        }
        return o;
    }

    private static JsonObject range(int min, int max) {
        JsonObject o = new JsonObject();
        o.addProperty("min", min);
        o.addProperty("max", max);
        return o;
    }

    private static String shortId(String fullId) {
        int colon = fullId.lastIndexOf(':');
        return colon >= 0 ? fullId.substring(colon + 1) : fullId;
    }
}
