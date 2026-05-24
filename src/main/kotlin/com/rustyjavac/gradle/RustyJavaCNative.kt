package com.rustyjavac.gradle

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Locale
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name

object RustyJavaCNative {

    private const val VERSION_TOML_URL =
        "https://raw.githubusercontent.com/CoffeePopStudio/RustyJavaC-Native/main/version.toml"

    private var handle: MethodHandle? = null
    private var loadedPath: String? = null

    data class PlatformArtifact(
        val name: String,
        val lib: String,
        val url: String,
        val sha256: String,
    )

    data class VersionManifest(
        val nativeVersion: String,
        val platforms: Map<String, PlatformArtifact>,
    )

    fun autoResolve(cacheRoot: Path): Path {
        return autoResolve(VERSION_TOML_URL, cacheRoot)
    }

    fun autoResolve(versionTomlUrl: String, cacheRoot: Path): Path {
        val manifest = fetchManifest(versionTomlUrl)
        val platformKey = detectPlatform()
        val artifact = manifest.platforms[platformKey]
            ?: throw IllegalStateException(
                "Unsupported platform: $platformKey. Available: ${manifest.platforms.keys}"
            )

        val cacheDir = cacheRoot.resolve(manifest.nativeVersion).resolve(artifact.name)
        val libFile = cacheDir.resolve(artifact.lib)

        if (!libFile.exists()) {
            downloadArtifact(artifact, libFile)
        }

        return libFile
    }

    private fun detectPlatform(): String {
        val os = System.getProperty("os.name").lowercase(Locale.ROOT)
        val arch = System.getProperty("os.arch").lowercase(Locale.ROOT)

        val osKey = when {
            os.contains("win") -> "windows"
            os.contains("mac") || os.contains("darwin") -> "macos"
            else -> "linux"
        }

        val archKey = when {
            arch.contains("aarch64") || arch.contains("arm64") -> "aarch64"
            arch.contains("amd64") || arch.contains("x86_64") -> "x86_64"
            else -> arch
        }

        return "$osKey-$archKey"
    }

    private fun fetchManifest(url: String): VersionManifest {
        val text = java.net.URI(url).toURL().readText()
        return parseVersionToml(text)
    }

    internal fun parseVersionToml(text: String): VersionManifest {
        var nativeVersion = ""
        var currentPlatform = ""
        val platforms = mutableMapOf<String, PlatformArtifact>()
        var currentName = ""
        var currentLib = ""
        var currentUrl = ""
        var currentSha256 = ""

        for (line in text.lines()) {
            val trimmed = line.trim()

            when {
                trimmed.startsWith("[platforms.") && trimmed.endsWith("]") -> {
                    if (currentPlatform.isNotEmpty()) {
                        platforms[currentPlatform] = PlatformArtifact(
                            currentName, currentLib, currentUrl, currentSha256
                        )
                    }
                    currentPlatform = trimmed.removePrefix("[platforms.").removeSuffix("]")
                    currentName = ""
                    currentLib = ""
                    currentUrl = ""
                    currentSha256 = ""
                }
                trimmed.startsWith("native = ") -> {
                    nativeVersion = extractValue(trimmed)
                }
                trimmed.startsWith("name = ") -> currentName = extractValue(trimmed)
                trimmed.startsWith("lib = ") -> currentLib = extractValue(trimmed)
                trimmed.startsWith("url = ") -> currentUrl = extractValue(trimmed)
                trimmed.startsWith("sha256 = ") -> currentSha256 = extractValue(trimmed)
            }
        }

        if (currentPlatform.isNotEmpty()) {
            platforms[currentPlatform] = PlatformArtifact(
                currentName, currentLib, currentUrl, currentSha256
            )
        }

        return VersionManifest(nativeVersion, platforms)
    }

    private fun extractValue(line: String): String {
        val eq = line.indexOf('=')
        if (eq < 0) return ""
        return line.substring(eq + 1).trim().removeSurrounding("\"")
    }

    private fun downloadArtifact(artifact: PlatformArtifact, dest: Path) {
        dest.parent.createDirectories()

        val tmpFile = dest.parent.resolve("${dest.name}.tmp")
        try {
            println("RustyJavaC: downloading native library from ${artifact.url}")
            val conn = java.net.URI(artifact.url).toURL().openConnection()
            conn.connectTimeout = 30_000
            conn.readTimeout = 300_000
            conn.getInputStream().use { input ->
                Files.copy(input, tmpFile, StandardCopyOption.REPLACE_EXISTING)
            }

            val downloadedSize = Files.size(tmpFile)
            println("RustyJavaC: downloaded $downloadedSize bytes")

            if (artifact.sha256.isNotEmpty()) {
                val actual = sha256(tmpFile)
                if (!artifact.sha256.equals(actual, ignoreCase = true)) {
                    Files.delete(tmpFile)
                    throw IllegalStateException(
                        "SHA-256 mismatch for ${dest.name}: expected ${artifact.sha256}, got $actual"
                    )
                }
            }

            Files.move(tmpFile, dest, StandardCopyOption.REPLACE_EXISTING)
            println("RustyJavaC: cached native library at ${dest.toAbsolutePath()}")
        } catch (e: Exception) {
            try { Files.deleteIfExists(tmpFile) } catch (_: Exception) {}
            throw e
        }
    }

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = Files.readAllBytes(path)
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun load(libPath: String) {
        if (libPath == loadedPath) return
        val resolved = resolveLibraryPath(libPath)
        System.load(resolved)
        val linker = Linker.nativeLinker()
        val symbol = SymbolLookup.loaderLookup()
            .find("rustyjavac_compile")
            .orElseThrow {
                UnsatisfiedLinkError("symbol 'rustyjavac_compile' not found in $resolved")
            }
        handle = linker.downcallHandle(
            symbol,
            FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
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
