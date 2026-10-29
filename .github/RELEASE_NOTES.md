# VulkanBoost 0.6 for Minecraft 1.21.11

VulkanBoost 0.6 expands the single-JAR optimization bundle from 19 to 25 pinned top-level mod/library components. It adds Structure Layout Optimizer and Cull Leaves, plus their required libraries and the libraries needed for an optional Fast Item Frames companion.

## Changes

- Adds Structure Layout Optimizer 1.1.4 for Jigsaw structure and NBT-piece generation.
- Adds Cull Leaves 4.1.1.1 for leaf-face culling.
- Adds Resourceful Config 3.11.3 and MidnightLib 1.9.2 for the new optimizers.
- Adds Puzzles Lib 21.11.11 and Forge Config API Port 21.11.0 to support the optional Fast Item Frames add-on.
- Keeps all embedded components pinned to reproducible Modrinth Maven artifacts.
- CI checks nested-JAR integrity, component IDs and pinned versions, required root dependencies, and checker unit tests.

## Fast Item Frames licensing note

Fast Item Frames 21.11.1 for Minecraft 1.21.11 is not bundled. Its upstream repository has a separate LICENSE-ASSETS.md stating that its resource assets are All Rights Reserved. The complete JAR must not be redistributed as part of VulkanBoost without the author's permission. Download it from the official [Modrinth version page](https://modrinth.com/mod/fast-item-frames/version/4QQcgJi6) and install it separately. In multiplayer it is required on both client and server; follow its official dependency instructions for the dedicated server.

## Compatibility

- Minecraft Java Edition 1.21.11
- Java 21
- Fabric Loader 0.20.1 or compatible release meeting the declared minimum
- VulkanMod 0.6.8+1.21.11 and a GPU/driver supporting Vulkan 1.2+

## Installation

1. Download the VulkanBoost-0.6-for-1.21.11.jar file.
2. Place it in the Minecraft profile's mods folder.
3. Remove separate copies of any components already bundled inside VulkanBoost to avoid duplicate mod IDs.
4. Use a test profile and back up worlds before testing. Structure-generation mixins can conflict with other mods that alter the same generation path.

Fabric Loader and Java are installed separately. This release still does not implement a custom upscaler, FXAA/SMAA, DLSS/DLAA, or temporal AA. A successful build is not a substitute for in-game compatibility testing.
