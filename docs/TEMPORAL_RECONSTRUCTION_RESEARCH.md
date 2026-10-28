# Temporal Reconstruction Research Plan

Status: research and architecture phase. No temporal anti-aliasing or temporal reconstruction algorithm is implemented by this branch.

## Product goal

Build a VulkanBoost-owned image-quality pipeline for Minecraft 1.21.11 + VulkanMod. The first quality target is a stable image at native output resolution, with less edge stair-stepping, thin-geometry shimmer, and motion instability. Lower-resolution upscaling is a later, separate experiment—not a prerequisite.

This is not a promise that temporal reconstruction can infer details absent from all input samples. The goal is to use multiple valid samples over time, while rejecting stale history and preserving moving or newly visible content.

## Why a native-resolution temporal approach?

A spatial filter only sees one frame. A temporal approach can accumulate complementary sub-pixel samples, but needs a correctly jittered projection, a history buffer, reprojection, and history validation. At native render resolution, this is closer to temporal anti-aliasing / native AA than a performance upscaler. It may improve temporal stability and edge quality without deliberately rendering the main scene at 70–77%.

Native output resolution by itself does not create new detail. The quality improvement comes from controlled sub-pixel sampling and valid temporal accumulation. This must be measured against a stable non-temporal reference.

## Existing knowledge to study

These are research references, not dependencies being copied into the project. No external code should be transplanted until its license, version constraints, API assumptions, and attribution requirements have been reviewed.

