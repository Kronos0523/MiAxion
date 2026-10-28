# MiAxion

**[简体中文 / Chinese](README.zh-CN.md)** | English

Bring mod knowledge into your conversations. Connect your questions to the world you are playing.

MiAxion is a desktop AI assistant for Minecraft, built around **knowledge packs**. Import reference material for your mods and modpacks, ask questions, inspect sources, and keep exploring through follow-up questions. Connect the game to add live world information, recipes, machine states, and voice-triggered actions.

**[Download 0.2.0](https://github.com/Kronos0523/MiAxion/releases/tag/v0.2.0)** · [Knowledge packs](#available-knowledge-packs) · [Getting started](#getting-started) · [Developer integration](docs/BRIDGE-DEVELOPMENT.zh-CN.md)

The desktop client runs on **Windows x64**. The release includes .NET/WPF 10.0.13; extract the complete archive and run `client/MiAXion.exe`. ONNX-based retrieval also requires [Microsoft Visual C++ v14 x64](https://aka.ms/vc14/vc_redist.x64.exe). The English overview below links to detailed guides currently written in Chinese.

## Knowledge packs: the core of MiAxion

Modded Minecraft questions go beyond crafting recipes: *What does this machine do? How does this mechanic work? What should I read next?* Knowledge packs give the assistant searchable reference material to draw on when explaining the content you play with.

- **Choose the knowledge you need.** Import individual packs for selected mods or a collection for a modpack. Filter, enable, disable, and manage packs in batches.
- **Ask, then follow up.** Start with an unfamiliar item or machine and continue asking about its purpose and requirements without repeating its full name every time.
- **Inspect the sources.** Expand the reference material attached to an answer to check what the explanation is based on.
- **Connect reference material with your game.** When a usable mod list is available, pack activation can be adapted to the instance. Bridge queries and JEI/REI add-ons can then provide current game information and instance-specific recipes.

A conversation might begin with **“Look up how the machine in my hand works in the knowledge library,”** continue with **“Check its recipe in this instance,”** and then ask **“What is the nearby machine's current state?”** These are example prompts: the knowledge pack supplies reference material, the recipe add-on supplies recipes, and a supported machine adapter supplies readable state. Each step requires the relevant content and capabilities.

### Available knowledge packs

Six downloadable files are included in the [0.2.0 Release](https://github.com/Kronos0523/MiAxion/releases/tag/v0.2.0). Choose what fits your game; you do not need all six.

| Pack | Contents | How to import |
|---|---|---|
| [Individual-pack marketplace](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/knowledge-packs.zip) | 763 individual packs to choose from | Extract the outer ZIP, then import the packs you want |
| [ATM10](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/atm10.zip) | Chinese reference collection; partial coverage | Import the collection ZIP directly |
| [BMC2](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/bmc2.zip) | Chinese reference collection; partial coverage | Import the collection ZIP directly |
| [Create-Delight-Remake](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/Create-Delight-Remake.zip) | Chinese reference collection; partial coverage | Import the collection ZIP directly |
| [Vanilla Minecraft — Chinese](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/minecraft_zh.zip) | Chinese reference material | Import the collection ZIP directly |
| [Vanilla Minecraft — English](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/minecraft_en.zip) | English reference material | Import the collection ZIP directly |

Pack counts are not mod counts. Coverage varies by content and version, and the two vanilla collections are not line-by-line translations. Modpacks may change recipes; current inventory and machine state still require game queries. See the [pack catalog](docs/DOWNLOADS.zh-CN.md#知识包获取) and [file sizes and SHA-256 hashes](docs/releases/knowledge-pack-files.json).

Knowledge content is stored locally. If you use an online model, relevant retrieved passages may be sent to the service you configure. See [data use and sharing](docs/DATA-USAGE.zh-CN.md).

## Explore, build, and interact

The prompts below illustrate use cases, rather than recorded test results. Natural-language answers depend on your chosen model; game queries and actions require the matching connection and enabled capabilities.

| Feature | Example | What it provides |
|---|---|---|
| World queries | “Find a nearby village.” “Locate this biome.” | Structure and biome lookup, with location, distance, and query scope |
| Current structure | “What structure am I inside?” | Identification of a loaded structure and supported targets in its covered area |
| Machine state | “Which machines are nearby?” “What state is this machine in?” | Available state, energy, fluid, or mechanical information from supported adapters |
| Recipes and materials | “How do I craft this?” “What is it used for?” “What materials am I missing?” | Recipes, uses, crafting trees, and material shortfalls |
| Player information | “What am I holding?” “How much iron is in my inventory?” | Main/off-hand items, inventory, equipment, and available player/environment information |
| FTB Quests | “What does this quest require?” | Visible quests, objectives, progress, rewards, and direct prerequisites |
| Voice casting | Bind your own phrase to an available spell action | Fixed-phrase matching without passing the command through an LLM |
| Game actions | “Open the quest book.” | Supported interface operations and single game actions |

### World and structure queries

Locate a structure or biome, or inspect the structure you are already standing in. Nearby queries can also report readable container locations and types. Structure observation relies on registered structures, loaded data, and supported target rules; it does not identify every player-built building or generate loot to inspect a chest.

World location, nearby machine/container, and structure queries are subject to local integrated-server access and loaded-area constraints, depending on the operation. They are not guaranteed on remote multiplayer servers.

### Machine state and instance-specific recipes

Use knowledge packs to understand a machine, then query the game for its readable state. Supported adapters may report running, idle, or blocked states and available energy, fluid, or mechanical information. Unreadable or unproven state remains unknown. Fields vary by mod and loader; Create information depends on the applicable adapter, while Mekanism-specific information requires its add-on.

JEI/REI add-ons expose recipes and uses from the running instance, with crafting-tree and material-shortfall queries available in the client. Supported resource types depend on the add-on and native recipe browser. Machine queries do not automatically control machines or calculate production-line throughput, energy efficiency, or upgrade efficiency.

### Quests and game actions

With compatible FTB Quests support, read visible task descriptions, objectives, progress, rewards, and direct dependencies. Quest queries do not submit tasks or claim rewards.

Available actions can open supported interfaces, select hotbar slots, or change perspective. Support is specific to the current action catalog and game conditions. Opening a mod interface does not imply universal closing or switching support, and this is not an automatic movement, combat, or AFK system.

## Voice conversation and voice casting

**Voice conversation:** hold your configured push-to-talk key. A local speech pack transcribes your speech, and your selected model handles the conversation. Replies can be read aloud in segments and stopped at any time. Speech recognition and synthesis run locally; whether the conversation reaches an online model depends on your model configuration.

**Casting mode:** configured phrases match actions directly, **without an LLM**. Give a supported action a phrase you find natural, check its mapping with the test-only option, then use it in-game. Approximate-name assistance in normal conversation does not rewrite casting phrases.

The release includes **Iron’s Spellbooks** and **Ars Nouveau** add-ons for NeoForge 1.21.1. Configure supported spell actions or quick-cast pages according to their capabilities. Casting remains subject to the target mod, equipment, game state, and action requirements; not every spell, page, or version has been tested.

Speech packs are distributed through **[Baidu Netdisk](https://pan.baidu.com/s/1Attd0pmh-a8Bm9AV95nrvg?pwd=9999)**, access code **9999**. See [speech installation and resource usage](docs/SPEECH-PACKS.zh-CN.md) before choosing a tier.

## Keep the conversation going

Follow-up questions can refer to the object you were just discussing. Known game terms, manually configured aliases, and limited approximate-name matching help with unfamiliar mod names. Ambiguous matches may still need clarification, and speech hints depend on backend support.

Past inventory counts and machine readings remain historical snapshots. Ask for a new game query when you need current information.

You can also provide a specific file path or registered document folder for the assistant to read reference material or logs, suggest next steps, and save notes when explicitly requested and enabled. Diagnosis does not automatically edit game files or run repair scripts, and saved notes do not automatically enter the knowledge-pack index.

## Supported platforms

| Minecraft | Loader |
|---|---|
| 1.20.1 | Forge |
| 1.20.1 | Fabric |
| 1.21.1 | Fabric |
| 1.21.1 | NeoForge |
| 26.3 | NeoForge |

These are game-side platforms; the desktop release is Windows x64. Available add-ons and actions differ by platform. Check the [Bridge and add-on manifest](docs/releases/0.2.0-mods.zh-CN.md) for actual versions, dependencies, and validation boundaries.

## Getting started

1. **Download the [complete client](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/MiAXion-0.2.0-win-x64.zip)**, extract it to a separate folder, and run `client/MiAXion.exe`. Keep the assets and license files; do not copy only the EXE.
2. **Configure your model service.** Supply the endpoint, model, and credentials required by your provider. Start your local model server first if using one. Basic text conversation does not require a game connection; service costs and network requirements depend on your choice.
3. **Import knowledge packs for your game.** Use the knowledge library to import and enable the packs you need. Extract the marketplace ZIP first; import the five collection ZIPs directly.
4. **Connect Minecraft when needed.** Download the [Bridge and add-on collection](https://github.com/Kronos0523/MiAxion/releases/download/v0.2.0/MiAXion-JAR-collection.zip), then install only the JARs matching your instance and desired features. Do not mix loader or Minecraft versions.
5. **Add speech if you want it.** Get the appropriate speech pack from the Netdisk link above, then configure the tier, devices, and shortcut.

Client, Bridge, add-ons, knowledge packs, and speech packs are managed separately. Updating the client does not require replacing every component.

Detailed guides (Chinese): [Quick start](docs/QUICKSTART.zh-CN.md) · [Model setup](docs/MODEL-SETUP.zh-CN.md) · [Downloads](docs/DOWNLOADS.zh-CN.md) · [Upgrade and rollback](docs/UPGRADE.zh-CN.md) · [FAQ](docs/FAQ.zh-CN.md)

## Demos

Real screenshots and livestream clips are being prepared, focusing on knowledge retrieval and sources, follow-up questions, world/structure queries, machine state, and voice casting. No public playable demo is linked yet.

## Open-source scope

MiAxion publishes selected components: **[SDKs, Java/C# protocol code, and a read-only NeoForge example](components/public-contracts/README.zh-CN.md)**, plus an [integration skill](skills/bridge-mod-integration/SKILL.md). Selected first-party components use MIT; third-party materials retain their own licenses.

The full desktop client, model orchestration, memory/retrieval implementation, and full base Bridge implementation are outside this source release. Knowledge content, model weights, speech models, and reference audio have separate terms; they do not inherit the SDK's MIT license.

See [build instructions](components/public-contracts/docs/BUILD.zh-CN.md), [third-party notices](components/public-contracts/THIRD-PARTY-NOTICES.md), and [integration guidance](docs/BRIDGE-DEVELOPMENT.zh-CN.md). SDK/protocol JARs are developer libraries, not installable game mods. Linux standalone builds and protocol tests passed for the exported source; Windows standalone builds and the example's in-game behavior remain unverified.

## Limitations and feedback

Model answers can be wrong. Reference coverage, game versions, adapter support, and readable game state affect results. A successful connection or build does not prove every feature works on every modpack. The [0.2.0 release notes](docs/releases/0.2.0.zh-CN.md) describe the actual validation scope.

Report problems through [Issues](https://github.com/Kronos0523/MiAxion/issues), with versions and clear reproduction steps. Remove API keys, pairing credentials, and personal information; do not upload your entire `data` directory or chat database. See the [feedback guide](docs/FEEDBACK.zh-CN.md).
