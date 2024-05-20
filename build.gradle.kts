import org.gradle.reproducer.MyTransform

repositories {
    maven {
        name = "local external deps"
        metadataSources {
            gradleMetadata()
        }
        setUrl(layout.projectDirectory.dir("external-deps"))
    }
}

val configurationWithExplicitAttribute by configurations.creating {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, "transformed-attribute"))
    }
}

val configurationWithOnlyArtifactTypeAttribute by configurations.creating {
    attributes {
        attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "text")
    }
}

val configurationWithBothAttributes by configurations.creating {
    attributes {
        attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "text")
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, "transformed-attribute"))
    }
}

val configurationWithNoAttribute by configurations.creating

dependencies {
    // this does not work with the artifactType only configuration as it's effectively completely unrelated
//    registerTransform(MyTransform::class.java) {
//        from.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, "untransformed-attribute"))
//        to.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, "transformed-attribute"))
//        parameters {
//            identifier.set("transform-without-artifact-type")
//        }
//    }

    registerTransform(MyTransform::class.java) {
        from.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, "untransformed-attribute"))
            .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "txt")
        to.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, "transformed-attribute"))
            .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, "text")
        parameters {
            identifier.set("transform-with-artifact-type")
        }
    }

    configurationWithExplicitAttribute("org.gradle.reproducer:artifact-transform-reproducer:1.0.0")
    configurationWithOnlyArtifactTypeAttribute("org.gradle.reproducer:artifact-transform-reproducer:1.0.0")
    configurationWithBothAttributes("org.gradle.reproducer:artifact-transform-reproducer:1.0.0")
    configurationWithNoAttribute("org.gradle.reproducer:artifact-transform-reproducer:1.0.0")
}

val resolveConfigurationWithExplicitAttribute =
    tasks.register<DefaultTask>("resolveConfigurationWithExplicitAttribute") {
        doFirst { println(configurationWithExplicitAttribute.singleFile.readText()) }
    }

val resolveConfigurationWithOnlyArtifactTypeAttribute =
    tasks.register<DefaultTask>("resolveConfigurationWithOnlyArtifactTypeAttribute") {
        doFirst { println(configurationWithOnlyArtifactTypeAttribute.singleFile.readText()) }
    }

val resolveConfigurationWithNoAttributesViaArtifactView =
    tasks.register<DefaultTask>("resolveConfigurationWithNoAttributesViaArtifactView") {
        val view = configurationWithNoAttribute.incoming.artifactView {
            attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class, "transformed-attribute"))
        }
        doFirst { println(view.files.singleFile.readText()) }
    }

val resolveConfigurationWithNoAttributesWithoutView =
    tasks.register<DefaultTask>("resolveConfigurationWithNoAttributesWithoutView") {
        doFirst { println(configurationWithNoAttribute.singleFile.readText()) }
    }

tasks.register<Task>("resolveConfigurations") {
    dependsOn(
        resolveConfigurationWithNoAttributesWithoutView,
        resolveConfigurationWithNoAttributesViaArtifactView,
        resolveConfigurationWithOnlyArtifactTypeAttribute,
        resolveConfigurationWithExplicitAttribute
    )
}