| Reference | Why it matters | Initial questions |
|---|---|---|
| [VulkanMod source and project page](https://modrinth.com/mod/vulkanmod) | Our renderer/backend and likely integration boundary | Where are current/previous projection matrices, depth attachments, render targets, and final presentation managed in 1.21.11? |
| [AMD FSR 2 technical docs](https://gpuopen.com/fidelityfx-superresolution-2/) | Mature temporal reconstruction reference | Required inputs, jitter conventions, motion-vector direction/scale, depth conventions, history reset rules, and reactive content handling |
| [FidelityFX SDK](https://github.com/GPUOpen-LibrariesAndSDKs/FidelityFX-SDK) | Current upstream SDK/reference samples | Which parts are shader algorithms versus API/backend-specific runtime code? How can a Vulkan renderer provide the required resources? |
| [FSR-Reloaded](https://github.com/Human-Name998/FSR-Reloaded) | Minecraft-specific rendering hooks and frame lifecycle | Which Minecraft classes and framebuffers are modified? Which shader/resource assumptions are version-specific? |
| [PureScale Modern](https://github.com/XsoraS/purescale) | Modern Minecraft upscaling integration, including native/runtime pathways | How does it separate world rendering, hand rendering and HUD? Which paths support 1.21.11 and which are specific to newer Minecraft versions? |
| [AuraLite TAA implementation notes](https://github.com/AlexanderNyr/AuraLite-Shaders) | Practical details on jitter, reprojection, clipping and reactive content | Which principles can transfer, and which require an Iris/OpenGL shaderpack architecture that VulkanMod does not share? |
| [RenderScale](https://github.com/Zolo101/RenderScale) | Prior experiments and history of resolution scaling in Minecraft | Treat only as a source-code reference for lessons learned; it is not a runtime dependency in this branch. |
| [Upshift](https://www.curseforge.com/minecraft/mc-mods/upshift) | A Minecraft-specific temporal accumulate (TAAU) described by its author | Study camera-rotation reprojection, jittered history, neighbourhood clamping, and—especially—render-target lifetime; the published page targets Minecraft 26.2/Java 25, not our runtime. |
| [Salt's Anti-Aliasing](https://github.com/Salt-and-Fries/Salt-s-Anti-Aliasing) | Modular AA architecture with separate platform adapters | Inspect the distinct 1.21.8–1.21.11 branch rather than copying hook descriptors from the 26.1.2 branch; note its stated separation of core pipeline planning and version-specific renderer glue. |
| [Fornax](https://github.com/Icehunter/fornax) | Temporal pass and explicit render-graph concepts | The project targets a newer Sodium/26.2 stack, not our VulkanMod 1.21.11 stack; use as a pass/resource-lifetime design reference only. |

### Important lessons from Minecraft temporal implementations

Upshift's public technical description is unusually relevant to this project, even though its current target is newer than ours. It describes a temporal accumulate path based on a jittered projection, camera-rotation reprojection, and neighbourhood-clamped history. It also says the engine path it targets does not expose object motion vectors, so its temporal path relies on camera-driven reprojection rather than vendor FSR 2's complete input set. This is a useful starting hypothesis, not proof that the same implementation is artifact-free.

The same description highlights a concrete resource-lifetime rule: it keeps the window target at display size, temporarily redirects world rendering through an internal target, and caches internal targets by size instead of resizing/freeing them mid-frame. It explains that renderer components may capture target references during frame-graph construction; replacing or freeing a target while those references remain live can create invalid/null texture access. We should audit that exact class of lifecycle issue against our own logged `0x0` size fields and VulkanMod 0.6.8 source before blaming a shader.

The core limitation of camera-only reprojection is equally important for the right-hand item and moving entities: a camera homography models camera motion, not independent object motion, animation, newly revealed surfaces, or transparency changes. A native-resolution temporal AA prototype should therefore start with conservative history rejection and specific tests for held items/entities, rather than trusting a camera-only warp to handle every pixel.

References:
- [Upshift design notes and compatibility](https://www.curseforge.com/minecraft/mc-mods/upshift)
- [Salt's Anti-Aliasing architecture notes](https://github.com/Salt-and-Fries/Salt-s-Anti-Aliasing)
- [Fornax pass/resource format](https://github.com/Icehunter/fornax)
- [AMD temporal upscaling inputs and conventions](https://github.com/GPUOpen-LibrariesAndSDKs/FidelityFX-SDK/blob/main/Kits/FidelityFX/docs/techniques/super-resolution-temporal.md)

## Source investigation matrix

Before writing the temporal pass, inspect and document these from the exact pinned dependencies and Minecraft version:

1. **Frame graph and lifecycle:** precise point where the world render target is complete, where post-processing occurs, how resize/reload recreates attachments, and which calls submit/present the final image.
2. **Texture and resource ownership:** color/depth formats, sample counts, dimensions, Vulkan usage flags, views, layout transitions, synchronization, lifetime, and resize behavior. Validate every assumption; do not infer successful pixels from a draw-call log.
3. **Camera state:** current and previous view/projection matrices, near/far planes, camera bobbing, FOV changes, camera cuts, teleportation, and world changes. Identify whether a stable jitter injection point exists without accumulating matrix error.
4. **Motion vectors:** whether VulkanMod exposes them for opaque entities, block entities, particles, translucent geometry, and the first-person hand/items. If unavailable, determine the limits of camera-only reprojection rather than pretending it is equivalent to per-object velocity.
5. **History validation:** disocclusion, depth mismatch, animated textures, foliage, particles, transparency, weather, emissive content, and sudden lighting changes.
6. **Composition boundaries:** first-person hand/items, crosshair, chat, menus and other HUD must not acquire ghost trails. Decide whether they are excluded from history, rendered afterward, or handled with explicit masks.
7. **Compatibility:** windowed/fullscreen resize, minimization and zero-sized intermediate dimensions, resource reload, VSync, different render scales, and common resource packs.
8. **License and provenance:** record exact source URL, commit/tag, license, files considered, any modified code, and required notices before reuse.

## Intended implementation phases

### Phase A — Clean base
- Keep a reproducible, single-JAR optimization bundle.
- Remove the old RenderScale integration, FSR shader patch task, FSR option hooks, diagnostic mixins, and FSR-only validation.
- Add bundle-level validation that rejects accidental reintroduction of those runtime resources.

### Phase B — Renderer map
- Pin exact Minecraft and VulkanMod artifacts.
- Trace render-target creation, resize, sampling, synchronization and presentation in source.
- Add a written architecture map and minimal diagnostics before introducing any temporal history.

### Phase C — Native-resolution AA prototype
- Start from native render resolution with a strict off-switch.
- Test projection jitter and history accumulation as separate changes.
- Add reprojection only after matrix conventions and depth are established.
- Reset history on resize, reload, camera cut, world change, or invalid dimensions.
- Prefer a conservative fallback whenever inputs are missing or invalid.

### Phase D — Artifact reduction
- Compare history clamping methods, disocclusion handling, reactive masking and sharpening.
- Test static geometry, slow and fast camera turns, player movement, held items, foliage, particles, translucency, sky, menus and resource packs.
- Capture repeatable screenshots and logs. Avoid subjective "looks fine" as the only acceptance condition.

### Phase E — Performance and release gates
- Measure GPU frame time, CPU frame time, memory use, and frame pacing against a matched baseline.
- Require stable pixels and repeatable gains before enabling the option by default.
- Keep the published release unchanged until the feature passes CI and in-game validation.

## Acceptance checklist

- [ ] No black screen on startup, joining a server, world change, or resource reload.
- [ ] No persistent trails on held items, entities, particles, or the HUD.
- [ ] No flicker or major instability on thin block geometry during camera movement.
- [ ] Correct recovery from resize, minimization, and zero-sized intermediate targets.
- [ ] All frame-history resources are released/recreated correctly.
- [ ] Native-resolution mode is compared at the same 2560x1440 output and matching video settings.
- [ ] Performance is reported using repeated measurements, not assumed from algorithm names.
- [ ] New rendering behavior is disabled by default until tested on the target setup.

## Current evidence and open hypotheses

The earlier experimental RenderScale chain logged that its two fullscreen draw calls were issued, but the user still observed visual artifacts in a test where both passes were simple copies. This makes the RenderScale/VulkanMod pass chain, target binding, sample coordinates, or resize/resource lifecycle worth investigating; it does not yet prove which one is responsible. The repeated zero-sized dimension in prior logs is a concrete clue to audit against source and runtime behavior, not a confirmed root cause.

The prior EASU-only experiment also allowed the user to enter a world without a black screen on one test, but had artifacts—especially on the held item. That is evidence of partial runtime progress, not evidence of a correct FSR implementation. Those FSR runtime paths are intentionally removed from this clean branch so a future temporal pipeline can be designed from the renderer's actual capabilities.
