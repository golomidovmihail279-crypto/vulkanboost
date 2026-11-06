# VulkanBoost

**A first-party Fabric client performance mod for Minecraft 1.21.11, built around VulkanMod.**

VulkanBoost combines a curated set of upstream optimization components with its own in-game Control Center, performance monitoring, reversible tuning, and optional memory-pressure assistance. The goal is not only to put several mods into one JAR, but to give players one reliable place to inspect and manage the parts VulkanBoost can safely control.

## What VulkanBoost provides

- **One installable JAR:** 25 pinned top-level mod and library components.
- **Control Center:** open it with the configurable `O` key binding. Screens use pagination and responsive positioning.
- **Complete component registry:** inspect the status of all 25 bundled components. Only Cull Leaves and Dynamic FPS currently have verified live switches; other components remain informational until a safe upstream control API is verified.
- **Performance monitor:** live FPS, frame time, Java heap usage, and loaded Fabric mod count. VulkanBoost does not invent per-mod CPU/GPU attribution.
- **Auto-tuning:** Balanced, FPS Priority, and Lower Memory profiles with preview, apply, and undo. The automatic recommendation uses logical CPU count; JVM `-Xmx` is displayed but is not treated as physical RAM.
- **Smart memory assistance:** optional and disabled by default. After sustained heap pressure it can lower render/simulation distances and restore them when pressure subsides, unless the player changes those values manually. It never calls `System.gc()`.
- **Reproducible validation:** CI builds the mod, verifies nested-JAR metadata and component IDs, checks first-party runtime classes, and runs Python/Java regression tests.

## Release status

Version **1.0** is published as the **Latest** non-prerelease GitHub release. This is the repository's publication status, not a claim that every manual Minecraft acceptance test has passed. The remaining in-game checks are tracked in the [release checklist](docs/VULKANBOOST_1.0_RELEASE_CHECKLIST.md); report reproducible issues before treating every feature as verified.

## Platform

- Minecraft Java Edition **1.21.11**
- Fabric Loader **0.20.1** (tested build environment; declared minimum: `>=0.19.3`)
- Java **21**
- GPU and driver supporting **Vulkan 1.2+**
- VulkanMod **0.6.8+1.21.11** and Fabric API are included

## Bundled components

