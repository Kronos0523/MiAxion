# 构建验证与剩余缺口

源码快照3aa5af2b6bd7a4500766e4919d01632c1c2c08f8，独立整理于2026-10-26。三份测试/语料文件取自修复提交a4cbb396f7e2990e7505dd7035b274297de854a9，逐文件来源见source-map.json；其余仍来自原快照。未公开、未推送；示例未运行游戏。

## 已执行

- Linux独立临时树，只包含公开范围；使用公共依赖缓存，不依赖原产品classes/bin/JAR输出。SDK编译器JDK21，API字节码17；协议与示例JDK21。
- 动作API制品、动作独立链接、配方制品/独立编译、聚合API独立消费者通过，类来源指向导出JAR。
- Java协议JAR含原schema及runtime-limits。新独立消费者复现原宿主SLF4J依赖缺口（NoClassDefFoundError），导出构建声明现有SLF4J2.0.9 runtimeOnly后通过；不加日志实现，缺provider时使用SLF4J默认NOP，不影响资源读取。
- Java协议全量336/336通过；C#协议全量265/265通过，零失败、零跳过。保留全部原测试；旧基线曾为Java334通过/2失败、C#262通过/3失败。
- NeoForge示例独立build与最终JAR入口/元数据/不夹带API检查通过。保留调用前后ensureCurrent、线程与已加载区块检查。没有执行游戏注册或实际查询。
- C#协议本体无PackageReference，锁定测试依赖恢复成功；测试依赖与运行库分别说明。

## 已修复的旧清单与语料问题

| 检查 | 现场原因 | 范围 |
|---|---|---|
| ProtocolActionBudget（Java/C#） | 共用action-budget-policy语料没有world.registry.search，而实际两端策略把它作为查询 | 两端各1项 |
| ProtocolCorpus manifest顺序（Java/C#） | manifest中world-registry-search位于resolve之前，不满足原有字典序断言 | 两端各1项 |
| PublicContractInventoryIsExact（C#） | 已公开ActionConfigurationEntry等类型未进入旧期望清单 | C#1项 |

Java失败对应实现、测试与语料均逐字节等同固定来源提交；C#另外在固定提交原文件的独立基线执行，复现相同3项失败。原基线执行只绕开CA1861编译风格警告以观察测试，不关闭测试断言；本候选把相同常量数组提为static readonly，正常分析器仍启用。

本轮核对后补齐共享查询语料、固定公共类型清单（含嵌套类型），整理manifest顺序和12项reason短标识。精确集合比较、排序与格式断言保留；未改schema、生产DTO或请求/响应语料。

拆分中额外发现的fixture定位问题已经修复：测试不再寻找内部AGENTS.md或全产品MiAxion.sln，而是寻找实际共享语料。3项RuntimeLimits路径失败消失，未删除或替换测试。所有生产协议类与schema保留原字节。

独立源码构建及协议测试已通过；示例游戏内注册及一次脚下方块查询仍待验，公开发布仍须维护者确认。Windows独立构建、其他加载器示例及基础Bridge五端源码不在本轮通过范围。不要把此源码候选宣传为全平台可安装Mod合集。

## 许可材料

MIT仅覆盖用户选定的自有组件。ITU的Apache-2.0纠正、三个JSON测试包的OSMFEULA、各包原始署名和附带许可证已单列。源包包含 Gradle wrapper JAR 与对应许可材料，其余游戏／工具链二进制未捆绑；如未来另发它们的二进制，应按实际分发内容补对应材料，不能沿用“仅源码引用”的范围说明。
