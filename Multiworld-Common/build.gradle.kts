import net.fabricmc.loom.task.RemapJarTask

plugins {

    id ("java-library")
    id ("maven-publish")
	id ("dev.architectury.loom") version "1.6-SNAPSHOT"
	id ("architectury-plugin") version "3.4-SNAPSHOT"
}

architectury {
    common("fabric", "forge")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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

dependencies {
    //minecraft ("com.mojang:minecraft:1.17.1")
    //mappings ("net.fabricmc:yarn:1.17.1+build.65:v2")
    //modImplementation ("net.fabricmc:fabric-loader:0.14.9")
    //modImplementation "net.fabricmc.fabric-api:fabric-api:0.28.5+1.15"
	
	// 1.19.4
    minecraft("com.mojang:minecraft:1.19.4") 
    mappings("net.fabricmc:yarn:1.19.4+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.14.18")
	
	modImplementation("xyz.nucleoid:fantasy:0.4.10+1.19.4")
	modImplementation("curse.maven:cyber-permissions-407695:4640544")
	modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")
	modImplementation("net.fabricmc.fabric-api:fabric-api-deprecated:0.86.1+1.19.4")
	
	setOf(
		"fabric-api-base",
		//"fabric-command-api-v1",
		"fabric-lifecycle-events-v1",
		"fabric-networking-api-v1"
	).forEach {
		// Add each module as a dependency
		modImplementation(fabricApi.module(it, "0.86.1+1.19.4"))
	}
}
