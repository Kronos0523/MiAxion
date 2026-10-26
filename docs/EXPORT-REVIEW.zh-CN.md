# SDK、协议与示例导出初审

审查日期：2026-10-26。源码基线：`3aa5af2b6bd7a4500766e4919d01632c1c2c08f8`。本轮只读审查提交树、构建文件、锁文件、缓存 POM 与 JAR 声明；没有导出产品源码、执行产品构建、改动开发工作树或公开发布。

结论：**SDK 与协议可作为首批导出候选，但尚不能标为可发布；现有示例需要整理改写。** 用户已于 2026-10-26 选择自有 SDK、协议、示例和公开 skill 使用 MIT。当前主要缺口是最小独立构建、第三方许可清单与正式示例。

## 候选范围

下面的数量是审查文件数，不是已批准公开的文件数。逐文件清单记录了源路径、Git blob、大小、SHA-256 与处置建议，保存在本地审查资料中；摘要用于固定审查对象，不代替权属或敏感信息审查。

| 组件 | 源路径 | 文件数 | 处置建议 |
|---|---|---:|---|
| Java 附属 SDK | `src/mod/bridge-extension-api/` | 55 | 保留公共类型、三个 API 边界及链接验证；重组独立构建 |
| 协议合同与测试材料 | `protocol/` | 163 | schema、运行限额优先；测试语料按消费者核对，不整目录默认放行 |
| Java 协议实现 | `src/mod/protocol-java/` | 142 | 与必要 schema／限额／测试语料一起整理 |
| C# 协议实现 | `src/dotnet/MiAxion.Protocol/` | 38 | 可独立列为候选，不因此导出客户端核心 |
| C# 协议测试 | `tests/dotnet/MiAxion.Protocol.Tests/` | 23 | 另审测试依赖与许可，不把无运行时依赖套用于测试项目 |
| 只读游戏示例 | `src/mod/test-fixtures/bridge-extension-example/` | 5 | TestOnly，需改写和重新核对元数据后才能作为正式示例 |
| 构建、文档与生成器 | 根构建规则、wrapper、声明等 | 17 | 仅提取必要部分，不整套导出内部构建系统 |

本轮没有覆盖所有平台 Bridge 实现或全部生产附属的完整源码依赖闭包；它们属于后续审查范围。

## 一、SDK：接口边界较清楚，构建边界尚未独立

公共 Java 类型共 50 个源文件，位于 `extension/api`、`action/api`、`recipe/api` 及 Fabric／Forge／NeoForge 入口包。静态 import 检查发现 SDK 内部引用均落在该模块的公共类型中，第三方引用主要为 Jackson；未发现通过 import 引用客户端核心的情形。该检查不等于对反射、资源和全部运行时行为的证明。

实际任务和身份：

- `actionApiJar`：SDK **1.2.0**，只打包动作接口与 NeoForge 动作兼容入口。
- `recipeApiJar`：SDK **0.1.9**，独立配方／术语及平台注册接口。不能沿用旧文档中的 0.1.7 作为当前身份。
- `jar`：被现有构建目录登记为 `sdk-readonly`，但默认打包 main 输出，实际包含整个聚合 API，不是只读接口的独立最小包。它继承根产品版本，尚无单独只读 SDK 发行身份。
- 生产 API 编译明确使用 Java 17；根构建工具链与动作链接运行检查仍使用 Java 21。文档应分别说明 SDK 字节码和构建／验证 JDK。

`RecipeQuery` 等公开类型暴露 `JsonNode`；SDK 使用 Jackson `compileOnly`，基础 Bridge 是运行时类与兼容依赖的提供者。附属不应重复打包 SDK 或另带冲突的 Jackson。当前锁定的 Jackson 2 核心／databind 为 2.22.1，annotations 为 2.22。

**导出前改造**：建立只包含所选 SDK 与协议模块的构建根，提取明确的版本、仓库、锁定和校验规则。原 `src/mod/settings.gradle` 引入 Bridge、多个生产附属和测试夹具；根构建还包含完整 Stage B 项目集合检查，不能原封不动复制后删除其他模块。

保留实际 `verifyActionApiArtifact`、`verifyActionApiLink`、`verifyRecipeApiArtifact` 的有用检查。只读聚合 API 需要自己的独立消费验证，不能因另一种 SDK 的链接检查通过就视为已覆盖。

## 二、协议：合同、实现和资源一起审

`protocol/schema/` 有 12 个 schema。此次解析并检查 `$ref` 的文档目标，未发现未解析的 schema 文档引用；没有验证所有 JSON Pointer、所有语义或实际收发链路。

Java 协议模块会将 `protocol/runtime-limits.json` 与 schema 复制到 JAR 中 `dev/miaxion/protocol/` 下。`SchemaRegistry` 使用固定逻辑 URI 注册本地资源，并关闭远程资源获取；这些 URI 不能被当成已经上线的下载服务。只复制 Java 类、漏掉资源，会破坏运行时闭包。

Java 测试通过相对目录读取 `protocol/fixtures`，其中 raw 边界语料有 40 个文件。不得为了缩小导出包直接删掉测试所需语料。可选择保留经核对的生成语料，或一起提供经审查的确定性生成器和生成步骤，并重新验证测试路径。

C# 协议项目面向 .NET 10，项目内没有 `PackageReference` 或其他 `ProjectReference`，锁文件依赖为空；它嵌入 discovery v1/v2 schema 和运行限额。源码 import 限于 System 与协议自身命名空间，可作为独立合同实现候选。仍需提取实际必要的根构建属性，不能无审查搬入包含全客户端依赖的中央版本文件。

