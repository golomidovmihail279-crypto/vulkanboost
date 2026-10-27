# VulkanBoost

**A single-JAR performance bundle for Minecraft Java Edition 1.21.11, built around VulkanMod.**

VulkanBoost packages selected upstream Fabric mods as nested JARs. Fabric Loader loads each component normally, so this project does **not** merge, shade, or rewrite their code.

> **Status: early alpha.** CI builds the package automatically. The mod combination still needs in-game testing on a clean Minecraft 1.21.11 profile with VulkanMod before it should be described as fully verified.

## Target platform

- Minecraft Java Edition **1.21.11**
- Fabric Loader **0.18.4 or newer**
- Java **21**
- A graphics device and driver supporting **Vulkan 1.2 or newer**
- VulkanMod **0.6.8+1.21.11** (included in the bundle)

## Included components

| Component | Version | Purpose | License |
|---|---|---|---|
| [VulkanMod](https://modrinth.com/mod/vulkanmod) | 0.6.8+1.21.11 | Replaces the OpenGL renderer with Vulkan | LGPL-3.0-only |
| [Lithium](https://modrinth.com/mod/lithium) | 0.21.4 | Optimizes game logic and ticks | LGPL-3.0-only |
| [FerriteCore](https://modrinth.com/mod/ferrite-core) | 8.2.0-fabric | Reduces memory usage | MIT |
| [Alternate Current](https://modrinth.com/mod/alternate-current) | 1.9.0 | Optimizes redstone dust calculations | MIT |
| [BadOptimizations](https://modrinth.com/mod/badoptimizations) | 2.4.1 | Provides non-rendering micro-optimizations | MIT |

The bundled components are intentionally conservative: shader/rendering mods that may call OpenGL directly are not included. Do not assume a component is compatible solely because it supports Minecraft 1.21.11; the combined profile needs real-world verification.

## Installation

1. Download the **vulkanboost** JAR from the successful GitHub Actions build artifact or a published release.
2. Put that one JAR into your Fabric instance's `mods` folder.
3. Remove separate copies of VulkanMod and the bundled mods from that instance to avoid duplicate mod IDs.
4. Make sure your GPU and driver support Vulkan 1.2 or newer, then launch Minecraft 1.21.11 with Fabric.

If the build artifact is not available yet, check the [Actions tab](https://github.com/golomidovmihail279-crypto/vulkanboost/actions) for the first build result.

## Anti-aliasing / jagged edges

**Native anti-aliasing is not implemented in the first alpha.** VulkanMod's current rendering pipeline uses a single-sample rasterization path. Proper MSAA requires compatible multisampled render targets and a resolve pass; changing a sample-count constant alone would break the rendering pipeline.

For an existing Vulkan shader pipeline, [Beryl](https://modrinth.com/mod/beryl) is the VulkanMod companion recommended by the VulkanMod project and targets Minecraft 1.21.11. It is licensed **ARR (All Rights Reserved)**, so VulkanBoost does not redistribute or embed it. Installing Beryl requires a separate JAR and is optional.

A native, redistributable FXAA/SMAA-style post-processing pass is tracked as a separate development task. It needs a VulkanMod-specific integration and in-game testing; this README will be updated when it is actually implemented.

## Compatibility notes

- Do **not** install Sodium alongside VulkanMod; they replace the rendering pipeline in incompatible ways.
- Do not assume Iris or ImmediatelyFast will work with VulkanMod. Check the current [VulkanMod compatibility notes](https://github.com/xCollateral/VulkanMod/wiki/Incompatible-mods) before adding other rendering mods.
- VulkanMod's compatibility list has historically been tested on older Minecraft versions. The exact 1.21.11 bundle must be checked in-game, especially after upstream updates.
- Back up important worlds before testing optimization bundles, and report crashes with the latest log.

## Build from source

Requirements: JDK 21 and Gradle 9.3.0.

```bash
gradle --no-daemon clean build
```

The installable remapped JAR is produced in `build/libs/`. A GitHub Actions workflow builds the project on pushes and pull requests and uploads the result as an artifact.

## Licenses

The MIT license at the repository root applies to **VulkanBoost's own project files only**. Third-party mods keep their own licenses. See [LICENSES/THIRD_PARTY_NOTICES.md](LICENSES/THIRD_PARTY_NOTICES.md) for the bundled components and upstream project pages.

## Development goals

- Verify the bundled mod combination on a clean Minecraft 1.21.11 + Fabric + VulkanMod profile.
- Add reproducible performance checks instead of claiming unmeasured FPS improvements.
- Investigate a Vulkan-native anti-aliasing post-process that can legally ship with this project.
