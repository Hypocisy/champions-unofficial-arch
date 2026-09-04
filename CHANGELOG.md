# Changelog

## [20.1.1.5] — 2026-09-05

> This release is the **complete editor refactor**, consolidating all changes since the last CurseForge release (several unreleased versions merged into one).

### Added

- **Full in-game editor overhaul** (see `EDITOR_REFACTOR_PLAN.md`):
  - **Visual selector editing**: a recursive `EntityFilter` tree editor with `all_of`/`any_of` nesting, switching between the eight filter types, and add/remove for child filters; dedicated config rows for `entity_type` (entity multi-select), `mod_id` (namespace multi-select), `mob_category`, and `attribute` (attribute picker + min/max). The same editor is reused for archetype `entity_filter` and modifier `conditions.entity_filter`.
  - **Complete UI-mode coverage**: archetype affix pools (nested tier_range / min-max count / candidates: affix picker, weight, strength range) and phases (id, repeatable, all parameters of the three condition types and three effect types) are fully editable visually; modifier `modifier.value`/`operation` and `conditions` (entity_filter, tier min/max, `affixes.values` multi-select).
  - **Inline hints in JSON mode**: live syntax validation (with line/column numbers), Codec semantic validation (missing fields / wrong types), and a required/optional key cheat-sheet panel.
  - **Datapack management (Packs page)**: lists all datapacks with one-click enable/disable (server-side reload); **export** the currently edited content as a well-formed datapack zip into `<world>/champions_exports/`; **import** zips from `<world>/champions_imports/`, which are copied into `datapacks/` and enabled automatically.
  - Generic searchable registry pickers (entity/attribute/effect/category/namespace/affix; single- and multi-select).
  - Architecture: `EditorSession` (state survives dialog round-trips), a `FormBuilder` row-based form DSL, `JsonPathOps` path read/write, five split tab panels, and a slimmed-down `ChampionEditorScreen` shell.
- New network packet `EditorPackActionPacket` (toggle/export/import); `EditorPayload` now carries the packs list; registered on both platforms.
- **Client-side config** (`ChampionConfigSpecClient` / `ChampionsClientConfig`): HUD offsets (`hudXOffset`/`hudYOffset`), HUD detection range (`hudRange`), Jade star spacing and bottom padding, Waila/Jade integration toggles, and other client options; on Fabric these register through Forge Config API Port and are baked automatically on load/reload.
- **lootSource config**: config-defined drops and loot tables now roll separately instead of one suppressing the other.
- **Damage type tags** (`ModDamageTypeTagsProvider`): added `IS_FIRE` and other damage type tags for `enkindling_bullet`, fixing fire immunity/resistance checks; Fabric gained `FabricDamageTypeProvider`, which generates damage_type JSON through the dynamic registry.
- **Entity type tags** (`ModEntityTypeTagsProvider` / `ChampionEntityTypes.Tags`): new `champions:is_ender` and `champions:allow_champions` tags replace hardcoded entity checks (e.g., the Infested affix spawning endermites from Ender-type mobs; `infestedEnderParasite` is configurable).
- **Fabric attack/tick event bridging** (`MixinLivingEntityTick`): injects into `LivingEntity#tick` to provide TickEvent dispatch and the PhaseProcessor's every-10-tick phase evaluation on Fabric.
- Fabric now registers the Champion Egg dispenser behavior.
- **Full editor localization**: new `EditorLang`; every editor/selector/form/tooltip string moved into language files (`gui.champions.*`, 120+ keys) and follows the game language.

### Changed

- Refactored `MinecraftMixin` (middle-click pick) and `MouseHelper` into lightweight implementations via `ChampionView`/`ChampionData`.
- Built-in affixes (Adaptable / Lively / Reflective / Shielding / Dampening / Knocking / Paralyzing / Wounding / Arctic / Desecrating / Enkindling / Infested / Magnetic / Hasty / Molten / Plagued) migrated to tag/registry-driven entity and damage-type checks.
- Config system rework: server config load/reload is now event-driven (`NeoForgeModConfigEvents`); client config split into its own spec.
- Datagen cleanup: NeoForge gained `ModDatapackProvider` to produce built-in registry data uniformly; removed the old `DamageTypeProvider` and `AffixDefaults`.
- **Lang-file generation rework**: per-language translations split into dedicated classes (`datagen/lang/*Translations.java`) behind a single provider; filled in 137 missing keys across ko_kr/ru_ru/tr_tr/uk_ua/pt_br (help command, item names, editor UI); unified Chinese terminology.
- Editor UI series: render-order refactor (chrome moved into `renderBackground`, scissored scroll clipping, unified palette), opaque backgrounds no longer let shader-pack GUI blur bleed through, input-box size fixes (`MultiLineEditBox` wrap width is fixed at construction and rebuilt on window resize).

### Fixed

- **Editor form area not clickable**: in Form mode the hidden JSON editor (`MultiLineEditBox`) swallowed all clicks inside the form panel and stole keyboard focus (vanilla 1.21.1's `mouseClicked` ignores `visible`); it is now fully removed from `children` while hidden.
- **Selector window truncation**: registry pickers changed from a 340px cap to full-width modals with larger rows and buttons.
- **`death.attack.reflection` death messages**: previously missing entirely on Fabric and English-only on NeoForge; now generated for every language on all platforms.
- Fabric config not being re-baked after a server-side reload.
- `enkindling_bullet` damage not recognized as fire damage.
- The Infested affix spawning incorrect parasites on Ender-type mobs.
- Duplicate modifier id paths (`modifier_setting/modifier_setting/x.json.json`) are no longer generated; polluted installs self-heal on load.

### Removed

- `AffixDefaults`, the old `DamageTypeProvider`, and other code replaced by the registry/tag approach.

### Build

- Added **one-click Modrinth / CurseForge publishing**: root task `./gradlew publishModPlatforms` (see `PUBLISHING.md`); release notes are pulled automatically from the matching version section of this file.

## [Unreleased]

- (nothing yet)
