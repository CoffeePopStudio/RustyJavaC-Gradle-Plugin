package com.rustyjavac.gradle

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle
import java.util.Locale

object RustyJavaCNative {

    private var handle: MethodHandle? = null
    private var loadedPath: String? = null

    fun load(libPath: String) {
        if (libPath == loadedPath) return
        val resolved = resolveLibraryPath(libPath)
        System.load(resolved)
        val linker = Linker.nativeLinker()
        val symbol = SymbolLookup.loaderLookup()
            .find("rustyjavac_compile")
            .orElseThrow { UnsatisfiedLinkError("symbol 'rustyjavac_compile' not found in $resolved") }
        handle = linker.downcallHandle(
            symbol,
            FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT
            )
        )
        loadedPath = libPath
    }

    private fun resolveLibraryPath(path: String): String {
        val os = System.getProperty("os.name").lowercase(Locale.ROOT)
        val ext = when {
            path.endsWith(".dll") || path.endsWith(".so") || path.endsWith(".dylib") -> ""
            os.contains("win") -> ".dll"
            os.contains("mac") -> ".dylib"
            else -> ".so"
        }
        return path + ext
    }

    fun compile(sources: List<String>, outputDir: String, javaVersion: Int): Int {
        val handle = checkNotNull(handle) { "RustyJavaCNative not loaded — call load() first" }

        Arena.ofConfined().use { arena ->
            val argv = arena.allocate(ValueLayout.ADDRESS, sources.size.toLong())
            sources.forEachIndexed { i, path ->
                argv.setAtIndex(ValueLayout.ADDRESS, i.toLong(), arena.allocateFrom(path))
            }
            val outDir = arena.allocateFrom(outputDir)
            return handle.invoke(argv, sources.size, outDir, javaVersion) as Int
        }
    }
}
