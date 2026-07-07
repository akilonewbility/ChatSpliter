# ChatSpliter — AI Agent Instructions

Minecraft 1.21.8 Fabric 客户端模组，通过关键词筛选将原版聊天窗口分离为多个独立可自定义的 HUD。

## 构建与运行

```bash
./gradlew build          # 编译并打包 → build/libs/chatspliter-1.0.0.jar
./gradlew runClient      # 在开发环境中启动 Minecraft（需要 Fabric 开发环境）
```

- Gradle 8.8 + Fabric Loom `1.7-SNAPSHOT`
- Java 21 (`-release 21`)，JVM 参数 `-Xmx2G`
- 零外部依赖 — 纯 Mixin，无需 Fabric API

## 架构

```
ChatSpliterMod (入口) ──→ ChatSpliterConfig (JSON 配置单例)
                      ──→ ChatHudManager (HUD 生命周期管理单例)
                            └── FilteredChatHud[] (每个过滤器组一个独立窗口)
```

| 层 | 包 | 职责 |
|---|---|---|
| 入口 | `ChatSpliterMod` | 注册按键绑定 (K)、初始化配置和 HUD、tick 处理 |
| 配置 | `config/` | `ChatSpliterConfig` (Gson 序列化单例)、`FilterGroup` (POJO)、`MatchMode` (ANY/ALL/REGEX) |
| Mixin | `mixin/` | 5 个客户端 Mixin：`ChatHud`、`InGameHud`、`MinecraftClient`、`Mouse`、`GameOptions` |
| HUD | `hud/` | `ChatHudManager` (消息路由/渲染)、`FilteredChatHud` (独立窗口渲染/交互/拖拽) |
| UI | `screen/` | `ConfigScreen` (主设置)、`GroupConfigScreen` (单组设置)、`GlobalConfigScreen`、`DebugScreen` |

## 关键约定

- **配置路径**：`<minecraft>/config/chatspliter.json`，使用 Gson `@SerializedName` 注解
- **线程安全**：配置中的 `filterGroups` 使用 `CopyOnWriteArrayList`
- **工作副本模式**：编辑配置时操作副本 (`FilterGroup.copy()`)，仅在保存时提交
- **翻译键**：所有 UI 文本通过 `chatspliter.*` 前缀的翻译键实现，见 `assets/chatspliter/lang/`
- **Mixin 目标**：仅客户端类，`compatibilityLevel: JAVA_21`，`required: true`
- **消息路由**：`ChatHud.addMessage` 被 Mixin 拦截 → `ChatHudManager.onChatMessage()` 分发给所有 HUD → 各 HUD 自行调用 `FilterGroup.matches()` 判断

## 筛选逻辑

关键词以 `not/` 或 `!/` 前缀为排除项（先检查排除项）。三种匹配模式：
- `ANY`：任意关键词匹配 → 通过
- `ALL`：全部关键词匹配 → 通过
- `REGEX`：正则匹配包含关键词 → 通过
- 仅有排除项（无包含关键词）时匹配所有消息

详见 [README.md](README.md) 获取完整文档。

## 注意事项

- 不要引入 Fabric API 依赖 — 此模组设计为零依赖
- 修改 Mixin 时保持 `chatspliter.mixins.json` 中的注入列表同步
- 新增翻译键需同时在 `en_us.json` 和 `zh_cn.json` 中添加
- HUD 渲染涉及缩放矩阵变换，修改 `FilteredChatHud.render()` 时注意 push/pop 边界
- `widget/` 目录当前为空，为预留的 UI 组件目录
