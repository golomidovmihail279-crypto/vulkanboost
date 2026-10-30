# VulkanBoost Control Center — v0.7 feature scope

**Status:** planned. This document defines required product scope; it does not claim that any of these features are already implemented.  
**Target baseline:** Minecraft 1.21.11, Fabric Loader 0.20.1, Java 21, VulkanMod 0.6.8+1.21.11.  
**Release policy:** keep the frozen 0.6 release unchanged. Develop the Control Center on a separate feature branch and target a later release.

## Product goal

Turn VulkanBoost from a JAR that mainly bundles third-party optimization mods into a configurable client mod with one place to inspect, tune, and control the optimizations it can safely manage.

## Required features

### 1. Unified settings panel

- Add a VulkanBoost screen accessible through a configurable key binding.
- Group controls into clear sections: Overview, Optimizations, Auto-tuning, Memory, and Monitor.
- Show the current value, a short explanation, and whether applying a change takes effect immediately or requires a restart.
- Persist settings in a versioned file under the Minecraft config directory.
- Include safe defaults and a reset-to-defaults action.

### 2. Optimization control

- Create a central registry of optimization features, each with a stable ID, description, current status, and a settings adapter.
- Integrate supported upstream configuration APIs/config files one component at a time.
- Only show a working toggle when VulkanBoost has a verified way to apply it.
- Show explicit states such as **Available**, **Restart required**, and **Not supported** rather than displaying switches that do nothing.
- Do not attempt to unload or hot-disable an already loaded Fabric mod while the game is running. A restart-required state is acceptable for startup-level options.

### 3. Performance monitor

Initial monitor:
- Current FPS and frame-time in milliseconds.
- Java heap usage: used, committed, and maximum.
- CPU/process information only where the running JVM and operating system expose reliable values.
- Vulkan/GPU information only where a reliable supported API is available.

Later extensions may add percentile frame-times, history graphs, and metrics emitted by VulkanBoost's own subsystems.

Do not claim precise per-mod CPU/GPU attribution unless a real profiling source exists. Third-party mods do not automatically expose comparable load counters.

### 4. Auto-tuning

- Detect only reliable hardware/runtime facts, such as logical CPU count, JVM maximum heap, Minecraft version, and the active renderer/device information exposed by supported APIs.
- Offer understandable presets such as **Balanced**, **FPS priority**, and **Lower memory use**.
- Generate a preview of proposed changes before applying them.
- Change only settings for which a verified adapter exists; preserve the user's prior values and provide an undo/reset path.
- Never change JVM launch arguments, OS settings, GPU drivers, or system-wide configuration automatically.
- Do not promise that a preset increases FPS on every machine. It should choose conservative settings based on available information.

### 5. Smart memory management

- Start with transparent heap monitoring and diagnostics.
- Manage only caches, background work, or memory limits that have a documented and safe control point.
- Prefer reducing unnecessary background work when the game is unfocused, where compatible.
- Do not run repeated forced garbage collection or advertise a generic “RAM cleaner”. Java's garbage collector owns heap reclamation; forcing it is not a reliable optimization.
- Explain which setting was changed and whether a restart is needed.

## Implementation architecture

1. **Client entry point:** register VulkanBoost's client initializer and configurable key binding.
2. **Configuration layer:** versioned config model, validation, defaults, persistence, and migration support.
3. **Feature registry:** a single source of truth for each feature's ID, description, state, supported actions, and restart requirement.
4. **Adapters:** isolated integration code for each upstream mod/configuration surface. An adapter must be tested before its controls are exposed.
5. **Metrics service:** collect lightweight measurements on the client thread or through safe sampling; keep UI rendering separate from metric collection.
6. **Auto-tuner:** read hardware/runtime facts, calculate a proposed profile, and apply it only through registered adapters.
7. **UI:** render the same registry and metrics used by the rest of the system; do not duplicate settings logic inside screen widgets.

## Delivery order

