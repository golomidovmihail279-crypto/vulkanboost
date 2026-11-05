# VulkanBoost 1.0 release checklist

## Automated gates

- [x] Keep the published v0.6 release and its JAR unchanged.
- [x] Integrate the Control Center and small-screen layout fixes on a separate release branch.
- [x] Show all 25 bundled top-level component IDs in the in-game registry.
- [x] Expose live switches only for verified Cull Leaves and Dynamic FPS adapters.
- [x] Use JVM max heap as an informational metric, not as a physical-RAM detector.
- [x] Ensure memory assistance never increases a distance setting.
- [x] Add recursive nested-JAR metadata regression tests.
- [x] Add registry-to-bundle manifest consistency tests.
- [x] Add tag-based build, test, checksum, and release automation.
- [ ] Confirm the v1.0 PR build passes on GitHub Actions.
- [ ] Inspect the exact release JAR and verify its 25 nested JARs and first-party classes.

## Manual Minecraft acceptance gate

These checks require a real game session. A successful Gradle build cannot replace them.

- [ ] Launch Minecraft 1.21.11 with Java 21, Fabric Loader 0.20.1, VulkanMod 0.6.8+1.21.11, and the VulkanBoost JAR.
- [ ] Confirm there is no startup crash or missing-dependency warning.
- [ ] Open and close the Control Center using the default key; change the key in Options → Controls and test the new binding.
- [ ] Open every screen at 320×180, 427×240, 640×360, 854×480, and 1920×1080 (or the closest available GUI-scaled sizes); confirm buttons do not overlap or disappear.
- [ ] Confirm all 25 component rows are reachable through pagination and installed/not-installed states are correct.
- [ ] Toggle Cull Leaves and Dynamic FPS when their upstream APIs are present; restart and confirm settings persist.
- [ ] Preview, apply, and undo each auto-tuning profile; verify manual changes made afterwards are preserved.
- [ ] Enable memory assistance in a disposable world; verify it only reduces distances after sustained pressure and restores them after pressure falls.
- [ ] Manually change render/simulation distance while memory assistance is active; confirm those manual values are not overwritten.
- [ ] Leave the world while memory assistance has changed options; confirm restoration is safe.
- [ ] Create and save a new world; load it again and inspect the log for errors related to C2ME, Bura, Fast Noise, or Structure Layout Optimizer.
- [ ] Test an existing world only after taking a backup.
- [ ] Review latest.log and any crash reports after the full pass.

## Publication rule

Do not publish v1.0 as a stable release until both automated and manual acceptance gates pass. The release workflow publishes a GitHub Release when a matching version tag is pushed. Do not create the `v1.0` tag before the manual acceptance gate is complete.
