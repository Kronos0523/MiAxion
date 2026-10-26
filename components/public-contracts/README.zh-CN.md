# MiAXion 公共接口与协议：独立源码候选

这是从固定源码快照整理的首批候选，**已通过 Linux 独立构建和协议测试，尚未公开发布**。Java 协议 336/336、C# 协议 265/265；示例游戏实测与 Windows 独立构建未验证。不是完整客户端或基础 Bridge 源码，不含用户数据、模型、语音和知识正文，也不包含开发 Git 历史。

| 组件 | 本候选发行身份 | 合同／目标 |
|---|---|---|
| 聚合 Bridge API | dev.miaxion.public:bridge-extension-api:0.1.0 | 只读、动作、配方／术语和平台注册类型；Java 17 字节码 |
| 动作 API 子制品 | miaxion-bridge-action-api-1.2.0.jar | 既有动作 API 1.2.0；不是安装 Mod |
| 配方 API 子制品 | miaxion-bridge-recipe-api-0.1.9.jar | 既有配方 API 0.1.9；不是安装 Mod |
| Java 协议 | dev.miaxion.public:protocol-java:0.1.0 | 协议信封 1.0；Java 21 |
| C# 协议 | MiAxion.Protocol 0.1.0 | .NET 10，无外部运行包引用 |
| 只读示例 | MiAXion Standing Block Example 0.1.0 | MC 1.21.1 / NeoForge 21.1.238 / Java 21 |

公共发行候选序号不等于原产品版本，也不改变动作／配方 API 版本。新坐标采用独立 group，避免用同一原产品坐标替换不同产物；尚未上传 Maven/NuGet，以下用本地 JAR。

- [独立构建与测试](docs/BUILD.zh-CN.md)
- [接入与运行所有者](docs/INTEGRATION.zh-CN.md)
- [协议导航](docs/PROTOCOL.zh-CN.md)
- [许可与依赖](THIRD-PARTY-NOTICES.md)
- [实际结果和待办](docs/VALIDATION.zh-CN.md)
- [公开版接入 skill](skills/bridge-mod-integration/SKILL.md)

自有选定代码和文档按 MIT，组件目录附 LICENSE；第三方条款单列。源文件映射见 docs/source-map.json。内容仅供评审和独立构建，不表示示例已完成游戏内注册或查询验收。
