# VulkanBoost

**A single-JAR performance bundle for Minecraft Java Edition 1.21.11, built around VulkanMod.**

This branch establishes a clean foundation for research into native-resolution temporal anti-aliasing and image reconstruction. It does not ship a reconstruction filter yet: the goal is to understand the renderer's frame lifecycle, available buffers, camera transforms, and object-rendering paths before adding an algorithm.

## Current state

- The one-JAR bundle retains 19 pinned top-level mod/library components.
- Custom rendering hooks and resolution-scaling integration are not part of this branch.
- No rendering option is advertised as working when no implementation exists.
- Historical published releases remain separately versioned; this source branch is the clean development base.
- Temporal reconstruction is a research target, not yet an implemented or validated feature.

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

The aggregate remains experimental. C2ME, Bura and Fast Noise affect overlapping chunk-generation or rendering-scheduling areas; troubleshoot them independently if crashes, missing chunks, flicker or frame pacing issues occur.

## Temporal reconstruction research

The target is **stable, clean image quality at native output resolution first**, not an automatic reduction to 70–77% render scale. A temporal approach may use sub-pixel camera jitter and information from previous frames to reduce edge stair-stepping and shimmer. It must also reject stale history when camera motion, animated entities, particles, transparency, or newly visible pixels make historical samples invalid.

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
python3 scripts/check_bundle.py build/libs/vulkanboost-0.5.jar
python3 -m unittest discover -s scripts -p "test_*.py"
```

CI verifies bundle metadata, nested JAR integrity, pinned core components, and the absence of unexpected project-owned rendering hooks. It does not claim that temporal reconstruction is implemented or that graphics quality has passed an in-game test.
