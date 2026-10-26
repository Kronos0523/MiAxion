---
name: bridge-mod-integration
description: 为 MiAxion 开发独立 Minecraft 附属或适配新的 Minecraft／加载器平台，核对公开 Bridge API、通信协议、注册生命周期、构建与交付。用于编码 Agent 的接入任务，不用于安装现成 JAR、修改客户端内部 AI 实现或开发外部 MCP 服务。
---

# MiAxion Bridge 接入与扩展

根据用户指定的平台和功能，在公开接口边界内完成接入。这个 skill 是编码指导，不是游戏插件，也不让任意 Mod 自动兼容。

## 先确认可用材料

阅读 [公开材料状态](../../docs/BRIDGE-DEVELOPMENT.zh-CN.md) 和实际工作仓库规则。当前 [独立组件源码](../../components/public-contracts/README.zh-CN.md) 包含 SDK、协议与 NeoForge 1.21.1 只读示例；按其 [构建说明](../../components/public-contracts/docs/BUILD.zh-CN.md) 获取本地制品，不能假设已经上传 Maven／NuGet。**基础 Bridge 实现尚未包含**，新平台移植仍需已授权的宿主源码。仅以 checkout 中真实类型、入口和验证结果为依据，不猜测方法签名，不把构建通过当作游戏实测。

公开目录应支持独立使用；不要求访问私有开发仓库、内部交接、Graphify 或作者的电脑。用户另有已授权源码或 SDK 时，先核对其版本和来源，再判断是否足够实施。

## 选择路线

- **开发独立附属**：只读查询、原生动作、配方／用途、术语捕获，阅读 [附属接入](references/addon-integration.md)。接口足够时只改附属，不为其另建配对或传输服务。
- **移植基础 Bridge**：新增 Minecraft 版本或加载器，阅读 [平台适配](references/platform-porting.md)。相同接口名不代表原生 API、映射或生命周期相同。
- 两条路线都使用 [公共合同与交付检查](references/contracts-and-delivery.md)，只核对本次相关项目。

从用户提供的实例元数据或 Mod 描述取得 Minecraft、加载器、Java、目标 Mod 版本，以及基础 Bridge／API 版本。仅询问实际缺失且会影响实现的信息。先明确请求是只读、动作还是提供器接入，不因相近能力存在而扩大功能。

## 接入边界

基础 Bridge 负责平台集成、连接、配对和能力派发；特定 Mod 的 API 适配放在独立附属。客户端核心、模型编排、记忆和检索不是本 skill 的修改范围。缺少公开宿主能力时报告缺口与所需合同变化，不绕过现有权限或向严格协议塞入未声明字段。

SDK 只作编译依赖，基础 Bridge 提供运行时 API 类。SDK JAR、链接验证样例和 TestOnly 夹具不是可安装的功能 Mod。新附属需要自己的真实加载器入口、元数据、原生实现和成品检查。

用户提供实例目录用于识别版本，不等于允许安装 JAR、改世界或启动游戏。按实际请求交付源码、构建产物和手动安装说明；保留旧包，实际部署遵循用户授权。

## 示例请求

> 使用 $bridge-mod-integration，为 Minecraft 1.20.1 Fabric 的指定模组制作只读机器状态附属。先核对我提供的模组版本、公开 SDK 和宿主注册入口；只读取公开 API 可提供的状态，不加载新区块或修改世界。材料足够时构建并交付源码、JAR 与手动验收步骤；缺少 SDK 或接口时列出具体缺口。

交付时区分源码审查、构建、成品检查与实机运行，不重复无关平台或已接受的测试。公开接口更新后，同步更新本 skill 使用的合同说明与示例。
