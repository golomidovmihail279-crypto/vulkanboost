# Temporal Reconstruction Research Plan

Status: research and architecture phase. No temporal anti-aliasing or temporal reconstruction algorithm is implemented by this branch.

## Product goal

Build a VulkanBoost-owned image-quality pipeline for Minecraft 1.21.11 + VulkanMod. The first quality target is a stable image at native output resolution, with less edge stair-stepping, thin-geometry shimmer, and motion instability. Other rendering approaches are separate future experiments, not prerequisites.

This is not a promise that temporal reconstruction can infer details absent from all input samples. The goal is to use multiple valid samples over time, while rejecting stale history and preserving moving or newly visible content.

## Why a native-resolution temporal approach?

A spatial filter only sees one frame. A temporal approach can accumulate complementary sub-pixel samples, but needs a correctly jittered projection, a history buffer, reprojection, and history validation. At native render resolution, this is closer to temporal anti-aliasing / native AA than a performance upscaler. It may improve temporal stability and edge quality without deliberately rendering the main scene at 70–77%.

Native output resolution by itself does not create new detail. The quality improvement comes from controlled sub-pixel sampling and valid temporal accumulation. This must be measured against a stable non-temporal reference.

## Existing knowledge to study

These are research references, not dependencies being copied into the project. No external code should be transplanted until its license, version constraints, API assumptions, and attribution requirements have been reviewed.

