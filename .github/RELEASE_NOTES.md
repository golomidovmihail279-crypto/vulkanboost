# VulkanBoost 0.1.0

Initial public release of VulkanBoost for Minecraft Java Edition 1.21.11 with Fabric Loader and VulkanMod.

## Included components

- VulkanMod 0.6.8+1.21.11
- Lithium 0.21.4
- FerriteCore 8.2.0-fabric
- Alternate Current 1.9.0
- BadOptimizations 2.4.1

## Verification and limitations

- The Gradle build and nested-JAR metadata validation passed in GitHub Actions.
- The package has not yet completed full in-game compatibility and stability testing across GPU vendors. Treat it as an initial release and back up worlds before testing.
- Native anti-aliasing, FSR upscaling and DLSS/DLAA are not implemented in this release.
- Do not install separate copies of the bundled components alongside VulkanBoost.
- Requires Minecraft 1.21.11, Fabric Loader, Java 21 and a GPU/driver supporting Vulkan 1.2+.

## Graphics roadmap

- FSR: feasibility and Vulkan pipeline integration tracked in issue #2.
- DLSS/DLAA: NVIDIA NGX feasibility and depth/motion-vector/jitter requirements tracked in issue #3.

See the repository README and third-party notices for installation details and licensing.
