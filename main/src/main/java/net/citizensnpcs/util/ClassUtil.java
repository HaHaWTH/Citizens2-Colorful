package net.citizensnpcs.util;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Utility class for getting the caller class.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
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
                    .param(java.util.function.Function.class)
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

    /**
     * Returns the class that called the method.
     *
     * @return The class that called the method.
     */
    public static Class<?> getCallerClass() {
        return getCallerClass(1 + 1);
    }

    /**
     * Returns the class that called the method.
     *
     * @param skipFrames The number of frames to skip.
     * @return The class that called the method.
     */
    public static Class<?> getCallerClass(int skipFrames) {
        if (STACK_WALKER_getInstance != null && RETAIN_CLASS_REFERENCE != null && STACK_WALKER_CLASS != null && STACK_FRAME_CLASS != null) {
            try {
                Set<Object> options = java.util.Collections.singleton(RETAIN_CLASS_REFERENCE);
                Object stackWalkerInstance = STACK_WALKER_getInstance.invoke(null, options, skipFrames + 1);

                java.util.function.Function<Stream<?>, Class<?>> walkFunction = stream -> {
                    try {
                        return stream
                                .map(frame -> {
                                    try {
                                        return (Class<?>) STACK_FRAME_GET_DECLARING_CLASS.invoke(frame);
                                    } catch (Exception e) {
                                        return null;
                                    }
                                })
                                .skip(skipFrames + 1)
                                .findFirst()
                                .orElse(null);
                    } catch (Exception e) {
                        return null;
                    }
                };

                return (Class<?>) STACK_WALKER_walk.invoke(stackWalkerInstance, walkFunction);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (SUN_REFLECT_REFLECTION_getCallerClass != null) {
            try {
                return (Class<?>) SUN_REFLECT_REFLECTION_getCallerClass.invoke(null, skipFrames + 2);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}