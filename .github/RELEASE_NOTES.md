# VulkanBoost 0.4.11 for Minecraft 1.21.11

This release removes VulkanBoost's custom damage-camera integration and leaves the official BetterHurtCam component as an unmodified nested dependency.

## Changes

- Removes all custom Java code used for damage-camera settings, the first-server setup prompt, and the custom settings key.
- Removes the config bridge and one-time migration from previous VulkanBoost settings. BetterHurtCam now owns its normal configuration directly.
- Removes all VulkanBoost translation files and the custom Russian resource overlay for BetterHurtCam.
- Removes the root client entrypoint. VulkanBoost adds no runtime classes, mixins, screens, keybindings, configuration wrappers, or resource patches for BetterHurtCam.
- Keeps the pinned upstream BetterHurtCam 1.12.0+mc1.21.11 Modrinth Maven artifact and Ukulib 1.10.2+1.21.11 unchanged inside the single JAR.
- Leaves the remaining optimization bundle unchanged.

## Verification

- CI builds the Java 21 remapped JAR and checks all 19 nested components, including the exact BetterHurtCam and Ukulib mod versions.
- CI also rejects root runtime classes, entrypoints, mixins, or asset overlays, so the bundle cannot silently reintroduce a custom layer around BetterHurtCam.
- A successful build validates compilation and packaging, not gameplay; the full bundle still needs real Minecraft 1.21.11 runtime testing.
- If you upgraded from an earlier version, an obsolete settings-state file may remain in your config folder. Version 0.4.11 no longer reads it; it can be deleted.

## Installation

Download `VulkanBoost-0.4.11-for-1.21.11.jar` and place it in `mods`. Remove separate copies of mods already bundled in VulkanBoost to avoid duplicate mod IDs.
