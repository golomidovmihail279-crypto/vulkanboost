# VulkanBoost 0.4.4 for Minecraft 1.21.11

Feature update: configurable NoHurtCam slider with a persistent local setting.

## Changes

- Added an in-game NoHurtCam settings screen, opened with **P** by default (rebindable in Options → Controls).
- Added a **0–100% slider**: 0% preserves vanilla hurt-camera shake; 100% removes the hurt-camera tilt.
- The slider previews changes while dragging and saves them to `config/vulkanboost-no-hurt-cam.properties` when **Done** is clicked. Pressing Esc cancels the edit.
- The original Mixin proportionally scales the hurt-camera rotation quaternions; no third-party NoHurtCam JAR is repackaged.
- Keeps 17 nested mod/library JARs and retains the 0.4.2 removal of DynamicRenderDistance and its unused Architectury API library, plus the 0.4.1 Optis/Osmium startup-crash fix.

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
- Download `VulkanBoost-0.4.4-for-1.21.11.jar` and put it in `mods`; remove separate copies of bundled mods/libraries to avoid duplicate mod IDs.
- Fabric Loader itself is still required and is not bundled.
- Press **P** in the game (default, rebindable) to open the NoHurtCam slider.

## Verification and limitations

- CI builds the JAR and validates the 17 nested mod/library IDs, forbidden component IDs, nested metadata, custom mixin config/class, settings screen, client initializer, and English/Russian translations.
- Build validation is not a substitute for an in-game test; camera behavior and full VulkanMod compatibility should still be tested in a real client.
- Native anti-aliasing, FSR and DLSS/DLAA are not implemented yet.
- ARR projects are not mirrored or redistributed; this NoHurtCam implementation is independent original project code.
