# 首批源码独立构建整理方案

基线为 `3aa5af2b6bd7a4500766e4919d01632c1c2c08f8`。本文是拆分方案，尚未执行源码导出或独立构建。许可状态见 [第三方核对](THIRD-PARTY-REVIEW.zh-CN.md)，候选范围见 [初审](EXPORT-REVIEW.zh-CN.md)。

## 首批边界与目录

建议保持现有相对布局，减少协议资源和测试语料路径变化：

```text
protocol/                         经逐文件复核的合同、schema、限额和测试语料
src/mod/                          新的最小 Gradle 构建根
  bridge-extension-api/           聚合 API、动作／配方制品及链接检查
  protocol-java/                 Java 协议实现与测试
src/dotnet/MiAxion.Protocol/       C# 协议库及局部构建属性
tests/dotnet/MiAxion.Protocol.Tests/  独立测试项目与依赖声明
examples/neoforge-1.21.1-readonly/ 单独的示例构建根
skills/bridge-mod-integration/    已整理的公开接入 skill
```

上面是拟用路径，不代表已存在或可执行。每个组件附带 MIT 正文，第三方例外单列。原测试夹具目录不整体搬入 examples；提取经审查的最小功能并重新整理工程。

## SDK 与 Java 协议

首批保持现有聚合 API 的类型集合，不新增只读二进制边界。公开名称写“聚合 API”，不要把含动作／配方的包称为纯只读 SDK。动作 API 保持 1.2.0，配方 API 保持 0.1.9；聚合 API 与协议的独立发行版本需在导出产物中明确，不能把原产品 0.1.27 无说明地当成全部组件版本。

新的 settings 只包含 SDK 与 Java 协议，不引入基础 Bridge、生产附属或所有测试夹具。提取实际需要的 Java 插件、仓库过滤、版本、严格锁定和校验配置；不复制原根 Stage B 全项目检查。保留 Gradle 8.14.5 wrapper 和分发校验值，定点允许 wrapper JAR 进入 Git。

构建／验证 JDK 使用 21；SDK API 编译目标仍为 17，协议目标按原模块实际配置核对。保留 `verifyActionApiArtifact`、`verifyActionApiLink`、`verifyRecipeApiArtifact`，并新增对聚合 API 的独立消费验证。检查消费者没有访问原工程源码输出，也没有重复打包 SDK 类。

保留协议 JAR 内 `dev/miaxion/protocol/` 的 schema 和限额。保持 `protocol/fixtures` 路径，raw 语料逐项复核后保留；如改用确定性生成步骤，必须证明测试仍能读取相同语料。不能用删除测试来解决相对路径失败。

## C# 协议与测试

协议库保持 .NET 10，无额外 PackageReference 或私有 ProjectReference。提取需要的语言版本、nullable、锁文件等局部属性；不要复制完整客户端中央包清单。验证 discovery v1/v2 schema 和限额确实进入程序集资源。

测试工程保持独立，明确三个 JSON NuGet 包的 OSMFEULA 与其他测试框架许可。先保留锁定版本进行可复现性验证，依赖替换必须单独说明行为影响并验证，不混入单纯源码导出。

## 首个真实示例

先准备 Minecraft 1.21.1、NeoForge 21.1.238、Java 21 的只读示例；这些来自被审查构建属性，不代表已实机验收。功能为读取本地玩家脚下已加载方块的注册 ID，不加载新区块，不修改世界。

保留已存在的真实入口：客户端 `@Mod`、`InterModEnqueueEvent`，通过 `InterModComms.sendTo` 和 `BridgeExtension.IMC_METHOD` 注册。保留线程检查、`hasChunkAt` 和调用前后 `ensureCurrent()`；不臆造注册 API。

改写 TestOnly／All Rights Reserved 元数据为正式示例身份和 MIT；同步 Mod 版本与注册对象版本，但能力合同版本按语义维护，不机械改成软件版本。补 Minecraft／NeoForge／宿主依赖范围，尤其不能把旧 `[0.1.0,)` 当成已证实的最低 Bridge 兼容版本。构建只依赖指定的独立 SDK 制品，基础 Bridge 为运行前置，不能复制到示例 JAR。

原动作和配方链接类继续作为开发验证材料，不宣称是可安装 Mod，也不宣传尚未实现的 Fabric／Forge 示例。

## 交付判定

1. 选定提交文件快照、导出文件映射、源摘要与改写说明可追溯；无历史、私有目录和真实凭据。
2. 干净目录不访问原开发树即可构建 SDK／协议，协议测试和独立链接检查通过；测试依赖与运行依赖分别列出。
3. 示例单独构建通过，元数据和 JAR 内容检查通过；游戏内手动验收另行记录，不能用构建结论替代。
4. LICENSE／NOTICE 与实际分发内容对应，更新公开 skill 的真实路径和命令；在此之前保持“源码尚未导出”的状态说明。
5. Linux 开发与验证由用户自行粘贴交接执行。Windows 候选目录只用短 Git 提交 ID，保留旧目录及运行中的实例。推送、公开和正式发布仍按明确授权执行。
