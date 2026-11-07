fn vulkanboost_background_framerate_limit(
    game: &crate::game::Game<'_>,
    focused: bool,
    minimized: bool,
    in_world: bool,
    limit: &mut i32,
) {
    use crate::game::vulkanboost as m;

    if !game.native_on(m::ID) {
        return;
    }

    let settings = m::settings(|key| game.native_setting(m::ID, key));
    *limit = m::effective_background_limit(
        *limit,
        focused,
        minimized,
        in_world,
        settings.background_cap,
    );
}
