import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask

plugins {
	id("fabric-loom") version "1.10-SNAPSHOT"
	`maven-publish`
	id("me.modmuss50.mod-publish-plugin") version "0.7.+"
}

base {
	archivesName.set(project.property("archives_base_name") as String)
}

version = "${project.property("mod_version")}+${project.property("minecraft_version")}"

if (System.getenv("GITHUB_TAG") != null) {
	version = System.getenv("GITHUB_TAG") // I hope I actually keep proper track of this. If not, yell at me.
}

group = project.property("maven_group") as String

allprojects {
	repositories {
		maven("https://maven.parchmentmc.org")
	}
}

subprojects {
	apply(plugin = "fabric-loom")

	rootProject.tasks.getByName<RemapJarTask>("remapJar").nestedJars.from(project.tasks.getByName("remapJar"))
}

repositories {
	maven("https://maven.ladysnake.org/releases") {
		name = "Ladysnake Mods"
	}
	maven("https://maven.cafeteria.dev") {
		content {
			includeGroup("net.adriantodt.fabricmc")
		}
	}
	maven("https://maven.jamieswhiteshirt.com/libs-release") {
		content {
			includeGroup("com.jamieswhiteshirt")
		}
	}
	maven("https://jitpack.io")
	maven("https://maven.shedaniel.me/")
	maven("https://maven.terraformersmc.com/")
	mavenLocal()
	mavenCentral()
	exclusiveContent {
		forRepository {
			maven("https://api.modrinth.com/maven") {
				name = "Modrinth"
			}
		}
		filter {
			includeGroup("maven.modrinth")
		}
	}
}

dependencies {
	//to change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${project.property("parchment_version")}:${project.property("parchment_snapshot")}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

	implementation(project(":apoli", "namedElements"))
	implementation(project(":calio", "namedElements"))

	modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${property("cca_version")}")
	modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${property("cca_version")}")

	modApi("me.shedaniel.cloth:cloth-config-fabric:${project.property("clothconfig_version")}") {
		exclude(group = "net.fabricmc.fabric-api")
	}

	modImplementation("com.terraformersmc:modmenu:${project.property("modmenu_version")}")
	modRuntimeOnly("maven.modrinth:lithium:mc1.21.7-0.18.0-fabric")

	include(implementation("com.moulberry:mixinconstraints:1.0.8")!!)
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand(
			"version" to project.version
		)
	}
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release.set(17)
}

val targetJavaVersion = "17"

java {
	val javaVersion = JavaVersion.toVersion(targetJavaVersion)
	if (JavaVersion.current() < javaVersion) {
		toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
	}

	sourceCompatibility = javaVersion
	targetCompatibility = javaVersion

	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()
}

tasks.jar {
	from("LICENSE")
}

tasks.publish {
	finalizedBy("publishMods")
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.property("archives_base_name") as String
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}

project.extensions.configure<ModPublishExtension>("publishMods") {
	file = project.tasks.named<RemapJarTask>("remapJar").get().archiveFile
	displayName = "Origins: Legacy v${project.version}"
	version = project.version as String
	changelog = System.getenv("RELEASE_DESCRIPTION") ?: ""
	type = ReleaseType.STABLE
	modLoaders.add("fabric")

	dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null
			|| providers.environmentVariable("CURSEFORGE_TOKEN").getOrNull() == null

	modrinth {
		projectId = project.property("publishing.modrinth").toString()
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		minecraftVersions.add(project.property("minecraft_version") as String)

		requires("fabric-api")
		embeds("cloth-config", "cardinal-components-api", "pal", "additionalentityattributes")
	}

	curseforge {
		type = ReleaseType.STABLE
		projectId = project.property("publishing.curseforge").toString()
		accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
		minecraftVersions.add(project.property("minecraft_version") as String)

		requires("fabric-api")
		embeds("cloth-config", "cardinal-components-api", "pal")
	}
}