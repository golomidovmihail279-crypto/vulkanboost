# VulkanBoost 0.1.0-alpha.1

Initial alpha release targeting Minecraft Java Edition 1.21.11 with Fabric Loader and VulkanMod.

## Included components

- VulkanMod 0.6.8+1.21.11
- Lithium 0.21.4
- FerriteCore 8.2.0-fabric
- Alternate Current 1.9.0
- BadOptimizations 2.4.1

## Important status

- The Gradle build and nested-JAR metadata validation passed in GitHub Actions.
- This is an early alpha. A full in-game compatibility and stability test across GPU vendors has not yet been completed.
- Native anti-aliasing, FSR upscaling and DLSS/DLAA are not implemented in this release.
- Do not install separate copies of the bundled components alongside VulkanBoost.
- Requires Minecraft 1.21.11, Fabric Loader and a GPU/driver supporting Vulkan 1.2+.

## Anti-aliasing and upscaling roadmap

- FSR: feasibility and Vulkan pipeline integration tracked in issue #2.
- DLSS/DLAA: NVIDIA NGX feasibility and required motion-vector/depth/jitter integration tracked in issue #3.

See the repository README and third-party notices for installation details and licensing.
