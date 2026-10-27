# VulkanBoost 0.4.9 for Minecraft 1.21.11

Integration and crash-fix update: use official BetterHurtCam and prevent the first-server setup screen from crashing while drawing its background.

## Changes

- Bundles the unmodified upstream BetterHurtCam 1.12.0+mc1.21.11 JAR and required Ukulib 1.10.2+1.21.11 JAR in the single VulkanBoost artifact.
- Removes the custom VulkanBoost hurt-camera mixin and its mixin registration so two competing implementations cannot target GameRenderer.
- Keeps the VulkanBoost first-multiplayer-join setup prompt and settings UI; they now read and write BetterHurtCam's actual config through its public manager.
- Migrates previous VulkanBoost reduction/enabled values into BetterHurtCam once. After migration, BetterHurtCam's `config/betterhurtcam.toml` is the single source of truth; setup-complete state remains in the legacy properties file.
- The reduction slider maps to BetterHurtCam's multiplier: 0% reduction = 1.0 (vanilla tilt), 50% = 0.5, 100% = 0.0 (tilt removed).
- Removes the duplicate VulkanBoost toggle binding. BetterHurtCam's real Toggle, Increase Strength and Decrease Strength actions appear in the standard Controls menu; VulkanBoost keeps its separate settings-opening key.
- Adds Russian translations for BetterHurtCam's UI and hotkeys.
- Fixes the 0.4.8 crash `IllegalStateException: Can only blur once per frame`. Both VulkanBoost screens now draw a normal dark background instead of invoking the blurred background path twice.
- Preserves all existing optimization mods and includes both upstream license notices.

## Upstream sources and licenses

- [BetterHurtCam 1.12.0+mc1.21.11](https://github.com/uku3lig/betterhurtcam/tree/1.21.11) — MIT
- [Ukulib 1.10.2+1.21.11](https://github.com/uku3lig/ukulib/releases/tag/1.10.2+1.21.11) — MPL-2.0

## Verification

- CI builds the Java 21 remapped JAR and checks all 19 nested components, including BetterHurtCam and Ukulib, the absence of the old competing mixin, the config migration bridge, Russian translations, and non-blurred rendering on both VulkanBoost screens.
- A successful build verifies compilation and packaging, not gameplay. BetterHurtCam's effect and the fix still need confirmation in a real Minecraft 1.21.11 client.
- Fabric Loader 0.19.3+, Java 21 and a Vulkan 1.2+ compatible GPU/driver are required. Fabric Loader itself is not bundled.

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
- BetterHurtCam 1.12.0+mc1.21.11
- Ukulib 1.10.2+1.21.11

## Installation

Download `VulkanBoost-0.4.9-for-1.21.11.jar` and place it in `mods`. Remove separate copies of mods already bundled in VulkanBoost to avoid duplicate mod IDs.
