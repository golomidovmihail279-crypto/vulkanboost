# VulkanBoost

**A single-JAR performance bundle for Minecraft Java Edition 1.21.11, built around VulkanMod.**

This branch establishes a clean foundation for research into native-resolution temporal anti-aliasing and image reconstruction. It does not ship a reconstruction filter yet: the goal is to understand the renderer's frame lifecycle, available buffers, camera transforms, and object-rendering paths before adding an algorithm.

## Current state

- The one-JAR bundle retains 25 pinned top-level mod/library components.
- Custom rendering hooks and resolution-scaling integration are not part of this branch.
- No rendering option is advertised as working when no implementation exists.
- The v0.6 package is the expanded optimization bundle; image reconstruction is not implemented yet.
- Temporal reconstruction is a research target, not yet an implemented or validated feature.
- The `feature/control-center-v0.7` branch contains the first-party **VulkanBoost Control Center** and its `0.7.1` safety update: transactional distance changes, rollback retry handling, and protection against overwriting settings changed after a preview or profile application. In-game validation is still required before a public release.

## Platform

- Minecraft Java Edition **1.21.11**
- Fabric Loader **0.20.1** (minimum declared by the project remains `>=0.19.3`)
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
| [Bura](https://modrinth.com/mod/bura) | 0.1.0 alpha | Chunk compilation/upload scheduling |
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

The aggregate remains experimental. C2ME, Bura and Fast Noise affect overlapping chunk-generation or rendering-scheduling areas; troubleshoot them independently if crashes, missing chunks, flicker or frame pacing issues occur. Structure Layout Optimizer changes structure-generation code paths; test new-world creation and saving before using the pack with important worlds.


## Temporal reconstruction research

The target is **stable, clean image quality at native output resolution first**. A temporal approach may use sub-pixel camera jitter and information from previous frames to reduce edge stair-stepping and shimmer. It must also reject stale history when camera motion, animated entities, particles, transparency, or newly visible pixels make historical samples invalid.

The plan, related projects, open technical questions, and validation matrix are documented in [Temporal Reconstruction Research](docs/TEMPORAL_RECONSTRUCTION_RESEARCH.md).

No temporal reconstruction is implemented by this branch yet. A clean package build is not proof of visual correctness; every future rendering change requires in-game validation on Minecraft 1.21.11 + VulkanMod.

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
python3 scripts/check_bundle.py build/libs/vulkanboost-0.7.1.jar
python3 -m unittest discover -s scripts -p "test_*.py"
```

CI verifies bundle version and metadata, nested JAR integrity, pinned core components, and the absence of unexpected project-owned rendering hooks. It also compiles and runs dependency-free Java regression tests for transactional option changes, partial write failures, save failures, manual overrides, and retryable rollback. These checks do not replace a live Minecraft test.
