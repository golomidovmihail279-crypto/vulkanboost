# VulkanBoost 0.4.3 for Minecraft 1.21.11

Feature release with a built-in, original NoHurtCam implementation.

## Changes

- Added an original client-side Mixin that cancels Minecraft's `GameRenderer.bobHurt` camera tilt.
- The built-in NoHurtCam disables the visual hurt-camera tilt; damage, knockback, invulnerability frames, and other camera effects are unchanged.
- No third-party NoHurtCam JAR is repackaged. The feature is part of the main VulkanBoost JAR.
- Keeps 17 nested mod/library JARs.
- Retains the 0.4.2 removal of DynamicRenderDistance and the unused Architectury API library, plus the 0.4.1 Optis/Osmium startup-crash fix.

## Built-in NoHurtCam

No separate mod download is required. The feature is enabled by default and disables the vanilla damage camera tilt on the client.

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
- Download `VulkanBoost-0.4.3-for-1.21.11.jar` and put it in the `mods` folder; remove separate copies of bundled mods/libraries to avoid duplicate mod IDs.
- Fabric Loader itself is still required and is not bundled.

## Verification and limitations

- CI builds the JAR and checks the 17 nested mod/library IDs plus the presence of the custom NoHurtCam mixin config and class.
- Build validation is not a substitute for an in-game test; camera behaviour and full VulkanMod compatibility should still be tested in a real client.
- Native anti-aliasing, FSR and DLSS/DLAA are not implemented yet.
- PerceptiFrame and Afterburner: Optimized remain future candidates pending exact reproducible version coordinates and VulkanMod runtime testing.
- ARR projects are not mirrored or redistributed; this NoHurtCam implementation is independent original project code.