C# 测试另依赖 xunit.v3 与 JsonSchema.Net，需要独立处理其锁文件、版本及许可材料。现有客户端交付资料曾对 JSON 系列包声明差异作过提示，本次不能把旧根声明中的 MIT 一栏当成这些测试包已核准的结论。

协议说明 `docs/current/protocol.md` 混有内部交接链接与历史演进段落。公开版应按实际 schema、版本协商、配对鉴权、请求取消和错误回执重新组织；保留外部接入所需的安全合同，不发布真实凭据，不要求阅读内部协调记录。

## 三、示例：区分链接测试和可安装 Mod

| 现有内容 | 实际作用 | 公开时的定位 |
|---|---|---|
| `src/action-api-link/.../ExternalActionAddon.java` | 验证独立 SDK 链接、运行时类来源、默认行为 | 合同验证样例，不是游戏 Mod |
| `src/recipe-api-link/.../StandaloneRecipeAddon.java`（位于 SDK 模块内） | 编译验证配方 API 使用 | SDK 链接样例，不是安装模板 |
| `test-fixtures/bridge-extension-example` | NeoForge 只读脚下方块查询夹具 | 可参考的功能逻辑，需整理为正式独立工程 |

只读夹具的 mod 元数据为 `1.0.0-testonly`、`license="All Rights Reserved"`，注册对象另使用 `1.0` 标签。公开示例应单独决定身份、版本与许可证，核对需要的 Minecraft／加载器／宿主版本和真实注册入口，不只改掉文件夹名或 TestOnly 文案。

该示例使用 NeoForge 与 Minecraft 原生 API，不能作为 Fabric 1.20.1 已可用的示例。首批建议交付一个真实可构建的只读示例，并将动作／配方链接样例单列为开发检查；要宣传其他平台示例，再分别补齐对应入口与构建。

## 四、许可证与材料缺口

选定提交未发现项目根 LICENSE；测试示例仍为 All Rights Reserved。这是原提交的审查事实。用户随后确定自有 SDK、协议、示例和公开 skill 采用 MIT；导出时为组件附带许可正文、保留原作者署名，并同步正式示例元数据。原开发仓库尚未修改，候选清单也未因此自动获准全部导出。第三方依赖原有条款不会被 MIT 替代。

以下是资料核对结果，不是最终再分发许可结论：

| 依赖／材料 | 已读证据 | 待办 |
|---|---|---|
| Jackson 2、Jackson 3 | 锁定版本 POM 多数声明 Apache-2.0，缓存 JAR 含 LICENSE／NOTICE | 按实际是否随包分发整理原文与归属，不混用两套版本 |
| NetworkNT 3.0.4、SnakeYAML Engine 3.0.1 | 锁定 POM 声明 Apache-2.0 | 精确版本的许可正文与分发材料尚需补齐 |
| ITU 1.14.0 | 原根声明写 MIT；POM 与后续核对的精确上游标签均为 Apache-2.0 | 按 Apache-2.0 更正导出声明，原开发仓库尚未修改 |
| SLF4J 2.0.9 | 已读实际 JAR 的 LICENSE.txt，确认为 MIT | 保留该版本正文与原署名 |
| JUnit 5.14.2／Platform 1.14.2 | POM 声明 EPL-2.0，JAR 带 LICENSE.md | 区分测试依赖和分发内容 |
| Gradle wrapper 与 NeoForge 工具链 | 原仓库有声明、wrapper 固定 8.14.5 及分发摘要 | 组件构建范围确定后保留对应许可和校验材料；不捆绑 Minecraft 运行时 |
| C# 测试依赖 | 项目引用与锁文件可定位 | 另核 NuGet 实际包声明，不用产品依赖摘要替代 |

初审只登记声明与摘要；后续已收集 23 个 Maven 条目和 20 个 NuGet 包的许可证据，补齐 ITU、NetworkNT 与 SnakeYAML 的精确标签许可原文，ITU 冲突已有更正依据。C# 三个 JSON 测试包的 OSMFEULA 已单列。详见 [第三方许可核对](THIRD-PARTY-REVIEW.zh-CN.md)；尚未形成最终发行包声明。

## 五、敏感内容与排除范围

对候选文本做了私钥头、常见用户绝对路径和 token 字段初筛。未发现私钥头或常见作者 home/Windows Users 路径命中；token 命中主要来自协议例子和测试语料，已定位到确定性生成器、占位文本和固定重复字符样例。模式检查不能证明不存在所有形式的敏感信息，最终允许清单仍需逐文件复核。

不导出客户端核心、模型编排、记忆／检索实现、内部交接／日志、真实用户数据、开发历史和完整构建缓存。开发工作树已有的未提交文件不进入本次提交树审查。

当前展示仓库忽略全部 `*.jar`。将来导入 Gradle wrapper 时需精确允许所选 wrapper JAR，不能为方便构建解除所有 JAR 的忽略规则。

## 后续执行顺序

1. 落实已选 MIT 的组件声明与署名，按已核实证据更正 ITU 等声明；首批按 [独立构建方案](EXPORT-BUILD-PLAN.zh-CN.md) 保持聚合 API 边界。
2. 在独立准备目录生成最小构建工程，按已审清单导出选定提交快照，保留协议资源、必要锁文件与校验材料，不导入完整历史。
3. 整理一个真实可安装的只读示例，补齐身份、依赖、版本和许可；链接测试保持独立定位。
4. 由 Linux 开发流程执行独立 SDK 链接、协议资源／语料测试和示例构建；只补这次导出相关的缺口。Windows 候选目录仅用短提交 ID，保留旧目录。
5. 将通过检查的真实文件路径与支持范围接入公开 skill；随后才按用户明确授权推送、公开和发版。

本次检查通过的是候选清单生成、schema 文档引用闭合及静态资料核对；**没有独立构建通过或实际游戏验收结论**。
