# VulkanBoost 0.4.5 for Minecraft 1.21.11

Feature update: NoHurtCam first-join setup, enable/disable hotkey and native Minecraft keybind integration.

## Changes

- On the first multiplayer-server join, a setup screen asks which keyboard or mouse button should toggle NoHurtCam.
- The setup screen lets the player choose the NoHurtCam strength (0–100%) and whether the effect starts enabled.
- Added **Toggle NoHurtCam** and **Open NoHurtCam settings** to the normal **Options → Controls → VulkanBoost** category. Both are rebindable there; the settings screen remains bound to **P** by default.
- The toggle state, strength and setup-completed flag are saved in the NoHurtCam config file under the Minecraft config directory. The key mapping is saved by Minecraft in options.txt.
- The NoHurtCam slider now has an explicit enabled/disabled control. Pressing Esc cancels pending setup/settings changes; **Set up later** postpones first-time setup until the next multiplayer connection.
- All bundled mods' own key mappings remain available in Minecraft's normal Controls screen when those mods register them. C2ME 0.3.6.0 itself does not expose a user-facing keybinding for its automatic chunk tasks, so no nonfunctional placeholder hotkey was added.
- Keeps 17 nested mod/library JARs and retains the earlier removals of DynamicRenderDistance/Architectury and Optis/Osmium.

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
- Download VulkanBoost-0.4.5-for-1.21.11.jar and put it in mods; remove separate copies of bundled mods/libraries to avoid duplicate mod IDs.
- Fabric Loader itself is still required and is not bundled.
- On your first multiplayer-server join, use the setup screen to choose a NoHurtCam toggle key. Later, edit either VulkanBoost key in Options → Controls.

## Verification and limitations

- CI builds the JAR and validates the 17 nested mod/library IDs, forbidden component IDs, nested metadata, custom mixin config/classes, setup and settings screens, client initializer, and English/Russian translations.
- Build validation is not a substitute for an in-game test; camera behavior, the setup prompt and full VulkanMod compatibility should still be tested in a real client.
- Native anti-aliasing, FSR and DLSS/DLAA are not implemented yet.
- ARR projects are not mirrored or redistributed; this NoHurtCam implementation is independent original project code.
