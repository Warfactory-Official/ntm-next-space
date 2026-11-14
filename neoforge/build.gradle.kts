import java.nio.file.Files
import java.nio.file.StandardCopyOption

import java.util.zip.ZipFile

plugins {
    java
    id("net.neoforged.moddev") version "2.0.148"
}

version = property("mod_version")!!
group = property("mod_group_id")!!
val ntmVersion = "${property("hbm_version")}+mc${property("minecraft_version")}"

base {
    archivesName = "${property("mod_id")}-neoforge"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.caffeinemc.net/releases") {
        content { includeGroup("net.caffeinemc") }
    }
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.blamejared.com/")
    maven("https://repo.warfactory.co/releases") {
        name = "Warfactory"
        content {
            includeGroup("dev.engine_room")
            includeModule("com.hbm", "ntm-next-neoforge")
            includeGroup("net.irisshaders")
        }
    }
    maven("https://repo.warfactory.co/snapshots") {
        content { includeGroup("mov.movblock.tenon") }
    }
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
    maven("https://cursemaven.com") {
        content { includeGroup("curse.maven") }
    }
    maven("https://maven.squiddev.cc") {
        content { includeGroup("cc.tweaked") }
    }
    maven("https://maven2.bai.lol") {
        content {
            includeGroup("mcp.mobius.waila")
            includeGroup("lol.bai")
        }
    }
    maven("https://maven.shedaniel.me/") {
        content {
            includeGroup("me.shedaniel")
            includeGroup("me.shedaniel.cloth")
            includeGroup("dev.architectury")
        }
    }
}

val ntmJar: File = configurations.detachedConfiguration(dependencies.create("com.hbm:ntm-next-neoforge:$ntmVersion"))
    .apply { isTransitive = false }.singleFile
val ntmAccess: File = rootProject.file(".gradle-ntm/${project.name}").also { directory ->
    directory.mkdirs()
    ZipFile(ntmJar).use { jar ->
        for (entry in listOf("META-INF/accesstransformer.cfg", "interface-injection.json")) {
            val bytes = jar.getInputStream(jar.getEntry(entry)).readBytes()
            directory.resolve(entry.substringAfterLast('/')).writeBytes(bytes)
        }
    }
}

val javacInternalExports = listOf("api", "code", "comp", "jvm", "main", "model", "parser", "tree", "util")
    .map { "--add-exports=jdk.compiler/com.sun.tools.javac.$it=ALL-UNNAMED" }
val javacInternalOpens = listOf("main", "comp", "parser", "code", "jvm")
    .map { "--add-opens=jdk.compiler/com.sun.tools.javac.$it=ALL-UNNAMED" }

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED")
    options.compilerArgs.add("--add-modules=jdk.incubator.vector")
    options.compilerArgs.addAll(javacInternalExports)
    options.forkOptions.jvmArgs!!.addAll(
        listOf("-XX:+UnlockExperimentalVMOptions", "-XX:-UseJVMCICompiler", "-XX:-EnableJVMCI")
    )
    options.forkOptions.memoryMaximumSize = "2g"
    options.forkOptions.jvmArgs = (options.forkOptions.jvmArgs ?: emptyList()) + listOf(
        "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED"
    )
    options.isFork = true
    options.forkOptions.jvmArgs!!.addAll(javacInternalExports)
    options.forkOptions.jvmArgs!!.addAll(javacInternalOpens)
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.add("-Xplugin:tenon-asm")
}

sourceSets.main {
    resources.exclude("assets/**/*.obj")
}

val pin = property("hbm_version") as String
val expansion = mapOf("version" to version, "hbm_version" to pin, "hbm_range" to "[$pin,$pin.1)",
    "minecraft_version" to property("minecraft_version"), "minecraft_next_version" to property("minecraft_next_version"))

tasks.named<ProcessResources>("processResources") {
    inputs.properties(expansion)
    filesMatching("META-INF/neoforge.mods.toml") { expand(expansion) }
}

tasks.named<Jar>("jar") {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
    includeEmptyDirs = false
    exclude("assets/**/*.obj")
}

val crankshaftVersion = property("crankshaft_version") as String

fun nestedModClasses(
    project: Project, taskName: String, coordinates: String, pattern: String,
    directory: String, fileName: String
): FileCollection {
    val archives = project.configurations.create("${taskName}Archive") {
        isCanBeConsumed = false
        isTransitive = false
    }
    project.dependencies.add(archives.name, coordinates)
    val output = project.layout.buildDirectory.dir("$directory/${coordinates.substringAfterLast(':')}")
    val extracted = project.layout.buildDirectory.dir("$directory/archive")
    val unpack = project.tasks.register<Sync>("${taskName}Archive") {
        from(archives.elements.map { jars -> jars.map { project.zipTree(it.asFile) } }) {
            include(pattern)
            eachFile { path = fileName }
        }
        includeEmptyDirs = false
        into(extracted)
        doLast { check(extracted.get().file(fileName).asFile.isFile) { "No $pattern in $coordinates" } }
    }
    val publish = publishFile(project, taskName,
        unpack.map { _ -> extracted.get().file(fileName) }, output.map { it.file(fileName) })
    return project.files(publish)
}

