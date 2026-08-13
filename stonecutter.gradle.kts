plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2.x"

stonecutter handlers {
    inherit("json5", "json")
}

stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
    dependencies["yacl"] = node.project.property("deps.yacl") as String

    replacements {
        string(current.parsed >= "26.1") {
            replace("rendering.v1.world", "rendering.v1.level")
            replace("WorldRenderEvents", "LevelRenderEvents")
            replace("WorldRenderContext", "LevelRenderContext")
            replace("c.matrices()", "c.poseStack()")
            replace("gui.render.state.pip.PictureInPictureRenderState", "renderer.state.gui.pip.PictureInPictureRenderState")
            replace("gui.GuiGraphics;", "gui.GuiGraphicsExtractor;")
            replace("GuiGraphics graphics", "GuiGraphicsExtractor graphics")
            replace("GuiGraphics.HoveredTextEffects", "GuiGraphicsExtractor.HoveredTextEffects")
            replace("renderContents", "extractContents")
            replace("renderDefaultSprite", "extractDefaultSprite")
            replace("renderDefaultLabel", "extractDefaultLabel")
            replace("drawCenteredString", "centeredText")
            replace("submitPicturesInPictureState", "addPicturesInPictureState")
        }
        string(current.parsed >= "26.2") {
            replace("getMainCamera", "mainCamera")
        }
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds every registered version and collects the jars"
    dependsOn(stonecutter.tasks.named("buildAndCollect"))
}
