import groovy.json.JsonSlurper

pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven("https://jitpack.io") {
			name = "JitPack"
			content {
				includeGroupAndSubgroups("com.github")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}

	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "com.replaymod.preprocess") {
				useModule("com.github.Fallen-Breath:preprocessor:${requested.version}")
			}
		}
	}
}

rootProject.name = "teamcraft"

@Suppress("UNCHECKED_CAST")
val settings = JsonSlurper().parseText(file("settings.json").readText()) as Map<String, List<String>>

for (minecraftVersion in settings.getValue("versions")) {
	include(":$minecraftVersion")

	project(":$minecraftVersion").apply {
		projectDir = file("versions/$minecraftVersion")
		buildFileName = if (minecraftVersion.startsWith("26.")) {
			"../../build.fabric.gradle.kts"
		} else {
			"../../build.fabric.remap.gradle.kts"
		}
	}
}
