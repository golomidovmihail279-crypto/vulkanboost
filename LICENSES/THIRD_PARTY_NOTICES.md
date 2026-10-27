# Third-party notices

VulkanBoost bundles upstream mod JARs using Fabric Loader's nested-JAR mechanism. BetterHurtCam and Ukulib are included as their original upstream JARs; VulkanBoost adds no wrapper integration or resource overrides for BetterHurtCam. The root MIT license covers VulkanBoost's own project files only.

| Component | Bundled version | Upstream license | Project page |
|---|---|---|---|
| Fabric API | 0.140.2+1.21.11 | Apache-2.0 | https://modrinth.com/mod/fabric-api |
| VulkanMod | 0.6.8+1.21.11 | LGPL-3.0-only | https://modrinth.com/mod/vulkanmod |
| Lithium | 0.21.4 | LGPL-3.0-only | https://modrinth.com/mod/lithium |
| FerriteCore | 8.2.0-fabric | MIT | https://modrinth.com/mod/ferrite-core |
| Alternate Current | 1.9.0 | MIT | https://modrinth.com/mod/alternate-current |
| BadOptimizations | 2.4.1 | MIT | https://modrinth.com/mod/badoptimizations |
| ModernFix-mVUS | 5.26.3-build.2 | LGPL-3.0-only | https://modrinth.com/mod/modernfix-mvus |
| ScalableLux | 0.1.6+fabric.c25518a | LGPL-3.0-only | https://modrinth.com/mod/scalablelux |
| Fast Noise | 1.0.36+1.21.11 | MPL-2.0 | https://modrinth.com/mod/zfastnoise |
| Krypton | 0.2.10 | LGPL-3.0-only | https://modrinth.com/mod/krypton |
| C2ME | 0.3.6.0.0 | MIT | https://modrinth.com/mod/c2me-fabric |
| Bura | 0.1.0 alpha | MIT | https://modrinth.com/mod/bura |
| Epitaph Optimizer | 1.0.0* | CC0-1.0 | https://modrinth.com/mod/epitaph-optimizer |
| Dynamic FPS | 3.11.4 | MIT | https://modrinth.com/mod/dynamic-fps |
| FastQuit | 3.1.3+mc1.21.11 | MIT | https://modrinth.com/mod/fastquit |
| Smart Particles | 1.11.10 | MIT | https://modrinth.com/mod/smart-particles |
| BetterHurtCam | 1.12.0+mc1.21.11 (unmodified upstream JAR) | MIT | https://github.com/uku3lig/betterhurtcam/tree/1.21.11 |
| Ukulib | 1.10.2+1.21.11 (unmodified upstream JAR) | MPL-2.0 | https://github.com/uku3lig/ukulib/releases/tag/1.10.2+1.21.11 |
| Cloth Config API | 21.11.153+fabric | LGPL-3.0-only | https://modrinth.com/mod/cloth-config |

PerceptiFrame and Afterburner: Optimized are listed as future candidates but are not bundled in 0.4.2 because the guessed Maven coordinates did not resolve; pin their exact upstream releases before redistribution. Keep these notices and comply with each upstream project's license conditions.

VulkanBoost does not redistribute StackCluster Optimizer, OptimizeMod, or NoRender+ because their listings state ARR (All Rights Reserved). OptiLeaves has been held back because public license listings conflict; do not embed or redistribute it until the license and permission are clarified. Beryl remains excluded: it is ARR, and should not be repackaged without explicit permission.
