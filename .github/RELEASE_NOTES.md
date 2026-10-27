# VulkanBoost 0.4.2 for Minecraft 1.21.11

Patch release with a simpler render-distance setup and optional visual extras listed separately.

## Changes

- Removed DynamicRenderDistance 1.2.0 so the bundle no longer automatically changes the player's configured view distance.
- Removed Architectury API 19.0.1+fabric, which was included only as a DynamicRenderDistance library.
- Reduced the bundle from 19 to 17 top-level nested mod/library JARs.
- Kept the previous 0.4.1 fix that removes Optis/Osmium, which caused a startup crash on Minecraft 1.21.11.
- Added official download links for optional No Hurt Cam and two resource packs. These remain separate from the optimizer JAR.

## Optional extras — separate downloads

- **No Hurt Cam 1.21.11 V1.0.2 (Fabric):** [official CurseForge file page](https://www.curseforge.com/minecraft/mc-mods/no-hurt-cam/files/7800959). The project is marked All Rights Reserved, so this release links to the upstream download instead of re-uploading the JAR. Install the downloaded JAR in `mods`.
- **Low Fire resource pack:** [official Modrinth page](https://modrinth.com/resourcepack/lower-fire). Install the downloaded ZIP in `resourcepacks`.
- **Truly Small Items resource pack:** [official Modrinth page](https://modrinth.com/resourcepack/small-items). Install the downloaded ZIP in `resourcepacks`.

These extras are optional and are not included inside `VulkanBoost-0.4.2-for-1.21.11.jar`.

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
- Optional resource packs go in `resourcepacks`; No Hurt Cam is an independently installed client-side mod.

## Verification and limitations

- CI builds the JAR and validates 17 top-level component IDs, uniqueness, recursive nested Fabric metadata, and the absence of Osmium, DynamicRenderDistance and Architectury API.
- This remains an experimental bundle; full in-game compatibility and performance testing is still needed. Back up worlds before testing.
- Native anti-aliasing, FSR and DLSS/DLAA are not implemented yet.
- PerceptiFrame and Afterburner: Optimized remain future candidates pending exact reproducible version coordinates and VulkanMod runtime testing.
- ARR projects are not mirrored or redistributed; OptiLeaves remains excluded pending license clarification.
