package android.util
object Log {
    @JvmStatic fun e(tag: String, message: String, error: Throwable? = null): Int {
        System.err.println("$tag: $message ${error?.javaClass?.simpleName.orEmpty()}")
        return 0
    }
}
