# VulkanBoost 0.4.2 for Minecraft 1.21.11

Patch release removing automatic render-distance adjustment.

## Changes

- Removed DynamicRenderDistance 1.2.0 so the bundle no longer automatically changes the player's configured view distance.
- Removed Architectury API 19.0.1+fabric, which was included only as a DynamicRenderDistance library.
- Reduced the bundle from 19 to 17 top-level nested mod/library JARs.
- Kept the previous 0.4.1 fix that removes Optis/Osmium, which caused a startup crash on Minecraft 1.21.11.

## Bundled components

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
- Bura 0.1.0 alpha
- Epitaph Optimizer 1.0.0
- Dynamic FPS 3.11.4
- FastQuit 3.1.3+mc1.21.11
- Smart Particles 1.11.10
- Cloth Config API 21.11.153+fabric

## Installation

- Minecraft 1.21.11, Fabric Loader 0.19.3+, Java 21 and a Vulkan 1.2+ compatible GPU/driver are required.
- Download `VulkanBoost-0.4.2-for-1.21.11.jar` and put it in the `mods` folder; remove separate copies of bundled mods/libraries to avoid duplicate mod IDs.
- Fabric Loader itself is still required and is not bundled.

## Verification and limitations

- CI builds the JAR and validates 17 top-level component IDs, uniqueness, recursive nested Fabric metadata, and the absence of Osmium, DynamicRenderDistance and Architectury API.
- This remains an experimental bundle; full in-game compatibility and performance testing is still needed. Back up worlds before testing.
- Native anti-aliasing, FSR and DLSS/DLAA are not implemented yet.
- PerceptiFrame and Afterburner: Optimized remain future candidates pending exact reproducible version coordinates and VulkanMod runtime testing.
- ARR projects are not mirrored or redistributed; OptiLeaves remains excluded pending license clarification.
