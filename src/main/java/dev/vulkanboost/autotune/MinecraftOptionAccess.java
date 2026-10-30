package dev.vulkanboost.autotune;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;
import java.util.OptionalInt;

/**
 * Small reflection bridge for vanilla integer options.
 * If a Minecraft version changes its option API, the tuner safely skips it.
 */
public final class MinecraftOptionAccess {
    private MinecraftOptionAccess() {
    }

    public static OptionalInt read(Minecraft client, String optionName) {
        try {
            Object option = optionObject(client, optionName);
            Object value = option.getClass().getMethod("get").invoke(option);
            return value instanceof Number number
                    ? OptionalInt.of(number.intValue())
                    : OptionalInt.empty();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return OptionalInt.empty();
        }
    }

    public static boolean write(Minecraft client, String optionName, int value)
            throws ReflectiveOperationException {
        Object option = optionObject(client, optionName);
        Method setter = null;
        for (Method method : option.getClass().getMethods()) {
            if (method.getName().equals("set") && method.getParameterCount() == 1) {
                Class<?> parameter = method.getParameterTypes()[0];
                if (parameter == int.class || parameter == Integer.class || parameter == Object.class) {
                    setter = method;
                    break;
                }
            }
        }
        if (setter == null) {
            return false;
        }
        setter.invoke(option, value);
        return true;
    }

    public static void save(Minecraft client) throws ReflectiveOperationException {
        client.options.getClass().getMethod("save").invoke(client.options);
    }

    private static Object optionObject(Minecraft client, String optionName)
            throws ReflectiveOperationException {
        return client.options.getClass().getMethod(optionName).invoke(client.options);
    }
}
