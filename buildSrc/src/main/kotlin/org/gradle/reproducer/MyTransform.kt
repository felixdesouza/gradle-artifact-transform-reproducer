package org.gradle.reproducer

import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input

abstract class MyTransform : TransformAction<MyTransform.Parameters> {

    interface Parameters : TransformParameters {
        @get:Input
        val identifier: Property<String>
    }

    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        println("Running transform via: ${parameters.identifier.get()}")
        val input = inputArtifact.get().asFile
        val output = outputs.file("transformed-${input.nameWithoutExtension}.text")

        val transformedText = input.readLines().joinToString(separator = "\n") { line -> "transformed-${line}" }
        output.writeText(transformedText)
    }
}
