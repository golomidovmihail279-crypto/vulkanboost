package dev.vulkanboost.optimization;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Adapter for Dynamic FPS' public configuration methods.
 * onConfigChanged() is called so the mod reapplies its state and saves it.
 */
public final class DynamicFpsAdapter implements OptimizationAdapter {
    private static final String MOD_ID = "dynamic_fps";
    private static final String CONFIG_CLASS = "dynamic_fps.impl.config.DynamicFPSConfig";
    private static final String MOD_CLASS = "dynamic_fps.impl.DynamicFPSMod";

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
            Field instanceField = Class.forName(CONFIG_CLASS).getField("INSTANCE");
            if (!Modifier.isStatic(instanceField.getModifiers())) {
                return false;
            }

            Object config = instanceField.get(null);
            if (config == null) {
                return false;
            }

            Method enabled = config.getClass().getMethod("enabled");
            Method setter = config.getClass().getMethod("setEnabled", boolean.class);
            Method save = config.getClass().getMethod("save");
            Method onConfigChanged = Class.forName(MOD_CLASS).getMethod("onConfigChanged");

            return !Modifier.isStatic(enabled.getModifiers())
                    && !Modifier.isStatic(setter.getModifiers())
                    && !Modifier.isStatic(save.getModifiers())
                    && Modifier.isStatic(onConfigChanged.getModifiers());
        } catch (ReflectiveOperationException | LinkageError exception) {
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        try {
            Object config = configInstance();
            return (boolean) config.getClass().getMethod("enabled").invoke(config);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            throw new IllegalStateException("Dynamic FPS config API is unavailable", exception);
        }
    }

    @Override
    public void setEnabled(boolean enabled) throws ReflectiveOperationException {
        if (!isSupported()) {
            throw new IllegalStateException("Dynamic FPS adapter is not available in this installation");
        }
        Object config = configInstance();
        config.getClass().getMethod("setEnabled", boolean.class).invoke(config, enabled);
        Class.forName(MOD_CLASS).getMethod("onConfigChanged").invoke(null);
    }

    private static Object configInstance() throws ReflectiveOperationException {
        Field instance = Class.forName(CONFIG_CLASS).getField("INSTANCE");
        return instance.get(null);
    }
}
