# VulkanBoost 0.4.7 for Minecraft 1.21.11

UX update: make the NoHurtCam slider's meaning explicit in Russian and English.

## Changes

- Renamed the slider label from the ambiguous “NoHurtCam strength” to “Reduce hurt-camera tilt” / “Уменьшение наклона камеры от урона”.
- Added an explanation at the slider: 0% keeps vanilla behavior, 50% reduces the hurt-camera tilt by about half, and 100% disables that tilt.
- The wording clarifies that 100% means maximum reduction, not a stronger effect. This setting only adjusts the hurt-camera rotation targeted by the built-in mixin.
- Keeps the 0.4.6 NoHurtCam setup key, two real keybindings, conflict checking and all 17 bundled mod/library JARs unchanged.

## Compatibility and verification

- Minecraft 1.21.11, Fabric Loader 0.19.3+, Java 21 and a Vulkan 1.2+ compatible GPU/driver are required.
- CI builds the remapped JAR and validates nested component metadata, the built-in NoHurtCam classes, and matching English/Russian translation keys. It also checks that the UI explains 0%, 50% and 100%.
- CI success confirms the build and packaging checks, not a full in-game test. The slider's visual behavior and the full mod bundle still need testing in a real client.
- No optimizers were removed; C2ME still has no fabricated keybinding.

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

- Download `VulkanBoost-0.4.7-for-1.21.11.jar` and put it in `mods`.
- Remove separate copies of the bundled mods/libraries to avoid duplicate mod IDs. Fabric Loader itself is required and not bundled.
