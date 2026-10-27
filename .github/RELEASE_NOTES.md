# VulkanBoost 0.5 for Minecraft 1.21.11

VulkanBoost 0.5 keeps its single-JAR install model and adds one simple Image Enhancement toggle directly to Minecraft's Video Settings.

## Changes

- Adds **RenderScale 1.4.0-alpha.1 for Minecraft 1.21.11** as an upstream nested dependency.
- Adds experimental **FSR 1 spatial upscaling**. RenderScale and its existing dependencies (Fabric API and Cloth Config API) are included inside the same VulkanBoost JAR.
- Adds one client-side Video Settings toggle that controls the bundled RenderScale FSR 1 mode: ON selects a 77% internal render scale with FSR 1 reconstruction; OFF returns to native scale and disables FSR.
- Keeps BetterHurtCam and Ukulib as their original upstream JARs; no BetterHurtCam screens, keybindings, translations, or resource overrides are introduced.
- Leaves the original BetterHurtCam and Ukulib JARs unchanged.
- DLSS/DLAA and temporal FSR 2+ are not part of version 0.5; they need a separate compatibility plan for VulkanMod's renderer.

## Verification

- CI builds the Java 21 remapped JAR and validates all 20 nested components, including the pinned BetterHurtCam, Ukulib, and RenderScale versions.
- CI permits only the two narrowly scoped client mixins used for the Video Settings toggle and the Russian/English strings for that setting. It rejects other root classes/assets and still checks upstream BetterHurtCam and Ukulib versions.
- RenderScale's FSR 1 feature is experimental for Minecraft 1.21.11. Compatibility with this specific VulkanMod combination has not been confirmed in-game. If enabled, compare image quality/FPS and watch for rendering glitches; turn the setting off if you see any. The 77% internal scale is a spatial upscaling preset, not DLSS or frame generation.
- This remains a single-JAR bundle. Fabric Loader itself is not included.

## Installation

Download `VulkanBoost-0.5-for-1.21.11.jar` and place it in `mods`. Remove separate copies of mods already bundled in VulkanBoost to avoid duplicate mod IDs.
