import org.gradle.api.publish.maven.MavenPublication

plugins {
	java
	`maven-publish`
	id("net.fabricmc.fabric-loom-remap")
	id("com.replaymod.preprocess")
}

fun projectProperty(name: String) = property(name).toString()

val modmenu_version = projectProperty("modmenu_version")
val minecraftVersion = projectProperty("minecraft_version")
val loaderVersion = projectProperty("loader_version")
val fabricApiVersion = projectProperty("fabric_api_version")
val modVersion = projectProperty("mod_version")
val modLoader = projectProperty("mod_loader")
val mavenGroup = projectProperty("maven_group")
val archivesBaseName = projectProperty("archives_base_name")
val testClientCount = providers.gradleProperty("test_client_count").get().toInt()

configurations.configureEach {
	resolutionStrategy.force("net.fabricmc:fabric-loader:$loaderVersion")
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
	modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
	modImplementation("com.terraformersmc:modmenu:${modmenu_version}")

	// Minecraft ships ICU4J at runtime; datagen uses its Chinese transliterator.
	compileOnly("com.ibm.icu:icu4j:78.3")
	compileOnly("org.jspecify:jspecify:1.0.0")
}

loom {
	runs {
		for (i in 1..testClientCount) {
			create("testClient$i") {
				client()

				// relative to the version project dir (versions/<id>)
				runDir("../../run/${project.name}/client$i")
				ideConfigGenerated(true)
				programArgs("--username", "DevPlayer$i")
			}
		}
	}
}

// IntelliJ replaces dots in Gradle subproject names with underscores when it
// creates module names, while Loom 1.15 keeps the dots in generated run
// configurations. Repair that mismatch and generate one compound launcher per
// Minecraft version after every IDEA sync.
tasks.named("ideaSyncTask") {
	outputs.upToDateWhen { false }
	doLast {
		val runConfigurations = rootProject.file(".idea/runConfigurations")
		runConfigurations.mkdirs()

		val loomModuleName = "${rootProject.name}.${project.name}.main"
		val ideaModuleName = "${rootProject.name}.${project.name.replace('.', '_')}.main"
		runConfigurations.listFiles { file -> file.extension == "xml" }?.forEach { file ->
			val original = file.readText()
			val repaired = original.replace(
				"<module name=\"$loomModuleName\"/>",
				"<module name=\"$ideaModuleName\"/>"
			)
			if (repaired != original) {
				file.writeText(repaired)
			}
		}

		val compoundName = "Minecraft Test Clients (${project.path})"
		val compoundXml = buildString {
			appendLine("<component name=\"ProjectRunConfigurationManager\">")
			appendLine("  <configuration default=\"false\" name=\"$compoundName\" type=\"CompoundRunConfigurationType\">")
			for (i in 1..testClientCount) {
				appendLine("    <toRun name=\"Minecraft Test Client$i (${project.path})\" type=\"Application\" />")
			}
			appendLine("    <method v=\"2\" />")
			appendLine("  </configuration>")
			appendLine("</component>")
		}
		val safeProjectName = project.name.replace('.', '_')
		runConfigurations.resolve("Minecraft_Test_Clients_$safeProjectName.xml").writeText(compoundXml)
	}
}

fabricApi {
	configureDataGeneration {
		client = true
	}
}

sourceSets.main {
	resources {
		exclude(".cache/**")

		// The datagen output lives at the root tree; the main project packs it
		// and serves it to every older node through preprocessResources.
		val mainProjectName = rootProject.file("versions/mainProject").readText().trim()
		if (project.name == mainProjectName) {
			srcDir(rootProject.file("src/main/generated"))
		} else {
			setSrcDirs(srcDirs.filterNot { it == file("src/main/generated") })
		}
	}
}

group = mavenGroup
// <name>-<version>-<loader>-<fabric_api_version>-mc
version = "$modVersion-$modLoader-$fabricApiVersion-mc"
base.archivesName.set(archivesBaseName)

tasks.processResources {
	val properties = mapOf(
		"version" to modVersion,
		"loader_dependency" to projectProperty("loader_dependency"),
		"minecraft_dependency" to projectProperty("minecraft_dependency"),
		"java_dependency" to projectProperty("java_dependency"),
		"java_compatibility" to "JAVA_21",
	)

	inputs.properties(properties)
	filesMatching(listOf("fabric.mod.json", "teamcraft.mixins.json")) {
		expand(properties)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release.set(21)
	options.compilerArgs.add("-Xlint:-processing")
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	from(rootProject.file("LICENSE")) {
		rename { "${it}_$archivesBaseName" }
	}
}

publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
			artifactId = archivesBaseName
			version = "$modVersion-mc$minecraftVersion"
		}
	}
}
