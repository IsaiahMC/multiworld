import net.fabricmc.loom.task.RemapJarTask
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency


plugins {

    id ("java-library")
    id ("maven-publish")
	id ("dev.architectury.loom") version "1.13-SNAPSHOT"
	id ("architectury-plugin") version "3.4-SNAPSHOT"
}

// Preprocess
extensions.extraProperties["targetVersion"] = "mcCommon"
extensions.extraProperties["inputSourceDir"] = "src/main/java"

val createPreprocessor = rootProject.extra["createPreprocessor"] as groovy.lang.Closure<*>
createPreprocessor.call(project)


architectury {
    common("fabric", "forge", "neoforge")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

base {
    archivesBaseName = "Multiworld"
    version = "-The-API"
    group = "me.isaiah.mods"
}

repositories {
	maven {
            url = uri("https://maven.fabricmc.net/")
        }
	  maven { url = uri("https://maven.nucleoid.xyz/") }
	  maven { url = uri("https://cursemaven.com/") }
	  maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

tasks.withType<Jar> { duplicatesStrategy = DuplicatesStrategy.INCLUDE }

configurations.all {
    resolutionStrategy {
        // Check for updates every build
        cacheChangingModulesFor(0, "seconds")
    }
}

dependencies {

	// 1.20
    minecraft("com.mojang:minecraft:1.20.1") 
    mappings("net.fabricmc:yarn:1.20.1+build.10")
    modImplementation("net.fabricmc:fabric-loader:0.18.3")
	
	modImplementation("xyz.nucleoid:fantasy:0.4.11+1.20-rc1")
	modImplementation("curse.maven:cyber-permissions-407695:4640544")
	modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")
	// modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:0.92.5+1.20.1")
	
	val ic = DefaultExternalModuleDependency(
		"com.javazilla.mods",
		"icommon-fabric-1.21.1",
		"1.21.1",
		null
	).apply {
		isChanging = true // Make sure we get the latest version of iCommon
	}

	modImplementation(ic)


	setOf(
		"fabric-api-base",
		"fabric-lifecycle-events-v1",
		"fabric-networking-api-v1",
		"fabric-events-interaction-v0",
		"fabric-command-api-v2"
	).forEach {
		// Add each module as a dependency
		modImplementation(fabricApi.module(it, "0.92.5+1.20.1"))
	}
}
