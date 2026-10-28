# VulkanBoost 0.5 for Minecraft 1.21.11

This refreshed package preserves VulkanBoost's single-JAR optimization bundle and removes the experimental custom rendering integration that caused visual artifacts. The package does not include a custom upscaling or temporal anti-aliasing implementation.

## Changes

- Retains the pinned set of 19 bundled mod and library components.
- Removes custom rendering hooks and the prior resolution-scaling integration.
- Keeps the development focus on a stable baseline before adding any new image-quality pipeline.
- Validates nested-JAR integrity, component IDs and versions, and the expected project-owned runtime structure.

## Compatibility

- Minecraft Java Edition 1.21.11
- Java 21
- Fabric Loader 0.20.1 or compatible release meeting the declared minimum
- VulkanMod 0.6.8+1.21.11 and a GPU/driver supporting Vulkan 1.2+

## Installation

1. Download `VulkanBoost-0.5-for-1.21.11.jar`.
2. Place it in the Minecraft profile's `mods` folder.
3. Remove separate copies of any components already bundled inside VulkanBoost to avoid duplicate mod IDs.
4. Use a test profile and back up worlds before testing.

Fabric Loader and Java are installed separately. The bundle remains experimental; a successful build is not a substitute for in-game compatibility testing. Native-resolution anti-aliasing and temporal reconstruction are not implemented in this package.
