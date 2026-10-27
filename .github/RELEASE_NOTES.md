# VulkanBoost 0.5 for Minecraft 1.21.11

A small first step toward in-game upscaling: VulkanBoost now bundles upstream RenderScale as a nested JAR, keeping the single-JAR install model.

## Changes

- Adds **RenderScale 1.4.0-alpha.1 for Minecraft 1.21.11** as an upstream nested dependency.
- Adds experimental **FSR 1 spatial upscaling**. RenderScale and its existing dependencies (Fabric API and Cloth Config API) are included inside the same VulkanBoost JAR.
- Keeps the bundle free of custom rendering code, new VulkanBoost entrypoints, mixins, wrappers, and resource overlays.
- Leaves the original BetterHurtCam and Ukulib JARs unchanged.
- DLSS/DLAA and temporal FSR 2+ are not part of version 0.5; they need a separate compatibility plan for VulkanMod's renderer.

## Verification

- CI builds the Java 21 remapped JAR and validates all 20 nested components, including the pinned BetterHurtCam, Ukulib, and RenderScale versions.
- CI rejects custom root runtime classes, entrypoints, mixins, or asset overlays.
- RenderScale's FSR 1 feature is marked experimental for Minecraft 1.21.11. Successful compilation/packaging does not prove it works correctly with this specific VulkanMod bundle; test startup, FSR enable/disable, image quality, FPS, menus/HUD, world changes, and exiting the game.
- This remains a single-JAR bundle. Fabric Loader itself is not included.

## Installation

Download `VulkanBoost-0.5-for-1.21.11.jar` and place it in `mods`. Remove separate copies of mods already bundled in VulkanBoost to avoid duplicate mod IDs.
