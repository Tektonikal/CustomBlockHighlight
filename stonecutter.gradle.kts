plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3.x"

stonecutter handlers {
    inherit("json5", "json")
}

stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    val isOrnithe = current.version == "1.8.9"
    if (!isOrnithe) {
        dependencies["fapi"] = node.project.property("deps.fabric_api") as String
        dependencies["yacl"] = node.project.property("deps.yacl") as String
    }

    constants["renderstates"] = current.parsed >= "1.21.9"

    replacements {
        string(current.parsed < "1.21.2" && !isOrnithe) {
            replace("getDeltaTracker()", "getTimer()")
            replace("getOffset(pos)", "getOffset(mc.level, pos)")
        }
        string(current.parsed < "1.21.5" && !isOrnithe) {
            replace(".position()", ".getPosition()")
            replace("player.gameMode()", "gameMode.getPlayerMode()")
        }
        string(current.parsed < "1.21.9" && !isOrnithe) {
            replace("rendering.v1.world.WorldRenderEvents", "rendering.v1.WorldRenderEvents")
            replace("rendering.v1.world.WorldRenderContext", "rendering.v1.WorldRenderContext")
            replace("setScreenAndShow", "setScreen")
            replace("END_MAIN", "LAST")
            replace("c.matrices()", "c.matrixStack()")
        }
        string(current.parsed < "26.1" && !isOrnithe) {
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
            replace("addPicturesInPictureState", "submitPicturesInPictureState")
            replace("afterExtract", "afterRender")
        }
        string(current.parsed < "26.1") {
            replace("centeredText", "drawCenteredString")
        }
        string(current.parsed < "1.21.11" && !isOrnithe) {
            replace("Identifier", "ResourceLocation")
            replace("camera.entity()", "camera.getEntity()")
            replace("net.minecraft.util.Util", "net.minecraft.Util")
        }
        string(current.parsed < "1.21.11") {
            replace("@NonNull ", "/*nn*/")
            replace("@Nullable ", "/*nl*/")
        }
        string(current.parsed < "26.2" && !isOrnithe) {
            replace("gameRenderer.lighting()", "gameRenderer.getLighting()")
            replace("mainCamera", "getMainCamera")
            replace("getModelViewMatrixCopy()", "getModelViewMatrix()")
            replace("gameRenderer.mainRenderTarget()", "getMainRenderTarget()")
            replace("getInstance().gui.screen()", "getInstance().screen")
        }
        string(current.parsed < "26.2") {
            replace("gui.hud.isHidden()", "options.hideGui")
        }
        string(current.parsed >= "26.3") {
            replace("com.mojang.blaze3d.opengl", "com.mojang.renderpearl.backend.opengl")
            replace("com.mojang.blaze3d.vulkan", "com.mojang.renderpearl.backend.vulkan")
            replace("com.mojang.blaze3d.textures", "com.mojang.renderpearl.api.textures")
            replace("com.mojang.blaze3d.buffers", "com.mojang.renderpearl.api.buffers")
            replace("com.mojang.blaze3d.GpuFormat", "com.mojang.renderpearl.api.GpuFormat")
            replace("com.mojang.blaze3d.systems.RenderPass", "com.mojang.renderpearl.api.commands.RenderPass")
            replace("com.mojang.blaze3d.systems.CommandEncoder", "com.mojang.renderpearl.api.commands.CommandEncoder")
            replace("com.mojang.blaze3d.PrimitiveTopology", "com.mojang.renderpearl.api.pipeline.PrimitiveTopology")
            replace("com.mojang.blaze3d.pipeline.RenderPipeline", "com.mojang.renderpearl.api.pipeline.RenderPipeline")
            replace("com.mojang.blaze3d.pipeline.DepthStencilState", "com.mojang.renderpearl.api.pipeline.DepthStencilState")
            replace("com.mojang.blaze3d.platform.CompareOp", "com.mojang.renderpearl.api.pipeline.CompareOp")
            replace("com.mojang.blaze3d.vertex.VertexFormat", "com.mojang.renderpearl.api.vertex.VertexFormat")
        }
        string(isOrnithe) {
            val shim = "tektonikal.customblockhighlight.legacy"
            replace("net.minecraft.world.phys.*", "$shim.phys.*")
            replace("net.minecraft.world.phys.AABB", "$shim.phys.AABB")
            replace("net.minecraft.world.phys.Vec3", "$shim.phys.Vec3")
            replace("net.minecraft.world.phys.shapes.*", "$shim.phys.shapes.*")
            replace("net.minecraft.world.phys.shapes.Shapes", "$shim.phys.shapes.Shapes")
            replace("net.minecraft.world.phys.shapes.VoxelShape", "$shim.phys.shapes.VoxelShape")
            replace("com.mojang.blaze3d.vertex.PoseStack", "$shim.vertex.PoseStack")
            replace("com.mojang.blaze3d.vertex.VertexConsumer", "$shim.vertex.VertexConsumer")
            replace("it.unimi.dsi.fastutil.Pair", "$shim.Pair")
            replace("net.minecraft.util.Mth", "$shim.Mth")
            replace("net.minecraft.util.RandomSource", "$shim.RandomSource")
            replace("net.minecraft.client.Camera", "$shim.Camera")
            replace("dev.isxander.yacl3.config.GsonConfigInstance", "$shim.GsonConfigInstance")
            replace("dev.isxander.yacl3.api.NameableEnum", "$shim.NameableEnum")
            replace("net.minecraft.network.chat.Component", "$shim.Component")
            replace("net.minecraft.core.BlockPos", "net.minecraft.util.math.BlockPos")
            replace("net.minecraft.core.Direction", "net.minecraft.util.math.Direction")
            replace("net.minecraft.world.entity.Entity", "net.minecraft.entity.Entity")
            replace("net.minecraft.world.entity.decoration.HangingEntity", "net.minecraft.entity.decoration.DecorationEntity")
            replace("net.minecraft.world.item.BoatItem", "net.minecraft.item.BoatItem")
            replace("net.minecraft.world.item.Items", "net.minecraft.item.Items")
            replace("net.minecraft.world.level.block.piston.PistonBaseBlock", "net.minecraft.block.PistonBaseBlock")
            replace("net.minecraft.world.level.block.piston.PistonHeadBlock", "net.minecraft.block.PistonHeadBlock")
            replace("net.minecraft.world.level.block.state.BlockState", "net.minecraft.block.state.BlockState")
            replace("net.minecraft.world.level.block.*", "net.minecraft.block.*")
        }
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds every registered version and collects the jars"
    dependsOn(stonecutter.tasks.named("buildAndCollect"))
}
