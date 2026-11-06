# VulkanBoost post-release audit

Audit baseline: the published VulkanBoost 1.0 release and its exact JAR checksum. Follow-up changes are made on a separate branch and must pass CI before merge.

## Corrected in this patch

- Auto-Tune now refreshes its preview and action-button state after failed apply/undo operations. If rollback was incomplete, the retained snapshot keeps Undo available for another attempt.
- Performance Monitor labels the Fabric Loader mod count as "Loaded Fabric mods" / "Загружено модов Fabric". It is the total number of loaded Fabric mods, not the number of VulkanBoost bundle components.
- README and release checklist now distinguish the GitHub Latest/non-prerelease publication state from completed in-game acceptance testing.
- Release automation now runs only for explicit version-tag pushes. Ordinary pushes to main no longer trigger release publication.
- The release workflow checks the tag/version match, passes the version to shell scripts through an environment variable, scopes write permission to the release job, and does not persist checkout credentials.
- Removed the obsolete v0.6 workflow that could republish/overwrite the v0.6 release when a specially marked commit appeared on main.

## Compatibility decisions — do not change without runtime evidence

### Bura

The bundle keeps the pinned Bura 0.1.0 artifact. Bura 0.2.0 is listed for Minecraft 1.21.11/Fabric, but its published tested combinations do not list VulkanMod and the project still describes alpha-stage visibility/culling limitations. Upgrade only after a disposable-world test with the exact VulkanMod build in this bundle. Source: https://modrinth.com/mod/bura

### Duplicate nested libraries

Several upstream nested mods package overlapping library names at different versions. This is not enough evidence to remove either copy: deleting a nested dependency can break the mod that declares it. Keep upstream artifacts intact until Fabric Loader's actual dependency-resolution output or a reproducible class-loading failure identifies a conflict. Inspect `latest.log` from the exact release profile.

## Manual acceptance still required

- Launch the exact release with Minecraft 1.21.11, Java 21, Fabric Loader, and VulkanMod.
- Verify Control Center and key rebinding.
- Exercise Auto-Tune apply, undo, failed-apply rollback, and failed-undo retry.
- Exercise Smart Memory Assistance in a disposable world, including manual distance changes and world exit.
- Test world creation/loading with C2ME, Bura, Fast Noise, and Structure Layout Optimizer.
- Review `latest.log` and crash reports.

A successful CI run verifies compilation and automated regression checks. It does not replace the manual checks above.
