package com.example.androidkeyboard.engines.android

import android.content.Context
import java.io.File
import java.io.IOException

/**
 * Installs the immutable libchewing system dictionaries from APK assets into
 * app-private, non-backed-up storage. libchewing requires real filesystem paths;
 * AssetManager streams cannot be passed directly to chewing_new2().
 */
object LibChewingDataInstaller {

    data class Paths(
        val systemDir: File,
        val userFile: File,
    )

    private val REQUIRED_FILES = listOf(
        "tsi.dat",
        "word.dat",
        "swkb.dat",
        "symbols.dat",
    )

    fun ensureInstalled(context: Context): Paths {
        val root = File(context.noBackupFilesDir, "libchewing")
        val systemDir = File(root, "system").apply {
            check(mkdirs() || isDirectory) { "Unable to create libchewing system directory" }
        }
        val userDir = File(root, "user").apply {
            check(mkdirs() || isDirectory) { "Unable to create libchewing user directory" }
        }

        REQUIRED_FILES.forEach { name -> installAsset(context, systemDir, name) }

        return Paths(
            systemDir = systemDir,
            userFile = File(userDir, "userdict.dat"),
        )
    }

    private fun installAsset(context: Context, systemDir: File, name: String) {
        val destination = File(systemDir, name)
        if (destination.isFile && destination.length() > 0L) return

        val temp = File(systemDir, ".$name.tmp")
        try {
            context.assets.open("libchewing/$name").use { input ->
                temp.outputStream().buffered().use { output -> input.copyTo(output) }
            }

            if (destination.exists() && !destination.delete()) {
                throw IOException("Unable to replace ${destination.absolutePath}")
            }
            if (!temp.renameTo(destination)) {
                temp.copyTo(destination, overwrite = true)
                if (!temp.delete()) temp.deleteOnExit()
            }
        } finally {
            if (temp.exists()) temp.delete()
        }

        check(destination.isFile && destination.length() > 0L) {
            "libchewing dictionary asset is missing or empty: $name"
        }
    }
}
