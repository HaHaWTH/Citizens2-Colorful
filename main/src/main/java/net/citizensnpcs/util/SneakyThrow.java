package net.citizensnpcs.util;

public class SneakyThrow {
    private SneakyThrow() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void sneaky(Throwable throwable) throws T {
        throw (T) throwable;
    }
}
