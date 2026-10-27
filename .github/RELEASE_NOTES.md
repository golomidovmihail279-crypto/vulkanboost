# VulkanBoost 0.4.0 for Minecraft 1.21.11

An expanded experimental optimization bundle with 22 top-level nested mod/library JARs.

## Additional components in 0.4.0

- C2ME 0.3.6.0.0
- DynamicRenderDistance 1.2.0
- PerceptiFrame (candidate build)
- Bura 0.1.0 alpha
- Epitaph Optimizer (candidate build)
- Dynamic FPS 3.11.4
- FastQuit 3.1.3+mc1.21.11
- Optis 1.0.0
- Afterburner: Optimized (experimental 1.21.11 build)
- Smart Particles 1.11.10
- Architectury API 19.0.1+fabric
- Cloth Config API 21.11.153+fabric

## Installation

- Minecraft 1.21.11, Fabric Loader 0.19.3+, Java 21 and a Vulkan 1.2+ compatible GPU/driver are required.
- Download `VulkanBoost-0.4.0-for-1.21.11.jar` and put only this bundle in the `mods` folder; do not also install duplicates of bundled mods/libraries.
- Fabric Loader itself is still required and is not bundled.

## Important test notes

- CI builds the JAR and validates nested metadata, but does not launch Minecraft or prove in-game compatibility.
- This release combines several experimental chunk scheduling/culling changes. Test on a disposable profile and back up worlds.
- Compare against 0.3.0 using the same world/position/settings. If issues appear, remove new optimizers one-by-one to identify the conflict.
- Native anti-aliasing, FSR and DLSS/DLAA are still not included; these remain a later development milestone.

ARR projects are not redistributed, and OptiLeaves is withheld pending license clarification.
