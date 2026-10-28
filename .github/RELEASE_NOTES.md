# VulkanBoost 0.5 for Minecraft 1.21.11

**Updated safe build:** the early 0.5 package could produce a black world when the experimental FSR path was enabled on Minecraft 1.21.11 with VulkanMod 0.6.8. This updated 0.5 build disables that unstable FSR path and uses RenderScale's normal linear upscale at 77% internal resolution instead. The black-screen issue was not reproduced in the user's latest in-game test of the safe fallback.

## Changes

- Keeps the single-JAR installation model with 20 nested mod/library JARs.
- Includes **RenderScale 1.4.0-alpha.1 for Minecraft 1.21.11**, along with its bundled dependencies.
- Adds a VulkanBoost graphics option that renders at **77% internal resolution** and uses the regular linear upscale path.
- Automatically disables the unstable RenderScale FSR path on this Minecraft 1.21.11 + VulkanMod combination to avoid the known black-world issue.
- Keeps BetterHurtCam and Ukulib as their original upstream JARs.
- DLSS/DLAA and temporal FSR 2+ are not included.

## Verification

- GitHub Actions successfully built the remapped Java 21 JAR and passed the bundle/shader validation checks for this source revision.
- The updated safe fallback was tested in Minecraft 1.21.11 with Fabric Loader 0.20.1, RenderScale 1.4.0-alpha.1 and VulkanMod 0.6.8. The user reported that the world remained visible with no black areas or flicker.
- **FSR reconstruction itself is temporarily disabled in this build.** The next 0.5.1 development cycle will focus on fixing and separately testing EASU and RCAS, rather than claiming FSR is working in 0.5.
- The mod bundle is experimental; other bundled mods and combinations may have their own issues. Fabric Loader and Java are not included in the JAR.

## Installation

Download `VulkanBoost-0.5-for-1.21.11.jar` and place it in `mods`. Remove separate copies of mods already bundled in VulkanBoost to avoid duplicate mod IDs.
