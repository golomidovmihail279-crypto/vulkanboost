# VulkanBoost 1.0

## First-party project features

- Added the VulkanBoost Control Center with a configurable key binding and responsive, paginated screens.
- Added a complete status registry for all 25 bundled top-level components.
- Added a performance monitor for FPS, frame time, Java heap usage, and loaded Fabric component count.
- Added three reversible auto-tuning profiles: Balanced, FPS Priority, and Lower Memory.
- Added optional Java heap-pressure assistance with manual-change preservation and retryable rollback.
- Added verified live settings adapters for Cull Leaves and Dynamic FPS. Components without safe verified APIs remain read-only.

## Bundle

- Retains the 25 pinned top-level components from VulkanBoost 0.6.
- Keeps upstream components unmodified and records their licenses in `LICENSES/THIRD_PARTY_NOTICES.md`.
- Fast Item Frames remains an optional external companion because its upstream resource assets are marked All Rights Reserved; the complete JAR is not redistributed without permission.

## Reliability and validation

- Added regression checks ensuring the 25 registry IDs match the bundle manifest.
- Requires core VulkanBoost runtime classes to be present in the built JAR.
- Hardens recursive nested-JAR metadata validation and rejects malformed metadata types.
- Corrects automatic profile recommendation so JVM maximum heap is not mistaken for physical RAM.
- Prevents memory assistance from increasing distances already below its soft minimum.
- Adds tag-based release automation with a SHA-256 checksum asset.

## Compatibility

Minecraft Java Edition 1.21.11 · Java 21 · Fabric Loader 0.20.1 · VulkanMod 0.6.8+1.21.11 · Vulkan-capable GPU/driver.

## Not included

No FSR, DLSS, DLAA, FXAA/SMAA, temporal anti-aliasing, or custom upscaling implementation is shipped in this release.
