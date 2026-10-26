# 独立构建

要求 JDK 21 与 .NET SDK 10.0.302（global.json 允许后续兼容 feature band）。SDK API 用 JDK21 编译为 Java17 字节码；游戏示例和 Java 协议用Java21。Gradle8.14.5 wrapper、分发校验与精简后的 dependency verification 均保留。联网首次构建只从所列公开仓库获取工具／依赖；已具备缓存时可加 --offline。

从这个公开候选根目录执行：

```bash
cd src/mod
./gradlew --no-daemon --dependency-verification strict \
  :bridge-extension-api:verifyActionApiArtifact \
  :bridge-extension-api:verifyActionApiLink \
  :bridge-extension-api:verifyRecipeApiArtifact \
  :bridge-extension-api:verifyAggregateApiLink \
  :protocol-java:jar :protocol-java:verifyIndependentConsumer
./gradlew --no-daemon --dependency-verification strict :protocol-java:test
cd ../..
dotnet restore tests/dotnet/MiAxion.Protocol.Tests/MiAxion.Protocol.Tests.csproj --locked-mode
dotnet test --project tests/dotnet/MiAxion.Protocol.Tests/MiAxion.Protocol.Tests.csproj --no-restore
cd examples/neoforge-1.21.1-readonly
../../src/mod/gradlew --no-daemon --dependency-verification strict build verifyExampleArtifact
```

Windows可用同路径的gradlew.bat；本候选实际独立构建仅在Linux完成，Windows及其他系统不据此签核。不要复制原产品构建输出或去关闭strict。本候选的协议全量测试已通过；旧清单/语料失败的修复范围和未实测边界见VALIDATION。

SDK产物位于src/mod/bridge-extension-api/build/libs。聚合SDK的独立消费者只编译引用该JAR及声明的Jackson API依赖，运行检查类来源；不引用sourceSets.main输出。动作、配方链接类是合同验证，不能放进mods。

示例是另一个Gradle根，只以compileOnly读取上述聚合SDK JAR。可用 `-PsdkJar=/绝对路径/bridge-extension-api-0.1.0.jar` 指定已构建的同一API。基础Bridge仅为游戏运行前置，未作为源码或编译JAR混入示例。最终安装的只是示例JAR和另行提供的基础Bridge，不能安装SDK。

不要整体复制开发树的Gradle/中央NuGet清单。测试语料保留protocol/fixtures与tests/fixtures/bridge两处；测试根定位使用实际语料，不依赖内部AGENTS或全产品solution。
