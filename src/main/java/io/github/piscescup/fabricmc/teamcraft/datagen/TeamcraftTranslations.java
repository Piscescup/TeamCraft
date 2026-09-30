package io.github.piscescup.fabricmc.teamcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

/**
 * Shared translation entries used by both language-specific providers.
 */
final class TeamcraftTranslations
{
    private TeamcraftTranslations() {
    }

    static void addEnglish(FabricLanguageProvider.TranslationBuilder b) {
        b.add("teamcraft.title.help", "Command Guide");
        b.add("teamcraft.title.status", "Current Status");
        b.add("teamcraft.title.candidates", "Candidate List");
        b.add("teamcraft.title.split_result", "Split Complete");
        b.add("teamcraft.title.team_info", "Team Details");

        b.add("teamcraft.gui.title", "TeamCraft Configuration");
        b.add("teamcraft.gui.category.split", "Split Settings");
        b.add("teamcraft.gui.category.appearance", "Team Appearance");
        b.add("teamcraft.gui.option.rule", "Split rule");
        b.add("teamcraft.gui.option.rule.tooltip", "Choose whether the split is calculated from a maximum team size or a fixed number of teams.");
        b.add("teamcraft.gui.option.rule.example", "Example: 10 players with 4 per team creates 3 teams; 10 players across 3 teams produces sizes 4 / 3 / 3.");
        b.add("teamcraft.gui.option.players_per_team", "Players per team");
        b.add("teamcraft.gui.option.players_per_team.tooltip", "The maximum number of players in each team. The required number of teams is calculated automatically.");
        b.add("teamcraft.gui.option.players_per_team.example", "Example: 10 players and a limit of 4 produces teams of 4 / 4 / 2.");
        b.add("teamcraft.gui.option.team_count", "Number of teams");
        b.add("teamcraft.gui.option.team_count.tooltip", "Creates exactly this many teams and distributes the candidate players as evenly as possible.");
        b.add("teamcraft.gui.option.team_count.example", "Example: 10 players in 3 teams produces team sizes of 4 / 3 / 3.");
        b.add("teamcraft.gui.option.mode", "Assignment mode");
        b.add("teamcraft.gui.option.mode.tooltip", "Ordered mode follows the candidate list; random mode shuffles it before assigning teams.");
        b.add("teamcraft.gui.option.mode.example", "Example: A, B, C, D in pairs becomes A+B / C+D in ordered mode, while random mode may differ each time.");
        b.add("teamcraft.gui.option.friendly_fire", "Friendly fire");
        b.add("teamcraft.gui.option.friendly_fire.tooltip", "Controls whether members of the same newly created team can damage each other.");
        b.add("teamcraft.gui.option.friendly_fire.example", "Example: when off, attacks between teammates do not deal damage.");
        b.add("teamcraft.gui.option.colors", "Team colors");
        b.add("teamcraft.gui.option.colors.tooltip", "Choose colors in team order. Leave the list empty for the default 16-color palette; a short list repeats.");
        b.add("teamcraft.gui.option.colors.example", "Example: red blue for 4 teams gives red / blue / red / blue.");
        b.add("teamcraft.gui.option.names", "Team names");
        b.add("teamcraft.gui.option.names.tooltip", "Enter display names separated by commas, semicolons, or |. Missing names are generated from team colors.");
        b.add("teamcraft.gui.option.names.example", "Example: Alpha, Bravo for 3 teams gives the third team an automatic color-based name.");
        b.add("teamcraft.gui.rule.players_per_team", "Players per team");
        b.add("teamcraft.gui.rule.team_count", "Fixed team count");
        b.add("teamcraft.gui.placeholder.number", "Enter a number");
        b.add("teamcraft.gui.placeholder.colors", "Default palette");
        b.add("teamcraft.gui.placeholder.names", "Automatic names");
        b.add("teamcraft.gui.session_note", "Applies to the next split in this server session; existing teams are unchanged");
        b.add("teamcraft.gui.defaults", "Defaults");
        b.add("teamcraft.gui.cancel", "Cancel");
        b.add("teamcraft.gui.apply", "Apply");
        b.add("teamcraft.gui.done", "Done");
        b.add("teamcraft.gui.loading", "Loading TeamCraft configuration...");
        b.add("teamcraft.gui.saved", "TeamCraft configuration saved");
        b.add("teamcraft.gui.defaults_ready", "Default values loaded; press Apply or Save & Split to save them");
        b.add("teamcraft.gui.error.server_unsupported", "This server does not support the TeamCraft configuration screen");
        b.add("teamcraft.gui.error.invalid_server", "The server rejected these settings; check the values and try again");
        b.add("teamcraft.gui.error.permission", "You do not have permission to edit TeamCraft settings");
        b.add("teamcraft.gui.error.players_per_team", "Players per team must be a whole number from %1$s to %2$s");
        b.add("teamcraft.gui.error.team_count", "Number of teams must be a whole number from %1$s to %2$s");
        b.add("teamcraft.gui.error.color", "Unknown team color: %s");
        b.add("teamcraft.gui.error.too_many_values", "At most %s values may be entered");
        b.add("teamcraft.gui.error.name_too_long", "Each team name may contain at most %s characters");
        b.add("teamcraft.gui.nav.team_config", "Team Setup");
        b.add("teamcraft.gui.nav.team_config.tooltip", "Configure how candidates are split, then save or create the teams.");
        b.add("teamcraft.gui.nav.own_team", "My Team");
        b.add("teamcraft.gui.nav.own_team.tooltip", "View and edit the managed team you currently belong to.");
        b.add("teamcraft.gui.nav.all_teams", "All Teams");
        b.add("teamcraft.gui.nav.all_teams.tooltip", "Browse every scoreboard team currently managed by TeamCraft.");
        b.add("teamcraft.gui.category.own_team", "My Team Settings");
        b.add("teamcraft.gui.category.all_teams", "All TeamCraft Teams");
        b.add("teamcraft.gui.colors.default_palette", "Using the default 16-color palette");
        b.add("teamcraft.gui.colors.slot", "Color #%s");
        b.add("teamcraft.gui.colors.remove", "Remove");
        b.add("teamcraft.gui.colors.picker", "Choose color");
        b.add("teamcraft.gui.colors.add", "Add color");
        b.add("teamcraft.gui.colors.use_defaults", "Use default palette");
        b.add("teamcraft.gui.own_team.none", "You are not in a TeamCraft-managed team");
        b.add("teamcraft.gui.own_team.none.tooltip", "Create the teams first, or join a team whose internal ID starts with teamcraft_.");
        b.add("teamcraft.gui.own_team.id", "Internal ID");
        b.add("teamcraft.gui.own_team.name", "Display name");
        b.add("teamcraft.gui.own_team.name.tooltip", "Changes the display name and the prefix shown before every member's name.");
        b.add("teamcraft.gui.own_team.name.example", "Example: changing the name to Builders displays [Builders] before member names.");
        b.add("teamcraft.gui.own_team.color", "Team color");
        b.add("teamcraft.gui.own_team.color.tooltip", "Changes this team's scoreboard color and member-name prefix color.");
        b.add("teamcraft.gui.own_team.color.example", "Example: choosing blue makes the team name and [Team] prefix blue.");
        b.add("teamcraft.gui.own_team.friendly_fire", "Friendly fire");
        b.add("teamcraft.gui.own_team.friendly_fire.tooltip", "Controls whether members of your existing team can damage each other.");
        b.add("teamcraft.gui.own_team.friendly_fire.example", "Example: when off, attacks between your teammates do not deal damage.");
        b.add("teamcraft.gui.own_team.members", "Members");
        b.add("teamcraft.gui.all_teams.none", "No TeamCraft-managed teams exist yet");
        b.add("teamcraft.gui.all_teams.tooltip", "ID: %1$s\nColor: %2$s\nFriendly fire: %3$s\nMembers: %4$s");
        b.add("teamcraft.gui.placeholder.team_name", "Enter a team name");
        b.add("teamcraft.gui.close", "Close");
        b.add("teamcraft.gui.split", "Save & Split");
        b.add("teamcraft.gui.refresh", "Refresh");
        b.add("teamcraft.gui.save_team", "Save Team");
        b.add("teamcraft.gui.refreshed", "Team information refreshed");
        b.add("teamcraft.gui.built", "Teams created; the All Teams page now shows the result");
        b.add("teamcraft.gui.team_saved", "Your team settings were saved");
        b.add("teamcraft.gui.error.no_candidates", "No candidate players are configured; run /teamcraft init <players...> first");
        b.add("teamcraft.gui.error.teams_exist", "TeamCraft teams already exist; run /teamcraft clear before splitting again");
        b.add("teamcraft.gui.error.too_many_teams", "The requested team count is greater than the number of candidate players");
        b.add("teamcraft.gui.error.team_not_found", "Your TeamCraft-managed team could not be found; refresh the page");
        b.add("teamcraft.gui.error.empty_team_name", "The team display name cannot be empty");
        b.add("key.teamcraft.open_config", "Open TeamCraft configuration");
        b.add("key.category.teamcraft.general", "TeamCraft");

        b.add("teamcraft.help.init", "Replace the candidate list (selectors such as @a are supported)");
        b.add("teamcraft.help.init_edit", "Add or remove candidate players");
        b.add("teamcraft.help.init_manage", "Show or clear the candidate list");
        b.add("teamcraft.help.players_per_team", "Limit the players in each team; the team count is calculated automatically");
        b.add("teamcraft.help.team_count", "Create exactly this many teams and distribute players as evenly as possible");
        b.add("teamcraft.help.mode", "Choose ordered or shuffled assignment");
        b.add("teamcraft.help.friendly_fire", "Allow or prevent damage between teammates");
        b.add("teamcraft.help.colors", "Set the color cycle, e.g. red blue green");
        b.add("teamcraft.help.names", "Set team names; quote names containing spaces");
        b.add("teamcraft.help.start", "Create and assign the teams");
        b.add("teamcraft.help.team", "Adjust a team after it has been created");
        b.add("teamcraft.help.status", "Show candidates, configuration and active teams");
        b.add("teamcraft.help.clear", "Remove created teams but keep the current setup");
        b.add("teamcraft.help.reset", "Remove teams and restore all defaults");

        b.add("teamcraft.common.empty", "Empty");
        b.add("teamcraft.common.default", "Default");
        b.add("teamcraft.common.custom", "Custom");
        b.add("teamcraft.common.none", "None");
        b.add("teamcraft.common.on", "On");
        b.add("teamcraft.common.off", "Off");

        b.add("teamcraft.status.candidates", "Candidates");
        b.add("teamcraft.status.candidates_value", "%1$s players • %2$s");
        b.add("teamcraft.status.split_rule", "Split rule");
        b.add("teamcraft.status.rule_team_count", "Fixed team count • %s teams • players distributed evenly");
        b.add("teamcraft.status.rule_players_per_team", "Players per team • up to %s players • team count calculated automatically");
        b.add("teamcraft.status.mode", "Assignment mode");
        b.add("teamcraft.status.friendly_fire", "Friendly fire");
        b.add("teamcraft.status.colors", "Color cycle");
        b.add("teamcraft.status.names", "Team names");
        b.add("teamcraft.status.names_auto", "Automatic • based on colors (e.g. Red Team, Blue Team)");
        b.add("teamcraft.status.created_teams", "Active teams");
        b.add("teamcraft.status.created_count", "%s teams");
        b.add("teamcraft.status.created_team", "%1$s  %2$s  • %3$s players");

        b.add("teamcraft.init.set", "Candidate list set: %1$s players • %2$s");
        b.add("teamcraft.init.add", "Added %1$s players • %2$s total");
        b.add("teamcraft.init.add_skipped", "Added %1$s players • %2$s total • skipped %3$s duplicates");
        b.add("teamcraft.init.remove", "Removed %1$s players • %2$s remaining");
        b.add("teamcraft.init.remove_missing", "Removed %1$s players • %2$s remaining • %3$s were not listed");
        b.add("teamcraft.init.list_empty", "The candidate list is empty. Use /teamcraft init <players...> first.");
        b.add("teamcraft.init.clear", "Candidate list cleared");

        b.add("teamcraft.config.players_per_team", "Split rule changed to players per team: up to %s players; the team count will be calculated automatically");
        b.add("teamcraft.config.team_count", "Split rule changed to fixed team count: %s teams; players will be distributed as evenly as possible");
        b.add("teamcraft.config.mode", "Assignment mode set to %s");
        b.add("teamcraft.config.friendly_fire", "Friendly fire set to %s for the next split");
        b.add("teamcraft.config.colors", "Color cycle set (repeats when needed): %s");
        b.add("teamcraft.config.colors_reset", "Color cycle restored to the default 16-color palette");
        b.add("teamcraft.config.names", "Team names set (missing names use their color): %s");
        b.add("teamcraft.config.names_reset", "Team names restored to automatic color-based names");
        b.add("teamcraft.config.reset", "All split settings restored to defaults");

        b.add("teamcraft.error.invalid_colors", "Unknown colors: %1$s • available: %2$s");
        b.add("teamcraft.error.no_colors", "No colors supplied • available: %s");
        b.add("teamcraft.error.no_names", "No team names supplied. Quote names containing spaces, e.g. \"Red Team\".");
        b.add("teamcraft.error.no_candidates", "The candidate list is empty. Use /teamcraft init <players...> first.");
        b.add("teamcraft.error.teams_exist", "TeamCraft teams already exist. Run /teamcraft clear before splitting again.");
        b.add("teamcraft.error.too_many_teams", "Team count (%1$s) cannot exceed candidate count (%2$s)");
        b.add("teamcraft.error.empty_name", "The team name cannot be empty");
        b.add("teamcraft.error.not_managed", "Team %1$s is not managed by TeamCraft; only IDs starting with %2$s can be changed");

        b.add("teamcraft.result.summary", "Summary");
        b.add("teamcraft.result.summary_value", "%1$s teams • %2$s players");
        b.add("teamcraft.result.team", "#%1$s  %2$s  • %3$s players\n       %4$s");
        b.add("teamcraft.result.assigned", "You were assigned to %s");

        b.add("teamcraft.team.color", "Changed %1$s to %2$s");
        b.add("teamcraft.team.name", "Renamed %1$s to %2$s; its player prefix was updated");
        b.add("teamcraft.team.friendly_fire", "%1$s friendly fire: %2$s");
        b.add("teamcraft.team.info.id", "Internal ID");
        b.add("teamcraft.team.info.name", "Display name");
        b.add("teamcraft.team.info.color", "Color");
        b.add("teamcraft.team.info.friendly_fire", "Friendly fire");
        b.add("teamcraft.team.info.members", "Members");
        b.add("teamcraft.team.info.members_value", "%1$s players • %2$s");
        b.add("teamcraft.team.auto_name", "%s Team");

        b.add("teamcraft.clear.none", "There are no TeamCraft teams to remove");
        b.add("teamcraft.clear.done", "Removed %s teams; candidates and settings were kept");
        b.add("teamcraft.reset.done", "Reset complete: removed %s teams and restored all defaults");

        b.add("teamcraft.mode.fixed", "Ordered");
        b.add("teamcraft.mode.random", "Random");
        addEnglishColors(b);
    }

