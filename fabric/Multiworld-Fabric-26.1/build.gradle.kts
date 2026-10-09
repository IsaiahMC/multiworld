import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency


plugins {
    id ("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    id ("maven-publish")
	id ("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

// Preprocess
extensions.extraProperties["targetVersion"] = "mc261"
extensions.extraProperties["inputSourceDir"] = "${rootProject.projectDir}/Multiworld-Common/src/main/java"
extensions.extraProperties["excludedFiles"] =
    listOf("java/me/isaiah/multiworld/command/GameruleCommand.java")
    //      java/me/isaiah/multiworld/command/GameruleCommand.java


val createPreprocessor = rootProject.extra["createPreprocessor"] as groovy.lang.Closure<*>
createPreprocessor.call(project)

base {
    archivesName = "Multiworld-Fabric"
    version = "26.1"
    group = "me.isaiah.mods"
}

repositories {
	// Fantasy 1.21
	mavenLocal()
}

configurations.all {
    resolutionStrategy {
        // Check for updates every build
        cacheChangingModulesFor(0, "seconds")
    }
}


dependencies {
	// annotationProcessor("com.pkware.jabel:jabel-javac-plugin:1.0.1-1")
	// compileOnly("com.pkware.jabel:jabel-javac-plugin:1.0.1-1")
	
	// Minecraft 26.1
    minecraft("com.mojang:minecraft:26.1.2") 
    implementation("net.fabricmc:fabric-loader:0.19.5")
	
	// fantasy snapshot: https://pisaiah.com/maven-repo/
	include("xyz.nucleoid:fantasy:0.8.0-beta.1+26.1.1")
	implementation("xyz.nucleoid:fantasy:0.8.0-beta.1+26.1.1")
	implementation("me.lucko:fabric-permissions-api:0.7.0")

	compileOnly("curse.maven:cyber-permissions-407695:7068279")

	implementation("net.fabricmc.fabric-api:fabric-api:0.145.4+26.1.2")

	
	val ic = DefaultExternalModuleDependency(
		"com.javazilla.mods",
		"icommon-fabric-1.21.9",
		"1.21.9",
		null
	).apply {
		isChanging = true // Make sure we get the latest version of iCommon
	}

	implementation(ic)
}

// Note: dimapi is not needed for 1.21
sourceSets {
    main {
        java {
            srcDir("${rootProject.projectDir}/Multiworld-Common/src/main/java/com")
            srcDir("src/main/java")
			exclude("**/dimapi/*.java")
			exclude("**/dimapi/*.class")
			exclude("**/dimapi/mixin/*.java")
			exclude("**/dimapi/mixin/*.class")



			// Seems to be fixed in vanilla 1.21.11
			exclude("**/**/MixinGameruleCommand.java")
        }
        resources {
            srcDir("${rootProject.projectDir}/Multiworld-Common/src/main/resources")
			
			exclude("**/dimapi/*.java")
			exclude("**/dimapi/*.class")
			exclude("**/dimapi/mixin/*.java")
			exclude("**/dimapi/mixin/*.class")
        }
    }
}

// Jabel
/*
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_25.toString() // for the IDE support
    options.release.set(25)

    javaCompiler.set(
        javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    )
}
*/

tasks.withType<Jar> { duplicatesStrategy = DuplicatesStrategy.INHERIT }

// val remapJar = tasks.getByName<RemapJarTask>("remapJar")

tasks.named("build") { finalizedBy("copyReport2") }

tasks.register<Copy>("copyReport2") {
    from(tasks.named("jar"))
    into("${project.rootDir}/output")
}

/*
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

            // artifact(jar)
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
*/