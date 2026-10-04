# GUI 绘制：各个类是干什么的？

这套 GUI 不依赖 MaLiLib。参考了它的“Screen + 内容列表 + 行组件”分工，以及 QuickCraft 的可折叠分组方式；保留 TeamCraft 自己的样式和 Minecraft 原生控件。

## 先从这里读

先看具体页面的 `build()`，例如 `TeamcraftTeamConfigTab`。它描述“放哪些内容”，不用计算像素坐标。
再看 `TeamcraftTab` 的 `init()` 和绘制方法，它们连接布局、内容列表和弹层。
需要修改具体样式时，再进入 `widget` 或 `render`。

| 类/目录                                              | 负责什么                                                          | 不负责什么               |
|------------------------------------------------------|-------------------------------------------------------------------|--------------------------|
| `TeamcraftTeamConfigTab` 等具体页面                  | 放哪些标题、分组和控件；点击后编辑哪个草稿字段                    | 滚动、裁剪、统一颜色     |
| `TeamcraftTab`                                       | Minecraft Screen 生命周期；标签栏、底部按钮；分发输入和绘制顺序   | 内容行的坐标和拖拽实现   |
| `TeamcraftPageContext`                               | 共享配置草稿、队伍快照、等待服务器状态、页面切换                  | 画按钮或文本             |
| `TeamcraftDialogScreen`                              | 提示框基类：父页面遮罩、矩形边框、文字换行和滚动、按钮与 Esc 返回 | 判断分组成功或检查权限   |
| `TeamcraftFeedbackScreen` / `PermissionDeniedScreen` | 选择结果文字和颜色；复用提示框基类                                | 重复实现布局与返回逻辑   |
| `layout/TeamcraftPageLayout`                         | 页面边距、标签换行后的内容区域、底部按钮位置                      | Minecraft 数据和绘制调用 |
| `layout/TeamcraftBounds`                             | 矩形的宽高和鼠标命中；右/下边界不包含在内                         | 任何业务逻辑             |
| `layout/TeamcraftScrollViewport`                     | 滚动范围、滚动距离、内容坐标转屏幕坐标                            | 文字、颜色和控件         |
| `widget/TeamcraftContentList`                        | 行高、缩进、分组展开状态、控件定位、可见区域裁剪、候选玩家拖拽    | 配置保存和网络请求       |
| `widget/TeamcraftButton`                             | 灰色立体按钮、纯文本折叠按钮、下面的小字                          | 决定点击后做什么         |
| `widget/TeamcraftCycleButton`                        | 点击或滚轮切换选项                                                | 保存服务器配置           |
| `widget/TeamcraftEditBox`                            | 输入框外观；编辑和焦点仍由原生 EditBox 处理                       | 数值校验和保存           |
| `widget/TeamcraftColorPopup`                         | 颜色弹层的位置、命中、选择和绘制                                  | 队伍改名、配置草稿       |
| `render/TeamcraftGuiTheme`                           | 公共配色、背景、边框、标题、按钮、滚动条和小字绘制                | 行布局、输入和状态       |
| `render/TeamcraftTooltip`                            | 悬停延时、紫色说明框和屏幕内定位                                  | 说明文字的业务内容       |
| `render/TeamcraftGuiLayers`                          | 颜色弹层、说明框、提示对话框的跨版本绘制层级与批次隔离            | 弹层内容与坐标           |

## 常见改动应该在哪里改？