| Component | Version | Purpose |
|---|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.140.2+1.21.11 | Fabric compatibility APIs |
| [VulkanMod](https://modrinth.com/mod/vulkanmod) | 0.6.8+1.21.11 | Vulkan rendering backend |
| [Lithium](https://modrinth.com/mod/lithium) | 0.21.4+mc1.21.11 | Game logic and tick optimization |
| [FerriteCore](https://modrinth.com/mod/ferrite-core) | 8.2.0-fabric | Memory optimization |
| [Alternate Current](https://modrinth.com/mod/alternate-current) | 1.9.0 | Redstone calculations |
| [BadOptimizations](https://modrinth.com/mod/badoptimizations) | 2.4.1 | General micro-optimizations |
| [ModernFix-mVUS](https://modrinth.com/mod/modernfix-mvus) | 5.26.3-build.2 | Memory/loading fixes |
| [ScalableLux](https://modrinth.com/mod/scalablelux) | 0.1.6+fabric.c25518a | Lighting updates |
| [Fast Noise](https://modrinth.com/mod/zfastnoise) | 1.0.36+1.21.11 | World generation |
| [Krypton](https://modrinth.com/mod/krypton) | 0.2.10 | Network stack |
| [C2ME](https://modrinth.com/mod/c2me-fabric) | 0.3.6.0.0 | Concurrent chunk management |
| [Bura](https://modrinth.com/mod/bura) | 0.1.0 alpha | Chunk compilation/upload scheduling; pinned pending VulkanMod compatibility testing |
| [Epitaph Optimizer](https://modrinth.com/mod/epitaph-optimizer) | 1.0.0 | Performance-friendly video settings |
| [Dynamic FPS](https://modrinth.com/mod/dynamic-fps) | 3.11.4 | Reduces background resource use |
| [FastQuit](https://modrinth.com/mod/fastquit) | 3.1.3+mc1.21.11 | Lets saves finish after returning to menu |
| [Smart Particles](https://modrinth.com/mod/smart-particles) | 1.11.10 | Particle rendering/limits |
| [Cloth Config API](https://modrinth.com/mod/cloth-config) | 21.11.153 | Shared configuration library |
| [BetterHurtCam](https://github.com/uku3lig/betterhurtcam/tree/1.21.11) | 1.12.0+mc1.21.11 | Damage-camera effects |
| [Ukulib](https://github.com/uku3lig/ukulib/releases/tag/1.10.2+1.21.11) | 1.10.2+1.21.11 | BetterHurtCam dependency |
| [Structure Layout Optimizer](https://modrinth.com/mod/structure-layout-optimizer/version/YDgvKeWI) | 1.1.4 (1.21.11 Fabric) | Optimizes Jigsaw structure and NBT-piece generation |
| [Resourceful Config](https://modrinth.com/mod/resourceful-config/version/8bR7M6K7) | 3.11.3 | Structure Layout Optimizer configuration library |
| [Cull Leaves](https://modrinth.com/mod/cull-leaves/version/yrL6pwHZ) | 4.1.1.1 (1.21.11 Fabric) | Avoids drawing hidden faces of leaf blocks |
| [MidnightLib](https://modrinth.com/mod/midnightlib/version/OeTayxh3) | 1.9.2+1.21.11 Fabric | Cull Leaves configuration library |
| [Puzzles Lib](https://modrinth.com/mod/puzzles-lib/version/O1SlsQzW) | 21.11.11 | Dependency for optional Fast Item Frames add-on |
| [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port/version/Idk1R3dD) | 21.11.0 Fabric | Dependency for optional Fast Item Frames add-on |

## Optional add-on: Fast Item Frames

Fast Item Frames is not embedded in this JAR. Its upstream repository separately marks its resource assets as All Rights Reserved, so the complete mod cannot be repackaged in this bundle without the author's permission. Download the official [Fast Item Frames 21.11.1 build for Minecraft 1.21.11](https://modrinth.com/mod/fast-item-frames/version/4QQcgJi6) and install it as a separate mod.

The add-on is required on both client and server in multiplayer. It replaces item-frame entities with block-based frames, so back up worlds before using its conversion options. The bundle includes Puzzles Lib and Forge Config API Port, but for a dedicated server install Fast Item Frames and the dependencies listed on its official page directly on the server as well.

## Compatibility notes

C2ME, Bura and Fast Noise affect overlapping chunk-generation or rendering-scheduling areas. Bura 0.2.0 is available for Minecraft 1.21.11, but its published tested-combination list does not include VulkanMod and the project still documents alpha-stage culling limitations; VulkanBoost therefore keeps the pinned 0.1.0 artifact until the newer build is tested with VulkanMod. Do not update it solely because a newer version exists. Some upstream nested mods also package overlapping library versions; do not manually delete or deduplicate those libraries without a Fabric Loader runtime log proving a conflict. Structure Layout Optimizer changes structure-generation code paths, so create and save a new test world before using the bundle with important worlds. Back up important saves before installing or updating a large optimization bundle.

## Graphics roadmap (not included in v1.0)

Future graphics work requires a separately designed and tested VulkanMod integration. This release does **not** include FSR, DLSS, DLAA, temporal anti-aliasing, FXAA/SMAA, or custom upscaling. Historical renderer experiments are not part of the current build.

## Installation

1. Download the supported release from [GitHub Releases](https://github.com/golomidovmihail279-crypto/vulkanboost/releases).
2. Put the single VulkanBoost JAR in the profile's `mods` folder.
3. Remove separate copies of bundled mod IDs to avoid duplicate components.
4. Use Java 21, Minecraft 1.21.11 and a compatible Fabric Loader.

Fabric Loader and Java are not included inside the bundle.

## License

The MIT project license applies to VulkanBoost's own files. Each nested component retains its original license; see [Third-party notices](LICENSES/THIRD_PARTY_NOTICES.md). Do not redistribute any component beyond the rights granted by its own license.

## Build and validation

Requirements: JDK 21 and Gradle 9.5.0.

```bash
gradle --no-daemon clean build
python3 scripts/check_bundle.py build/libs/vulkanboost-1.0.jar
python3 -m unittest discover -s scripts -p "test_*.py"
```

CI also compiles and runs dependency-free Java regression tests for transactional option changes, small-screen layout, and memory-distance policy. These checks do not replace a live Minecraft test.
