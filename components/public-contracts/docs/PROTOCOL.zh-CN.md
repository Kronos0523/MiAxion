# 协议材料导航

本包提供外部合同、编码／校验库及确定性语料，不包含运行中的客户端或Bridge服务器。实际schema和实现优先，不能根据旧示例猜新字段或将逻辑schema URI当下载服务。

- protocol/schema/discovery.schema.json、discovery-v2.schema.json：实例发现；C#同时嵌入两者。
- protocol/schema/common.schema.json、request-envelope.schema.json、request.schema.json、response.schema.json：信封与请求／响应。信封版本1.0，requestId关联，timestamp/deadline为UTC，action与payload按合同组合。
- protocol/schema/extensions.schema.json、recipe.schema.json等：能力数据。配方查询接口1.9、指定来源1.3，与SDK发行版本分开；资源种类／ID／类型UID联合表示身份，未知数量不猜。
- protocol/runtime-limits.json：真实限额来源，Java/C#均打包此资源；不在指南另复制一份易过时数字表。
- protocol/fixtures/valid、invalid、raw、traces：共享语料；其中token为固定测试值，不是可用凭据，不用于实际连接。

发现只定位实例，不授权访问。使用发现记录的loopback endpoint、subprotocol和实例身份协商，不把Minecraft发行号当接口兼容条件。配对请求获宿主批准后才取得会话令牌；令牌绑定当前连接，不写日志或硬编码进工具。session.pair.approved是配对请求的特殊成功响应；业务成功/失败关联原requestId，request.cancel与session.revoke按各自合同处理。断线不自动重放动作。

Java SchemaRegistry仅装载JAR内固定schema资源，禁止远程schema fetch。独立Java协议库需要Jackson2、NetworkNT/Jackson3、ITU、SnakeYAML和SLF4J运行依赖；它们通过Gradle声明，不嵌入本体JAR。独立版补显式SLF4J运行依赖，避免依赖原Bridge提供日志API。C#协议本体无NuGet包依赖，JSON Schema/xUnit仅用于测试。

完整外部字段与错误／取消分支按保留的schema、Contracts/DTO及RawProtocolCorpus测试阅读。未知能力、部分结果和实际授权是不同层面：不能把静态库校验成功当作主机支持，更不能自行放宽宿主执行权限。协议测试已通过；旧基线修复记录及尚未验证的实机／Windows 范围见 [VALIDATION](VALIDATION.zh-CN.md)。
