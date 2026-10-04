package io.github.piscescup.fabricmc.teamcraft.text;

/**
 * Single source of truth for every TeamCraft translation.
 *
 * <p>Each entry stores the translation key followed by Simplified Chinese and
 * US English text. Language providers only select the desired field.</p>
 */
public enum TeamcraftTranslations {
    KEY_CATEGORY_TEAMCRAFT_GENERAL("key.category.teamcraft.general", "TeamCraft", "TeamCraft"),
    KEY_TEAMCRAFT_OPEN_CONFIG("key.teamcraft.open_config", "打开 TeamCraft 配置", "Open TeamCraft configuration"),
    KEY_TEAMCRAFT_OPEN_TEAMS("key.teamcraft.open_teams", "查看所有队伍", "Show all teams"),
    KEY_TEAMCRAFT_OPEN_OWN_TEAM("key.teamcraft.open_own_team", "查看自己的队伍", "Show my team"),
    GUI_NAV_HOTKEYS("teamcraft.gui.nav.hotkeys", "快捷键", "Hotkeys"),
    GUI_NAV_HOTKEYS_TOOLTIP("teamcraft.gui.nav.hotkeys.tooltip", "设置常用页面的快捷键，仅保存在本地", "Bind shortcuts for common pages; saved locally"),
    GUI_EXPAND_HINT("teamcraft.gui.expand_hint", "提示：点击 [+] 以展开", "Hint: click [+] to expand"),
    GUI_TOOLTIP_FUNCTION("teamcraft.gui.tooltip.function", "功能：", "Function: "),
    GUI_TOOLTIP_EXAMPLE("teamcraft.gui.tooltip.example", "示例：", "Example: "),
    GUI_RESET("teamcraft.gui.reset", "重置", "Reset"),
    GUI_RESET_TOOLTIP("teamcraft.gui.reset.tooltip", "重置仅恢复此项的默认值；保存后才会应用到服务器。", "Reset restores only this setting to its default; save to apply it to the server."),
    GUI_BUTTON_ACTION("teamcraft.gui.button.action", "操作：%s", "Action: %s"),
    GUI_BUTTON_CHANGE("teamcraft.gui.button.change", "点击或滚动切换%s", "Click or scroll to change %s"),
    GUI_BUTTON_EXPAND("teamcraft.gui.button.expand", "展开此分组", "Expand this group"),
    GUI_BUTTON_COLLAPSE("teamcraft.gui.button.collapse", "收起此分组", "Collapse this group"),
    GUI_BUTTON_COLOR("teamcraft.gui.button.color", "打开颜色选择器", "Open the color picker"),
    GUI_BUTTON_SELECT_ALL("teamcraft.gui.button.select_all", "将所有在线玩家加入候选名单", "Add all online players to the candidates"),
    GUI_BUTTON_CLEAR_CANDIDATES("teamcraft.gui.button.clear_candidates", "清空候选玩家选择", "Clear the candidate selection"),
    GUI_BUTTON_SELECT_PLAYER("teamcraft.gui.button.select_player", "将 %s 加入候选名单", "Add %s to the candidates"),
    GUI_BUTTON_REMOVE_PLAYER("teamcraft.gui.button.remove_player", "将 %s 移出候选名单；拖动可调整顺序", "Remove %s from the candidates; drag to reorder"),
    GUI_BUTTON_ADD_NAME("teamcraft.gui.button.add_name", "将输入的队名加入列表", "Add the entered team name to the list"),
    GUI_BUTTON_REMOVE_NAME("teamcraft.gui.button.remove_name", "从列表移除此队名", "Remove this team name from the list"),
    GUI_BUTTON_NAME_SLOT("teamcraft.gui.button.name_slot", "此位置使用的队名", "The team name configured for this slot"),
    GUI_BUTTON_ADD_COLOR("teamcraft.gui.button.add_color", "将选中的颜色加入列表", "Add the selected color to the list"),
    GUI_BUTTON_REMOVE_COLOR("teamcraft.gui.button.remove_color", "从列表移除此颜色", "Remove this color from the list"),
    GUI_BUTTON_DEFAULT_COLORS("teamcraft.gui.button.default_colors", "清空自定义颜色，恢复默认调色板", "Clear custom colors and restore the default palette"),
    GUI_BUTTON_DETAILS("teamcraft.gui.button.details", "查看此队伍的详细信息", "View this team's details"),
    GUI_BUTTON_BIND_KEY("teamcraft.gui.button.bind_key", "点击后按下键盘或鼠标按键设置快捷键", "Click, then press a keyboard or mouse button to bind a shortcut"),
    GUI_BUTTON_CLEAR_KEY("teamcraft.gui.button.clear_key", "清除该快捷键绑定", "Clear this shortcut binding"),
    GUI_BUTTON_RESET_KEY("teamcraft.gui.button.reset_key", "恢复该快捷键的默认绑定", "Restore this shortcut's default binding"),
    GUI_BUTTON_CLOSE("teamcraft.gui.button.close", "关闭配置页面", "Close the configuration screen"),
    GUI_BUTTON_DEFAULTS("teamcraft.gui.button.defaults", "将全部分队配置草稿恢复默认值", "Restore all split settings in the draft to defaults"),
    GUI_BUTTON_APPLY("teamcraft.gui.button.apply", "保存配置和候选名单，不执行分队", "Save settings and candidates without splitting teams"),
    GUI_BUTTON_SPLIT("teamcraft.gui.button.split", "保存配置并为候选玩家分队", "Save settings and split the candidate players into teams"),
    GUI_BUTTON_REFRESH("teamcraft.gui.button.refresh", "从服务器重新获取配置与队伍信息", "Reload settings and team information from the server"),
    GUI_BUTTON_SAVE_TEAM("teamcraft.gui.button.save_team", "保存当前队伍的修改", "Save changes to your team"),
    GUI_BUTTON_CLEAR_TEAMS("teamcraft.gui.button.clear_teams", "解散全部 TeamCraft 队伍，保留配置和候选名单", "Disband all TeamCraft teams, keeping settings and candidates"),
    GUI_HOTKEYS_HINT("teamcraft.gui.hotkeys.hint", "点击按键按钮后按下键盘或鼠标按键；Esc 取消绑定。快捷键也可以在游戏的按键设置中修改。", "Click a key button, then press a keyboard or mouse button. Esc clears the binding. These shortcuts also appear in Minecraft's Controls settings."),
    GUI_HOTKEYS_WAITING("teamcraft.gui.hotkeys.waiting", "请按下按键…", "Press a key…"),
    GUI_HOTKEYS_RESET("teamcraft.gui.hotkeys.reset", "重置", "RESET"),
    GUI_HOTKEYS_CLEAR("teamcraft.gui.hotkeys.clear", "清除", "Clear"),
    GUI_HOTKEYS_CONFLICT("teamcraft.gui.hotkeys.conflict", "此按键与 %s 冲突，请选择其他按键", "This key conflicts with %s; choose another key"),
    GUI_HOTKEYS_OPEN_CONFIG_TOOLTIP("teamcraft.gui.hotkeys.open_config.tooltip", "打开 TeamCraft 配置页面。服务器仍会检查权限。", "Open TeamCraft's configuration page. Server permissions still apply."),
    GUI_HOTKEYS_OPEN_TEAMS_TOOLTIP("teamcraft.gui.hotkeys.open_teams.tooltip", "直接打开所有队伍列表，查看队名、颜色和成员。", "Open the all-teams list to view team names, colors and members."),
    GUI_HOTKEYS_OPEN_OWN_TEAM_TOOLTIP("teamcraft.gui.hotkeys.open_own_team.tooltip", "直接打开自己的队伍页面，查看所属队伍。修改仍需要相应权限。", "Open your team's page. Changing team settings still requires permission."),
    CLEAR_DONE("teamcraft.clear.done", "已解散 %s 支队伍；候选名单与配置均已保留", "Removed %s teams; candidates and settings were kept"),
    CLEAR_NONE("teamcraft.clear.none", "当前没有由 TeamCraft 创建的队伍", "There are no TeamCraft teams to remove"),
    COLOR_AQUA("teamcraft.color.aqua", "青色", "Aqua"),
    COLOR_BLACK("teamcraft.color.black", "黑色", "Black"),
    COLOR_BLUE("teamcraft.color.blue", "蓝色", "Blue"),
    COLOR_DARK_AQUA("teamcraft.color.dark_aqua", "深青色", "Dark Aqua"),
    COLOR_DARK_BLUE("teamcraft.color.dark_blue", "深蓝色", "Dark Blue"),
    COLOR_DARK_GRAY("teamcraft.color.dark_gray", "深灰色", "Dark Gray"),
    COLOR_DARK_GREEN("teamcraft.color.dark_green", "深绿色", "Dark Green"),
    COLOR_DARK_PURPLE("teamcraft.color.dark_purple", "深紫色", "Dark Purple"),
    COLOR_DARK_RED("teamcraft.color.dark_red", "深红色", "Dark Red"),
    COLOR_GOLD("teamcraft.color.gold", "金色", "Gold"),
    COLOR_GRAY("teamcraft.color.gray", "灰色", "Gray"),
    COLOR_GREEN("teamcraft.color.green", "绿色", "Green"),
    COLOR_LIGHT_PURPLE("teamcraft.color.light_purple", "粉红色", "Light Purple"),
    COLOR_RED("teamcraft.color.red", "红色", "Red"),
    COLOR_WHITE("teamcraft.color.white", "白色", "White"),
    COLOR_YELLOW("teamcraft.color.yellow", "黄色", "Yellow"),
    COMMON_CUSTOM("teamcraft.common.custom", "自定义", "Custom"),
    COMMON_DEFAULT("teamcraft.common.default", "默认", "Default"),
    COMMON_EMPTY("teamcraft.common.empty", "空", "Empty"),
    COMMON_NONE("teamcraft.common.none", "无", "None"),
    COMMON_OFF("teamcraft.common.off", "关闭", "Off"),
    COMMON_ON("teamcraft.common.on", "开启", "On"),
    CONFIG_COLORS(
        "teamcraft.config.colors",
        "颜色循环已设置（不足时重复使用）：%s",
        "Color cycle set (repeats when needed): %s"
    ),
    CONFIG_COLORS_RESET(
        "teamcraft.config.colors_reset",
        "颜色循环已恢复为默认 16 色调色板",
        "Color cycle restored to the default 16-color palette"
    ),
    CONFIG_FRIENDLY_FIRE(
        "teamcraft.config.friendly_fire",
        "友方伤害已设为%s，将在下次分队时生效",
        "Friendly fire set to %s for the next split"
    ),
    CONFIG_MODE(
        "teamcraft.config.mode",
        "分队方式已设为%s",
        "Assignment mode set to %s"
    ),
    CONFIG_NAMES(
        "teamcraft.config.names",
        "队名已设置（不足时按颜色命名）：%s",
        "Team names set (missing names use their color): %s"
    ),
    CONFIG_NAMES_RESET(
        "teamcraft.config.names_reset",
        "队名已恢复为按颜色自动命名",
        "Team names restored to automatic color-based names"
    ),
    CONFIG_PLAYERS_PER_TEAM(
        "teamcraft.config.players_per_team",
        "分队规则已改为限制每队人数：最多 %s 人/队；队伍数量将自动计算",
        "Split rule changed to players per team: up to %s players; the team count will be calculated automatically"
    ),
    CONFIG_RESET("teamcraft.config.reset", "全部分队设置已恢复默认", "All split settings restored to defaults"),
    CONFIG_TEAM_COUNT(
        "teamcraft.config.team_count",
        "分队规则已改为固定队伍数量：%s 队；候选玩家将尽量平均分配",
        "Split rule changed to fixed team count: %s teams; players will be distributed as evenly as possible"
    ),
    ERROR_EMPTY_NAME(
        "teamcraft.error.empty_name",
        "队名不能为空",
        "The team name cannot be empty"
    ),
    ERROR_INVALID_COLORS(
        "teamcraft.error.invalid_colors",
        "无法识别的颜色：%1$s • 可用颜色：%2$s",
        "Unknown colors: %1$s • available: %2$s"
    ),
    ERROR_NO_CANDIDATES(
        "teamcraft.error.no_candidates",
        "候选名单为空，请先使用 /teamcraft init <players...> 设置名单",
        "The candidate list is empty. Use /teamcraft init <players...> first."
    ),
    ERROR_NO_COLORS("teamcraft.error.no_colors", "未提供颜色 • 可用颜色：%s", "No colors supplied • available: %s"),
    ERROR_NO_NAMES(
        "teamcraft.error.no_names",
        "未提供队名；含空格的名称可使用引号，例如 \"Red Team\"",
        "No team names supplied. Quote names containing spaces, e.g. \"Red Team\"."
    ),
    ERROR_NOT_MANAGED(
        "teamcraft.error.not_managed",
        "队伍 %1$s 不是由 TeamCraft 创建的；只能修改 ID 以 %2$s 开头的队伍",
        "Team %1$s is not managed by TeamCraft; only IDs starting with %2$s can be changed"
    ),
    ERROR_TEAM_AMBIGUOUS(
        "teamcraft.error.team_ambiguous",
        "有多支队伍使用名称 %s，请改用对应的内部 ID",
        "Multiple teams use the name %s; use the corresponding internal ID"
    ),
    ERROR_TEAM_NOT_FOUND(
        "teamcraft.error.team_not_found",
        "找不到内部 ID 或当前名称为 %s 的 TeamCraft 队伍",
        "No TeamCraft team has the internal ID or current name %s"
    ),
    ERROR_TEAMS_EXIST(
        "teamcraft.error.teams_exist",
        "已存在 TeamCraft 队伍，请先清除现有队伍后重试",
        "TeamCraft teams already exist. Please clear existing teams and try again."
    ),
    ERROR_TOO_MANY_TEAMS(
        "teamcraft.error.too_many_teams",
        "队伍数量（%1$s）不能超过候选人数（%2$s）",
        "Team count (%1$s) cannot exceed candidate count (%2$s)"
    ),
    GUI_ALL_TEAMS_CLEAR("teamcraft.gui.all_teams.clear", "解除所有队伍", "Remove All Teams"),
    GUI_ALL_TEAMS_CLEARED(
        "teamcraft.gui.all_teams.cleared",
        "已解除所有 TeamCraft 队伍",
        "All TeamCraft teams were removed"
    ),
    GUI_ALL_TEAMS_NONE(
        "teamcraft.gui.all_teams.none",
        "当前还没有 TeamCraft 管理的队伍",
        "No TeamCraft-managed teams exist yet"
    ),
    GUI_ALL_TEAMS_TOOLTIP(
        "teamcraft.gui.all_teams.tooltip",
        "内部 ID：%1$s\n颜色：%2$s\n友方伤害：%3$s\n成员：%4$s",
        "ID: %1$s\nColor: %2$s\nFriendly fire: %3$s\nMembers: %4$s"
    ),
    GUI_APPLY("teamcraft.gui.apply", "应用", "Apply"),
    GUI_BUILT(
        "teamcraft.gui.built",
        "分队完成；结果已显示在“所有队伍”页面",
        "Teams created; the All Teams page now shows the result"
    ),
    GUI_CANCEL("teamcraft.gui.cancel", "取消", "Cancel"),
    GUI_CANDIDATES_CLEAR("teamcraft.gui.candidates.clear", "清空选择", "Clear Selection"),
    GUI_CANDIDATES_NONE_ONLINE(
        "teamcraft.gui.candidates.none_online",
        "当前没有在线玩家",
        "No players are currently online"
    ),
    GUI_CANDIDATES_OFFLINE("teamcraft.gui.candidates.offline", "离线", "Offline"),
    GUI_CANDIDATES_ONLINE("teamcraft.gui.candidates.online", "在线", "Online"),
    GUI_CANDIDATES_PLAYER_EXAMPLE(
        "teamcraft.gui.candidates.player.example",
        "已勾选的玩家会参加下一次分队；上下拖动可调整顺序。",
        "A checked player joins the next split; drag it vertically to change its order."
    ),
    GUI_CANDIDATES_PLAYER_TOOLTIP(
        "teamcraft.gui.candidates.player.tooltip",
        "点击将该玩家加入或移出候选名单；按住已选择的玩家上下拖动可调整顺序。离线玩家会保留，直到手动移除。",
        "Click to add or remove this player. Hold and drag a selected player vertically to reorder the list. Offline players remain until removed."
    ),
    GUI_CANDIDATES_SELECT_ALL("teamcraft.gui.candidates.select_all", "全选在线玩家", "Select All Online"),
    GUI_CANDIDATES_SUMMARY("teamcraft.gui.candidates.summary", "已选择 %1$s 人 • 在线 %2$s 人", "%1$s selected • %2$s online"),
    GUI_CATEGORY_ALL_TEAMS("teamcraft.gui.category.all_teams", "所有队伍信息展示", "All TeamCraft Teams"),
    GUI_CATEGORY_APPEARANCE("teamcraft.gui.category.appearance", "队伍外观", "Team Appearance"),
    GUI_CATEGORY_CANDIDATES("teamcraft.gui.category.candidates", "候选玩家", "Candidate Players"),
    GUI_CATEGORY_CANDIDATES_EXAMPLE(
        "teamcraft.gui.category.candidates.example",
        "先全选在线玩家，再点击增删；拖动已选择的玩家可调整顺序。",
        "Select everyone online, click to add or remove players, and drag selected players to reorder them."
    ),
    GUI_CATEGORY_CANDIDATES_TOOLTIP(
        "teamcraft.gui.category.candidates.tooltip",
        "选择参加下一次分队的玩家；可以逐个选择或全选在线玩家，并可拖动调整顺序。",
        "Choose players for the next split, select them individually or all at once, and drag them to change their order."
    ),
    GUI_CATEGORY_OWN_TEAM("teamcraft.gui.category.own_team", "所属队伍信息配置", "My Team Settings"),
    GUI_CATEGORY_SPLIT("teamcraft.gui.category.split", "分队设置", "Split Settings"),
    GUI_CATEGORY_SPLIT_CONFIG("teamcraft.gui.category.split_config", "队伍分队配置", "Team Split Config"),
    GUI_CLOSE("teamcraft.gui.close", "关闭", "Close"),
    GUI_COLORS_ADD("teamcraft.gui.colors.add", "添加颜色", "Add color"),
    GUI_COLORS_DEFAULT_PALETTE(
        "teamcraft.gui.colors.default_palette",
        "正在使用默认 16 色调色板",
        "Using the default 16-color palette"
    ),
    GUI_COLORS_PICKER("teamcraft.gui.colors.picker", "选择颜色", "Choose color"),
    GUI_COLORS_REMOVE("teamcraft.gui.colors.remove", "移除", "Remove"),
    GUI_COLORS_SLOT("teamcraft.gui.colors.slot", "颜色 #%s", "Color #%s"),
    GUI_COLORS_USE_DEFAULTS("teamcraft.gui.colors.use_defaults", "使用默认调色板", "Use default palette"),
    GUI_DEFAULTS("teamcraft.gui.defaults", "默认值", "Defaults"),
    GUI_DEFAULTS_READY(
        "teamcraft.gui.defaults_ready",
        "已载入默认值；按“应用”或“保存并分队”后才会保存",
        "Default values loaded; press Apply or Save & Split to save them"
    ),
    GUI_DETAILS_BACK("teamcraft.gui.details.back", "返回", "Back"),
    GUI_DETAILS_COLOR("teamcraft.gui.details.color", "队伍颜色", "Team color"),
    GUI_DETAILS_DISBAND("teamcraft.gui.details.disband", "解散队伍", "Disband Team"),
    GUI_DETAILS_DISBANDED("teamcraft.gui.details.disbanded", "队伍已解散", "The team was disbanded"),
    GUI_DETAILS_MEMBER("teamcraft.gui.details.member", "%1$s. %2$s", "%1$s. %2$s"),
    GUI_DETAILS_MEMBERS("teamcraft.gui.details.members", "队伍成员（%s）", "Team members (%s)"),
    GUI_DETAILS_NAME("teamcraft.gui.details.name", "队伍名称", "Team name"),
    GUI_DETAILS_TITLE("teamcraft.gui.details.title", "队伍详情", "Team Details"),
    GUI_DONE("teamcraft.gui.done", "完成", "Done"),
    GUI_ERROR_COLOR("teamcraft.gui.error.color", "无法识别的队伍颜色：%s", "Unknown team color: %s"),
    GUI_ERROR_EMPTY_TEAM_NAME(
        "teamcraft.gui.error.empty_team_name",
        "队伍显示名称不能为空",
        "The team display name cannot be empty"
    ),
    GUI_ERROR_INVALID_SERVER(
        "teamcraft.gui.error.invalid_server",
        "服务器拒绝了这些设置，请检查数值后重试",
        "The server rejected these settings; check the values and try again"
    ),
    GUI_ERROR_NAME_TOO_LONG(
        "teamcraft.gui.error.name_too_long",
        "每个队伍名称最多包含 %s 个字符",
        "Each team name may contain at most %s characters"
    ),
    GUI_ERROR_NO_CANDIDATES(
        "teamcraft.gui.error.no_candidates",
        "尚未选择候选玩家，请先从列表中添加玩家",
        "No candidate players are selected; choose players from the list first"
    ),
    GUI_ERROR_PERMISSION(
        "teamcraft.gui.error.permission",
        "权限不足，无法执行此 TeamCraft 操作",
        "You do not have permission to perform this TeamCraft action"
    ),
    GUI_ERROR_PLAYERS_PER_TEAM(
        "teamcraft.gui.error.players_per_team",
        "每队人数必须是 %1$s 到 %2$s 之间的整数",
        "Players per team must be a whole number from %1$s to %2$s"
    ),
    GUI_ERROR_SERVER_UNSUPPORTED(
        "teamcraft.gui.error.server_unsupported",
        "当前服务器不支持 TeamCraft 配置界面",
        "This server does not support the TeamCraft configuration screen"
    ),
    GUI_ERROR_TEAM_COUNT(
        "teamcraft.gui.error.team_count",
        "队伍数量必须是 %1$s 到 %2$s 之间的整数",
        "Number of teams must be a whole number from %1$s to %2$s"
    ),
    GUI_ERROR_TEAM_NOT_FOUND(
        "teamcraft.gui.error.team_not_found",
        "找不到你所属的 TeamCraft 队伍，请刷新页面",
        "Your TeamCraft-managed team could not be found; refresh the page"
    ),
    GUI_ERROR_TEAMS_EXIST(
        "teamcraft.gui.error.teams_exist",
        "已存在 TeamCraft 队伍，请先清除现有队伍后重试",
        "TeamCraft teams already exist. Please clear existing teams and try again."
    ),
    GUI_ERROR_TOO_MANY_TEAMS(
        "teamcraft.gui.error.too_many_teams",
        "队伍数量不能大于候选玩家数量",
        "The requested team count is greater than the number of candidate players"
    ),
    GUI_ERROR_TOO_MANY_VALUES("teamcraft.gui.error.too_many_values", "最多只能填写 %s 项", "At most %s values may be entered"),
    GUI_HELP_APPEARANCE(
        "teamcraft.gui.help.appearance",
        "3. 按需添加队伍名称和颜色；未添加时会使用自动队名和默认调色板。",
        "3. Add team names and colors as needed; automatic names and the default palette are used when omitted."
    ),
    GUI_HELP_BUILD(
        "teamcraft.gui.help.build",
        "4. 点击“应用”只保存设置；点击“保存并分队”会立即创建队伍。",
        "4. Apply only saves the settings; Save & Split immediately creates the teams."
    ),
    GUI_HELP_CANDIDATES(
        "teamcraft.gui.help.candidates",
        "1. 在“候选玩家”中逐个选择，或使用“全选在线玩家”。",
        "1. Select candidate players individually, or use Select All Online."
    ),
    GUI_HELP_CMD_BUILD(
        "teamcraft.gui.help.command.build",
        "作用：按照当前候选名单和已保存配置创建并分配队伍。\n参数：无；执行前必须有候选玩家，并且不能已有 TeamCraft 队伍。\n示例：/teamcraft build-teams",
        "Purpose: Create and assign teams from the current candidates and saved settings.\nParameters: None; candidates must exist and no TeamCraft teams may already be active.\nExample: /teamcraft build-teams"
    ),
    GUI_HELP_CMD_BUILD_COLORS(
        "teamcraft.gui.help.command.build_colors",
        "作用：使用临时颜色列表立即创建队伍，不修改已保存的颜色配置。\n参数：<colors...> 为按队伍顺序排列的颜色 ID；数量不足时循环使用。\n示例：/teamcraft build-teams colors red blue",
        "Purpose: Create teams immediately with a one-time color list without changing the saved colors.\nParameters: <colors...> are color IDs in team order; a short list repeats.\nExample: /teamcraft build-teams colors red blue"
    ),
    GUI_HELP_CMD_BUILD_NAMES(
        "teamcraft.gui.help.command.build_names",
        "作用：使用临时名称列表立即创建队伍，不修改已保存的名称配置。\n参数：<names...> 为按队伍顺序排列的名称；含空格的名称必须加引号。\n示例：/teamcraft build-teams names \"红队\" \"蓝队\"",
        "Purpose: Create teams immediately with one-time names without changing the saved names.\nParameters: <names...> are names in team order; quote names containing spaces.\nExample: /teamcraft build-teams names \"Red Team\" \"Blue Team\""
    ),
    GUI_HELP_CMD_CLEAR(
        "teamcraft.gui.help.command.clear",
        "作用：解散所有由 TeamCraft 创建的队伍，并释放其中的玩家。\n参数：无；候选名单和分队配置会保留。\n示例：/teamcraft clear",
        "Purpose: Disband every team created by TeamCraft and release its players.\nParameters: None; the candidate list and split settings are kept.\nExample: /teamcraft clear"
    ),
    GUI_HELP_CMD_CONFIG_COLORS(
        "teamcraft.gui.help.command.config_colors",
        "作用：保存下次分队使用的颜色循环。\n参数：<colors...> 为 Minecraft 颜色 ID，例如 red、dark_blue、light_purple；按队伍顺序使用，不足时循环。\n示例：/teamcraft config colors red blue green",
        "Purpose: Save the color cycle used by the next split.\nParameters: <colors...> are Minecraft color IDs such as red, dark_blue and light_purple; they are used in team order and repeat when needed.\nExample: /teamcraft config colors red blue green"
    ),
    GUI_HELP_CMD_CONFIG_COLORS_RESET(
        "teamcraft.gui.help.command.config_colors_reset",
        "作用：清除自定义颜色列表，恢复默认 16 色调色板。\n参数：无。\n示例：/teamcraft config colors reset",
        "Purpose: Clear the custom color list and restore the default 16-color palette.\nParameters: None.\nExample: /teamcraft config colors reset"
    ),
    GUI_HELP_CMD_CONFIG_FRIENDLY_FIRE(
        "teamcraft.gui.help.command.config_friendly_fire",
        "作用：设置以后创建的队伍是否允许队友互相伤害。\n参数：<true|false>；true 为允许，false 为禁止，不会修改现有队伍。\n示例：/teamcraft config friendlyfire false",
        "Purpose: Set whether teammates in subsequently created teams may damage each other.\nParameters: <true|false>; true allows it and false prevents it; existing teams are unchanged.\nExample: /teamcraft config friendlyfire false"
    ),
    GUI_HELP_CMD_CONFIG_MODE(
        "teamcraft.gui.help.command.config_mode",
        "作用：设置候选玩家的分配顺序。\n参数：<fixed|random>；fixed 按候选名单顺序分配，random 会先打乱名单。\n示例：/teamcraft config mode random",
        "Purpose: Set how candidate players are ordered for assignment.\nParameters: <fixed|random>; fixed follows the candidate list, while random shuffles it first.\nExample: /teamcraft config mode random"
    ),
    GUI_HELP_CMD_CONFIG_NAMES(
        "teamcraft.gui.help.command.config_names",
        "作用：保存按队伍顺序使用的显示名称。\n参数：<names...> 为一个或多个名称；含空格的名称必须加引号，名称不足时其余队伍按颜色自动命名。\n示例：/teamcraft config names \"红队\" \"蓝队\"",
        "Purpose: Save display names in team order.\nParameters: <names...> are one or more names; quote names containing spaces, and missing names are generated from team colors.\nExample: /teamcraft config names \"Red Team\" \"Blue Team\""
    ),
    GUI_HELP_CMD_CONFIG_NAMES_RESET(
        "teamcraft.gui.help.command.config_names_reset",
        "作用：清除自定义队伍名称，恢复按颜色自动命名。\n参数：无。\n示例：/teamcraft config names reset",
        "Purpose: Clear custom team names and restore automatic color-based names.\nParameters: None.\nExample: /teamcraft config names reset"
    ),
    GUI_HELP_CMD_CONFIG_PLAYERS_PER_TEAM(
        "teamcraft.gui.help.command.config_players_per_team",
        "作用：切换为“每队人数”规则，由候选人数自动计算队伍数量。\n参数：<players> 为 1–1000 的整数，表示每队最多人数。\n示例：/teamcraft config players-per-team 4",
        "Purpose: Switch to the players-per-team rule, which calculates the team count from the candidates.\nParameters: <players> is an integer from 1 to 1000 and is the maximum size of each team.\nExample: /teamcraft config players-per-team 4"
    ),
    GUI_HELP_CMD_CONFIG_RESET(
        "teamcraft.gui.help.command.config_reset",
        "作用：只恢复分队配置的默认值。\n参数：无；不会清空候选名单，也不会解散现有队伍。\n示例：/teamcraft config reset",
        "Purpose: Restore only the split settings to their defaults.\nParameters: None; candidates are not cleared and existing teams are not disbanded.\nExample: /teamcraft config reset"
    ),
    GUI_HELP_CMD_CONFIG_TEAM_COUNT(
        "teamcraft.gui.help.command.config_team_count",
        "作用：切换为“固定队伍数”规则，并尽量平均分配候选玩家。\n参数：<teams> 为 1–100 的整数；创建时不能大于候选玩家数。\n示例：/teamcraft config team-count 3",
        "Purpose: Switch to a fixed team count and distribute candidates as evenly as possible.\nParameters: <teams> is an integer from 1 to 100 and cannot exceed the candidate count when building.\nExample: /teamcraft config team-count 3"
    ),
    GUI_HELP_CMD_HELP(
        "teamcraft.gui.help.command.help",
        "作用：在聊天栏中显示 TeamCraft 命令概览。\n参数：无；直接执行 /teamcraft 与 /teamcraft help 效果相同。\n示例：/teamcraft help",
        "Purpose: Show the TeamCraft command summary in chat.\nParameters: None; /teamcraft and /teamcraft help are equivalent.\nExample: /teamcraft help"
    ),
    GUI_HELP_CMD_INIT(
        "teamcraft.gui.help.command.init",
        "作用：用指定玩家替换整个候选名单。\n参数：<players> 为一个或多个在线玩家、玩家名或选择器，例如 @a、@p。\n示例：/teamcraft init @a",
        "Purpose: Replace the entire candidate list with the selected players.\nParameters: <players> selects one or more online players by name or selector, such as @a or @p.\nExample: /teamcraft init @a"
    ),
    GUI_HELP_CMD_INIT_ADD(
        "teamcraft.gui.help.command.init_add",
        "作用：把指定玩家追加到候选名单，已有玩家会被跳过。\n参数：<players> 为一个或多个在线玩家、玩家名或选择器。\n示例：/teamcraft init add Steve Alex",
        "Purpose: Add the selected players to the candidate list; duplicates are skipped.\nParameters: <players> selects one or more online players by name or selector.\nExample: /teamcraft init add Steve Alex"
    ),
    GUI_HELP_CMD_INIT_CLEAR(
        "teamcraft.gui.help.command.init_clear",
        "作用：清空候选玩家名单。\n参数：无；不会改变分队配置或已经创建的队伍。\n示例：/teamcraft init clear",
        "Purpose: Empty the candidate player list.\nParameters: None; split settings and already-created teams are unchanged.\nExample: /teamcraft init clear"
    ),
    GUI_HELP_CMD_INIT_LIST(
        "teamcraft.gui.help.command.init_list",
        "作用：在聊天栏中显示当前候选玩家数量和名单。\n参数：无。\n示例：/teamcraft init list",
        "Purpose: Show the current candidate count and names in chat.\nParameters: None.\nExample: /teamcraft init list"
    ),
    GUI_HELP_CMD_INIT_REMOVE(
        "teamcraft.gui.help.command.init_remove",
        "作用：从候选名单中移除指定玩家。\n参数：<players> 为一个或多个在线玩家、玩家名或选择器。\n示例：/teamcraft init remove Steve",
        "Purpose: Remove the selected players from the candidate list.\nParameters: <players> selects one or more online players by name or selector.\nExample: /teamcraft init remove Steve"
    ),
    GUI_HELP_CMD_RESET(
        "teamcraft.gui.help.command.reset",
        "作用：解散全部 TeamCraft 队伍，并清空候选名单、恢复所有默认配置。\n参数：无；这是完整重置。\n示例：/teamcraft reset",
        "Purpose: Disband all TeamCraft teams, clear the candidates and restore every default setting.\nParameters: None; this is a full reset.\nExample: /teamcraft reset"
    ),
    GUI_HELP_CMD_STATUS(
        "teamcraft.gui.help.command.status",
        "作用：查看当前候选玩家、分队规则、模式、友方伤害、颜色、名称和已创建队伍。\n参数：无。\n示例：/teamcraft status",
        "Purpose: Show the candidates, split rule, mode, friendly fire, colors, names and created teams.\nParameters: None.\nExample: /teamcraft status"
    ),
    GUI_HELP_CMD_TEAM_COLOR(
        "teamcraft.gui.help.command.team_color",
        "作用：修改自己所在 TeamCraft 队伍的颜色及玩家名前缀颜色。\n参数：<team> 仅支持自己队伍的当前显示名称或内部 ID（如 teamcraft_1）；<color> 为颜色 ID。\n示例：/teamcraft manage team teamcraft_1 color blue",
        "Purpose: Change your own TeamCraft team's color and player-name prefix color.\nParameters: <team> must be your own team's current display name or internal ID, such as teamcraft_1; <color> is a color ID.\nExample: /teamcraft manage team teamcraft_1 color blue"
    ),
    GUI_HELP_CMD_TEAM_FRIENDLY_FIRE(
        "teamcraft.gui.help.command.team_friendly_fire",
        "作用：立即修改自己所在队伍的友方伤害。\n参数：<team> 仅支持自己队伍的当前显示名称或内部 ID；<true|false> 分别表示允许或禁止队友互伤。\n示例：/teamcraft manage team teamcraft_1 friendlyfire false",
        "Purpose: Immediately change friendly fire for your own team.\nParameters: <team> must be your own team's current display name or internal ID; <true|false> allows or prevents teammate damage.\nExample: /teamcraft manage team teamcraft_1 friendlyfire false"
    ),
    GUI_HELP_CMD_TEAM_INFO(
        "teamcraft.gui.help.command.team_info",
        "作用：查看自己所在队伍的内部 ID、显示名称、颜色、友方伤害和成员。\n参数：<team> 仅支持自己 TeamCraft 队伍的当前显示名称或内部 ID。\n示例：/teamcraft manage team teamcraft_1 info",
        "Purpose: Show your own team's internal ID, display name, color, friendly fire and members.\nParameters: <team> must be your own TeamCraft team's current display name or internal ID.\nExample: /teamcraft manage team teamcraft_1 info"
    ),
    GUI_HELP_CMD_TEAM_NAME(
        "teamcraft.gui.help.command.team_name",
        "作用：修改自己所在队伍的显示名称，并同步更新玩家名前缀。\n参数：<team> 仅支持自己队伍的当前显示名称或内部 ID；<name> 为新名称，含空格时必须加引号。改名后候选名称随之更新，内部 ID 不变。\n示例：/teamcraft manage team teamcraft_1 name \"建筑队\"",
        "Purpose: Change your own team's display name and update its player prefix.\nParameters: <team> must be your own team's current display name or internal ID; <name> is the new name and must be quoted when it contains spaces. Renaming updates name suggestions without changing the internal ID.\nExample: /teamcraft manage team teamcraft_1 name \"Builders\""
    ),
    GUI_HELP_COMMAND_BUILD_TITLE("teamcraft.gui.help.command.build_title", "创建队伍", "Build Teams"),
    GUI_HELP_COMMAND_CANDIDATES_TITLE("teamcraft.gui.help.command.candidates_title", "候选名单命令", "Candidate List Commands"),
    GUI_HELP_COMMAND_CLEANUP_TITLE("teamcraft.gui.help.command.cleanup_title", "清理与重置", "Cleanup and Reset"),
    GUI_HELP_COMMAND_CONFIG_TITLE("teamcraft.gui.help.command.config_title", "分队配置命令", "Split Configuration Commands"),
    GUI_HELP_COMMAND_GENERAL_TITLE("teamcraft.gui.help.command.general_title", "常用信息命令", "General Information Commands"),
    GUI_HELP_COMMAND_INTRO(
        "teamcraft.gui.help.command.intro",
        "下方列出全部命令。<参数> 必填；<A|B> 表示二选一；... 表示可填写多项。",
        "Every command is listed below. <argument> is required, <A|B> means choose one, and ... means multiple values are accepted."
    ),
    GUI_HELP_COMMAND_TEAM_TITLE("teamcraft.gui.help.command.team_title", "现有队伍管理", "Existing Team Management"),
    GUI_HELP_COMMAND_TITLE("teamcraft.gui.help.command_title", "命令帮助", "Command Help"),
    GUI_HELP_MANAGEMENT_TITLE("teamcraft.gui.help.management_title", "队伍管理", "Team Management"),
    GUI_HELP_MANAGE_TEAMS(
        "teamcraft.gui.help.manage_teams",
        "在“所有队伍”中点击任意队伍，可以查看名称、颜色和每位成员。",
        "Click any entry under All Teams to view its name, color, and every member."
    ),
    GUI_HELP_QUICK_START("teamcraft.gui.help.quick_start", "快速开始", "Quick Start"),
    GUI_HELP_REMOVE_TEAMS(
        "teamcraft.gui.help.remove_teams",
        "详情页中的“解散队伍”只删除当前队伍；“解除所有队伍”会删除全部 TeamCraft 队伍。",
        "Disband Team removes only the current team; Remove All Teams deletes every TeamCraft team."
    ),
    GUI_HELP_SPLIT(
        "teamcraft.gui.help.split",
        "2. 选择按每队人数或固定队伍数分队，并设置分配方式与友方伤害。",
        "2. Choose players per team or a fixed team count, then set assignment mode and friendly fire."
    ),
    GUI_LOADING("teamcraft.gui.loading", "正在读取 TeamCraft 配置……", "Loading TeamCraft configuration..."),
    GUI_NAMES_ADD("teamcraft.gui.names.add", "添加名称", "Add Name"),
    GUI_NAMES_DEFAULT("teamcraft.gui.names.default", "正在使用按颜色自动生成的队名", "Using automatic color-based team names"),
    GUI_NAMES_REMOVE("teamcraft.gui.names.remove", "移除", "Remove"),
    GUI_NAMES_SLOT("teamcraft.gui.names.slot", "名称 #%1$s：%2$s", "Name #%1$s: %2$s"),
    GUI_NAV_ALL_TEAMS("teamcraft.gui.nav.all_teams", "所有队伍信息展示", "All Teams"),
    GUI_NAV_ALL_TEAMS_TOOLTIP(
        "teamcraft.gui.nav.all_teams.tooltip",
        "查看当前由 TeamCraft 管理的全部记分板队伍。",
        "Browse every scoreboard team currently managed by TeamCraft."
    ),
    GUI_NAV_HELP("teamcraft.gui.nav.help", "帮助", "Help"),
    GUI_NAV_HELP_TOOLTIP(
        "teamcraft.gui.nav.help.tooltip",
        "查看 TeamCraft GUI 的使用流程和队伍管理说明。",
        "View the TeamCraft GUI workflow and team-management instructions."
    ),
    GUI_NAV_OWN_TEAM("teamcraft.gui.nav.own_team", "所属队伍信息配置", "My Team"),
    GUI_NAV_OWN_TEAM_TOOLTIP(
        "teamcraft.gui.nav.own_team.tooltip",
        "查看并配置你当前所属的 TeamCraft 队伍。",
        "View and edit the managed team you currently belong to."
    ),
    GUI_NAV_TEAM_CONFIG("teamcraft.gui.nav.team_config", "队伍配置", "Team Setup"),
    GUI_NAV_TEAM_CONFIG_TOOLTIP(
        "teamcraft.gui.nav.team_config.tooltip",
        "设置候选玩家的分队方式，然后保存配置或直接创建队伍。",
        "Configure how candidates are split, then save or create the teams."
    ),
    GUI_OPTION_COLORS("teamcraft.gui.option.colors", "队伍颜色", "Team colors"),
    GUI_OPTION_COLORS_EXAMPLE(
        "teamcraft.gui.option.colors.example",
        "示例：4 支队伍填写 red blue，将依次使用红、蓝、红、蓝。",
        "Example: red blue for 4 teams gives red / blue / red / blue."
    ),
    GUI_OPTION_COLORS_TOOLTIP(
        "teamcraft.gui.option.colors.tooltip",
        "按队伍顺序选择颜色。列表为空时使用默认 16 色；颜色不足时会循环使用。",
        "Choose colors in team order. Leave the list empty for the default 16-color palette; a short list repeats."
    ),
    GUI_OPTION_FRIENDLY_FIRE("teamcraft.gui.option.friendly_fire", "友方伤害", "Friendly fire"),
    GUI_OPTION_FRIENDLY_FIRE_EXAMPLE(
        "teamcraft.gui.option.friendly_fire.example",
        "示例：关闭后，队友之间的攻击不会造成伤害。",
        "Example: when off, attacks between teammates do not deal damage."
    ),
    GUI_OPTION_FRIENDLY_FIRE_TOOLTIP(
        "teamcraft.gui.option.friendly_fire.tooltip",
        "控制新创建队伍中的队友能否互相造成伤害。",
        "Controls whether members of the same newly created team can damage each other."
    ),
    GUI_OPTION_MODE("teamcraft.gui.option.mode", "分配方式", "Assignment mode"),
    GUI_OPTION_MODE_EXAMPLE(
        "teamcraft.gui.option.mode.example",
        "示例：A、B、C、D 每队 2 人，按顺序为 A+B / C+D；随机模式每次可能不同。",
        "Example: A, B, C, D in pairs becomes A+B / C+D in ordered mode, while random mode may differ each time."
    ),
    GUI_OPTION_MODE_TOOLTIP(
        "teamcraft.gui.option.mode.tooltip",
        "按顺序会严格依照候选名单分组；随机会先打乱名单再分组。",
        "Ordered mode follows the candidate list; random mode shuffles it before assigning teams."
    ),
    GUI_OPTION_NAMES("teamcraft.gui.option.names", "队伍名称", "Team names"),
    GUI_OPTION_NAMES_EXAMPLE(
        "teamcraft.gui.option.names.example",
        "示例：依次添加“先锋队”和“后援队”；第 3 队会使用颜色自动命名。",
        "Example: add Alpha and Bravo; a third team receives an automatic color-based name."
    ),
    GUI_OPTION_NAMES_TOOLTIP(
        "teamcraft.gui.option.names.tooltip",
        "输入一个显示名称并点击“添加名称”。名称不足时，其余队伍会按颜色自动命名。",
        "Enter one display name and click Add Name. Missing names are generated from team colors."
    ),
    GUI_OPTION_PLAYERS_PER_TEAM("teamcraft.gui.option.players_per_team", "每队人数", "Players per team"),
    GUI_OPTION_PLAYERS_PER_TEAM_EXAMPLE(
        "teamcraft.gui.option.players_per_team.example",
        "示例：10 人、每队最多 4 人，会分成 4 / 4 / 2。",
        "Example: 10 players and a limit of 4 produces teams of 4 / 4 / 2."
    ),
    GUI_OPTION_PLAYERS_PER_TEAM_TOOLTIP(
        "teamcraft.gui.option.players_per_team.tooltip",
        "限制每支队伍最多容纳的人数，所需队伍数量会自动计算。",
        "The maximum number of players in each team. The required number of teams is calculated automatically."
    ),
    GUI_OPTION_RULE("teamcraft.gui.option.rule", "分队规则", "Split rule"),
    GUI_OPTION_RULE_EXAMPLE(
        "teamcraft.gui.option.rule.example",
        "示例：10 人且每队 4 人会分成 3 队；10 人固定 3 队会分成 4 / 3 / 3。",
        "Example: 10 players with 4 per team creates 3 teams; 10 players across 3 teams produces sizes 4 / 3 / 3."
    ),
    GUI_OPTION_RULE_TOOLTIP(
        "teamcraft.gui.option.rule.tooltip",
        "选择按每队人数上限自动计算队伍数，或直接固定队伍数量。",
        "Choose whether the split is calculated from a maximum team size or a fixed number of teams."
    ),
    GUI_OPTION_TEAM_COUNT("teamcraft.gui.option.team_count", "队伍数量", "Number of teams"),
    GUI_OPTION_TEAM_COUNT_EXAMPLE(
        "teamcraft.gui.option.team_count.example",
        "示例：10 人固定分成 3 队，各队人数为 4 / 3 / 3。",
        "Example: 10 players in 3 teams produces team sizes of 4 / 3 / 3."
    ),
    GUI_OPTION_TEAM_COUNT_TOOLTIP(
        "teamcraft.gui.option.team_count.tooltip",
        "固定创建多少支队伍，候选玩家会尽量平均分配。",
        "Creates exactly this many teams and distributes the candidate players as evenly as possible."
    ),
    GUI_OWN_TEAM_COLOR("teamcraft.gui.own_team.color", "队伍颜色", "Team color"),
    GUI_OWN_TEAM_COLOR_EXAMPLE(
        "teamcraft.gui.own_team.color.example",
        "示例：选择蓝色后，队名和 [队伍] 前缀都会显示为蓝色。",
        "Example: choosing blue makes the team name and [Team] prefix blue."
    ),
    GUI_OWN_TEAM_COLOR_TOOLTIP(
        "teamcraft.gui.own_team.color.tooltip",
        "修改队伍在记分板中的颜色和成员名字前缀颜色。",
        "Changes this team's scoreboard color and member-name prefix color."
    ),
    GUI_OWN_TEAM_FRIENDLY_FIRE("teamcraft.gui.own_team.friendly_fire", "友方伤害", "Friendly fire"),
    GUI_OWN_TEAM_FRIENDLY_FIRE_EXAMPLE(
        "teamcraft.gui.own_team.friendly_fire.example",
        "示例：关闭后，你所在队伍的队友之间攻击不会造成伤害。",
        "Example: when off, attacks between your teammates do not deal damage."
    ),
    GUI_OWN_TEAM_FRIENDLY_FIRE_TOOLTIP(
        "teamcraft.gui.own_team.friendly_fire.tooltip",
        "控制当前已存在队伍中的成员能否互相造成伤害。",
        "Controls whether members of your existing team can damage each other."
    ),
    GUI_OWN_TEAM_ID("teamcraft.gui.own_team.id", "内部 ID", "Internal ID"),
    GUI_OWN_TEAM_MEMBERS("teamcraft.gui.own_team.members", "队伍成员", "Members"),
    GUI_OWN_TEAM_NAME("teamcraft.gui.own_team.name", "显示名称", "Display name"),
    GUI_OWN_TEAM_NAME_EXAMPLE(
        "teamcraft.gui.own_team.name.example",
        "示例：改为“建筑队”后，成员名字前会显示 [建筑队]。",
        "Example: changing the name to Builders displays [Builders] before member names."
    ),
    GUI_OWN_TEAM_NAME_TOOLTIP(
        "teamcraft.gui.own_team.name.tooltip",
        "修改队伍显示名称，以及显示在每位成员名字前的队伍前缀。",
        "Changes the display name and the prefix shown before every member's name."
    ),
    GUI_OWN_TEAM_NONE("teamcraft.gui.own_team.none", "你当前不属于 TeamCraft 管理的队伍", "You are not in a TeamCraft-managed team"),
    GUI_OWN_TEAM_NONE_TOOLTIP(
        "teamcraft.gui.own_team.none.tooltip",
        "请先创建队伍，或加入内部 ID 以 teamcraft_ 开头的队伍。",
        "Create the teams first, or join a team whose internal ID starts with teamcraft_."
    ),
    GUI_PLACEHOLDER_COLORS("teamcraft.gui.placeholder.colors", "默认调色板", "Default palette"),
    GUI_PLACEHOLDER_NAMES("teamcraft.gui.placeholder.names", "输入队伍名称", "Enter a team name"),
    GUI_PLACEHOLDER_NUMBER("teamcraft.gui.placeholder.number", "输入数字", "Enter a number"),
    GUI_PLACEHOLDER_TEAM_NAME("teamcraft.gui.placeholder.team_name", "输入队伍名称", "Enter a team name"),
    GUI_REFRESH("teamcraft.gui.refresh", "刷新", "Refresh"),
    GUI_REFRESHED("teamcraft.gui.refreshed", "队伍信息已刷新", "Team information refreshed"),
    GUI_RULE_PLAYERS_PER_TEAM("teamcraft.gui.rule.players_per_team", "每队人数", "Players per team"),
    GUI_RULE_TEAM_COUNT("teamcraft.gui.rule.team_count", "固定队伍数", "Fixed team count"),
    GUI_SAVE_TEAM("teamcraft.gui.save_team", "保存队伍", "Save Team"),
    GUI_SAVED("teamcraft.gui.saved", "TeamCraft 配置已保存", "TeamCraft configuration saved"),
    GUI_SESSION_NOTE(
        "teamcraft.gui.session_note",
        "仅影响本次服务器会话中的下一次分队，不会修改已创建的队伍",
        "Applies to the next split in this server session; existing teams are unchanged"
    ),
    GUI_SPLIT("teamcraft.gui.split", "保存并分队", "Save & Split"),
    GUI_TEAM_SAVED("teamcraft.gui.team_saved", "所属队伍设置已保存", "Your team settings were saved"),
    GUI_TITLE("teamcraft.gui.title", "TeamCraft 配置", "TeamCraft Configuration"),
    HELP_CLEAR("teamcraft.help.clear", "解散已创建队伍，但保留当前配置", "Remove created teams but keep the current setup"),
    HELP_COLORS("teamcraft.help.colors", "设置颜色循环，例如 red blue green", "Set the color cycle, e.g. red blue green"),
    HELP_FRIENDLY_FIRE("teamcraft.help.friendly_fire", "设置是否允许队友之间互相伤害", "Allow or prevent damage between teammates"),
    HELP_INIT("teamcraft.help.init", "替换候选名单（支持 @a 等选择器）", "Replace the candidate list (selectors such as @a are supported)"),
    HELP_INIT_EDIT("teamcraft.help.init_edit", "添加或移除候选玩家", "Add or remove candidate players"),
    HELP_INIT_MANAGE("teamcraft.help.init_manage", "查看或清空候选名单", "Show or clear the candidate list"),
    HELP_MODE("teamcraft.help.mode", "选择按名单顺序或打乱后分配", "Choose ordered or shuffled assignment"),
    HELP_NAMES("teamcraft.help.names", "设置队名；含空格的名称需要引号", "Set team names; quote names containing spaces"),
    HELP_PLAYERS_PER_TEAM(
        "teamcraft.help.players_per_team",
        "限制每队人数，并根据候选人数自动计算队伍数量",
        "Limit the players in each team; the team count is calculated automatically"
    ),
    HELP_RESET("teamcraft.help.reset", "解散队伍并恢复全部默认设置", "Remove teams and restore all defaults"),
    HELP_START("teamcraft.help.start", "创建队伍并分配玩家", "Create and assign the teams"),
    HELP_STATUS("teamcraft.help.status", "查看候选玩家、配置与现有队伍", "Show candidates, configuration and active teams"),
    HELP_TEAM("teamcraft.help.team", "管理自己所在的队伍；仅支持自己的队伍 ID 或当前名称", "Manage your own team using its internal ID or current display name"),
    ERROR_NOT_OWN_TEAM("teamcraft.error.not_own_team", "只能管理自己所在的 TeamCraft 队伍，请使用自己的队伍 ID 或当前名称", "You can only manage your own TeamCraft team; use its internal ID or current display name"),
    HELP_INVITE("teamcraft.help.invite", "邀请在线玩家加入自己的队伍；邀请有效期为 120 秒", "Invite an online player to your team; invitations expire after 120 seconds"),
    HELP_LEAVE_TEAM("teamcraft.help.leaveteam", "退出自己的 TeamCraft 队伍；队伍和其他成员不受影响，候选名单和配置保持不变。退出会取消你发送和收到的待处理邀请。", "Leave your own TeamCraft team without disbanding it or affecting other members, candidates, or configuration. Pending invitations sent or received by you are cancelled."),
    TEAM_LEFT("teamcraft.team.left", "已退出队伍 %s", "You left %s"),
    ERROR_NOT_IN_TEAM("teamcraft.error.not_in_team", "你当前没有加入 TeamCraft 队伍", "You do not currently belong to a TeamCraft team"),
    GUI_LEAVE_TEAM("teamcraft.gui.leave_team", "退出队伍", "Leave Team"),
    GUI_LEAVE_TEAM_TOOLTIP("teamcraft.gui.leave_team.tooltip", "仅退出自己的队伍，不会解散队伍或保存未提交的设置", "Leave only your own team; do not disband it or save pending changes"),
    GUI_TEAM_LEFT("teamcraft.gui.team_left", "已退出队伍，队伍和其他成员保持不变", "You left the team; the team and other members are unchanged"),
    GUI_NAV_INVITATIONS("teamcraft.gui.nav.invitations", "队伍邀请", "Invitations"),
    GUI_NAV_INVITATIONS_TOOLTIP("teamcraft.gui.nav.invitations.tooltip", "邀请在线玩家加入队伍，接受或拒绝收到的邀请", "Invite online players to your team and accept or decline received invitations"),
    GUI_INVITE_RECEIVED("teamcraft.gui.invite.received", "收到的邀请", "Received Invitation"),
    GUI_INVITE_RECEIVED_TOOLTIP("teamcraft.gui.invite.received.tooltip", "接受后加入邀请者的队伍；拒绝不会加入。邀请在 120 秒后失效，队伍被解散或邀请者离队后也会失效。", "Accept to join the inviter's team, or decline to stay unassigned. Invitations expire after 120 seconds and become invalid if the team is disbanded or the inviter leaves."),
    GUI_INVITE_SEND("teamcraft.gui.invite.send", "邀请玩家", "Invite Players"),
    GUI_INVITE_SEND_TOOLTIP("teamcraft.gui.invite.send.tooltip", "点击在线玩家向其发送邀请，只列出尚未加入队伍的玩家。对方接受后才会入队；每人最多有一条待处理的邀请。", "Click an online player to send an invitation. Only players without a team are listed. They join only after accepting; each player can have one pending invitation."),
    GUI_INVITE_NONE("teamcraft.gui.invite.none", "当前没有待处理的邀请", "You have no pending invitation"),
    GUI_INVITE_FROM("teamcraft.gui.invite.from", "邀请者", "Invited by"),
    GUI_INVITE_TEAM("teamcraft.gui.invite.team", "队伍", "Team"),
    GUI_INVITE_REMAINING("teamcraft.gui.invite.remaining", "剩余 %s 秒", "%s seconds remaining"),
    GUI_INVITE_ACCEPT("teamcraft.gui.invite.accept", "接受", "Accept"),
    GUI_INVITE_DECLINE("teamcraft.gui.invite.decline", "拒绝", "Decline"),
    GUI_INVITE_NO_PLAYERS("teamcraft.gui.invite.no_players", "没有可邀请的在线玩家", "No online players are available to invite"),
    GUI_INVITE_PLAYER("teamcraft.gui.invite.player", "邀请 %s", "Invite %s"),
    GUI_INVITE_PLAYER_HINT("teamcraft.gui.invite.player_hint", "邀请 %s 加入你的队伍", "Invite %s to join your team"),
    GUI_INVITE_OFFLINE("teamcraft.gui.invite.offline", "该玩家已离线，请刷新后重试", "That player is offline; refresh and try again"),
    GUI_INVITE_TIMEOUT("teamcraft.gui.invite.timeout", "邀请请求超时，请刷新后重试。操作可能已完成，请先检查队伍或邀请状态。", "The invitation request timed out. Refresh and check your team or invitation before retrying; the action may already have completed."),
    HELP_INVITE_ACCEPT("teamcraft.help.invite_accept", "接受当前邀请并加入队伍", "Accept your pending invitation and join the team"),
    HELP_INVITE_DECLINE("teamcraft.help.invite_decline", "拒绝当前邀请", "Decline your pending invitation"),
    INVITE_SENT("teamcraft.invite.sent", "已邀请 %1$s 加入 %2$s", "Invited %1$s to join %2$s"),
    INVITE_RECEIVED("teamcraft.invite.received", "%1$s 邀请你加入 %2$s。请在 %3$s 秒内使用 /teamcraft invite accept 接受，或使用 /teamcraft invite decline 拒绝。", "%1$s invited you to join %2$s. Use /teamcraft invite accept to accept or /teamcraft invite decline to decline within %3$s seconds."),
    INVITE_ACCEPTED("teamcraft.invite.accepted", "已接受邀请，加入 %s", "Accepted the invitation and joined %s"),
    INVITE_JOINED("teamcraft.invite.joined", "%1$s 接受了邀请，加入 %2$s", "%1$s accepted your invitation and joined %2$s"),
    INVITE_DECLINED("teamcraft.invite.declined", "已拒绝加入 %s 的邀请", "Declined the invitation to join %s"),
    INVITE_REJECTED("teamcraft.invite.rejected", "%s 拒绝了你的邀请", "%s declined your invitation"),
    INVITE_ERROR_NO_TEAM("teamcraft.invite.error.no_team", "请先加入 TeamCraft 队伍，再邀请玩家", "Join a TeamCraft team before inviting players"),
    INVITE_ERROR_SELF("teamcraft.invite.error.self", "不能邀请自己", "You cannot invite yourself"),
    INVITE_ERROR_HAS_TEAM("teamcraft.invite.error.has_team", "%s 已有队伍，请先退出现有队伍后重试", "%s already belongs to a team; leave the existing team before trying again"),
    INVITE_ERROR_PENDING("teamcraft.invite.error.pending", "%s 已有待处理的邀请，请等待其回复或邀请过期", "%s already has a pending invitation; wait for a reply or for it to expire"),
    INVITE_ERROR_NONE("teamcraft.invite.error.none", "没有待处理的邀请，或邀请已过期", "You have no pending invitation, or it has expired"),
    INVITE_ERROR_UNAVAILABLE("teamcraft.invite.error.unavailable", "邀请已失效：队伍已被解散，或邀请者已不在该队伍", "The invitation is no longer valid: the team was disbanded or the inviter left it"),
    HELP_TEAM_COUNT(
        "teamcraft.help.team_count",
        "固定队伍数量，并将候选玩家尽量平均分配",
        "Create exactly this many teams and distribute players as evenly as possible"
    ),
    INIT_ADD("teamcraft.init.add", "已添加 %1$s  • 当前共 %2$s 人", "Added %1$s  • %2$s total"),
    INIT_ADD_DUPLICATED(
        "teamcraft.init.add_skipped",
        "%1$s 已经被添加 • 当前共 %2$s 人 ",
        "%1$s duplicates, skipped"
    ),
    INIT_CLEAR("teamcraft.init.clear", "候选名单已清空", "Candidate list cleared"),
    INIT_LIST_EMPTY(
        "teamcraft.init.list_empty",
        "候选名单为空，请先使用 /teamcraft init <players...> 设置名单",
        "The candidate list is empty. Use /teamcraft init <players...> first."
    ),
    INIT_REMOVE("teamcraft.init.remove", "已移除 %1$s • 剩余 %2$s 人", "Removed %1$s • %2$s remaining"),
    INIT_REMOVE_MISSING(
        "teamcraft.init.remove_missing",
        "%1$s 不在名单中 • 剩余 %2$s 人。",
        "%1$s was not listed • %2$s remaining."
    ),
    INIT_SET("teamcraft.init.set", "候选名单已设置：%1$s 人 • %2$s", "Candidate list set: %1$s players • %2$s"),
    MODE_FIXED("teamcraft.mode.fixed", "按顺序", "Ordered"),
    MODE_RANDOM("teamcraft.mode.random", "随机", "Random"),
    RESET_DONE("teamcraft.reset.done", "重置完成：已解散 %s 支队伍并恢复全部默认设置", "Reset complete: removed %s teams and restored all defaults"),
    RESULT_ASSIGNED("teamcraft.result.assigned", "你已被分配到%s", "You were assigned to %s"),
    RESULT_SUMMARY("teamcraft.result.summary", "分队概览", "Summary"),
    RESULT_SUMMARY_VALUE("teamcraft.result.summary_value", "%1$s 队 • %2$s 人", "%1$s teams • %2$s players"),
    RESULT_TEAM("teamcraft.result.team", "#%1$s  %2$s  • %3$s 人\n       %4$s", "#%1$s  %2$s  • %3$s players\n       %4$s"),
    STATUS_CANDIDATES("teamcraft.status.candidates", "候选玩家", "Candidates"),
    STATUS_CANDIDATES_VALUE("teamcraft.status.candidates_value", "%1$s 人 • %2$s", "%1$s players • %2$s"),
    STATUS_COLORS("teamcraft.status.colors", "颜色循环", "Color cycle"),
    STATUS_CREATED_COUNT("teamcraft.status.created_count", "%s 队", "%s teams"),
    STATUS_CREATED_TEAM("teamcraft.status.created_team", "%1$s  %2$s  • %3$s 人", "%1$s  %2$s  • %3$s players"),
    STATUS_CREATED_TEAMS("teamcraft.status.created_teams", "现有队伍", "Active teams"),
    STATUS_FRIENDLY_FIRE("teamcraft.status.friendly_fire", "友方伤害", "Friendly fire"),
    STATUS_MODE("teamcraft.status.mode", "分队方式", "Assignment mode"),
    STATUS_NAMES("teamcraft.status.names", "队名方案", "Team names"),
    STATUS_NAMES_AUTO("teamcraft.status.names_auto", "自动 • 按颜色命名（如红队、蓝队）", "Automatic • based on colors (e.g. Red Team, Blue Team)"),
    STATUS_RULE_PLAYERS_PER_TEAM(
        "teamcraft.status.rule_players_per_team",
        "限制每队人数 • 最多 %s 人/队 • 自动计算队伍数量",
        "Players per team • up to %s players • team count calculated automatically"
    ),
    STATUS_RULE_TEAM_COUNT(
        "teamcraft.status.rule_team_count",
        "固定队伍数量 • %s 队 • 玩家尽量均摊",
        "Fixed team count • %s teams • players distributed evenly"
    ),
    STATUS_SPLIT_RULE("teamcraft.status.split_rule", "分队规则", "Split rule"),
    TEAM_AUTO_NAME("teamcraft.team.auto_name", "%s队", "%s Team"),
    TEAM_COLOR("teamcraft.team.color", "已将%1$s改为%2$s", "Changed %1$s to %2$s"),
    TEAM_FRIENDLY_FIRE("teamcraft.team.friendly_fire", "%1$s的友方伤害：%2$s", "%1$s friendly fire: %2$s"),
    TEAM_INFO_COLOR("teamcraft.team.info.color", "颜色", "Color"),
    TEAM_INFO_FRIENDLY_FIRE("teamcraft.team.info.friendly_fire", "友方伤害", "Friendly fire"),
    TEAM_INFO_ID("teamcraft.team.info.id", "内部 ID", "Internal ID"),
    TEAM_INFO_MEMBERS("teamcraft.team.info.members", "成员", "Members"),
    TEAM_INFO_MEMBERS_VALUE("teamcraft.team.info.members_value", "%1$s 人 • %2$s", "%1$s players • %2$s"),
    TEAM_INFO_NAME("teamcraft.team.info.name", "显示名称", "Display name"),
    TEAM_NAME("teamcraft.team.name", "已将 %1$s 重命名为%2$s；玩家名前缀已同步更新", "Renamed %1$s to %2$s; its player prefix was updated"),
    TITLE_CANDIDATES("teamcraft.title.candidates", "候选名单", "Candidate List"),
    TITLE_HELP("teamcraft.title.help", "命令指南", "Command Guide"),
    TITLE_SPLIT_RESULT("teamcraft.title.split_result", "分队完成", "Split Complete"),
    TITLE_STATUS("teamcraft.title.status", "当前状态", "Current Status"),
    TITLE_TEAM_INFO("teamcraft.title.team_info", "队伍详情", "Team Details"),
    PERMISSION_GET("teamcraft.permission.get", "当前 %1$s 权限：%2$s", "The current %1$s permission is %2$s"),
    PERMISSION_SET("teamcraft.permission.set", "已将 %1$s 权限设置为 %2$s", "Set the %1$s permission to %2$s")
    ;

    private final String key;
    private final String zhCnTranslation;
    private final String enUsTranslation;

    TeamcraftTranslations(String key, String zhCnTranslation, String enUsTranslation) {
        this.key = key;
        this.zhCnTranslation = zhCnTranslation;
        this.enUsTranslation = enUsTranslation;
    }

    public String resolve(String suffix) {
        return this.key + "." + suffix;
    }

    public String key() {
        return this.key;
    }

    public String zhCnTranslation() {
        return this.zhCnTranslation;
    }

    public String enUsTranslation() {
        return this.enUsTranslation;
    }
}
