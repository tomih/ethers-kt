package io.ethers.abigen

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.ethers.abigen.reader.JsonAbiReaderRegistry
import java.io.File
import java.nio.file.Files
import kotlin.reflect.KClass

/**
 * Handles building and compiling contract wrappers generated by [AbiContractBuilder].
 * */
object AbigenCompiler {
    private const val GENERATED_CLASS_PACKAGE = "io.ethers.abigen.test"
    private val GENERATED_CLASS_DEST_DIR = GENERATED_CLASS_PACKAGE.replace('.', '/')
    private val SOURCE_FOLDER = Files.createTempDirectory("abigen-test").toFile().apply { deleteOnExit() }

    /**
     * Build the contract wrapper of ABI located at [resourceName], compile it, and return the generated class. The
     * generated class can be inspected via reflection to verify that the generated code structure is correct.
     * */
    fun compile(resourceName: String): KClass<*> {
        val abiUrl = AbigenCompiler::class.java.getResource(resourceName)
            ?: throw IllegalArgumentException("Resource not found: $resourceName")

        val abi = JsonAbiReaderRegistry.readAbi(abiUrl) ?: throw IllegalArgumentException("Invalid ABI: $resourceName")

        val contractName = resourceName.removeSuffix(".json").split("/").last()
        val outFile = File(SOURCE_FOLDER, "$GENERATED_CLASS_DEST_DIR/$contractName.kt")

        AbiContractBuilder(
            contractName,
            GENERATED_CLASS_PACKAGE,
            SOURCE_FOLDER,
            abi,
            emptyMap(),
        ).run()

        val result = KotlinCompilation().apply {
            sources = listOf(SourceFile.fromPath(outFile))
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        if (result.exitCode != KotlinCompilation.ExitCode.OK) {
            throw IllegalStateException("Compilation failed: ${result.messages}")
        }

        return result.classLoader.loadClass("$GENERATED_CLASS_PACKAGE.$contractName").kotlin
    }
}
