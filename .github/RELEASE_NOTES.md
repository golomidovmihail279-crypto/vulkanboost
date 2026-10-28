# VulkanBoost 0.5.1-dev for Minecraft 1.21.11

This development version establishes a clean base for future native-resolution temporal anti-aliasing work. It does not yet implement a temporal reconstruction algorithm and does not advertise unimplemented rendering features.

## Changes

- Removes custom rendering hooks and the previous resolution-scaling integration from the development source tree.
- Retains the single-JAR installation model and 19 pinned nested mod/library components.
- Validates nested-JAR metadata, component IDs and versions, archive integrity, and the absence of project-owned rendering hooks.
- Documents the renderer investigation plan and future acceptance criteria.

## Validation

- GitHub Actions builds the remapped Java 21 JAR.
- The bundle validator and its unit tests must pass before an artifact is accepted.
- The development artifact is not a claim of visual correctness or completed temporal anti-aliasing.
- Full in-game validation on Minecraft 1.21.11 with VulkanMod is still required before any new rendering behavior is enabled.

## Installation

Use a disposable Minecraft 1.21.11 profile with Java 21 and a compatible Fabric Loader. Install only one copy of each bundled component to avoid duplicate mod IDs.
