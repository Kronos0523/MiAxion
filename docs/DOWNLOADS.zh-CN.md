# 下载与组件选择

**[0.2.71 公测版](https://github.com/Kronos0523/MiAxion/releases/tag/v0.2.71) 已发布。** 本次包含客户端、14 个 JAR 的模组合集和勇者之章3知识合集；旧版六个知识包保留，语音包沿用网盘。

[发布与升级说明](releases/0.2.71.zh-CN.md) · [附件清单](releases/0.2.71-assets.json) · [SHA-256](releases/0.2.71-SHA256SUMS.txt)

## Windows 客户端 0.2.71

完整解压后运行 `client/MiAXion.exe`，保留 `client/app/`、`client/assets/` 及许可证。内含 .NET/WPF 10.0.13，检索引擎需要[VC++ v14 x64](https://aka.ms/vc14/vc_redist.x64.exe)。

大小：373,866,038 字节（356.55 MiB）。来源提交：`18a0bb21a0dfd0c498f63a76e06bd2414b666194`。

SHA-256：`7888b35830a7ac6e3f70c6e4400079a40e0e5cff222e92544909c59e990d1953`。

升级先退出客户端并保留旧目录，完整复制 `client/data` 到新目录，勿共用同一份数据或仅复制数据库。

## 应该下载哪些内容

| 内容 | 何时需要 | 安装位置或用途 | 当前入口 |
|---|---|---|---|
| Windows x64 完整客户端 | 使用桌面助手 | 完整解压到独立目录，运行 client/MiAXion.exe | [下载客户端](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.71/MiAXion-0.2.71-win-x64.zip) |
| 基础 Bridge | 连接游戏、读取其提供的状态 | 匹配 Minecraft／加载器的实例 `mods` | [五端组件明细](releases/0.2.71.zh-CN.md)；[下载模组合集](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.71/MiAXion-JAR-collection.zip) |
| JEI／REI 配方附属及其他模组附属 | 使用对应配方或模组能力 | 同一实例 `mods`；核对目标模组及前置依赖 | [9 个附属组件明细](releases/0.2.71.zh-CN.md)；[下载模组合集](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.71/MiAXion-JAR-collection.zip) |
| 语音包 | 语音输入／输出 | 按该包说明，在客户端语音设置中导入 | [百度网盘](https://pan.baidu.com/s/1Attd0pmh-a8Bm9AV95nrvg?pwd=9999)，提取码 `9999` |
| 音色包 | 选择朗读声音 | 先安装适用语音运行包，再按格式导入音色／参考音频 | [语雀音色包入口](https://www.yuque.com/baicaigongchang1145haoyuangong/ib3g1e/nwnaga50cazb2v93) |
| 知识包 | 需要对应资料 | 在“知识库”导入并启用；超市先解压选包 | [散包超市、3 个整合包合集及原版中文／英文包](#知识包获取) |
| SDK／协议源码与开发示例 | 开发附属或接入协议 | 独立开发工程；SDK／协议 JAR 不是 Mod | [源码入口](../components/public-contracts/README.zh-CN.md) |

只用文字聊天时，先取完整客户端并配置模型服务。语音包沿用发布者提供的 [百度网盘分享](https://pan.baidu.com/s/1Attd0pmh-a8Bm9AV95nrvg?pwd=9999)，提取码 **9999**。先阅读[语音包安装与占用说明](SPEECH-PACKS.zh-CN.md)，或网盘中的《使用说明.md》，按需选择轻量、均衡或高品质：单档可选独立包，多档可选共享底座加对应扩展，不必全部下载。网盘内说明以实际上传版本为准。

**均衡款自带音色，可直接使用；高品质款必须另外导入音色包后才能朗读。** 高品质语音运行包本身不能替代音色包。

音色包下载入口：[语雀音色包页面](https://www.yuque.com/baicaigongchang1145haoyuangong/ib3g1e/nwnaga50cazb2v93)。音色包用于选择朗读声音，与提供识别／朗读环境的语音运行包分开；按页面说明选择适配格式，再使用客户端对应的音色或参考音频入口导入。

## 知识包获取

新增[勇者之章3合集](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.71/chapter-of-yuusha-3.zip)（260 个子包，85.69 MiB），直接导入整个 ZIP。

上一批知识包包括 **一个散包超市、三个整合包合集，以及原版中文、英文两包**，共六个下载文件。按自己的玩法和所需资料语言选择，不必全部下载。

| 入口 | 实际文件 | 小包数 | 下载大小 | 当前状态 |
|---|---|---:|---:|---|
| 散包超市 | `knowledge-packs.zip` | 763 | 468.68 MiB | [下载](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/knowledge-packs.zip) |
| ATM10 合集 | `atm10.zip` | 216 | 148.71 MiB | 中文资料，部分覆盖；[下载](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/atm10.zip) |
| BMC2 合集 | `bmc2.zip` | 306 | 28.11 MiB | 中文资料，部分覆盖；[下载](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/bmc2.zip) |
| Create-Delight-Remake 合集 | `Create-Delight-Remake.zip` | 319 | 83.31 MiB | 中文资料，部分覆盖；[下载](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/Create-Delight-Remake.zip) |
| 原版中文资料 | `minecraft_zh.zip` | 14 | 29.34 MiB | 中文资料合集；[下载](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/minecraft_zh.zip) |
| 原版英文资料 | `minecraft_en.zip` | 8 | 203.67 MiB | 英文资料合集；[下载](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/minecraft_en.zip) |

**散包超市先解压，再选择需要的小包 ZIP 导入**，不要将外层 `knowledge-packs.zip` 作为单个知识包导入。三个整合包合集及原版中文、英文包均包含客户端支持的合集清单，可在“知识库”的导入入口选择对应 ZIP；客户端按合集清单处理成员包，无需手动拆成编号小包。本轮核对了包结构及 0.2.0 导入代码入口，未实际执行整包导入。

小包数量不等于模组数量，也不表示覆盖该整合包全部内容。三个整合包合集的清单均标记为部分覆盖；原版两包的清单标记为 complete，仅表示构建清单的覆盖状态，不代表覆盖全部 Minecraft 版本或全部知识。中英文包的内容及拆分数量不同，不视为逐条对应的翻译版本。部分条目的适用版本尚未声明，具体游戏／整合包版本需补核，不仅凭包名判断兼容性。

精确字节数和 SHA-256 见 [文件记录](releases/knowledge-pack-files.json) 与 [本批六个包的校验文件](releases/knowledge-pack-SHA256SUMS.txt)。知识包独立于客户端版本，不统一标为 0.2.0；后续更新以各包自身清单为准。

发布者已于 2026-10-27 确认本批知识资料的分发许可核对完成，各项原始许可和署名要求继续适用，不随客户端或 SDK 自动采用同一许可。下载入口见上表。

## 基础 Bridge 要匹配实例

[下载 0.2.71 模组合集](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.71/MiAXion-JAR-collection.zip)（42.12 MiB，含 5 个基础 Bridge 和 9 个附属）。使用东方女仆附属时，请选择 Forge 1.20.1，安装 Bridge 0.1.32 或以上版本（合集内为 0.1.35），并另装东方女仆本体 1.5.0。

校验下载时，请在[本版 SHA256SUMS.txt](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.71/SHA256SUMS.txt) 中找到 `MiAXion-JAR-collection.zip` 对应的一行，与本地 ZIP 的 SHA-256 比对。

解压后，按实例的 Minecraft 版本和加载器在[本版组件清单](https://github.com/Kronos0523/MiAxion/releases/tag/v0.2.71#附件校验与来源)中找到对应的基础 Bridge 和所需附属，只将匹配的 JAR 放入实例的 `mods` 目录。

平台适配范围不等于所有附属在每端都有相同能力。下载前查看实际附件说明；不要将五个平台的 JAR 一起安装，也不要用改名方式适配不同版本。

## 版本与更新

每次 Release 分别列出客户端、各平台 Bridge、每个附属和语音包的版本、适用条件、是否需要更新。客户端升级不自动意味着其他组件必须重装。

0.2.0 仅整理客户端发布布局，继续采用自包含部署，没有因本次布局变化要求更换已有匹配的 Bridge、附属或语音包。新用户仍须按各组件的实际版本及前置要求安装。

附件清单已登记真实文件名、字节数、SHA-256 和来源提交。语音包沿用已有交付记录，缺失的信息保持待补，不用文件夹日期猜版本。

安装见 [首次使用](QUICKSTART.zh-CN.md)，升级见 [备份与回退](UPGRADE.zh-CN.md)。源码构建通过不代替客户端包的 Windows 验证，也不代表所有平台均已游戏实测。
