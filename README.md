# VulkanBoost

**A single-JAR performance bundle for Minecraft Java Edition 1.21.11, built around VulkanMod.**

VulkanBoost packages selected upstream Fabric mods as nested JARs. Fabric Loader discovers the bundled components through their own metadata; the projects are not merged, shaded, or rewritten.

> **Release status:** the JAR and nested-mod metadata are validated in CI. The combined mod set still needs full in-game compatibility and stability testing across GPU vendors.

## Target platform

- Minecraft Java Edition **1.21.11**
- Fabric Loader **0.18.4 or newer** (still required in the game profile)
- Java **21**
- A graphics device and driver supporting **Vulkan 1.2 or newer**
- VulkanMod **0.6.8+1.21.11** and Fabric API (both included)

## Included components

| Component | Version | Purpose | License |
|---|---|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.140.2+1.21.11 | Hooks and compatibility APIs for Fabric mods | Apache-2.0 |
| [VulkanMod](https://modrinth.com/mod/vulkanmod) | 0.6.8+1.21.11 | Vulkan rendering backend | LGPL-3.0-only |
| [Lithium](https://modrinth.com/mod/lithium) | 0.21.4 | Optimizes game logic and ticks | LGPL-3.0-only |
| [FerriteCore](https://modrinth.com/mod/ferrite-core) | 8.2.0-fabric | Reduces memory use | MIT |
| [Alternate Current](https://modrinth.com/mod/alternate-current) | 1.9.0 | Optimizes redstone dust calculations | MIT |
| [BadOptimizations](https://modrinth.com/mod/badoptimizations) | 2.4.1 | Non-rendering micro-optimizations | MIT |
| [ModernFix-mVUS](https://modrinth.com/mod/modernfix-mvus) | 5.26.3-build.2 | Memory, loading and bug-fix optimizations | LGPL-3.0-only |
| [ScalableLux](https://modrinth.com/mod/scalablelux) | 0.1.6+fabric.c25518a | Faster lighting updates | LGPL-3.0-only |
| [Fast Noise](https://modrinth.com/mod/zfastnoise) | 1.0.36+1.21.11 | World-generation optimizations | MPL-2.0 |
| [Krypton](https://modrinth.com/mod/krypton) | 0.2.10 | Network-stack optimizations for multiplayer | LGPL-3.0-only |

The 0.3.0 release adds Fabric API so it does not need to be downloaded separately. The bundle contains ten top-level components. This does **not** guarantee that every combination will work perfectly with VulkanMod; full in-game testing is still needed.

## Installation

1. Download **VulkanBoost-0.3.0-for-1.21.11.jar** from [GitHub Releases](https://github.com/golomidovmihail279-crypto/vulkanboost/releases).
2. Put that one JAR into your Fabric instance's `mods` folder.
3. Remove separate copies of Fabric API, VulkanMod and the other bundled mods to avoid duplicate mod IDs.
4. Ensure your game profile already uses Fabric Loader and your GPU/driver supports Vulkan 1.2 or newer, then launch Minecraft 1.21.11.

The goal is one JAR in `mods`; **Fabric Loader itself is still required** and must be installed by your launcher/profile. The [Actions tab](https://github.com/golomidovmihail279-crypto/vulkanboost/actions) contains build and bundle-validation logs.

## Anti-aliasing / jagged edges

**Native anti-aliasing is not implemented in 0.3.0.** VulkanMod's current pipeline uses single-sample rasterization. Proper MSAA requires compatible multisampled render targets and a resolve pass; changing a sample-count constant alone would not be sufficient.

For an existing Vulkan shader pipeline, [Beryl](https://modrinth.com/mod/beryl) is the companion recommended by the VulkanMod project for supported versions. It is licensed **ARR (All Rights Reserved)**, so VulkanBoost does not redistribute or embed it. Installing Beryl requires a separate JAR and is optional.

A native, redistributable FXAA/SMAA-style post-processing pass remains a separate task. FSR-style upscaling and NVIDIA DLSS/DLAA feasibility are tracked in [issue #2](https://github.com/golomidovmihail279-crypto/vulkanboost/issues/2) and [issue #3](https://github.com/golomidovmihail279-crypto/vulkanboost/issues/3). None of these features is included in this release.

## Compatibility decisions

- Do **not** install Sodium alongside VulkanMod; they replace the rendering pipeline in incompatible ways.
- Iris depends on Sodium and is not included.
- ImmediatelyFast and More Culling are not included because VulkanMod's compatibility notes warn about crashes or visual issues with listed versions. Those notes are older and are not a substitute for testing Minecraft 1.21.11.
- C2ME was not bundled in 0.3.0. Its 1.21.11 releases are alpha builds, and Fast Noise was chosen for world-generation optimization rather than combining overlapping chunk-generation systems before testing.
- The compatibility of the complete ten-component bundle has not been validated in a live Minecraft session. Back up worlds before testing and report issues with the latest log.

## Build from source

Requirements: JDK 21 and Gradle 9.5.0.

```bash
gradle --no-daemon clean build
```

The internal remapped JAR is produced in `build/libs/`. GitHub Actions builds the project, recursively validates the nested mod metadata, checks the expected ten top-level components and uploads the artifact. After a successful build of the current `main` commit, a normal Latest GitHub Release is published with the user-facing filename `VulkanBoost-0.3.0-for-1.21.11.jar`.

## Licenses

The MIT license at the repository root applies to **VulkanBoost's own project files only**. Third-party components retain their own licenses. See [LICENSES/THIRD_PARTY_NOTICES.md](LICENSES/THIRD_PARTY_NOTICES.md) for project links and license details.

## Development goals

- Test the combined mod set on a clean Minecraft 1.21.11 + Fabric + VulkanMod profile.
- Add reproducible benchmarks rather than making unmeasured FPS claims.
- Implement and test a Vulkan-native anti-aliasing/upscaling path.
