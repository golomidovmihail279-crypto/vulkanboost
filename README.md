# VulkanBoost

**A single-JAR experimental performance bundle for Minecraft Java Edition 1.21.11, built around VulkanMod.**

This release contains 17 top-level mod/library JARs: the core optimizers, chunk-generation experiments, and entity/particle/texture optimizers. Automatic render-distance adjustment has been removed in 0.4.2.

> **Important:** CI validates packaging and Fabric metadata only. This deliberately aggressive combination has not been tested in-game as a whole. C2ME, Bura and Fast Noise touch overlapping chunk/rendering paths; be ready to disable the experimental additions if you see crashes, missing chunks, flicker, or worse frame pacing.

## Platform

- Minecraft Java Edition **1.21.11**
- Fabric Loader **0.19.3 or newer** (required by Epitaph Optimizer)
- Java **21**
- GPU/driver supporting **Vulkan 1.2+**
- VulkanMod **0.6.8+1.21.11** and Fabric API are included

## Bundled components

| Component | Version | Purpose | License |
|---|---|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.140.2+1.21.11 | Fabric compatibility APIs | Apache-2.0 |
| [VulkanMod](https://modrinth.com/mod/vulkanmod) | 0.6.8+1.21.11 | Vulkan rendering backend | LGPL-3.0-only |
| [Lithium](https://modrinth.com/mod/lithium) | 0.21.4 | Game logic/tick optimization | LGPL-3.0-only |
| [FerriteCore](https://modrinth.com/mod/ferrite-core) | 8.2.0-fabric | Reduces memory usage | MIT |
| [Alternate Current](https://modrinth.com/mod/alternate-current) | 1.9.0 | Redstone calculations | MIT |
| [BadOptimizations](https://modrinth.com/mod/badoptimizations) | 2.4.1 | Non-rendering micro-optimizations | MIT |
| [ModernFix-mVUS](https://modrinth.com/mod/modernfix-mvus) | 5.26.3-build.2 | Memory/loading fixes | LGPL-3.0-only |
| [ScalableLux](https://modrinth.com/mod/scalablelux) | 0.1.6+fabric.c25518a | Lighting updates | LGPL-3.0-only |
| [Fast Noise](https://modrinth.com/mod/zfastnoise) | 1.0.36+1.21.11 | World generation | MPL-2.0 |
| [Krypton](https://modrinth.com/mod/krypton) | 0.2.10 | Network stack | LGPL-3.0-only |
| [C2ME](https://modrinth.com/mod/c2me-fabric) | 0.3.6.0.0 | Concurrent chunk management | MIT |
| [Bura](https://modrinth.com/mod/bura) | 0.1.0 alpha | Chunk compilation/upload scheduling | MIT |
| [Epitaph Optimizer](https://modrinth.com/mod/epitaph-optimizer) | 1.0.0* | Applies performance-friendly video settings | CC0-1.0 |
| [Dynamic FPS](https://modrinth.com/mod/dynamic-fps) | 3.11.4 | Reduces background resource use | MIT |
| [FastQuit](https://modrinth.com/mod/fastquit) | 3.1.3+mc1.21.11 | Returns to menu while save finishes | MIT |
| [Smart Particles](https://modrinth.com/mod/smart-particles) | 1.11.10 | Particle rendering/limits | MIT |
| [Cloth Config API](https://modrinth.com/mod/cloth-config) | 21.11.153+fabric | Required library for FastQuit | LGPL-3.0-only |

PerceptiFrame and Afterburner: Optimized remain tracked as candidates below, but are not inside this JAR: the first artifact version pin and the second artifact coordinate did not resolve in Gradle. I will not ship them until exact, reproducible versions are pinned.

## Installation

1. Download **VulkanBoost-0.4.2-for-1.21.11.jar** from [GitHub Releases](https://github.com/golomidovmihail279-crypto/vulkanboost/releases).
2. Put that one JAR in your Fabric profile's `mods` folder.
3. Remove separate copies of Fabric API, VulkanMod, the bundled optimization mods and their bundled libraries to avoid duplicate mod IDs.
4. Use Fabric Loader **0.19.3 or newer**, Java 21, and Minecraft 1.21.11.

Fabric Loader itself is still required; it is not inside the mod JAR.

## Test plan and known risks

Version 0.4.1 removed Optis/Osmium after a reproducible startup crash on Minecraft 1.21.11. Version 0.4.2 also removes DynamicRenderDistance and its now-unused Architectury API library. The remaining broad experimental bundle is not a promise of more FPS. Benchmark the same world, position, render distance and video settings against 0.3.0. Check startup, world creation, chunk generation, chunk uploads, visibility/flicker, save/quit, entity shadows, particles, texture-heavy resource packs, multiplayer and background behavior.

- C2ME, Bura and Fast Noise overlap in chunk/render scheduling. If issues occur, the first diagnostic build should remove one at a time.
- Optis was removed in 0.4.1 after a startup crash in OsmiumClient: it references the missing Minecraft class `net.minecraft.client.texture.AbstractTexture` on Minecraft 1.21.11.
- Automatic render-distance adjustment is intentionally excluded; VulkanBoost leaves the configured view distance alone.
- Dynamic FPS and FastQuit are convenience/background/save optimizers, not guaranteed active-game FPS boosters.
- PerceptiFrame is a rendering-detail candidate and Afterburner: Optimized modifies chunk drawing/culling. Both need exact build coordinates and separate VulkanMod runtime testing before bundling.
- Native anti-aliasing, FSR upscaling and DLSS/DLAA are **not** implemented yet.

## License notes

The MIT project license applies only to VulkanBoost's own files. Each nested component retains its own license; see [LICENSES/THIRD_PARTY_NOTICES.md](LICENSES/THIRD_PARTY_NOTICES.md).

Three candidates were deliberately **not** redistributed: StackCluster Optimizer, OptimizeMod and NoRender+ list ARR (All Rights Reserved). OptiLeaves is also held back because its listing/license information appears inconsistent and it modifies render/culling paths. They can be reconsidered after permission/license clarification and separate compatibility testing.

## Build

Requirements: JDK 21 and Gradle 9.5.0.

```bash
gradle --no-daemon clean build
```

GitHub Actions validates the single installable JAR, the expected top-level component count, unique top-level mod IDs, and recursive nested Fabric metadata. A normal Latest Release is published only after CI succeeds for the current `main` commit.
