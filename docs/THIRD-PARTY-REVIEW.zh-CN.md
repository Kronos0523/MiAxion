# SDK 与协议第三方许可核对

日期：2026-10-26。源码基线：`3aa5af2b6bd7a4500766e4919d01632c1c2c08f8`。这是导出准备记录，不是最终二进制分发清单，也不覆盖全部 Bridge 平台和生产附属。

自有 SDK、协议、示例和公开 skill 采用 MIT；下列依赖保留原许可。审查覆盖 SDK／Java 协议锁文件中的 23 个 Maven 条目（含 BOM、测试依赖），以及 C# 协议测试锁文件中的 20 个 NuGet 包。不能把这些条目全部视为客户端运行时依赖。

## 已核实的差异

| 项目 | 核对结果 | 导出处理 |
|---|---|---|
| ITU 1.14.0 | 缓存 POM 与上游 v1.14.0 的 LICENSE 均为 Apache-2.0 | 纠正原产品声明中的 MIT；不修改上游署名 |
| SLF4J API 2.0.9 | 实际 JAR 的 LICENSE.txt 为 MIT，署名 2004–2022 QOS.ch Sarl | 使用该版本自带正文，不能用官网当前年份替换 |
| Jackson jsr310 2.22.1 | 虽然 POM 无直接 licenses 段，实际 JAR 的 LICENSE 明确 Apache-2.0 | 保留其 LICENSE 与 NOTICE |
| JsonSchema.Net 9.3.0、JsonPointer.Net 7.0.1、Json.More.Net 3.0.1 | 实际 nuspec 指向包内 OSMFEULA.txt | 测试依赖单独披露，不能简化为仅 MIT |

ITU 的精确标签指向 `f5fbc9ec887dfbb46ec59407bc437e296ba381e4`，已保存其 [LICENSE](https://raw.githubusercontent.com/ethlo/itu/f5fbc9ec887dfbb46ec59407bc437e296ba381e4/LICENSE) 与 POM。原声明冲突已获得可用于更正的上游证据；开发仓库原文件尚未修改。

JSON 系列的 [OSMFEULA 原文](https://raw.githubusercontent.com/json-everything/json-everything/0b08e9d043ce40bf20b1d39911c111c4f14ebacd/OSMFEULA.txt) 区分 MIT 源码与上游预编译包，并约定符合其条件的营收使用者支付维护费；同时说明自行编译和 MIT 权利的关系。本次不替用户或下游判断收费条件是否满足。导出测试工程时必须披露原文及包来源，不能把测试包混入无外部包依赖的协议库本体。是否调整依赖留给独立构建流程处理，不在声明整理中静默换版本或删测试。

## Java 材料

| 依赖 | 版本 | 已取得材料 |
|---|---|---|
| Jackson 2 core/databind/jsr310；annotations | 2.22.1；2.22 | POM、JAR LICENSE／NOTICE |
| Jackson 3 core/databind/dataformat-yaml | 3.1.1 | POM、JAR LICENSE／NOTICE |
| ITU | 1.14.0 | POM、精确标签 LICENSE |
| NetworkNT json-schema-validator | 3.0.4 | POM、[精确标签 LICENSE](https://raw.githubusercontent.com/networknt/json-schema-validator/2276a17e117c8977eacac2dcef04908939463bed/LICENSE) |
| SnakeYAML Engine | 3.0.1 | POM、[精确标签 LICENSE](https://bitbucket.org/snakeyaml/snakeyaml-engine/raw/8ca9ad4a0f54422634e76f8db6873cdd46789c36/LICENSE.txt) |
| SLF4J API | 2.0.9 | POM、JAR MIT 正文 |
| JUnit Jupiter／Platform | 5.14.2／1.14.2 | POM、JAR EPL-2.0 正文 |
| API Guardian／OpenTest4J | 1.1.2／1.3.0 | POM、JAR Apache-2.0 正文 |
| Jackson 2／3 与 JUnit BOM | 2.22.1／3.1.1／5.14.2 | POM；版本管理条目，不当作运行 JAR |

许可证正文已收集不代表最终归属材料完整：对 JAR 未附 NOTICE 的项目，后续仍需核对实际随包内容、上游归属及是否包含改动，不能推断“无 NOTICE 就无其他义务”。

## C# 测试材料

20 个包的 nuspec 均已读取。xunit.v3 系列 3.2.2 与 xunit.analyzers 1.27.0 声明 Apache-2.0；Humanizer.Core 3.0.10 与相关 Microsoft 包声明 MIT；上述三个 JSON 包采用文件型 OSMFEULA 声明。声明为表达式的包仍需按最终分发方式补齐许可正文和署名。

校验分别比较 NuGet 缓存元数据的 contentHash 与锁文件，以及实际 nupkg 的 SHA-512 与缓存侧车摘要；20 个包两项均一致。这是本地缓存一致性检查，不是新执行的 restore、上游签名认证或构建验证。

## 最终组装仍需补齐

- 按独立构建的实际解析结果分开记录 API 编译依赖、协议运行依赖、测试依赖和构建工具。SDK 不额外捆绑宿主提供的 Jackson／API 类。
- Gradle wrapper、插件及 NeoForge 工具链单列清单；本轮 43 个依赖条目不覆盖这些工具。不要打包 Minecraft 运行时。
- 保留选定依赖的许可正文、原署名和适用 NOTICE；生成与实际发行包一致的 THIRD-PARTY-NOTICES，不能复制整个客户端的旧清单。
- 发布前按实际文件集合复核；本记录本身不授权推送、公开仓库或发布 Release。
