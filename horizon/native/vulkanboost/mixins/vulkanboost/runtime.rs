//! VulkanBoost's Horizon-native policy and settings.
//!
//! This file is also the standalone Cargo library used by the unit tests.

pub const ID: &str = "vulkanboost";
pub const NAME: &str = "VulkanBoost: Horizon Edition";
pub const VERSION: &str = "0.1.0";
pub const DESCRIPTION: &str =
    "Background frame-rate cap to reduce resource use while the game is unfocused.";

pub const BACKGROUND_CAPS: [i32; 4] = [15, 30, 45, 60];
pub const DEFAULT_BACKGROUND_CAP: i32 = 30;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct Settings {
    pub background_cap: i32,
}

impl Default for Settings {
    fn default() -> Self {
        Self {
            background_cap: DEFAULT_BACKGROUND_CAP,
        }
    }
}

impl Settings {
    pub fn read(get: impl Fn(&str) -> Option<String>) -> Self {
        let background_cap = get("background_cap")
            .and_then(|value| value.trim().parse::<i32>().ok())
            .filter(|value| BACKGROUND_CAPS.contains(value))
            .unwrap_or(DEFAULT_BACKGROUND_CAP);

        Self { background_cap }
    }

    pub fn labels(&self) -> Vec<String> {
        vec![format!("Background FPS cap: {} FPS", self.background_cap)]
    }

    pub fn click(&mut self, button: usize) {
        if button != 0 {
            return;
        }

        let current = BACKGROUND_CAPS
            .iter()
            .position(|value| *value == self.background_cap)
            .unwrap_or(1);
        self.background_cap = BACKGROUND_CAPS[(current + 1) % BACKGROUND_CAPS.len()];
    }

    pub fn stored(&self) -> [(&'static str, String); 1] {
        [("background_cap", self.background_cap.to_string())]
    }
}

/// Applies the configured cap only while a world is open and the window is
/// unfocused or minimized. It can only lower Horizon's chosen limit.
pub fn effective_background_limit(
    vanilla_limit: i32,
    focused: bool,
    minimized: bool,
    in_world: bool,
    background_cap: i32,
) -> i32 {
    if !in_world || (focused && !minimized) {
        return vanilla_limit;
    }

    let cap = background_cap.max(1);
    if vanilla_limit < 1 {
        cap
    } else {
        vanilla_limit.min(cap)
    }
}

thread_local! {
    static SETTINGS: std::cell::Cell<Option<Settings>> = const {
        std::cell::Cell::new(None)
    };
}

/// Reads settings once through Horizon's own setting store.
pub fn settings(get: impl Fn(&str) -> Option<String>) -> Settings {
    SETTINGS.with(|stored| {
        let value = stored.get().unwrap_or_else(|| Settings::read(get));
        stored.set(Some(value));
        value
    })
}

/// Updates the in-memory copy after a button click in the settings page.
pub fn set_settings(settings: Settings) {
    SETTINGS.with(|stored| stored.set(Some(settings)));
}

#[cfg(test)]
mod tests {
    use super::*;

    fn stored<'a>(pairs: &'a [(&'a str, &'a str)]) -> impl Fn(&str) -> Option<String> + 'a {
        |key| {
            pairs
                .iter()
                .find(|pair| pair.0 == key)
                .map(|pair| pair.1.to_owned())
        }
    }

    #[test]
    fn default_cap_is_thirty_fps() {
        assert_eq!(Settings::read(stored(&[])), Settings::default());
    }

    #[test]
    fn reads_only_supported_caps() {
        assert_eq!(
            Settings::read(stored(&[("background_cap", "45\n")])),
            Settings { background_cap: 45 }
        );
        assert_eq!(
            Settings::read(stored(&[("background_cap", "999")])),
            Settings::default()
        );
    }

    #[test]
    fn click_cycles_through_supported_caps() {
        let mut settings = Settings::default();
        settings.click(0);
        assert_eq!(settings.background_cap, 45);
        settings.click(0);
        assert_eq!(settings.background_cap, 60);
        settings.click(0);
        assert_eq!(settings.background_cap, 15);
    }

    #[test]
    fn background_cap_applies_when_unfocused() {
        assert_eq!(effective_background_limit(260, false, false, true, 30), 30);
    }

    #[test]
    fn background_cap_applies_when_minimized() {
        assert_eq!(effective_background_limit(260, true, true, true, 30), 30);
    }

    #[test]
    fn foreground_gameplay_is_unchanged() {
        assert_eq!(effective_background_limit(260, true, false, true, 30), 260);
    }

    #[test]
    fn menus_are_unchanged() {
        assert_eq!(
            effective_background_limit(260, false, false, false, 30),
            260
        );
    }

    #[test]
    fn a_lower_horizon_limit_is_preserved() {
        assert_eq!(effective_background_limit(20, false, false, true, 30), 20);
    }
}