    static void addSimplifiedChinese(FabricLanguageProvider.TranslationBuilder b) {
        b.add("teamcraft.title.help", "命令指南");
        b.add("teamcraft.title.status", "当前状态");
        b.add("teamcraft.title.candidates", "候选名单");
        b.add("teamcraft.title.split_result", "分队完成");
        b.add("teamcraft.title.team_info", "队伍详情");

        b.add("teamcraft.gui.title", "TeamCraft 配置");
        b.add("teamcraft.gui.category.split", "分队设置");
        b.add("teamcraft.gui.category.appearance", "队伍外观");
        b.add("teamcraft.gui.option.rule", "分队规则");
        b.add("teamcraft.gui.option.rule.tooltip", "选择按每队人数上限自动计算队伍数，或直接固定队伍数量。");
        b.add("teamcraft.gui.option.rule.example", "示例：10 人且每队 4 人会分成 3 队；10 人固定 3 队会分成 4 / 3 / 3。");
        b.add("teamcraft.gui.option.players_per_team", "每队人数");
        b.add("teamcraft.gui.option.players_per_team.tooltip", "限制每支队伍最多容纳的人数，所需队伍数量会自动计算。");
        b.add("teamcraft.gui.option.players_per_team.example", "示例：10 人、每队最多 4 人，会分成 4 / 4 / 2。");
        b.add("teamcraft.gui.option.team_count", "队伍数量");
        b.add("teamcraft.gui.option.team_count.tooltip", "固定创建多少支队伍，候选玩家会尽量平均分配。");
        b.add("teamcraft.gui.option.team_count.example", "示例：10 人固定分成 3 队，各队人数为 4 / 3 / 3。");
        b.add("teamcraft.gui.option.mode", "分配方式");
        b.add("teamcraft.gui.option.mode.tooltip", "按顺序会严格依照候选名单分组；随机会先打乱名单再分组。");
        b.add("teamcraft.gui.option.mode.example", "示例：A、B、C、D 每队 2 人，按顺序为 A+B / C+D；随机模式每次可能不同。");
        b.add("teamcraft.gui.option.friendly_fire", "友方伤害");
        b.add("teamcraft.gui.option.friendly_fire.tooltip", "控制新创建队伍中的队友能否互相造成伤害。");
        b.add("teamcraft.gui.option.friendly_fire.example", "示例：关闭后，队友之间的攻击不会造成伤害。");
        b.add("teamcraft.gui.option.colors", "队伍颜色");
        b.add("teamcraft.gui.option.colors.tooltip", "按队伍顺序选择颜色。列表为空时使用默认 16 色；颜色不足时会循环使用。");
        b.add("teamcraft.gui.option.colors.example", "示例：4 支队伍填写 red blue，将依次使用红、蓝、红、蓝。");
        b.add("teamcraft.gui.option.names", "队伍名称");
        b.add("teamcraft.gui.option.names.tooltip", "使用逗号、分号或 | 分隔显示名称。名称不足时，其余队伍会按颜色自动命名。");
        b.add("teamcraft.gui.option.names.example", "示例：3 支队伍填写“先锋队, 后援队”，第 3 队会使用颜色自动命名。");
        b.add("teamcraft.gui.rule.players_per_team", "每队人数");
        b.add("teamcraft.gui.rule.team_count", "固定队伍数");
        b.add("teamcraft.gui.placeholder.number", "输入数字");
        b.add("teamcraft.gui.placeholder.colors", "默认调色板");
        b.add("teamcraft.gui.placeholder.names", "自动命名");
        b.add("teamcraft.gui.session_note", "仅影响本次服务器会话中的下一次分队，不会修改已创建的队伍");
        b.add("teamcraft.gui.defaults", "默认值");
        b.add("teamcraft.gui.cancel", "取消");
        b.add("teamcraft.gui.apply", "应用");
        b.add("teamcraft.gui.done", "完成");
        b.add("teamcraft.gui.loading", "正在读取 TeamCraft 配置……");
        b.add("teamcraft.gui.saved", "TeamCraft 配置已保存");
        b.add("teamcraft.gui.defaults_ready", "已载入默认值；按“应用”或“保存并分队”后才会保存");
        b.add("teamcraft.gui.error.server_unsupported", "当前服务器不支持 TeamCraft 配置界面");
        b.add("teamcraft.gui.error.invalid_server", "服务器拒绝了这些设置，请检查数值后重试");
        b.add("teamcraft.gui.error.permission", "你没有修改 TeamCraft 设置的权限");
        b.add("teamcraft.gui.error.players_per_team", "每队人数必须是 %1$s 到 %2$s 之间的整数");
        b.add("teamcraft.gui.error.team_count", "队伍数量必须是 %1$s 到 %2$s 之间的整数");
        b.add("teamcraft.gui.error.color", "无法识别的队伍颜色：%s");
        b.add("teamcraft.gui.error.too_many_values", "最多只能填写 %s 项");
        b.add("teamcraft.gui.error.name_too_long", "每个队伍名称最多包含 %s 个字符");
        b.add("teamcraft.gui.nav.team_config", "队伍配置");
        b.add("teamcraft.gui.nav.team_config.tooltip", "设置候选玩家的分队方式，然后保存配置或直接创建队伍。");
        b.add("teamcraft.gui.nav.own_team", "所属队伍信息配置");
        b.add("teamcraft.gui.nav.own_team.tooltip", "查看并配置你当前所属的 TeamCraft 队伍。");
        b.add("teamcraft.gui.nav.all_teams", "所有队伍信息展示");
        b.add("teamcraft.gui.nav.all_teams.tooltip", "查看当前由 TeamCraft 管理的全部记分板队伍。");
        b.add("teamcraft.gui.category.own_team", "所属队伍信息配置");
        b.add("teamcraft.gui.category.all_teams", "所有队伍信息展示");
        b.add("teamcraft.gui.colors.default_palette", "正在使用默认 16 色调色板");
        b.add("teamcraft.gui.colors.slot", "颜色 #%s");
        b.add("teamcraft.gui.colors.remove", "移除");
        b.add("teamcraft.gui.colors.picker", "选择颜色");
        b.add("teamcraft.gui.colors.add", "添加颜色");
        b.add("teamcraft.gui.colors.use_defaults", "使用默认调色板");
        b.add("teamcraft.gui.own_team.none", "你当前不属于 TeamCraft 管理的队伍");
        b.add("teamcraft.gui.own_team.none.tooltip", "请先创建队伍，或加入内部 ID 以 teamcraft_ 开头的队伍。");
        b.add("teamcraft.gui.own_team.id", "内部 ID");
        b.add("teamcraft.gui.own_team.name", "显示名称");
        b.add("teamcraft.gui.own_team.name.tooltip", "修改队伍显示名称，以及显示在每位成员名字前的队伍前缀。");
        b.add("teamcraft.gui.own_team.name.example", "示例：改为“建筑队”后，成员名字前会显示 [建筑队]。");
        b.add("teamcraft.gui.own_team.color", "队伍颜色");
        b.add("teamcraft.gui.own_team.color.tooltip", "修改队伍在记分板中的颜色和成员名字前缀颜色。");
        b.add("teamcraft.gui.own_team.color.example", "示例：选择蓝色后，队名和 [队伍] 前缀都会显示为蓝色。");
        b.add("teamcraft.gui.own_team.friendly_fire", "友方伤害");
        b.add("teamcraft.gui.own_team.friendly_fire.tooltip", "控制当前已存在队伍中的成员能否互相造成伤害。");
        b.add("teamcraft.gui.own_team.friendly_fire.example", "示例：关闭后，你所在队伍的队友之间攻击不会造成伤害。");
        b.add("teamcraft.gui.own_team.members", "队伍成员");
        b.add("teamcraft.gui.all_teams.none", "当前还没有 TeamCraft 管理的队伍");
        b.add("teamcraft.gui.all_teams.tooltip", "内部 ID：%1$s\n颜色：%2$s\n友方伤害：%3$s\n成员：%4$s");
        b.add("teamcraft.gui.placeholder.team_name", "输入队伍名称");
        b.add("teamcraft.gui.close", "关闭");
        b.add("teamcraft.gui.split", "保存并分队");
        b.add("teamcraft.gui.refresh", "刷新");
        b.add("teamcraft.gui.save_team", "保存队伍");
        b.add("teamcraft.gui.refreshed", "队伍信息已刷新");
        b.add("teamcraft.gui.built", "分队完成；结果已显示在“所有队伍”页面");
        b.add("teamcraft.gui.team_saved", "所属队伍设置已保存");
        b.add("teamcraft.gui.error.no_candidates", "候选玩家为空，请先执行 /teamcraft init <players...>");
        b.add("teamcraft.gui.error.teams_exist", "已存在 TeamCraft 队伍，请先执行 /teamcraft clear 再重新分队");
        b.add("teamcraft.gui.error.too_many_teams", "队伍数量不能大于候选玩家数量");
        b.add("teamcraft.gui.error.team_not_found", "找不到你所属的 TeamCraft 队伍，请刷新页面");
        b.add("teamcraft.gui.error.empty_team_name", "队伍显示名称不能为空");
        b.add("key.teamcraft.open_config", "打开 TeamCraft 配置");
        b.add("key.category.teamcraft.general", "TeamCraft");

        b.add("teamcraft.help.init", "替换候选名单（支持 @a 等选择器）");
        b.add("teamcraft.help.init_edit", "添加或移除候选玩家");
        b.add("teamcraft.help.init_manage", "查看或清空候选名单");
        b.add("teamcraft.help.players_per_team", "限制每队人数，并根据候选人数自动计算队伍数量");
        b.add("teamcraft.help.team_count", "固定队伍数量，并将候选玩家尽量平均分配");
        b.add("teamcraft.help.mode", "选择按名单顺序或打乱后分配");
        b.add("teamcraft.help.friendly_fire", "设置是否允许队友之间互相伤害");
        b.add("teamcraft.help.colors", "设置颜色循环，例如 red blue green");
        b.add("teamcraft.help.names", "设置队名；含空格的名称需要引号");
        b.add("teamcraft.help.start", "创建队伍并分配玩家");
        b.add("teamcraft.help.team", "在分队后调整指定队伍");
        b.add("teamcraft.help.status", "查看候选玩家、配置与现有队伍");
        b.add("teamcraft.help.clear", "解散已创建队伍，但保留当前配置");
        b.add("teamcraft.help.reset", "解散队伍并恢复全部默认设置");

        b.add("teamcraft.common.empty", "空");
        b.add("teamcraft.common.default", "默认");
        b.add("teamcraft.common.custom", "自定义");
        b.add("teamcraft.common.none", "无");
        b.add("teamcraft.common.on", "开启");
        b.add("teamcraft.common.off", "关闭");

        b.add("teamcraft.status.candidates", "候选玩家");
        b.add("teamcraft.status.candidates_value", "%1$s 人 • %2$s");
        b.add("teamcraft.status.split_rule", "分队规则");
        b.add("teamcraft.status.rule_team_count", "固定队伍数量 • %s 队 • 玩家尽量均摊");
        b.add("teamcraft.status.rule_players_per_team", "限制每队人数 • 最多 %s 人/队 • 自动计算队伍数量");
        b.add("teamcraft.status.mode", "分队方式");
        b.add("teamcraft.status.friendly_fire", "友方伤害");
        b.add("teamcraft.status.colors", "颜色循环");
        b.add("teamcraft.status.names", "队名方案");
        b.add("teamcraft.status.names_auto", "自动 • 按颜色命名（如红队、蓝队）");
        b.add("teamcraft.status.created_teams", "现有队伍");
        b.add("teamcraft.status.created_count", "%s 队");
        b.add("teamcraft.status.created_team", "%1$s  %2$s  • %3$s 人");

        b.add("teamcraft.init.set", "候选名单已设置：%1$s 人 • %2$s");
        b.add("teamcraft.init.add", "已添加 %1$s 人 • 当前共 %2$s 人");
        b.add("teamcraft.init.add_skipped", "已添加 %1$s 人 • 当前共 %2$s 人 • 跳过 %3$s 个重复玩家");
        b.add("teamcraft.init.remove", "已移除 %1$s 人 • 剩余 %2$s 人");
        b.add("teamcraft.init.remove_missing", "已移除 %1$s 人 • 剩余 %2$s 人 • %3$s 人原本不在名单中");
        b.add("teamcraft.init.list_empty", "候选名单为空，请先使用 /teamcraft init <players...> 设置名单");
        b.add("teamcraft.init.clear", "候选名单已清空");

        b.add("teamcraft.config.players_per_team", "分队规则已改为限制每队人数：最多 %s 人/队；队伍数量将自动计算");
        b.add("teamcraft.config.team_count", "分队规则已改为固定队伍数量：%s 队；候选玩家将尽量平均分配");
        b.add("teamcraft.config.mode", "分队方式已设为%s");
        b.add("teamcraft.config.friendly_fire", "友方伤害已设为%s，将在下次分队时生效");
        b.add("teamcraft.config.colors", "颜色循环已设置（不足时重复使用）：%s");
        b.add("teamcraft.config.colors_reset", "颜色循环已恢复为默认 16 色调色板");
        b.add("teamcraft.config.names", "队名已设置（不足时按颜色命名）：%s");
        b.add("teamcraft.config.names_reset", "队名已恢复为按颜色自动命名");
        b.add("teamcraft.config.reset", "全部分队设置已恢复默认");

        b.add("teamcraft.error.invalid_colors", "无法识别的颜色：%1$s • 可用颜色：%2$s");
        b.add("teamcraft.error.no_colors", "未提供颜色 • 可用颜色：%s");
        b.add("teamcraft.error.no_names", "未提供队名；含空格的名称可使用引号，例如 \"Red Team\"");
        b.add("teamcraft.error.no_candidates", "候选名单为空，请先使用 /teamcraft init <players...> 设置名单");
        b.add("teamcraft.error.teams_exist", "已存在 TeamCraft 队伍，请先执行 /teamcraft clear 再次分队");
        b.add("teamcraft.error.too_many_teams", "队伍数量（%1$s）不能超过候选人数（%2$s）");
        b.add("teamcraft.error.empty_name", "队名不能为空");
        b.add("teamcraft.error.not_managed", "队伍 %1$s 不是由 TeamCraft 创建的；只能修改 ID 以 %2$s 开头的队伍");

        b.add("teamcraft.result.summary", "分队概览");
        b.add("teamcraft.result.summary_value", "%1$s 队 • %2$s 人");
        b.add("teamcraft.result.team", "#%1$s  %2$s  • %3$s 人\n       %4$s");
        b.add("teamcraft.result.assigned", "你已被分配到%s");

        b.add("teamcraft.team.color", "已将%1$s改为%2$s");
        b.add("teamcraft.team.name", "已将 %1$s 重命名为%2$s；玩家名前缀已同步更新");
        b.add("teamcraft.team.friendly_fire", "%1$s的友方伤害：%2$s");
        b.add("teamcraft.team.info.id", "内部 ID");
        b.add("teamcraft.team.info.name", "显示名称");
        b.add("teamcraft.team.info.color", "颜色");
        b.add("teamcraft.team.info.friendly_fire", "友方伤害");
        b.add("teamcraft.team.info.members", "成员");
        b.add("teamcraft.team.info.members_value", "%1$s 人 • %2$s");
        b.add("teamcraft.team.auto_name", "%s队");

        b.add("teamcraft.clear.none", "当前没有由 TeamCraft 创建的队伍");
        b.add("teamcraft.clear.done", "已解散 %s 支队伍；候选名单与配置均已保留");
        b.add("teamcraft.reset.done", "重置完成：已解散 %s 支队伍并恢复全部默认设置");

        b.add("teamcraft.mode.fixed", "按顺序");
        b.add("teamcraft.mode.random", "随机");
        addChineseColors(b);
    }

