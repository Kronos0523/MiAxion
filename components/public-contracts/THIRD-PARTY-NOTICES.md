# 许可与依赖边界

选定MiAXion自有SDK、协议、测试、示例和公开skill使用MIT，组件目录有完整正文。保留原有署名。MIT不替代以下第三方许可，不自动开放未列入快照的产品实现。

## 实际依赖

| 范围 | 直接依赖与作用 | 传递依赖／材料 |
|---|---|---|
| 聚合/配方SDK编译 | Jackson databind2.22.1（compileOnly，公开JsonNode类型） | core2.22.1、annotations2.22，Apache-2.0；宿主提供运行类 |
| Java协议运行 | Jackson databind/jsr3102.22.1、NetworkNT3.0.4、SLF4J2.0.9 | Jackson3.1.1、ITU1.14.0、SnakeYAML Engine3.0.1；前三类Apache-2.0，SLF4J为MIT |
| Java测试 | JUnit Jupiter5.14.2、Platform1.14.2 | EPL-2.0；API Guardian1.1.2、OpenTest4J1.3.0为Apache-2.0 |
| C#协议本体 | 无外部PackageReference | 依赖.NET10标准库；本包不捆绑.NET运行时 |
| C#协议测试 | xunit.v3 3.2.2、JsonSchema.Net9.3.0 | 20个NuGet包见下方原始声明；测试包不进入协议运行库 |
| 构建工具 | Gradle wrapper8.14.5（本包包含wrapper JAR与脚本） | Apache-2.0，分发LICENSE/NOTICE附于third-party/build-tools |
| 示例工具链 | ModDev2.0.142、NeoForge21.1.238、NeoForm Runtime2.0.22、Java21 | 独立Gradle锁列出其依赖；这些游戏/工具二进制不随源包分发 |

ITU1.14.0是Apache-2.0，按精确标签LICENSE纠正旧产品表中的MIT。NetworkNT和SnakeYAML的精确源码许可正文、Jackson/SLF4J/JUnit等实际包内LICENSE/NOTICE均保留于third-party。未修改第三方代码。

JsonSchema.Net9.3.0、JsonPointer.Net7.0.1、Json.More.Net3.0.1的实际NuGet包声明OSMFEULA，已附原文及对应源码MIT；不能将上游预编译测试包写成仅MIT。下游自行核对条款是否适用，本次不更换版本或去掉相关测试。

xUnit系列声明Apache-2.0；Humanizer与Microsoft测试平台系列声明MIT。包内nuspec保留版本、原作者和repository修订；对应xUnit、analyzers、Humanizer、ApplicationInsights、testfx的精确修订LICENSE在additional-upstream。Bcl.AsyncInterfaces及Win32.Registry包内原始LICENSE已保留，不用不同修订替代。依赖明细见third-party/dependency-evidence.json；BOM/POM是版本管理材料，不当作运行JAR。

ModDev JAR内含多份原始许可证和归属（含其打包依赖），按entry索引分别保存以避免同名覆盖。NeoForm Runtime实际POM声明LGPL-2.1-only，NeoForge实际POM声明LGPL2.1；未捆绑它们的二进制或Minecraft。NeoForge POM的历史LICENSE链接不能冒充当前精确版本全文，正式再分发这些工具/游戏二进制前需要另外补齐对应来源、许可和义务。这里只让构建工具从其公开仓库解析。

本候选没有模型、语音权重、知识文章、游戏存档或玩家内容。独立构建不意味着这些内容获准再分发；协议旧基线失败及示例未实机范围仍见VALIDATION。
