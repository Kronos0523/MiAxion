---
name: bridge-mod-integration
description: Build MiAXion game addons with the public SDK, review registration and protocol compatibility, or plan a new Minecraft/loader Bridge adaptation when host source is supplied. Does not install mods or start games without user instruction.
---

# MiAXion public Bridge integration

This public skill ships with the SDK/protocol candidate. Read [current scope and paths](../../README.zh-CN.md) and [integration contracts](../../docs/INTEGRATION.zh-CN.md) before generating a project. No private coordination folder, Graphify installation or client source is required.

## Choose the route

- **Addon**: use existing read-only, action, recipe or terminology interfaces. Start with the real [NeoForge read-only example](../../examples/neoforge-1.21.1-readonly/) and [addon checklist](references/addons.md). Infer exact native versions from supplied metadata; request only missing decisions. Do not invent registration calls, dynamic targets or arbitrary UI controls.
- **New base Bridge target**: read [platform checklist](references/platform-port.md). The current public candidate does not contain base Bridge implementation. Obtain the authorized host source first; do not claim this SDK alone can build a replacement Bridge.

Use exact resource IDs and native type identity; preserve unknown values, partial results and source coverage. Reuse host pairing, authorization, cancellation and current-session rules. Registration is not permission to execute. A local instance path grants version inspection, not installation or world changes.

Build with [standalone commands](../../docs/BUILD.zh-CN.md). Inspect final metadata, packaged resources, API class ownership and independent consumer results. Report compile, artifact verification, actual loader registration and gameplay separately. Existing test failures stay visible in [validation](../../docs/VALIDATION.zh-CN.md); do not delete tests to make a candidate green.

Keep official mod names and distributed artifact display names branded MiAXion. SDKs are compile-only dependencies, not installable mods. Preserve previous artifacts for rollback; do not bundle a separate startup/bootstrap mod as an invented prerequisite.
