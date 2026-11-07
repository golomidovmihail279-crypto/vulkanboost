# VulkanBoost: Horizon Edition — architecture

## Separation

The Fabric edition remains the independent Minecraft 1.21.11 project. Horizon Edition is a separate native mod for Horizon's own mod system:

- `.hnmod` source bundle, not a Fabric JAR.
- Rust source integrated with Horizon through `horizon-mixins`.
- API compatibility declared in `compatibility.json`.
- No Java bytecode, Fabric Loader, Fabric API, or VulkanMod dependency.

## Design rules

1. Prefer documented native hook points and the smallest required hook API version.
2. Preserve vanilla/Horizon behaviour when the feature is disabled.
3. A limiter may only lower the limit already chosen by Horizon; it must never increase it.
4. No background worker, polling loop, network access, process execution, or OS-wide setting changes unless a later feature has a clear requirement and a reviewed design.
5. Keep policy logic independent from Horizon types so it can be unit-tested with ordinary `cargo test`.
6. Do not duplicate Horizon features such as its official FPS counter.
7. Measure real frame time and resource usage before claiming a performance improvement.

## 0.1.0-dev scope

The first feature hooks Horizon's `native_framerate_limit` callback (hook API 167). When a world is active and the game window is unfocused or minimized, VulkanBoost applies a user-selected cap. Horizon still owns the final limit and can keep a lower value.

The setting is exposed in Options → Mods → VulkanBoost → Settings and is persisted through Horizon's own native-setting helpers.

This changes background resource usage only. It is not an in-game FPS boost.

## Later work — not implemented yet

- Inspect Horizon's profiling hooks and data sources before designing a performance monitor.
- Design a Control Center only where Horizon does not already provide the same control.
- Evaluate any optimization against vanilla Horizon using repeatable before/after tests.
- Add tuning features only when a documented hook can change the relevant setting safely and reversibly.

## Compatibility pins

The Horizon and horizon-mixins revisions in `compatibility.json` are explicit reproducibility pins. The mod requests hook API 167 rather than the newest API number because it only needs the frame-rate-limit callback. Do not raise this number without adding a newer hook.
