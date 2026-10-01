plugins {
	`maven-publish`
	id("net.fabricmc.fabric-loom") version "1.15.3" apply false
	id("net.fabricmc.fabric-loom-remap") version "1.15.3" apply false

	// Fallen-Breath's fork supports mixed obfuscated and unobfuscated projects.
	id("com.replaymod.preprocess") version "c5abb4fb12"
}

preprocess {
	strictExtraMappings = false

	val mc121 = createNode("1.21", 1_21_00, "")
	val mc1211 = createNode("1.21.1", 1_21_01, "")
	val mc1213 = createNode("1.21.3", 1_21_03, "")
	val mc1214 = createNode("1.21.4", 1_21_04, "")
	val mc1215 = createNode("1.21.5", 1_21_05, "")
	val mc1218 = createNode("1.21.8", 1_21_08, "")
	val mc12110 = createNode("1.21.10", 1_21_10, "")
	val mc12111 = createNode("1.21.11", 1_21_11, "")
	val mc2612 = createNode("26.1.2", 26_01_02, "")
	val mc262 = createNode("26.2", 26_02_00, "")
	val mc263 = createNode("26.3", 26_03_00, "")

	// The source tree is based on 26.2, so links point towards older versions.
	mc263.link(mc262, file("versions/mapping-26.2-26.3.txt"))
	mc262.link(mc2612, file("versions/mapping-26.1.2-26.2.txt"))
	mc2612.link(mc12111, file("versions/mapping-1.21.11-26.1.2.txt"))
	mc12111.link(mc12110, file("versions/mapping-1.21.10-1.21.11.txt"))
	mc12110.link(mc1218, file("versions/mapping-1.21.8-1.21.10.txt"))
	mc1218.link(mc1215, file("versions/mapping-1.21.5-1.21.8.txt"))
	mc1215.link(mc1214, file("versions/mapping-1.21.4-1.21.5.txt"))
	mc1214.link(mc1213, file("versions/mapping-1.21.3-1.21.4.txt"))
	mc1213.link(mc1211, file("versions/mapping-1.21.1-1.21.3.txt"))
	mc1211.link(mc121, file("versions/mapping-1.21-1.21.1.txt"))

	for (node in getNodes()) {
		findProject(node.project)?.extensions?.extraProperties?.set("mcVersion", node.mcVersion)
	}
}

subprojects {
	tasks.withType<Jar>().configureEach {
		if (name == "sourcesJar") {
			tasks.findByName("preprocessResources")?.let {
				dependsOn(it)
			}
		}
	}
}

val versionProjects = subprojects.toList()

tasks.register("buildAndGather") {
	group = "build"
	description = "Builds every supported Minecraft version and gathers release jars."

	versionProjects.forEach { versionProject ->
		evaluationDependsOn(versionProject.path)
		dependsOn(versionProject.tasks.named("build"))
	}

	doLast {
		val outputDirectory = layout.buildDirectory
			.dir("release")
			.get()
			.asFile

		delete(outputDirectory)
		outputDirectory.mkdirs()

		versionProjects.forEach { versionProject ->
			copy {
				// Gather only the jar of the current naming, so stale jars left
				// in libs/ by earlier builds are never collected.
				from(versionProject.layout.buildDirectory.dir("libs")) {
					include("${versionProject.property("archives_base_name")}-${versionProject.version}.jar")
				}
				into(outputDirectory)
			}
		}
	}
}
