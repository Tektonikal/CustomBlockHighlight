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

    constants["renderstates"] = current.parsed >= "1.21.9"

    replacements {
        string(current.parsed < "1.21.2") {
            replace("getDeltaTracker()", "getTimer()")
            replace("getOffset(pos)", "getOffset(mc.level, pos)")
        }
        string(current.parsed < "1.21.5") {
            replace(".position()", ".getPosition()")
            replace("player.gameMode()", "gameMode.getPlayerMode()")
        }
        string(current.parsed < "1.21.9") {
            replace("rendering.v1.world.WorldRenderEvents", "rendering.v1.WorldRenderEvents")
            replace("rendering.v1.world.WorldRenderContext", "rendering.v1.WorldRenderContext")
            replace("setScreenAndShow", "setScreen")
            replace("END_MAIN", "LAST")
            replace("c.matrices()", "c.matrixStack()")
        }
        string(current.parsed < "26.1") {
            replace("rendering.v1.level.LevelRenderEvents", "rendering.v1.world.WorldRenderEvents")
            replace("rendering.v1.level.LevelRenderContext", "rendering.v1.world.WorldRenderContext")
            replace("c.poseStack()", "c.matrices()")
            replace("LevelRenderEvents", "WorldRenderEvents")
            replace("LevelRenderContext", "WorldRenderContext")
            replace("renderer.state.gui.pip.PictureInPictureRenderState", "gui.render.state.pip.PictureInPictureRenderState")
            replace("gui.GuiGraphicsExtractor;", "gui.GuiGraphics;")
            replace("GuiGraphicsExtractor graphics", "GuiGraphics graphics")
            replace("GuiGraphicsExtractor guiGraphics", "GuiGraphics guiGraphics")
            replace("GuiGraphicsExtractor.HoveredTextEffects", "GuiGraphics.HoveredTextEffects")
            replace("extractContents", "renderContents")
            replace("\"extractBackground\"", "\"renderBackground\"")
            replace("super.extractBackground(", "super.renderBackground(")
            replace("void extractBackground(", "void renderBackground(")
            replace("extractDefaultSprite", "renderDefaultSprite")
            replace("extractDefaultLabel", "renderDefaultLabel")
            replace("centeredText", "drawCenteredString")
            replace("addPicturesInPictureState", "submitPicturesInPictureState")
            replace("afterExtract", "afterRender")
        }
        string(current.parsed < "1.21.11") {
            replace("Identifier", "ResourceLocation")
            replace("camera.entity()", "camera.getEntity()")
            replace("@NonNull ", "/*nn*/")
            replace("@Nullable ", "/*nl*/")
            replace("net.minecraft.util.Util", "net.minecraft.Util")
        }
        string(current.parsed < "26.2") {
            replace("gameRenderer.lighting()", "gameRenderer.getLighting()")
            replace("mainCamera", "getMainCamera")
            replace("getModelViewMatrixCopy()", "getModelViewMatrix()")
            replace("gameRenderer.mainRenderTarget()", "getMainRenderTarget()")
            replace("gui.hud.isHidden()", "options.hideGui")
            replace("getInstance().gui.screen()", "getInstance().screen")
        }
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds every registered version and collects the jars"
    dependsOn(stonecutter.tasks.named("buildAndCollect"))
}
