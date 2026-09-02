package top.theillusivec4.champions.common.datagen.lang;

import top.theillusivec4.champions.common.datagen.ChampionLanguageProvider;

/** Brazilian Portuguese (pt_br) translations. */
public final class BrazilianPortugueseTranslations {

    private BrazilianPortugueseTranslations() {}

    public static void add(ChampionLanguageProvider p) {
        // ── Affixes & ranks ─────────────────────────────────────────────────
        p.addAffix("adaptable",   "Adaptável");
        p.addAffix("arctic",      "Ártico");
        p.addAffix("dampening",   "Amortecedor");
        p.addAffix("desecrating", "Profanador");
        p.addAffix("enkindling",  "Incendiário");
        p.addAffix("hasty",       "Apresado");
        p.addAffix("infested",    "Infestado");
        p.addAffix("knocking",    "Empurrador");
        p.addAffix("lively",      "Vigoroso");
        p.addAffix("magnetic",    "Magnético");
        p.addAffix("molten",      "Fundido");
        p.addAffix("paralyzing",  "Paralisante");
        p.addAffix("plagued",     "Pestilento");
        p.addAffix("reflective",  "Reflexivo");
        p.addAffix("shielding",   "Protetor");
        p.addAffix("wounding",    "Dilacerante");
        p.addRank(1, "Comum");
        p.addRank(2, "Habilidoso");
        p.addRank(3, "Elite");
        p.addRank(4, "Lendário");
        p.addRank(5, "Supremo");

        // ── Commands ────────────────────────────────────────────────────────
        p.add("argument.champions.affix.unknown",     "Afixo desconhecido %s");
        p.add("commands.champions.summon.success",    "Invocado novo %s");
        p.add("commands.champions.egg.success",       "Criado novo %s");
        p.add("command.champions.unknown_entity",     "Entidade desconhecida: %s");
        p.add("command.champions.unknown_tier",       "Nenhum nível encontrado com o nível %s. Disponível: %s");
        p.add("command.champions.egg.unknown_entity", "Entidade desconhecida");
        p.add("command.champions.not_living_entity",  "%s não é uma entidade viva.");
        p.add("command.champions.not_champion",       "%s não é um campeão.");
        p.add("command.champions.spawn_cancelled",    "A criação foi cancelada por um ouvinte SpawnChampion.");
        p.add("command.champions.remove.success",     "Status de campeão removido de %s");
        p.add("command.champions.remove.success_deleted", "Status de campeão removido e entidade excluída.");
        p.add("command.champions.info.title",         "§6Informações do Campeão:§r");
        p.add("command.champions.info.tier",          "  §eNível:§r %s (Nível %s)");
        p.add("command.champions.info.affixes",       "  §eAfixos:§r");
        p.add("command.champions.info.affixes.none",  "  §eAfixos:§r Nenhum");
        p.add("command.champions.info.affix_entry",   "    - %s (Força: %s)");
        p.add("command.champions.info.archetype",     "  §eArquétipo:§r %s");
        p.add("command.champions.help.title",         "§6Comandos do Champions:§r");
        p.add("command.champions.help.summon",        "  §e/champions summon <entidade|@category:X|@archetype:X> <nível> [afixos...] [at <x> <y> <z>]§r - Gera um campeão");
        p.add("command.champions.help.apply",         "  §e/champions apply <alvo> <nível> [afixos...]§r - Aplica campeão a uma entidade existente");
        p.add("command.champions.help.remove",        "  §e/champions remove <alvo> [excluirEntidade]§r - Remove o status de campeão");
        p.add("command.champions.help.info",          "  §e/champions info <alvo>§r - Mostra informações do campeão (disponível para todos os jogadores)");
        p.add("command.champions.help.egg",           "  §e/champions egg <entidade|@category:X|@archetype:X> <nível|random> [afixos...]§r - Cria um ovo de campeão");
        p.add("command.champions.help.editor",        "  §e/champions editor§r - Abre o editor de datapacks");

        // ── Item / advancements / stats ─────────────────────────────────────
        p.add("item.champions.egg",                 "Ovo de Campeão");
        p.add("item.champions.egg.tooltip",         "Afixos Aleatórios");
        p.add("item.champions.egg.random",          "Aleatório");
        p.add("item.champions.egg.no_affixes",      "Sem afixos");
        p.add("advancements.champions.kill_a_champion.title",       "Caçador de Campeões");
        p.add("advancements.champions.kill_a_champion.description", "Mate um monstro hostil poderoso");
        p.add("stat.champions.champion_mobs_killed", "Campeões Derrotados");
        p.add("config.jade.plugin_champions.enable_affix_compact", "Ativar afixos compactos no Jade");

        // ── In-game editor screen (gui.champions.*) ─────────────────────────
        p.add("gui.champions.editor.title",         "Editor de Campeões");
        p.add("gui.champions.editor.tab.archetypes", "Arquétipos");
        p.add("gui.champions.editor.tab.tiers",      "Níveis");
        p.add("gui.champions.editor.tab.modifiers",  "Modificadores");
        p.add("gui.champions.editor.tab.config",     "Configuração");
        p.add("gui.champions.editor.tab.packs",      "Pacotes de Dados");
        p.add("gui.champions.editor.view.form",     "Formulário");
        p.add("gui.champions.editor.view.json",     "JSON");
        p.add("gui.champions.editor.new",           "§a+ Novo");
        p.add("gui.champions.editor.delete",        "§cExcluir");
        p.add("gui.champions.editor.save_reload",   "Salvar e Recarregar");
        p.add("gui.champions.editor.close",         "Fechar");
        p.add("gui.champions.editor.entries",       "§8ENTRADAS §7%s");
        p.add("gui.champions.editor.unsaved",       "§e● %s não salvo(s)");
        p.add("gui.champions.editor.error.not_object", "O JSON deve ser um objeto");
        p.add("gui.champions.editor.error.invalid_json", "JSON inválido: %s");
        p.add("gui.champions.editor.toggle.true",   "§aSim");
        p.add("gui.champions.editor.toggle.false",  "§cNão");
        p.add("gui.champions.editor.pick_affix",    "Escolher afixo… §8%s");

        p.add("gui.champions.picker.done",          "Concluído");
        p.add("gui.champions.picker.cancel",        "Cancelar");
        p.add("gui.champions.picker.search_hint",   "§7Pesquisar…");
        p.add("gui.champions.picker.selected",      "Selecionado: %s ·");
        p.add("gui.champions.picker.count",         "%s / %s");
        p.add("gui.champions.picker.title.affixes",       "Afixos");
        p.add("gui.champions.picker.title.affix_values",  "Valores de afixo");
        p.add("gui.champions.picker.title.entity_types",  "Tipos de entidade");
        p.add("gui.champions.picker.title.mod_namespaces", "Namespaces de mods");
        p.add("gui.champions.picker.title.mob_categories", "Categorias de mobs");
        p.add("gui.champions.picker.title.filter_type",   "Tipo de filtro");

        p.add("gui.champions.editor.label.id",             "ID");
        p.add("gui.champions.editor.label.weight",         "peso");
        p.add("gui.champions.editor.label.type",           "tipo");
        p.add("gui.champions.editor.label.min",            "mín");
        p.add("gui.champions.editor.label.max",            "máx");
        p.add("gui.champions.editor.label.tier_min",       "nível mín");
        p.add("gui.champions.editor.label.tier_max",       "nível máx");
        p.add("gui.champions.editor.label.min_count",      "qtd. mín");
        p.add("gui.champions.editor.label.max_count",      "qtd. máx");
        p.add("gui.champions.editor.label.affix",          "afixo");
        p.add("gui.champions.editor.label.min_strength",   "força mín");
        p.add("gui.champions.editor.label.max_strength",   "força máx");
        p.add("gui.champions.editor.label.strength",       "força");
        p.add("gui.champions.editor.label.below",          "abaixo de");
        p.add("gui.champions.editor.label.seconds",        "segundos");
        p.add("gui.champions.editor.label.count",          "quantidade");
        p.add("gui.champions.editor.label.amount",         "valor");
        p.add("gui.champions.editor.label.operation",      "operação");
        p.add("gui.champions.editor.label.attribute",      "atributo");
        p.add("gui.champions.editor.label.effect",         "efeito");
        p.add("gui.champions.editor.label.amplifier",      "nível do efeito");
        p.add("gui.champions.editor.label.infinite",       "infinito");
        p.add("gui.champions.editor.label.duration_ticks", "duração (ticks)");
        p.add("gui.champions.editor.label.enable",         "ativar");
        p.add("gui.champions.editor.label.value",          "valor");
        p.add("gui.champions.editor.label.level",          "nível");
        p.add("gui.champions.editor.label.color",          "cor");
        p.add("gui.champions.editor.label.icon",           "ícone");
        p.add("gui.champions.editor.label.repeatable",     "repetível");
        p.add("gui.champions.editor.label.tag",            "tag");
        p.add("gui.champions.editor.label.whitelist",      "lista branca");
        p.add("gui.champions.editor.label.state",          "estado");

        p.add("gui.champions.editor.header.archetype",       "Arquétipo");
        p.add("gui.champions.editor.header.tier_range",      "Faixa de Níveis");
        p.add("gui.champions.editor.header.entity_filter",   "Filtro de Entidade");
        p.add("gui.champions.editor.header.affix_pools",     "Pools de Afixos");
        p.add("gui.champions.editor.header.pool",            "Pool %s");
        p.add("gui.champions.editor.header.candidates",      "Candidatos");
        p.add("gui.champions.editor.header.candidate",       "Candidato %s");
        p.add("gui.champions.editor.header.phases",          "Fases");
        p.add("gui.champions.editor.header.phase",           "Fase: %s");
        p.add("gui.champions.editor.header.condition",       "Condição");
        p.add("gui.champions.editor.header.effects",         "Efeitos");
        p.add("gui.champions.editor.header.effect",          "Efeito %s");
        p.add("gui.champions.editor.header.modifier_setting", "Configuração de Modificador");
        p.add("gui.champions.editor.header.modifier",        "Modificador");
        p.add("gui.champions.editor.header.conditions",      "Condições");
        p.add("gui.champions.editor.header.tier",            "Nível");
        p.add("gui.champions.editor.header.display",         "Exibição");
        p.add("gui.champions.editor.header.import_export",   "Importar / Exportar");
        p.add("gui.champions.editor.header.world_datapacks", "Pacotes de Dados do Mundo");
        p.add("gui.champions.editor.header.pack",            "Pacote: %s");
        p.add("gui.champions.editor.header.server_config",   "Configuração do Servidor");
        p.add("gui.champions.editor.header.filter",          "Filtro %s");

        p.add("gui.champions.editor.action.add_candidate",     "+ Adicionar candidato");
        p.add("gui.champions.editor.action.add_pool",          "+ Adicionar pool");
        p.add("gui.champions.editor.action.add_effect",        "+ Adicionar efeito");
        p.add("gui.champions.editor.action.add_phase",         "+ Adicionar fase");
        p.add("gui.champions.editor.action.add_filter",        "+ Adicionar filtro");
        p.add("gui.champions.editor.action.add_child_filter",  "+ Adicionar subfiltro");
        p.add("gui.champions.editor.action.pick_affix_values",  "Valores de afixo… (%s selecionado(s))");
        p.add("gui.champions.editor.action.pick_entity_types",  "Tipos de entidade… (%s selecionado(s))");
        p.add("gui.champions.editor.action.pick_mod_namespaces", "Namespaces de mods… (%s selecionado(s))");
        p.add("gui.champions.editor.action.pick_categories",    "Categorias… (%s selecionado(s))");
        p.add("gui.champions.editor.action.export",            "§bExportar conteúdo do editor → zip");
        p.add("gui.champions.editor.action.import",            "§bImportar zips de champions_imports/");

        p.add("gui.champions.editor.hint.unknown_condition", "§ctipo de condição desconhecido");
        p.add("gui.champions.editor.hint.unknown_effect",    "§ctipo de efeito desconhecido");
        p.add("gui.champions.editor.hint.unknown_filter",    "§ctipo de filtro desconhecido: %s");
        p.add("gui.champions.editor.hint.matches_every",     "corresponde a todas as entidades");
        p.add("gui.champions.editor.hint.filters_anded",     "os subfiltros são combinados com E");
        p.add("gui.champions.editor.hint.filters_ored",      "os subfiltros são combinados com OU");
        p.add("gui.champions.editor.hint.tier_display",      "cor: string hex · ícone: caminho da textura");
        p.add("gui.champions.editor.hint.json_view_only",    "affixes.matches / affixes.count: use a visualização JSON");
        p.add("gui.champions.editor.hint.export_target",     "grava champions_<hora>.zip em");
        p.add("gui.champions.editor.hint.exports_dir",       "<world>/champions_exports/");
        p.add("gui.champions.editor.hint.import_dir",        "coloque zips de pacotes em <world>/champions_imports/");
        p.add("gui.champions.editor.hint.import_copied",     "eles são copiados para datapacks/ e ativados");
        p.add("gui.champions.editor.hint.no_packs",          "nenhum pacote carregado");
        p.add("gui.champions.editor.hint.packs_enabled",     "%s / %s ativado(s)");
        p.add("gui.champions.editor.hint.reload_on_toggle",  "alternar recarrega os recursos do servidor");
        p.add("gui.champions.editor.hint.select_pack",       "selecione um pacote na lista para ativar/desativar");
        p.add("gui.champions.editor.hint.config_reload",     "os valores são aplicados ao Salvar e Recarregar");
        p.add("gui.champions.editor.pack.enabled",           "§a● ativado");
        p.add("gui.champions.editor.pack.disabled",          "§c○ desativado");

        p.add("gui.champions.editor.filter.type.any",          "qualquer");
        p.add("gui.champions.editor.filter.type.all_of",       "todos (E)");
        p.add("gui.champions.editor.filter.type.any_of",       "qualquer um (OU)");
        p.add("gui.champions.editor.filter.type.entity_type",  "tipo de entidade");
        p.add("gui.champions.editor.filter.type.entity_tag",   "tag de entidade");
        p.add("gui.champions.editor.filter.type.mod_id",       "ID do mod");
        p.add("gui.champions.editor.filter.type.mob_category", "categoria de mob");
        p.add("gui.champions.editor.filter.type.attribute",    "atributo");
    }
}