- **Phase A — foundation:** client entry point, config persistence, key binding, basic settings screen, feature registry.
- **Phase B — real controls:** audit bundled mods and implement verified adapters; mark unsupported or restart-only settings honestly.
- **Phase C — monitor:** FPS, frame-time, heap metrics, and a compact in-game overlay/screen.
- **Phase D — auto-tuning:** hardware detection, three conservative presets, preview/apply/undo.
- **Phase E — memory management:** supported cache/background-work controls and clear diagnostics.

Each phase must build and pass automated checks before moving on. The completed feature set also requires an in-game test on the target Minecraft/Fabric/Java/VulkanMod combination.

## Implementation status on `feature/control-center-v0.7`

The first implementation of all five phases is now present in this branch. Automated CI and in-game validation are tracked separately.

| Phase | Implemented scope | Remaining validation |
|---|---|---|
| A — Foundation | Fabric client initializer, default `O` key binding, Control Center screen, versioned persistent settings | Open and use the screen in Minecraft |
| B — Optimization controls | Verified live adapters for Cull Leaves and Dynamic FPS; real loader status for Lithium, Structure Layout Optimizer, and ModernFix | Confirm both live adapters in a running 1.21.11 instance; add more adapters only after auditing their upstream APIs |
| C — Monitor | FPS, frame time, used/committed/max Java heap, and loaded Fabric component count | Compare displayed values with the running client; GPU/per-component utilization is intentionally not fabricated |
| D — Auto-tuning | CPU thread and max Java heap detection; Balanced, FPS Priority, and Lower Memory profiles; distance-setting preview, apply, and undo | Check vanilla option access and resulting chunk changes in-game |
| E — Memory assistance | Opt-in heap-pressure monitoring; configurable 70–95% threshold (85% default); sustained-pressure reduction of render/simulation distance; restoration after pressure falls if the user has not changed those options | Run a long-world test and confirm pressure, reduction, recovery, and manual-override behavior |

### Current real control coverage

- **Cull Leaves:** checks the bundled mod and the public `CullLeavesConfig.enabled` field, then saves through MidnightLib's `MidnightConfig.write("cullleaves")` method. The upstream config save hook refreshes the affected world renderer.
- **Dynamic FPS:** checks the bundled mod's `DynamicFPSConfig.INSTANCE`, `setEnabled(boolean)`, `save()`, and `DynamicFPSMod.onConfigChanged()` methods before exposing the live switch.
- **Lithium, Structure Layout Optimizer, ModernFix:** shown as loaded components where present, but not presented as working switches until a safe configuration interface is verified.

### Auto-tune safety

The tuner uses the available logical CPU count and maximum Java heap as its initial hardware signals. Profiles only lower render and simulation distances; they do not silently increase the user's view distance. It enables only registered, verified feature adapters. Original values are captured so the last profile can be undone. No operating-system settings, JVM launch arguments, drivers, or GPU settings are modified.

### Memory-assistance safety

Memory assistance is disabled by default and must be enabled in the UI. It samples Java heap pressure every 100 client ticks, requires three consecutive high-pressure samples before intervening, and only changes vanilla render/simulation distances. It never calls `System.gc()`. It waits for pressure to fall below a hysteresis threshold before restoring previous values, and avoids overwriting distance settings the user changed manually. This reduces the active chunk workload; it does not promise to instantly reclaim Java heap.

## Acceptance criteria

- The settings screen opens reliably and the key binding can be changed.
- Settings survive a game restart and malformed config files recover to safe defaults without deleting the user's data silently.
- Every visible switch changes a verified setting; unsupported options are not presented as functional toggles.
- Restart-required changes are clearly labelled.
- Monitor values update without noticeable extra frame-time cost.
- Auto-tuning previews its changes and can restore the previous values.
- Memory management never relies on repeated forced garbage collection.
- No feature is labelled complete based only on compilation; it must pass an in-game check.

## Important limitation

VulkanBoost currently has no project-owned Java runtime implementation; its existing build is primarily a nested-mod bundle. The Control Center therefore requires new Fabric client code and tests. The bundled mods cannot all be switched on/off live merely because they are packed into one JAR. Actual controls depend on each upstream mod exposing a safe configuration interface or on a separately tested VulkanBoost integration.
