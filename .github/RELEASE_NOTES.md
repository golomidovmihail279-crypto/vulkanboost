# VulkanBoost 0.4.6 for Minecraft 1.21.11

Feature update: align the first-server-join NoHurtCam wizard with the settings-opening key and improve conflict handling.

## Changes

- On the first multiplayer-server connection before setup is completed, the wizard asks which keyboard or mouse button should open NoHurtCam settings.
- The wizard still lets the player set NoHurtCam strength (0–100%) and choose whether the effect starts enabled.
- The **Open NoHurtCam settings** binding can be reassigned in the setup wizard and later in the normal **Options → Controls → VulkanBoost** category. It defaults to **P** when no other key is configured.
- The wizard checks the chosen key against existing vanilla and mod key mappings and asks for another key on a conflict.
- **Toggle NoHurtCam** remains a separate, real action available in the standard Controls screen. No toggle shortcut is fabricated for optimizers that have no key-driven action.
- The setup-completed flag and NoHurtCam strength/enabled state remain persisted in the config. Minecraft continues to store key mappings in `options.txt`.
- The wizard can be postponed until the next multiplayer connection and is not reopened on every screen transition during the current connection.
- Retains all 17 bundled mod/library JARs and the existing NoHurtCam implementation and settings.

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
- Download `VulkanBoost-0.4.6-for-1.21.11.jar` and put it in `mods`; remove separate copies of bundled mods/libraries to avoid duplicate mod IDs.
- Fabric Loader itself is still required and is not bundled.
- The first multiplayer setup asks for the key that opens NoHurtCam settings; both VulkanBoost keybindings remain rebindable later via **Options → Controls**.

## Verification and limitations

- CI builds the remapped JAR and validates the 17 nested mod/library IDs, nested metadata, custom mixin config/classes, settings/setup classes, source-level keybinding intent, and matching English/Russian translation keys.
- Build validation is not a substitute for an in-game test. The setup prompt, key conflicts, camera behavior and complete VulkanMod bundle still require testing in a real client.
- C2ME 0.3.6.0 is an automatic chunk-performance engine; it does not warrant a fabricated hotkey in VulkanBoost. Bundled mods' own keybindings, when registered, remain in their normal Minecraft Controls category.
- Native anti-aliasing, FSR upscaling and DLSS/DLAA are not implemented yet.
- ARR projects are not mirrored or redistributed; NoHurtCam remains original project code.
