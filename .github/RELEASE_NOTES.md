# VulkanBoost 0.2.0

Performance bundle update for Minecraft Java Edition 1.21.11 with Fabric Loader and VulkanMod.

## Included components

- VulkanMod 0.6.8+1.21.11
- Lithium 0.21.4
- FerriteCore 8.2.0-fabric
- Alternate Current 1.9.0
- BadOptimizations 2.4.1
- ModernFix-mVUS 5.26.3-build.2
- ScalableLux 0.1.6+fabric.c25518a
- Fast Noise 1.0.36+1.21.11
- Krypton 0.2.10

## Why these additions

- ModernFix-mVUS: performance, memory and loading fixes; the upstream release explicitly lists Minecraft 1.21.11.
- ScalableLux: faster lighting updates; the selected Fabric version supports Minecraft 1.21.9–1.21.11.
- Fast Noise: optimized vanilla world generation; the selected release explicitly lists Minecraft 1.21.11.
- Krypton: network-stack optimizations for multiplayer; the selected Fabric release supports Minecraft 1.21.9–1.21.11.

## Verification and limitations

- The Gradle build and nested-JAR metadata validation must pass in GitHub Actions before publication.
- The bundle has not completed a full in-game compatibility/stability test across GPU vendors. Back up worlds before testing.
- Native anti-aliasing, FSR upscaling and DLSS/DLAA are not implemented in this release.
- Do not install separate copies of bundled components alongside VulkanBoost.
- Requires Minecraft 1.21.11, Fabric Loader, Java 21 and a GPU/driver supporting Vulkan 1.2+.

## Compatibility choices

The package excludes Sodium, Iris, ImmediatelyFast and More Culling because they are renderer-focused and VulkanMod's compatibility notes warn about conflicts or visual issues. C2ME was not bundled in this release: the selected Fast Noise component already targets world-generation performance, and this alpha-stage chunk pipeline should be evaluated separately before combining overlapping chunk-generation optimizers.

See the README and third-party notices for installation details and licensing.
