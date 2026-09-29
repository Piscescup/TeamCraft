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
