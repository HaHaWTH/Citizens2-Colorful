package net.citizensnpcs.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class for getting the caller class.
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class ClassUtil {
    private ClassUtil() {
    }

    private static final Method SUN_REFLECT_REFLECTION_getCallerClass;
    private static final Method STACK_WALKER_getInstance;
    private static final Object RETAIN_CLASS_REFERENCE;
    private static final Class<?> STACK_WALKER_CLASS;
    private static final Class<?> STACK_FRAME_CLASS;
    private static final Method STACK_FRAME_GET_DECLARING_CLASS;
    private static final Method STACK_WALKER_walk;

    static {
        Method md_getCallerClass = null;
        try {
            md_getCallerClass = ReflectionChain.fromClass("sun.reflect.Reflection")
                    .name("getCallerClass")
                    .accessible(true)
                    .param(int.class)
                    .declaredMethod();
        } catch (Exception ignored) {
        }
        SUN_REFLECT_REFLECTION_getCallerClass = md_getCallerClass;

        Method md_getInstance = null;
        Object retainClassReference = null;
        Class<?> stackWalkerClass = null;
        Class<?> stackFrameClass = null;
        Method md_getDeclaringClass = null;
        Method md_walk = null;
        try {
            stackWalkerClass = Class.forName("java.lang.StackWalker");
            md_getInstance = ReflectionChain.fromClass("java.lang.StackWalker")
                    .name("getInstance")
                    .accessible(true)
                    .params(Set.class, int.class)
                    .declaredMethod();

            Class<? extends Enum> stackWalker$Option = (Class<? extends Enum>) Class.forName("java.lang.StackWalker$Option");
            retainClassReference = Enum.valueOf(stackWalker$Option, "RETAIN_CLASS_REFERENCE");
            stackFrameClass = Class.forName("java.lang.StackWalker$StackFrame");
            md_getDeclaringClass = ReflectionChain.fromClass(stackFrameClass)
                    .name("getDeclaringClass")
                    .accessible(true)
                    .declaredMethod();
            md_walk = ReflectionChain.fromClass(stackWalkerClass)
                    .name("walk")
                    .accessible(true)
                    .param(Function.class)
                    .declaredMethod();
        } catch (Exception ignored) {
        }
        STACK_WALKER_getInstance = md_getInstance;
        RETAIN_CLASS_REFERENCE = retainClassReference;
        STACK_WALKER_CLASS = stackWalkerClass;
        STACK_FRAME_CLASS = stackFrameClass;
        STACK_FRAME_GET_DECLARING_CLASS = md_getDeclaringClass;
        STACK_WALKER_walk = md_walk;
    }

    private static final int OVERLOAD_METHOD_DEFAULT_SKIP_FRAMES = 2;
    public static Class<?> getCallerClassPreferSunReflection() {
        return getCallerClassPreferSunReflection(OVERLOAD_METHOD_DEFAULT_SKIP_FRAMES);
    }

    public static Class<?> getCallerClassPreferStackWalker() {
        return getCallerClassPreferStackWalker(OVERLOAD_METHOD_DEFAULT_SKIP_FRAMES);
    }

    /**
     * Returns the class that called the method, preferring to StackWalker if available.
     *
     * @param skipFrames The number of frames to skip.
     * @return The class that called the method.
     */
    public static Class<?> getCallerClassPreferStackWalker(int skipFrames) {
        if (STACK_WALKER_CLASS != null && STACK_FRAME_CLASS != null) {
            try {
                Set<Object> options = Collections.singleton(RETAIN_CLASS_REFERENCE);
                Object stackWalkerInstance = STACK_WALKER_getInstance.invoke(null, options, skipFrames + 1);

                Function<Stream<?>, Class<?>> walkFunction = stream -> {
                    try {
                        return stream
                                .map(frame -> {
                                    try {
                                        return (Class<?>) STACK_FRAME_GET_DECLARING_CLASS.invoke(frame);
                                    } catch (Exception e) {
                                        SneakyThrow.sneaky(e);
                                        return null;
                                    }
                                })
                                .skip(skipFrames + 1)
                                .findFirst()
                                .orElse(null);
                    } catch (Exception e) {
                        SneakyThrow.sneaky(e);
                        return null;
                    }
                };

                return (Class<?>) STACK_WALKER_walk.invoke(stackWalkerInstance, walkFunction);
            } catch (Exception e) {
                SneakyThrow.sneaky(e);
                return null;
            }
        }
        if (SUN_REFLECT_REFLECTION_getCallerClass != null) {
            try {
                return (Class<?>) SUN_REFLECT_REFLECTION_getCallerClass.invoke(null, skipFrames + 2);
            } catch (Exception th) {
                SneakyThrow.sneaky(th);
            }
        }
        throw new IllegalStateException("No supported methods found.");
    }

    /**
     * Returns the class that called the method, preferring to {@link sun.reflect.Reflection} if available.
     *
     * @param skipFrames The number of frames to skip.
     * @return The class that called the method.
     */
    public static Class<?> getCallerClassPreferSunReflection(int skipFrames) {
        if (STACK_WALKER_CLASS != null && STACK_FRAME_CLASS != null) {
            try {
                Set<Object> options = Collections.singleton(RETAIN_CLASS_REFERENCE);
                Object stackWalkerInstance = STACK_WALKER_getInstance.invoke(null, options, skipFrames + 1);

                Function<Stream<?>, Class<?>> walkFunction = stream -> {
                    try {
                        return stream
                                .map(frame -> {
                                    try {
                                        return (Class<?>) STACK_FRAME_GET_DECLARING_CLASS.invoke(frame);
                                    } catch (Exception e) {
                                        SneakyThrow.sneaky(e);
                                        return null;
                                    }
                                })
                                .skip(skipFrames + 1)
                                .findFirst()
                                .orElse(null);
                    } catch (Exception e) {
                        SneakyThrow.sneaky(e);
                        return null;
                    }
                };

                return (Class<?>) STACK_WALKER_walk.invoke(stackWalkerInstance, walkFunction);
            } catch (Exception e) {
                SneakyThrow.sneaky(e);
                return null;
            }
        }
        throw new IllegalStateException("No supported methods found.");
    }
}