fun publishFile(
    project: Project, taskName: String, source: Provider<RegularFile>, destination: Provider<RegularFile>
): TaskProvider<Task> = project.tasks.register(taskName) {
    inputs.file(source).withPathSensitivity(PathSensitivity.NONE)
    outputs.file(destination)
    doLast {
        val target = destination.get().asFile.toPath()
        val input = source.get().asFile.toPath()
        if (Files.exists(target) && Files.mismatch(input, target) == -1L) return@doLast
        val pending = target.resolveSibling(target.fileName.toString() + ".tmp")
        Files.createDirectories(target.parent)
        Files.copy(input, pending, StandardCopyOption.REPLACE_EXISTING)
        Files.move(pending, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }
}

val crankshaftIris = nestedModClasses(
    project,
    "crankshaftIris",
    "dev.engine_room:crankshaft-neoforge:$crankshaftVersion",
    "META-INF/jarjar/*crankshaft-iris-*.jar",
    "crankshaft-iris",
    "crankshaft-iris.jar"
)

val c2meSerializer = nestedModClasses(
    project,
    "c2meSerializer",
    property("dependency_neoforge_c2me") as String,
    "META-INF/jars/c2me-neoforge-rewrites-chunk-serializer-*.jar",
    "c2me-serializer",
    "c2me-serializer.jar"
)

dependencies {
    compileOnly("com.hbm:ntm-next-neoforge:$ntmVersion")
    compileOnly(property("dependency_neoforge_appliedenergistics2") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_cc_tweaked_api") as String)
    compileOnly(property("dependency_neoforge_alexscaves") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_codxlib") as String) { isTransitive = false }
    compileOnly(property("dependency_worldedit") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_effortless_building") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_sophisticated_core") as String)
    compileOnly(property("dependency_neoforge_sophisticated_storage") as String)
    compileOnly(property("dependency_neoforge_sophisticated_backpacks") as String)
    compileOnly(property("dependency_neoforge_compact_storage") as String)
    compileOnly(property("dependency_neoforge_travelersbackpack") as String)
    compileOnly(property("dependency_neoforge_balm") as String)
    compileOnly(property("dependency_neoforge_trinkets_updated") as String)
    compileOnly(property("dependency_neoforge_curios") as String)
    compileOnly(property("dependency_neoforge_iris") as String)
    compileOnly(property("dependency_neoforge_sodium") as String)
    compileOnly("io.github.douira:glsl-transformer:3.0.0-pre3")
    compileOnly("org.antlr:antlr4-runtime:4.13.1")
    compileOnly(property("dependency_neoforge_toms_storage") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_storage_drawers") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_sound_physics") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_entity_model_features") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_punchy_fpa") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_not_enough_bandwidth") as String) { isTransitive = false }
    compileOnly("com.github.luben:zstd-jni:1.5.6-9")
    compileOnly(property("dependency_neoforge_bandwidth_optimizer") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_replaymod") as String) { isTransitive = false }
    compileOnly(property("dependency_neoforge_hindsight") as String) { isTransitive = false }
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.5")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly(property("dependency_neoforge_scalablelux") as String)
    compileOnly(property("dependency_neoforge_lithium") as String)

    annotationProcessor("mov.movblock.tenon:tenon-asm:${property("tenon_version")}")

    compileOnly("dev.engine_room:crankshaft-neoforge:${crankshaftVersion}")
    compileOnly(crankshaftIris)
    compileOnly(c2meSerializer)

    compileOnly(property("dependency_neoforge_jei_common_api") as String)

    compileOnly("me.shedaniel:RoughlyEnoughItems-api:${property("rei_version")}")
    compileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin:${property("rei_version")}")
    compileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:${property("rei_version")}") { isTransitive = false }

    compileOnly(property("dependency_neoforge_jade") as String)
    compileOnly(property("dependency_neoforge_wthit_api") as String)
    compileOnly(property("dependency_neoforge_the_one_probe") as String)

    compileOnly("maven.modrinth:continuity-neoforged:LoaT9Sjl")
    compileOnly("maven.modrinth:fusion-connected-textures:zPVqWQrW")
    compileOnly("maven.modrinth:ctmlib:26.3.0.1")
}

val nativeDensityCompiler = project.property("minecraft_version") == "26.3"

repositories {
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
}

val c2meJars = configurations.create("c2meJars") {
    isCanBeConsumed = false
    isTransitive = false
}
val c2meModules = tasks.register<Sync>("c2meModules") {
    from(c2meJars.elements.map { jars -> jars.map { zipTree(it.asFile) } }) {
        include("META-INF/jars/c2me-neoforge-base-*.jar", "META-INF/jars/c2me-neoforge-opts-dfc-*.jar",
            "META-INF/jars/c2me-neoforge-opts-accel-opencl-*.jar")
        eachFile { path = name }
    }
    includeEmptyDirs = false
    into(layout.buildDirectory.dir("c2me"))
    doLast {
        check(destinationDir.listFiles { f -> f.name.endsWith(".jar") }!!.size == if (nativeDensityCompiler) 1 else 3) {
            "c2me-neoforge/c2me-ocl lost a nested module"
        }
    }
}

dependencies {
    add("compileOnly", "maven.modrinth:voxy:${property("voxy")}") { isTransitive = false }
    add("compileOnly", "maven.modrinth:distanthorizonsapi:${property("distant_horizons_api")}") { isTransitive = false }
    add("c2meJars", "maven.modrinth:c2me-neoforge:${property("c2me_neoforge")}")
    if (!nativeDensityCompiler) {
        add("c2meJars", "maven.modrinth:c2me-ocl:${property("c2me_ocl_neoforge")}")
    }
    add("compileOnly", fileTree(layout.buildDirectory.dir("c2me")) { include("*.jar") }.builtBy(c2meModules))
}

neoForge {
    version = property("neo_version") as String
    accessTransformers.from(ntmAccess.resolve("accesstransformer.cfg"))
    interfaceInjectionData.from(ntmAccess.resolve("interface-injection.json"))
}
