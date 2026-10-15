package com.example.androidkeyboard.runtime

/** TEST_ONLY. Commands are fixed diagnostic commands; the destination is an owned evidence path. */
internal fun captureR2ShellDump(command: String, destination: String, execute: (String, String?) -> String) {
    val path = "'" + destination.replace("'", "'\\''") + "'"
    val script = """
        $command > $path 2>&1
        r2_dump_status=${'$'}?
        if [ "${'$'}r2_dump_status" -eq 0 ] && [ -s $path ]; then
            printf 'R2_DUMP_OK\n'
        else
            printf 'R2_DUMP_FAILED:%s\n' "${'$'}r2_dump_status"
        fi
    """.trimIndent() + "\n"
    check(execute("sh", script).trim() == "R2_DUMP_OK") {
        "TEST_ONLY raw dump was not verified as nonempty with a successful command"
    }
}
