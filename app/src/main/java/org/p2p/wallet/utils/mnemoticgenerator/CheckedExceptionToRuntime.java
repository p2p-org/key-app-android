package org.p2p.wallet.utils.mnemoticgenerator;

public final class CheckedExceptionToRuntime {
    public interface Func<T> {
        T run() throws Exception;
    }

    public interface Action {
        void run() throws Exception;
    }

    /**
     * Promotes any exceptions thrown to {@link RuntimeException}
     *
     * @param function Function to run
     * @param <T>      Return type
     * @return returns the result of the function
     */
    public static <T> T toRuntime(final Func<T> function) {
        try {
            return function.run();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Promotes any exceptions thrown to {@link RuntimeException}
     *
     * @param function Function to run
     */
    public static void toRuntime(final Action function) {
        try {
            function.run();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
