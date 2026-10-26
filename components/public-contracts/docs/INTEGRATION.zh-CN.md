# 接入与所有权

首批交付聚合公共API，不称“纯只读SDK”。Java包 `extension.api` 为只读SPI，`action.api` 为语义单次动作，`recipe.api` 与平台注册包承载配方和术语接口。声明注册不等于执行授权；SDK不拥有连接、配对、游戏线程或Mod生命周期。

API类由基础Bridge在运行时提供。附属只compileOnly依赖SDK，不能重复打包API、Jackson、Minecraft或宿主实现。聚合API暴露JsonNode，因此编译消费者需要对应Jackson；主机提供运行版本。不要以显示名称建立动作、配方或材料身份。数量未知保留未知，类型UID、单位和来源覆盖范围不能凭经验填充。

## NeoForge只读示例

参见 examples/neoforge-1.21.1-readonly/src/main/java/dev/miaxion/extension/example/StandingBlockExtension.java。

- `@Mod(..., dist=Dist.CLIENT)` 构造器订阅 `InterModEnqueueEvent`。
- 使用 `InterModComms.sendTo("miaxion_bridge", BridgeExtension.IMC_METHOD, ...)`，IMC方法由真实API常量提供。
- 声明id必须与实际发送Mod身份一致；能力ID使用自己的命名空间。
- host将handler调度到声明线程；读前读后ensureCurrent，核对客户端线程/player/level，先hasChunkAt再读取脚下方块。不强制加载新区块、不修改世界、不读取容器。
- 示例Mod版本与注册对象同为0.1.0；能力合同仍1.0。代码未引入新注册API。

元数据宿主范围为[0.1.27,)，它是本次已阅读源码的保守起点，**不是声称0.1.27是历史最早可用版本，也不是所有后续版本兼容证明**。保留API_MAJOR=1的真实合同核验，未沿用没有证据的[0.1.0,)；Minecraft1.21.1和NeoForge21.1.238是此示例构建组合。基础Bridge实机兼容、示例注册和一次只读调用仍待验。

## 其他入口与新平台

动作入口见action.api与neoforge.api中的现有接口；配方／术语见fabric.api、forge.api、neoforge.api。不把接口存在当作任意平台宿主已安装实现。首批只包含NeoForge只读Mod示例，其他链接类是开发检查。

基础Bridge五平台移植需另外提供已授权的宿主实现源码；当前包没有它们。公开skill会列出需要的输入和核对项，不生成依赖不存在文件的移植模板。不要把整套客户端或内部模型编排当作附属开发依赖。
