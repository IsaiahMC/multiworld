import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency


plugins {
    id ("fabric-loom") version "1.11-SNAPSHOT"
    id ("maven-publish")
	id ("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

base {
    archivesBaseName = "Multiworld-Fabric"
    version = "bundle"
    group = "me.isaiah.mods"
}

dependencies {

	annotationProcessor("com.pkware.jabel:jabel-javac-plugin:1.0.1-1")
	compileOnly("com.pkware.jabel:jabel-javac-plugin:1.0.1-1")

	// 1.20
    minecraft("com.mojang:minecraft:1.20.1") 
    mappings("net.fabricmc:yarn:1.20.1+build.10")
    modImplementation("net.fabricmc:fabric-loader:0.16.9")
	
	// bundle jars
	// include(project(":Multiworld-Fabric-1.18.2"))
	// include(project(":Multiworld-Fabric-1.19.2"))
	// include(project(":Multiworld-Fabric-1.19.4"))
	include(project(":Multiworld-Fabric-1.20.1"))
	// include(project(":Multiworld-Fabric-1.20.4"))
	include(project(":Multiworld-Fabric-1.20.6"))
	include(project(":Multiworld-Fabric-1.21.1"))
	include(project(":Multiworld-Fabric-1.21.4"))
	include(project(":Multiworld-Fabric-1.21.5"))
	include(project(":Multiworld-Fabric-1.21.9"))
}


sourceSets {
    main {
        java {
            // Needs fixing for 1.18:
            exclude("me/isaiah/**/*.java")
            exclude("multiworld/mixin/*.java")
            exclude("**/Multiworld.mixins.json")
            exclude("org/minecarts/**/*.java")
			
			srcDirs("src/main/java") 
        }
        resources {
			 exclude("**/Multiworld.mixins.json")
        }
    }
}

// Jabel
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_21.toString() // for the IDE support
    options.release.set(8)

    javaCompiler.set(
        javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    )
}


tasks.withType<Jar> { duplicatesStrategy = DuplicatesStrategy.INHERIT }

tasks.getByName<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    /*filesMatching("fabric.mod.json") {
        expand(
            mutableMapOf(
                "version" to ext.get("mod_version")
            )
        )
    }*/
}

val remapJar = tasks.getByName<RemapJarTask>("remapJar")

tasks.named("build") { finalizedBy("copyReport2") }

tasks.register<Copy>("copyReport2") {
    from(remapJar)
    into("${project.rootDir}/output")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = project.group.toString()
            artifactId = project.name.lowercase()
            version = project.version.toString()
            
            pom {
                name.set(project.name.lowercase())
                description.set("A concise description of my library")
                url.set("http://www.example.com/")
            }

            artifact(remapJar)
        }
    }

    repositories {
        val mavenUsername: String? by project
        val mavenPassword: String? by project
        mavenPassword?.let {
            maven(url = "https://repo.codemc.io/repository/maven-releases/") {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }
}