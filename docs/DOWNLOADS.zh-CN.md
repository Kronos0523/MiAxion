# 下载与组件选择

**[0.2.0 自包含公测版](https://github.com/Kronos0523/MiAxion/releases/tag/v0.2.0) 已发布。** 客户端、模组合集和六个知识包从 GitHub Release 下载；语音包继续使用百度网盘。源码开放范围见 [开发入口](BRIDGE-DEVELOPMENT.zh-CN.md)。

本批附件为客户端、模组合集和六个知识包，共 8 个 ZIP；文件及 SHA-256 已核对。见 [附件总清单](releases/0.2.0-assets.json) 与 [统一校验文件](releases/0.2.0-all-SHA256SUMS.txt)。语音包继续使用百度网盘，按需选择，不必下载所有附件。

## Windows 客户端 0.2.0

| 项目 | 内容 |
|---|---|
| 文件 | `MiAXion-0.2.0-win-x64.zip` |
| 下载状态 | [下载客户端](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/MiAXion-0.2.0-win-x64.zip) |
| 大小 | 368,825,942 字节，约 351.74 MiB |
| 来源提交 | `4d1add3f1438d1d0267e07d10c74874157513ec7` |
| 启动入口 | 完整解压后运行 `client/MiAXion.exe` |
| 系统与运行环境 | Windows x64；内含 .NET/WPF 10.0.13，无需另装 .NET 或 SDK |
| 原生依赖 | ONNX 检索引擎需要 Microsoft Visual C++ v14 x64 运行库 |

SHA-256：

```text
0b8afaae4b0d7ab3234a84c920966413bcca847579305eb7dacde487a07e3789
```

下载后可在 PowerShell 执行 `Get-FileHash .\MiAXion-0.2.0-win-x64.zip -Algorithm SHA256`，与上值比较。校验文件见 [SHA256SUMS](releases/0.2.0-SHA256SUMS.txt)。

如提示缺少 MSVCP140／VCRUNTIME140，使用[微软官方 VC++ x64 安装入口](https://aka.ms/vc14/vc_redist.x64.exe)。请保留解压后的 assets、说明及许可证文件；依赖已收拢不代表只需复制 EXE。本包不含个人 data、知识正文、游戏 Mod 或语音包。

变化和验证边界见 [0.2.0 发布说明](releases/0.2.0.zh-CN.md)。完全未安装 .NET 的干净 Windows 环境尚未实测，不将现有 Windows 启动检查等同于该项。

## 应该下载哪些内容

| 内容 | 何时需要 | 安装位置或用途 | 当前入口 |
|---|---|---|---|
| Windows x64 完整客户端 | 使用桌面助手 | 完整解压到独立目录，运行 client/MiAXion.exe | [下载客户端](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/MiAXion-0.2.0-win-x64.zip) |
| 基础 Bridge | 连接游戏、读取其提供的状态 | 匹配 Minecraft／加载器的实例 `mods` | [五端组件明细](releases/0.2.0-mods.zh-CN.md)；[下载模组合集](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/MiAXion-JAR-collection.zip) |
| JEI／REI 配方附属及其他模组附属 | 使用对应配方或模组能力 | 同一实例 `mods`；核对目标模组及前置依赖 | [8 个附属组件明细](releases/0.2.0-mods.zh-CN.md)；[下载模组合集](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/MiAXion-JAR-collection.zip) |
| 语音包 | 语音输入／输出 | 按该包说明，在客户端语音设置中导入 | [百度网盘](https://pan.baidu.com/s/1Attd0pmh-a8Bm9AV95nrvg?pwd=9999)，提取码 `9999` |
| 音色包 | 选择朗读声音 | 先安装适用语音运行包，再按格式导入音色／参考音频 | [语雀音色包入口](https://www.yuque.com/baicaigongchang1145haoyuangong/ib3g1e/nwnaga50cazb2v93) |
| 知识包 | 需要对应资料 | 在“知识库”导入并启用；超市先解压选包 | [散包超市、3 个整合包合集及原版中文／英文包](#知识包获取) |
| SDK／协议源码与开发示例 | 开发附属或接入协议 | 独立开发工程；SDK／协议 JAR 不是 Mod | [源码入口](../components/public-contracts/README.zh-CN.md) |

只用文字聊天时，先取完整客户端并配置模型服务。语音包沿用发布者提供的 [百度网盘分享](https://pan.baidu.com/s/1Attd0pmh-a8Bm9AV95nrvg?pwd=9999)，提取码 **9999**。先阅读[语音包安装与占用说明](SPEECH-PACKS.zh-CN.md)，或网盘中的《使用说明.md》，按需选择轻量、均衡或高品质：单档可选独立包，多档可选共享底座加对应扩展，不必全部下载。网盘内说明以实际上传版本为准。

**均衡款自带音色，可直接使用；高品质款必须另外导入音色包后才能朗读。** 高品质语音运行包本身不能替代音色包。

音色包下载入口：[语雀音色包页面](https://www.yuque.com/baicaigongchang1145haoyuangong/ib3g1e/nwnaga50cazb2v93)。音色包用于选择朗读声音，与提供识别／朗读环境的语音运行包分开；按页面说明选择适配格式，再使用客户端对应的音色或参考音频入口导入。

## 知识包获取

本批知识包包括 **一个散包超市、三个整合包合集，以及原版中文、英文两包**，共六个下载文件。按自己的玩法和所需资料语言选择，不必全部下载。

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

本批模组使用 `MiAXion-JAR-collection.zip`，含 5 个基础 Bridge 和 8 个附属，约 42.00 MiB；[大小与 SHA-256 记录](releases/0.2.0-mod-archive.json)。[下载模组合集](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/MiAXion-JAR-collection.zip)，解压后只安装匹配平台的 JAR。

| Minecraft | 加载器 | 正式附件状态 |
|---|---|---|
| 1.20.1 | Forge | Bridge 0.1.31，JEI 附属 0.1.3；已包含在模组合集 |
| 1.20.1 | Fabric | Bridge 0.1.26，REI 附属 0.1.13；已包含在模组合集 |
| 1.21.1 | Fabric | Bridge 0.1.25，JEI 附属 0.1.3；已包含在模组合集 |
| 1.21.1 | NeoForge | Bridge 0.1.27，JEI 0.1.10、Ars 0.1.3、Iron 0.1.5、Mekanism 0.1.2 附属；已包含在模组合集 |
| 26.3 | NeoForge | Bridge 0.1.13，JEI 附属 0.1.4；已包含在模组合集 |

完整文件名、加载器及前置版本见 [Bridge 与附属清单](releases/0.2.0-mods.zh-CN.md)。

平台适配范围不等于所有附属在每端都有相同能力。下载前查看实际附件说明；不要将五个平台的 JAR 一起安装，也不要用改名方式适配不同版本。

## 版本与更新

每次 Release 分别列出客户端、各平台 Bridge、每个附属和语音包的版本、适用条件、是否需要更新。客户端升级不自动意味着其他组件必须重装。

0.2.0 仅整理客户端发布布局，继续采用自包含部署，没有因本次布局变化要求更换已有匹配的 Bridge、附属或语音包。新用户仍须按各组件的实际版本及前置要求安装。

附件清单已登记真实文件名、字节数、SHA-256 和来源提交。语音包沿用已有交付记录，缺失的信息保持待补，不用文件夹日期猜版本。

安装见 [首次使用](QUICKSTART.zh-CN.md)，升级见 [备份与回退](UPGRADE.zh-CN.md)。源码构建通过不代替客户端包的 Windows 验证，也不代表所有平台均已游戏实测。
