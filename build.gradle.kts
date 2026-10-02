import net.ornithemc.ploceus.api.PloceusGradleExtensionApi

plugins {
    id("dev.kikugie.loom-back-compat")
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" apply false
    id("ploceus") version "1.17.4" apply false
    id("dev.kikugie.fletching-table.fabric") version "0.2.0-alpha.9"
    id("dev.kikugie.fletching-table.lang") version "0.2.0-alpha.9"
}

val isOrnithe = sc.current.version == "1.8.9"
val ploceus = if (isOrnithe) {
    pluginManager.apply("net.fabricmc.fabric-loom-remap")
    pluginManager.apply("ploceus")
    configurations.configureEach { exclude(group = "org.lwjgl.lwjgl") }
    extensions.getByType<PloceusGradleExtensionApi>().apply { setIntermediaryGeneration(2) }
} else null

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = property("mod.archives_name") as String

val requiredJava: JavaVersion = when {
    isOrnithe -> JavaVersion.VERSION_25
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    maven("https://api.modrinth.com/maven") { name = "Modrinth (unscoped)" }
    maven("https://maven.isxander.dev/releases") { name = "Xander Releases" }
    maven("https://maven.isxander.dev/snapshots") { name = "Xander Snapshots" }
    strictMaven("https://maven.terraformersmc.com/releases", "TerraformersMC", "com.terraformersmc")
    if (isOrnithe) {
        maven("https://maven.ornithemc.net/releases") { name = "OrnitheMC" }
        maven("https://repo.polyfrost.org/releases") { name = "Polyfrost Releases" }
        maven("https://repo.polyfrost.org/snapshots") { name = "Polyfrost Snapshots" }
        maven("https://maven.cloverclient.com/releases") {
            content { includeGroup("pl.tomgirl") }
        }
        google()
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    if (isOrnithe) {
        mappings(ploceus!!.layeredMappings {
            mappings("net.ornithemc:feather-gen2:${sc.current.version}+build.${property("deps.feather_build")}:v2") {
                containsUnpick()
            }
            mappings(rootProject.file("mappings/feather-overrides.tiny"))
        })
        ploceus.dependOsl(property("deps.osl_version") as String)
    } else {
        loomx.applyMojangMappings()
    }

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    if (isOrnithe) {
        val oneconfig = property("deps.oneconfig") as String
        modImplementation("org.polyfrost.oneconfig:${sc.current.version}-ornithe:$oneconfig")
        for (module in arrayOf("commands", "config", "config-impl", "events", "internal", "ui", "utils", "hud")) {
            implementation("org.polyfrost.oneconfig:$module:$oneconfig")
        }
        val joml = "org.joml:joml:${property("deps.joml")}"
        implementation(joml)
        add("include", joml)
    } else {
        modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
        modImplementation("dev.isxander:yet-another-config-lib:${property("deps.yacl")}-fabric")
        modImplementation("com.terraformersmc:modmenu:${property("deps.modmenu")}")
    }
}

fletchingTable {
    lang.configure(sourceSets.main.get()) {}
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    runConfigs.remove(runConfigs.getByName("server"))

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        jvmArguments.add("-Dmixin.debug.export=true")
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, property: String) {
            val value: String = sc.properties[property]
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", "mod.id")
            register("name", "mod.name")
            register("minecraft", "mod.mc_compat")
            register("loader", "deps.fabric_loader")
            put("version", project.version.toString())
        }
        inputs.property("version", project.version.toString())

        filesMatching("fabric.mod.json") { expand(props) }
        if (isOrnithe) filesMatching("fabric.mod.json") {
            filter { line ->
                line.takeUnless { "\"fabric-api\"" in it || "ModMenuIntegration" in it }
                    ?.replace("\"yet_another_config_lib_v3\": \"*\"", "\"oneconfigv1\": \"*\"")
            }
        }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        inputs.property("mixinJava", mixinJava)
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod_version}/`"

        inputs.property("version", project.property("mod.version"))
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
