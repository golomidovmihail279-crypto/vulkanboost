# VulkanBoost 0.4.1 for Minecraft 1.21.11

Patch release removing Optis/Osmium after a client-startup crash on Minecraft 1.21.11.

## Fix

- Removed Optis 1.0.0 (loaded as `osmium`). Its entrypoint references `net.minecraft.client.texture.AbstractTexture`, which is not present under that name in this Minecraft version, causing `NoClassDefFoundError` during client initialization.
- Reduced the bundle from 20 to 19 top-level nested mod/library JARs.
- Added a CI guard so the known incompatible `osmium` component cannot be shipped accidentally in a later bundle build.

## Still included

- Fabric API 0.140.2+1.21.11
- VulkanMod 0.6.8+1.21.11
- Lithium 0.21.4
- FerriteCore 8.2.0-fabric
- Alternate Current 1.9.0
- BadOptimizations 2.4.1
- ModernFix-mVUS 5.26.3-build.2
- ScalableLux 0.1.6+fabric.c25518a
- Fast Noise 1.0.36+1.21.11
- Krypton 0.2.10
- C2ME 0.3.6.0.0
- DynamicRenderDistance 1.2.0
- Bura 0.1.0 alpha
- Epitaph Optimizer 1.0.0
- Dynamic FPS 3.11.4
- FastQuit 3.1.3+mc1.21.11
- Smart Particles 1.11.10
- Architectury API 19.0.1+fabric
- Cloth Config API 21.11.153+fabric

## Installation

- Minecraft 1.21.11, Fabric Loader 0.19.3+, Java 21 and a Vulkan 1.2+ compatible GPU/driver are required.
- Download `VulkanBoost-0.4.1-for-1.21.11.jar` and put only this bundle in the `mods` folder; do not also install duplicates of bundled mods/libraries.
- Fabric Loader itself is still required and is not bundled.

## Verification and limitations

- CI builds the JAR and validates 19 top-level component IDs, uniqueness, and recursive nested Fabric metadata.
- This is a broad experimental bundle; full in-game compatibility and performance testing is still needed. Back up worlds before testing.
- Native anti-aliasing, FSR and DLSS/DLAA are not implemented yet.
- PerceptiFrame and Afterburner: Optimized remain future candidates pending exact reproducible version coordinates and VulkanMod runtime testing.
- ARR projects are not redistributed; OptiLeaves remains excluded pending license clarification.
