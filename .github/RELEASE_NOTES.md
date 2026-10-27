# VulkanBoost 0.3.0 for Minecraft 1.21.11

The main goal of this release is simple installation: **one JAR in the `mods` folder**, with Fabric API included alongside the optimization components.

## Included top-level components

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

## Installation

- Requires Minecraft 1.21.11, Fabric Loader 0.18.4 or newer, Java 21 and a GPU/driver supporting Vulkan 1.2+.
- Download `VulkanBoost-0.3.0-for-1.21.11.jar` and put it in the `mods` folder.
- Do not also install separate copies of Fabric API, VulkanMod, or the other bundled components; duplicate IDs can prevent launch.
- Fabric Loader itself is still required; it is installed by the launcher/profile, not bundled inside the mod.

## Verification and limitations

- GitHub Actions builds the remapped JAR and validates the ten top-level bundled mod IDs and nested Fabric metadata.
- The bundle still needs full in-game compatibility and stability testing across GPU vendors. Please test on a disposable profile and back up worlds.
- Native anti-aliasing, FSR upscaling and DLSS/DLAA are not implemented in this release.

See the README and third-party notices for installation details and licenses.
