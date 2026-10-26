# Bridge 与附属开发入口

首批 SDK、协议和 NeoForge 只读示例已整合到 [独立组件目录](../components/public-contracts/README.zh-CN.md)，仍为本地候选，尚未推送或正式发布。自有选定组件采用 MIT，第三方材料单列；完整客户端和基础 Bridge 实现不在此次源码范围内。

## 编码 Agent 使用

入口：[bridge-mod-integration](../skills/bridge-mod-integration/SKILL.md)。包含独立附属开发、基础 Bridge 平台适配两条路线，以及公共合同和交付规则。不要求访问原开发仓库或安装 Graphify。

支持 skill 的工具可读取该目录；调用方式按各工具实际支持为准。无自动发现功能时，直接让 Agent 阅读上述 SKILL.md 及其相对引用。推荐使用本仓库完整 checkout。单独复制时必须同时保留 skill 的 references、agents、所引用的 docs 和 components/public-contracts，并保持相对目录布局；只复制 SKILL.md 或 skill 加本页会缺少源码与构建入口。

## 公开材料状态

| 材料 | 当前状态 | 外部作者现在能做什么 |
|---|---|---|
| 接入 skill 与工作流程 | 已接入真实源码路径，尚未推送 | 按实际接口准备附属，检查验证范围 |
| 聚合／动作／配方 SDK | [源码与构建](../components/public-contracts/docs/BUILD.zh-CN.md) 已整合 | 构建本地 JAR；版本分别为 0.1.0／1.2.0／0.1.9，不安装到 mods |
| Bridge 公共层与平台源码 | 尚未导出 | 新平台移植仍需另行提供已授权宿主源码 |
| 通信协议 schema 与实现 | [协议导航](../components/public-contracts/docs/PROTOCOL.zh-CN.md) 已整合 | 阅读真实合同，构建 Java／C# 协议库 |
| 独立附属示例 | [NeoForge 1.21.1 只读示例](../components/public-contracts/examples/neoforge-1.21.1-readonly/) 构建通过 | 可查看真实入口；游戏内注册与查询仍待验 |
| 依赖与许可证 | [组件声明](../components/public-contracts/THIRD-PARTY-NOTICES.md) 已整合 | 区分运行、编译、测试依赖及工具链材料 |

每项落地时更新真实路径、适用版本和验证范围，再将其标为可用。不要通过创建空 SDK／示例目录制造已公开的印象。

候选 `97290d36` 的交付记录与测试报告显示 Linux 独立构建、SDK／协议独立消费者及示例构建通过；Java 协议 336/336、C# 协议 265/265。Windows 独立构建和示例游戏实测未执行。接收审阅见 [整合记录](SOURCE-INTEGRATION.zh-CN.md)，原构建细节见 [验证记录](../components/public-contracts/docs/VALIDATION.zh-CN.md)。

前期 [导出初审](EXPORT-REVIEW.zh-CN.md)、[第三方核对](THIRD-PARTY-REVIEW.zh-CN.md) 和 [独立构建方案](EXPORT-BUILD-PLAN.zh-CN.md) 保留为历史准备记录；当前源码与验证状态以本页及组件目录为准。

## 开放范围

见 [组件开放方案](OPEN-SOURCE-SCOPE.zh-CN.md)。公开 skill 和公开 API、协议、示例放在同一仓库，随接口变化一起更新。原开发仓库的内部完整版保留，不把内部维护材料整套同步过来。
