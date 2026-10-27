# VulkanBoost 0.4.8 for Minecraft 1.21.11

Fix update: retarget NoHurtCam to the exact damage-tilt angle in Minecraft 1.21.11.

## Changes

- Replaced the broad `PoseStack.mulPose(Quaternionfc)` modifier with a targeted argument modifier for `Axis.rotationDegrees(float)` inside `GameRenderer.bobHurt`.
- Targets the third matching invocation (`ordinal = 2`) in the verified Minecraft 1.21.11 method, which is the directional hurt-tilt angle. The modifier no longer rewrites the death roll and the two surrounding yaw rotations.
- A reduction of 0% leaves the vanilla angle unchanged; 50% halves that angle; 100% sets it to zero.
- The injection declares `require = 1`, so if this target is not found on startup, Mixin fails loudly instead of silently applying no change.
- Kept all 17 bundled mod/library JARs, NoHurtCam settings, first-join key wizard, keybinds and translations.

## Research basis

- Cross-checked the 1.21.11 `GameRenderer.bobHurt` call order against the public Minecraft 1.21.11 source mirror.
- Compared the exact `Axis.rotationDegrees(float)` injection strategy with [BetterHurtCam's 1.21.11 source](https://github.com/uku3lig/betterhurtcam/tree/1.21.11). VulkanBoost retains its own implementation and settings.

## Verification

- GitHub Actions builds the Java 21 remapped JAR and validates nested mod metadata, the NoHurtCam class, the targeted method/ordinal, and Russian/English UI text.
- A successful build confirms compilation and packaging, not actual runtime behavior in a Minecraft client. The new hook still needs to be tested in-game.
- Minecraft 1.21.11, Fabric Loader 0.19.3+, Java 21 and a Vulkan 1.2+ compatible GPU/driver are required.
- No optimizers were removed. C2ME still has no fabricated keybinding.

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

- Download `VulkanBoost-0.4.8-for-1.21.11.jar` and put it in `mods`.
- Remove separate copies of the bundled mods/libraries to avoid duplicate mod IDs. Fabric Loader itself is required and is not bundled.
