# VulkanBoost 0.4.10 for Minecraft 1.21.11

Clarity update for the built-in BetterHurtCam slider. Its percentage now describes the BetterHurtCam camera effect directly instead of saying only “reduce hurt-camera tilt.”

## Slider meaning

- **0% — no visible BetterHurtCam camera effect:** camera tilt behaves like vanilla Minecraft.
- **50% — partial effect:** about half of the hurt-camera tilt is removed.
- **100% — full effect:** hurt-camera tilt is fully removed.
- The slider is labeled **“BetterHurtCam effect: X%”** in English and **“Эффект BetterHurtCam: X%”** in Russian.
- The setup wizard uses the same explanation, and CI checks that the English and Russian labels and help text match the intended meaning.
- This is a UI/wording update: the underlying mapping remains the same (BetterHurtCam multiplier 1.0 at 0%, 0.5 at 50%, and 0.0 at 100%).

## Included since 0.4.9

- Bundles the unmodified upstream BetterHurtCam 1.12.0+mc1.21.11 JAR and required Ukulib 1.10.2+1.21.11 JAR in the single VulkanBoost artifact.
- The VulkanBoost setup wizard and settings screen continue to read and write BetterHurtCam's real configuration.
- Russian translations for BetterHurtCam's UI and hotkeys remain included.
- The non-blurred screen backgrounds from 0.4.9 remain in place to avoid the previously reported `Can only blur once per frame` crash.

## Verification

- CI builds the Java 21 remapped JAR and checks all 19 nested components, including BetterHurtCam and Ukulib, unique mod IDs, the absence of the old competing mixin, the config bridge, matching locale keys, and the slider's 0%/50%/100% semantics.
- A successful build verifies compilation and packaging, not gameplay. The new wording and the 0.4.9 crash fix still need confirmation in a real Minecraft 1.21.11 client.
- Fabric Loader 0.19.3+, Java 21 and a Vulkan 1.2+ compatible GPU/driver are required. Fabric Loader itself is not bundled.

## Installation

Download `VulkanBoost-0.4.10-for-1.21.11.jar` and place it in `mods`. Remove separate copies of mods already bundled in VulkanBoost to avoid duplicate mod IDs.
