import java.util.zip.ZipFile

plugins {
    java
    id("net.neoforged.moddev") version "2.0.147"
}

version = property("mod_version")!!
group = property("mod_group_id")!!
val ntmVersion = "${property("hbm_version")}+mc${property("minecraft_version")}"

base {
    archivesName = "${property("mod_id")}-neoforge"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

repositories {
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
            includeModule("com.hbm", "ntm-next-processor")
            includeModule("com.hbm", "ntm-next-templates")
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
val javacInternalOpens = listOf("main")
    .map { "--add-opens=jdk.compiler/com.sun.tools.javac.$it=ALL-UNNAMED" }

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED")
    options.compilerArgs.add("--add-modules=jdk.incubator.vector")
    options.isFork = true
    options.forkOptions.memoryMaximumSize = "2g"
    options.forkOptions.jvmArgs!!.addAll(javacInternalExports)
    options.forkOptions.jvmArgs!!.addAll(javacInternalOpens)
}

val ntmTemplates = configurations.create("ntmTemplates") {
    isCanBeConsumed = false
    isTransitive = false
}

sourceSets.main {
    resources.exclude("assets/**/*.obj")
}

tasks.named<JavaCompile>("compileJava") {
    inputs.files(ntmTemplates).withPropertyName("ntmTemplates")
    val templates = ntmTemplates.elements
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "-Xplugin:tenon-asm",
            "-Xplugin:tenon-inject packages=com.hbm.interfaces.injected",
            "-Xplugin:tenon-traits templates=" + templates.get().single().asFile.absolutePath,
            "-Xplugin:hbm-compiler syncTo=container-sync.idx",
        )
    })
    options.compilerArgs.addAll(javacInternalExports)
}

val pin = property("hbm_version") as String
val expansion = mapOf("version" to version, "hbm_version" to pin, "hbm_range" to "[$pin,$pin.1)")

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

val crankshaftVersion = "1.5.9+mc26.2"

val crankshaftJar = configurations.create("crankshaftJar") {
    isCanBeConsumed = false
    isTransitive = false
}
val crankshaftIris = tasks.register<Sync>("crankshaftIris") {
    from(crankshaftJar.elements.map { jars -> jars.map { zipTree(it.asFile) } }) {
        include("META-INF/jarjar/dev.engine_room.crankshaft-iris-*.jar")
        eachFile { path = "crankshaft-iris.jar" }
    }
    includeEmptyDirs = false
    into(layout.buildDirectory.dir("crankshaft-iris"))
    doLast { check(destinationDir.resolve("crankshaft-iris.jar").isFile) { "no META-INF/jarjar/dev.engine_room.crankshaft-iris-*.jar in crankshaft-neoforge" } }
}

dependencies {
    compileOnly("com.hbm:ntm-next-neoforge:$ntmVersion")
    ntmTemplates("com.hbm:ntm-next-templates:$ntmVersion")
    compileOnly("org.appliedenergistics:appliedenergistics2:26.2.1-alpha") { isTransitive = false }
    compileOnly("cc.tweaked:cc-tweaked-26.2-forge-api:1.120.2")
    compileOnly("maven.modrinth:sophisticated-core:HJLJBWYT")
    compileOnly("maven.modrinth:sophisticated-storage:9jOzQdN5")
    compileOnly("maven.modrinth:sophisticated-backpacks:LPnOSNVg")
    compileOnly("maven.modrinth:compact-storage:nIOL2PLd")
    compileOnly("maven.modrinth:travelersbackpack:wxBR2pcr")
    compileOnly("maven.modrinth:balm:lGba52SO")
    compileOnly("maven.modrinth:trinkets-updated:jhbhwQFz")
    compileOnly("maven.modrinth:curios:BI7D0sbK")
    compileOnly("maven.modrinth:iris:k55HdONq")
    compileOnly("net.caffeinemc:sodium-neoforge-mod:0.9.2+mc26.2")
    compileOnly("io.github.douira:glsl-transformer:3.0.0-pre3")
    compileOnly("org.antlr:antlr4-runtime:4.13.1")
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.5")
    compileOnly("org.jspecify:jspecify:1.0.0")
    compileOnly("maven.modrinth:scalablelux:0.3.0-alpha.0.16+26.2")
    compileOnly("maven.modrinth:lithium:mc26.2-0.25.3-neoforge")
    compileOnly("mov.movblock.tenon:tenon-strip-api:${property("tenon_version")}")

    annotationProcessor("com.hbm:ntm-next-processor:$ntmVersion")
    annotationProcessor("mov.movblock.tenon:tenon-asm:${property("tenon_version")}")
    annotationProcessor("mov.movblock.tenon:tenon-traits:${property("tenon_version")}")
    annotationProcessor("mov.movblock.tenon:tenon-inject:${property("tenon_version")}")

    compileOnly("dev.engine_room:crankshaft-neoforge:${crankshaftVersion}")
    crankshaftJar("dev.engine_room:crankshaft-neoforge:${crankshaftVersion}")
    compileOnly(files(layout.buildDirectory.file("crankshaft-iris/crankshaft-iris.jar")).builtBy(crankshaftIris))

    compileOnly("mezz.jei:jei-26.2-common-api:30.35.0.224")

    compileOnly("me.shedaniel:RoughlyEnoughItems-api:${property("rei_version")}")
    compileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin:${property("rei_version")}")
    compileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:${property("rei_version")}") { isTransitive = false }

    compileOnly("maven.modrinth:jade:26.2.10+neoforge")
    compileOnly("mcp.mobius.waila:wthit-api:neo-20.0.0")
    compileOnly("maven.modrinth:the-one-probe:26.2_neo-15.0.1")

    compileOnly("curse.maven:neocontinuity-1377279:8896544")
    compileOnly("maven.modrinth:ctmlib:26.2.0.1")
}

neoForge {
    version = property("neo_version") as String
    accessTransformers.from(ntmAccess.resolve("accesstransformer.cfg"))
    interfaceInjectionData.from(ntmAccess.resolve("interface-injection.json"))
}
