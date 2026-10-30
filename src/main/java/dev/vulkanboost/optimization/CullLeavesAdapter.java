package dev.vulkanboost.optimization;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Adapter for Cull Leaves' documented MidnightConfig fields.
 * The upstream config's write hook refreshes affected world rendering.
 */
public final class CullLeavesAdapter implements OptimizationAdapter {
    private static final String MOD_ID = "cullleaves";
    private static final String CONFIG_CLASS = "eu.midnightdust.cullleaves.config.CullLeavesConfig";
    private static final String LIBRARY_CLASS = "eu.midnightdust.lib.config.MidnightConfig";

    @Override
    public String id() {
        return MOD_ID;
    }

    @Override
    public boolean isInstalled() {
        return FabricLoader.getInstance().isModLoaded(MOD_ID);
    }

    @Override
    public boolean isSupported() {
        if (!isInstalled()) {
            return false;
        }
        try {
            Field enabled = Class.forName(CONFIG_CLASS).getField("enabled");
            if (enabled.getType() != boolean.class || !Modifier.isStatic(enabled.getModifiers())) {
                return false;
            }

            Method write = Class.forName(LIBRARY_CLASS).getMethod("write", String.class);
            return Modifier.isStatic(write.getModifiers());
        } catch (ReflectiveOperationException | LinkageError exception) {
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        try {
            return configField().getBoolean(null);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            throw new IllegalStateException("Cull Leaves config field is unavailable", exception);
        }
    }

    @Override
    public void setEnabled(boolean enabled) throws ReflectiveOperationException {
        if (!isSupported()) {
            throw new IllegalStateException("Cull Leaves adapter is not available in this installation");
        }
        configField().setBoolean(null, enabled);
        Class<?> library = Class.forName(LIBRARY_CLASS);
        library.getMethod("write", String.class).invoke(null, MOD_ID);
    }

    private static Field configField() throws ReflectiveOperationException {
        return Class.forName(CONFIG_CLASS).getField("enabled");
    }
}
