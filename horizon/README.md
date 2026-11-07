# VulkanBoost — Horizon Edition

**Separate experimental native-Rust edition of VulkanBoost for Horizon.**

This folder is isolated from the existing Fabric / Minecraft 1.21.11 release. It does not change the Java mod or its release files.

## Target

- Horizon, the unofficial Rust reimplementation of Minecraft: Java Edition.
- Minecraft Java Edition data version **26.3**, which the current Horizon documentation lists as its supported game version.
- Native mods built as `.hnmod` bundles through `horizon-mixins`.
- Current compatibility baseline: Horizon hook API **167**. The checked source revision is pinned in `native/vulkanboost/compatibility.json`.

Horizon is not a Mojang or Microsoft product. Players need their own legally obtained Minecraft Java Edition files. Java/Fabric/Forge mod JARs do not run as code in Horizon.

## First slice: Background FPS cap

Version **0.1.0-dev** starts with one narrowly scoped feature:

- When a world is open and the Horizon window is unfocused or minimized, lower the frame-rate limit to a configurable cap (15 / 30 / 45 / 60 FPS; default 30).
- Never raise Horizon's own limit. If Horizon already selected a lower limit, keep it.
- Do not change the frame-rate limit while the game is focused.
- The setting is stored through Horizon's native settings API.
- The normal Horizon mod switch disables the feature.

This is a background resource-saving feature. **It does not claim to increase FPS while actively playing.** Horizon already has its own FPS counter, so VulkanBoost does not duplicate it.

## Development

The Rust policy logic has standalone unit tests:

```sh
cargo test --manifest-path horizon/native/vulkanboost/Cargo.toml
cargo fmt --manifest-path horizon/native/vulkanboost/Cargo.toml -- --check
```

To check the actual native integration, copy `horizon/native/vulkanboost/` into `horizon-rust/horizon-mods/native/vulkanboost/` and run from the `horizon-mods` repository:

```sh
python3 native/tools/native.py check vulkanboost
python3 native/tools/native.py package vulkanboost
```

The package step creates `native/target/vulkanboost-0.1.0.hnmod`. The GitHub Actions workflow performs these checks on this branch.

## Important limits

- The native-mod API is experimental and may change.
- Native mods are not sandboxed. This project deliberately avoids network access, process launching, broad file access, and third-party runtime dependencies.
- Horizon uses its own `wgpu` renderer and can select Vulkan or DirectX 12. This edition must not assume that every Windows installation is using Vulkan.
- No FSR, DLSS, temporal reconstruction, custom anti-aliasing, or renderer patching is included.
- Do not call this a stable release until the built `.hnmod` has been installed and tested in the actual Horizon client.