| Reference | Why it matters | Initial questions |
|---|---|---|
| [VulkanMod source and project page](https://modrinth.com/mod/vulkanmod) | Our renderer/backend and likely integration boundary | Where are current/previous projection matrices, depth attachments, render targets, and final presentation managed in 1.21.11? |
| [PureScale Modern](https://github.com/XsoraS/purescale) | Modern Minecraft upscaling integration, including native/runtime pathways | How does it separate world rendering, hand rendering and HUD? Which paths support 1.21.11 and which are specific to newer Minecraft versions? |
| [AuraLite TAA implementation notes](https://github.com/AlexanderNyr/AuraLite-Shaders) | Practical details on jitter, reprojection, clipping and reactive content | Which principles can transfer, and which require an Iris/OpenGL shaderpack architecture that VulkanMod does not share? |
| [Upshift](https://www.curseforge.com/minecraft/mc-mods/upshift) | A Minecraft-specific temporal accumulate (TAAU) described by its author | Study camera-rotation reprojection, jittered history, neighbourhood clamping, and—especially—render-target lifetime; the published page targets Minecraft 26.2/Java 25, not our runtime. |
| [Salt's Anti-Aliasing](https://github.com/Salt-and-Fries/Salt-s-Anti-Aliasing/tree/fabric-1.21.8-11) | Modular AA architecture with a separate 1.21.8–1.21.11 renderer branch | The branch documents portable core planning, renderer-specific adapters, jitter, persistent history and TAA; its manifest says All-Rights-Reserved, while the repo overview and Modrinth listing show different license labels. Treat code reuse as blocked until the exact branch license is clarified. Its branch README calls the Vulkan backend a placeholder. |
| [Fornax](https://github.com/Icehunter/fornax) | Temporal pass and explicit render-graph concepts | The project targets a newer Sodium/26.2 stack, not our VulkanMod 1.21.11 stack; use as a pass/resource-lifetime design reference only. |

### Important lessons from Minecraft temporal implementations

Upshift's public technical description is unusually relevant to this project, even though its current target is newer than ours. It describes a temporal accumulate path based on a jittered projection, camera-rotation reprojection, and neighbourhood-clamped history. It also says the engine path it targets does not expose object motion vectors, so its temporal path relies on camera-driven reprojection rather than a full per-object motion-input pipeline. This is a useful starting hypothesis, not proof that the same implementation is artifact-free.

Salt's Anti-Aliasing has a specifically named `fabric-1.21.8-11` branch whose target includes Minecraft 1.21.11. Its README gives a useful architecture pattern: renderer-independent pass planning and target descriptions live in core code; Minecraft descriptors and render resources live behind version-specific adapters; mixins delegate to a platform facade. However, that branch's README labels its Vulkan backend a placeholder, and its manifest says `All-Rights-Reserved` while the repo overview and Modrinth page show Apache-2.0/MIT labels. We must resolve the exact branch's applicable license before copying any code; for now we only use the public architecture notes as research and do not copy its implementation.

The same description highlights a concrete resource-lifetime rule: it keeps the window target at display size, temporarily redirects world rendering through an internal target, and caches internal targets by size instead of resizing/freeing them mid-frame. It explains that renderer components may capture target references during frame-graph construction; replacing or freeing a target while those references remain live can create invalid/null texture access. We should audit that exact class of lifecycle issue against our own logged `0x0` size fields and VulkanMod 0.6.8 source before blaming a shader.

The core limitation of camera-only reprojection is equally important for the right-hand item and moving entities: a camera homography models camera motion, not independent object motion, animation, newly revealed surfaces, or transparency changes. A native-resolution temporal AA prototype should therefore start with conservative history rejection and specific tests for held items/entities, rather than trusting a camera-only warp to handle every pixel.

References:
- [Upshift design notes and compatibility](https://www.curseforge.com/minecraft/mc-mods/upshift)
- [Salt's Anti-Aliasing architecture notes](https://github.com/Salt-and-Fries/Salt-s-Anti-Aliasing)
- [Fornax pass/resource format](https://github.com/Icehunter/fornax)

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

## Preliminary source audit — VulkanMod 1.21 branch

**Scope warning:** this audit is based on the public `xCollateral/VulkanMod` branch `1.21`, commit [`3adda56191e1a09ce3155de33f8d8334482e089f`](https://github.com/xCollateral/VulkanMod/commit/3adda56191e1a09ce3155de33f8d8334482e089f). The project pins the Modrinth artifact `maven.modrinth:JYQhtZtO:hWKRTXbQ` (`0.6.8+1.21.11`). The branch is a useful source map, **not yet proof that every line matches that exact published artifact**. Before writing runtime mixins, compare the published JAR/source and mappings for the pinned version.

### Findings from the source map

1. **Frame lifecycle:** VulkanMod's `MinecraftMixin` starts `Renderer.beginFrame()` around the frame clear and calls `Renderer.endFrame()` before display update. `Renderer` acquires a swapchain image, begins the main pass, ends the pass, submits the command buffer and presents. Swapchain out-of-date/suboptimal results trigger recreation. Relevant source files: [MinecraftMixin](https://github.com/xCollateral/VulkanMod/blob/3adda56191e1a09ce3155de33f8d8334482e089f/src/main/java/net/vulkanmod/mixin/render/MinecraftMixin.java), [Renderer](https://github.com/xCollateral/VulkanMod/blob/3adda56191e1a09ce3155de33f8d8334482e089f/src/main/java/net/vulkanmod/vulkan/Renderer.java).

2. **The main target is the swapchain in this source branch:** `DefaultMainPass` sets its main framebuffer to `Vulkan.getSwapChain()`. Its color image is transitioned for rendering and then to `VK_IMAGE_LAYOUT_PRESENT_SRC_KHR` at the end of the pass. Its `bindAsTexture()` path ends an active main/aux pass before transitioning that same color image to shader-read layout. This is not yet a clean world-color input plus independent output target. A temporal resolve must not sample and write the same image in one pass; the implementation needs a separate scene target and a deliberate resolve/composition boundary. Source: [DefaultMainPass](https://github.com/xCollateral/VulkanMod/blob/3adda56191e1a09ce3155de33f8d8334482e089f/src/main/java/net/vulkanmod/vulkan/pass/DefaultMainPass.java), [MainTargetMixin](https://github.com/xCollateral/VulkanMod/blob/3adda56191e1a09ce3155de33f8d8334482e089f/src/main/java/net/vulkanmod/mixin/render/target/MainTargetMixin.java).

3. **RenderTarget operations are adapted through mixins:** `RenderTargetMixin` replaces clear, read/write binding and screen blit behavior. It can postpone clears until a target is actually bound and transitions a bound texture for reading. A new pass must respect those deferred-clear rules and the current bound framebuffer/render pass; bypassing the adapter with direct assumptions about Minecraft's vanilla framebuffer IDs would be unsafe. Source: [RenderTargetMixin](https://github.com/xCollateral/VulkanMod/blob/3adda56191e1a09ce3155de33f8d8334482e089f/src/main/java/net/vulkanmod/mixin/render/target/RenderTargetMixin.java).

4. **Framebuffer constraints:** the examined `Framebuffer` builder creates color and optional depth images, with the relevant attachment/sampling usage flags. Its source explicitly marks multiple color attachments as unsupported and validates that the requested count is at most one. Therefore, we cannot assume that a motion-vector attachment can be added by simply requesting a second color attachment. Depth sampling and vector generation need separate verification. Source: [Framebuffer](https://github.com/xCollateral/VulkanMod/blob/3adda56191e1a09ce3155de33f8d8334482e089f/src/main/java/net/vulkanmod/vulkan/framebuffer/Framebuffer.java).

5. **Image layouts and ownership are explicit:** `RenderPass` transitions color/depth attachments into renderable layouts and records their final layouts; `VulkanImage` tracks its current layout and performs transitions. History textures must have a defined layout at every read/write, be kept alive while submitted GPU work can reference them, and be invalidated/recreated after swapchain changes. Source: [RenderPass](https://github.com/xCollateral/VulkanMod/blob/3adda56191e1a09ce3155de33f8d8334482e089f/src/main/java/net/vulkanmod/vulkan/framebuffer/RenderPass.java), [VulkanImage](https://github.com/xCollateral/VulkanMod/blob/3adda56191e1a09ce3155de33f8d8334482e089f/src/main/java/net/vulkanmod/vulkan/texture/VulkanImage.java), [SwapChain](https://github.com/xCollateral/VulkanMod/blob/3adda56191e1a09ce3155de33f8d8334482e089f/src/main/java/net/vulkanmod/vulkan/framebuffer/SwapChain.java).

6. **Zero-size and resize are first-class failure cases:** the examined swapchain code has an explicit zero-extent path and recreates swapchain-dependent resources on resize/out-of-date results. Do not allocate or dispatch a resolve at zero width/height. Reset temporal history on resize, minimize/restore, swapchain recreation, world change, resource reload, camera cut, and any failed/missing render input.

7. **Projection jitter has no verified safe hook yet:** `RenderSystemMixin` forwards projection-matrix updates into VulkanMod's render system, and `LevelRendererMixin` receives world-render matrices. This identifies places to investigate, not permission to modify every projection globally: jitter must be applied only to the world camera projection and restored before unrelated camera, hand, screen and HUD passes. `GameRendererMixin` in the examined branch primarily adapts shader loading and is not itself an established temporal-resolve boundary.

8. **Motion vectors and composition remain unproven:** this source path did not reveal an existing motion-vector attachment or a verified post-world/pre-hand resolve point. A camera-only reprojection prototype would be expected to fail on independently moving entities, held items, particles, transparency and disocclusion. Do not treat a successful static-scene test as sufficient.

### Consequence for the first prototype

Do **not** add a filter directly onto the swapchain image and do not port another mod's renderer interception blindly. The next implementation milestone is a version-pinned render-path map plus minimal diagnostics for target identity, dimensions, image layout, bound pass, and swapchain generation. Then identify whether Minecraft/VulkanMod permits the world scene to be captured into a separate color/depth target and resolved before hand/HUD composition. Keep the existing baseline path as the fallback and keep this experiment off the stable release until in-game validation is possible.

## Intended implementation phases

### Phase A — Clean base
- Keep a reproducible, single-JAR optimization bundle.
- Keep custom rendering hooks out of the clean base until a concrete design and test plan are approved.
- Validate the bundle structure, nested components, and absence of unplanned project-owned rendering hooks.

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

Earlier fullscreen-pass experiments logged that draw calls were issued, but visual artifacts remained even in a test where both passes were simple copies. Target binding, sample coordinates, and resize/resource lifecycle remain possible causes; the logs do not establish which is responsible. A repeated zero-sized dimension in prior logs is a concrete clue to audit against source and runtime behavior, not a confirmed root cause.

A prior scaling experiment allowed a world to load in one test but still showed artifacts, especially on the held item. That is partial runtime evidence, not proof of a correct image-quality pipeline. These experimental runtime paths are not part of this clean branch; future work should be designed around the renderer's verified capabilities.