    private static void addEnglishColors(FabricLanguageProvider.TranslationBuilder b) {
        b.add("teamcraft.color.black", "Black");
        b.add("teamcraft.color.dark_blue", "Dark Blue");
        b.add("teamcraft.color.dark_green", "Dark Green");
        b.add("teamcraft.color.dark_aqua", "Dark Aqua");
        b.add("teamcraft.color.dark_red", "Dark Red");
        b.add("teamcraft.color.dark_purple", "Dark Purple");
        b.add("teamcraft.color.gold", "Gold");
        b.add("teamcraft.color.gray", "Gray");
        b.add("teamcraft.color.dark_gray", "Dark Gray");
        b.add("teamcraft.color.blue", "Blue");
        b.add("teamcraft.color.green", "Green");
        b.add("teamcraft.color.aqua", "Aqua");
        b.add("teamcraft.color.red", "Red");
        b.add("teamcraft.color.light_purple", "Light Purple");
        b.add("teamcraft.color.yellow", "Yellow");
        b.add("teamcraft.color.white", "White");
    }

    private static void addChineseColors(FabricLanguageProvider.TranslationBuilder b) {
        b.add("teamcraft.color.black", "黑色");
        b.add("teamcraft.color.dark_blue", "深蓝色");
        b.add("teamcraft.color.dark_green", "深绿色");
        b.add("teamcraft.color.dark_aqua", "深青色");
        b.add("teamcraft.color.dark_red", "深红色");
        b.add("teamcraft.color.dark_purple", "深紫色");
        b.add("teamcraft.color.gold", "金色");
        b.add("teamcraft.color.gray", "灰色");
        b.add("teamcraft.color.dark_gray", "深灰色");
        b.add("teamcraft.color.blue", "蓝色");
        b.add("teamcraft.color.green", "绿色");
        b.add("teamcraft.color.aqua", "青色");
        b.add("teamcraft.color.red", "红色");
        b.add("teamcraft.color.light_purple", "粉红色");
        b.add("teamcraft.color.yellow", "黄色");
        b.add("teamcraft.color.white", "白色");
    }

}