- **加一个配置项**：在对应页面 `build()` 中调用 `addLabeledWidget(...)`；编辑回调写入草稿。
- **加一个标签页**：继承 `TeamcraftTab`，实现 `build()`，再注册。见 [新增页面说明](gui-pages.md)。
- **改背景、边框或按钮颜色**：改 `TeamcraftGuiTheme`。
- **改按钮文字位置或折叠按钮的小字**：改 `TeamcraftButton`。
- **改页面边距、标签栏与底部区域**：改 `TeamcraftPageLayout`。
- **改行高、缩进、输入框列宽**：改 `TeamcraftContentList`。
- **改滚动或坐标换算**：改 `TeamcraftScrollViewport`；它没有游戏依赖，可以单独测试。
- **改颜色选择弹层**：改 `TeamcraftColorPopup`。
- **改悬停延时或说明框**：改 `TeamcraftTooltip`。
- **改按钮的简短用途说明**：用 `TeamcraftButton.hoverHint(...)`，不要把整行的功能和示例复制给按钮。
- **改保存、刷新或权限响应**：看 `TeamcraftConfigActions`、`TeamcraftConfigResponseHandler`，不要放进绘制类。
- **改返回提示框样式**：改 `TeamcraftDialogScreen`；普通结果用 `TeamcraftFeedbackScreen`，专用提示框继承基类。见 [提示框扩展说明](gui-pages.md#result-dialogs)。

## 从 build() 到屏幕上出现内容

`init()` 先计算外围布局和标签位置，再让内容列表开始构建。
页面的 `build()` 通过辅助方法添加行。内容列表记录行的本地坐标和控件，不直接保存业务数据。
构建完成后计算内容高度，将滚动距离限制到有效范围，并定位原生控件。

绘制顺序是：

1. 背景与标题。
2. 在内容区域内裁剪绘制标题、行和原生控件，然后画滚动条。
3. 固定的标签栏与底部按钮。
4. 颜色选择弹层。
5. 悬停说明框。

只有内容区域滚动；标签栏、底部按钮不滚动。输入控件同时注册到 Screen，所以原生焦点和键盘行为仍可用。

## 为什么有“本地坐标”和“屏幕坐标”？

本地坐标是该行距内容起点的距离。滚动只改变映射，不改变行本身的位置：

`屏幕 Y = 内容区域顶部 + 行的本地 Y - 滚动距离`

统一由 `TeamcraftScrollViewport.toScreenY()` 换算，避免绘制、点击和拖拽各算一套。
裁剪区外的原生控件隐藏且不可点击；部分可见的文本行由裁剪区域截断。

## 折叠、草稿和临时状态

- 折叠状态保存在每个页面的内容列表中，使用稳定 ID。队伍改名不影响展开状态；Help 的父项收起后，子项状态仍保留。
- `begin()` 重建行，但不清空展开状态、滚动距离或正在进行的候选拖拽。
- 草稿保存在 `TeamcraftPageContext` 或具体页面字段中，不放在控件对象中，因此重建不会丢失它。
- 离开页面或收到新的服务器候选快照时，清理拖拽、颜色弹层和热键捕获，避免残留输入状态。
- 等待服务器时禁用标签切换与修改控件；收到响应后恢复。绘制类不绕过服务器权限检查。
- 服务器响应先更新草稿与队伍快照，再显示结果框。分组成功后确认返回 All Teams，校验失败或权限不足返回原标签页；关闭提示框不退出整个配置界面。
- 颜色弹层、悬停说明框和提示对话框统一通过 `TeamcraftGuiLayers.draw(...)` 绘制：1.21～1.21.5 的旧版 GuiGraphics 要先 `flush()` 底层页面，再用独立 Z 层绘制弹层，最后刷新并还原矩阵；1.21.8 及以上用 `nextStratum()`。仅调整 Java 调用顺序会让旧版的底层文字批次覆盖弹层。新增弹层时也应使用这个公共入口。

## 独立验证布局

使用项目的 JDK 25：

```powershell
.\scripts\verify-gui-layout.ps1 -JavaHome '你的 JDK 25 路径'
```

测试不启动 Minecraft，检查小窗口、标签换行、矩形命中、滚动边界、折叠后的滚动修正和坐标换算。
游戏内仍需检查实际按钮点击、拖拽、输入框、提示框和颜色选择的视觉效果。

## 参考源码

- [MaLiLib GuiListBase](https://github.com/sakura-ryoko/malilib/blob/26.3/src/main/java/fi/dy/masa/malilib/gui/GuiListBase.java)：页面把列表生命周期、输入和绘制交给列表组件。
- [MaLiLib WidgetListBase](https://github.com/sakura-ryoko/malilib/blob/26.3/src/main/java/fi/dy/masa/malilib/gui/widgets/WidgetListBase.java)：列表维护行组件和滚动。
- [QuickCraft 配置页](https://github.com/qk-yiyihehe/quickcraft/blob/main/tracks/26x/src/main/java/com/yiyihehe/quickcraft/gui/QuickCraftConfigScreen.java)：分组头、子项缩进和展开状态。

这里只借鉴职责划分和交互方式，没有复制整个库，也没有添加 MaLiLib 依赖。
