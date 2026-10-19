package tektonikal.customblockhighlight.config.screenrenderbullshit;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
//? if >1.8.9 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
//?}
import net.minecraft.network.chat.Component;
//? if >1.8.9 {
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.model.block.BakedModel;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.item.ItemStack;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import tektonikal.customblockhighlight.Renderer;
*///?}
import net.minecraft.world.phys.shapes.Shapes;
//? if >=1.21.11 {
import org.jspecify.annotations.NonNull;
//?}
//? if <1.21.8 && >1.8.9 {
/*import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Quaternionf;
*///?}
import tektonikal.customblockhighlight.CustomBlockHighlight;
import tektonikal.customblockhighlight.config.BlockHighlightConfig;
import tektonikal.customblockhighlight.config.ConfigManager;
import tektonikal.customblockhighlight.util.Tweener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class PresetsScreen extends Screen {
    private static final int PREVIEW_HALF_SIZE = 100;
    private static final int PREVIEW_SLOT_SWAP_DISTANCE = 250;

    private final boolean firstTime;
    private final Screen parent;

    private Preset hoveredPreset = Preset.VANILLA;
    private final float[] presetVals = new float[Preset.values().length];

    private final Tweener tweener = new Tweener(() -> hoveredPreset.ordinal(), 15);
    private float xAngle, yAngle;
    private final Tweener xAngleTweener = new Tweener(() -> xAngle, 20);
    private final Tweener yAngleTweener = new Tweener(() -> yAngle, 20);

    public PresetsScreen(boolean firstTime, Screen parent) {
        //? if >1.8.9
        super(Component.translatable("cbh.presets.screenTitle"));
        this.firstTime = firstTime;
        this.parent = parent;
    }

    public static void loadPreset(Preset preset) {
        BlockHighlightConfig.ACTIVE_INSTANCE = ConfigManager.loadPreset(preset.name);
        //? if =1.8.9
        //ConfigManager.save(); // OneConfig only saves after its own properties change
    }

    @Override
    //? if >1.8.9 {
    protected void init() {
    //?} else
    //public void init() {
        for (Preset preset : Preset.values()) {
            if (preset != Preset.CURRENT_CONFIG) {
                addButton(height / 4 + (height / 8) * preset.ordinal(), preset);
            }
        }
    }

    //? if >1.8.9 {
    public void addButton(int y, Preset preset) {
        addRenderableWidget(new Button(width / 32, y, width / 2, 18, preset.meow, button -> {
            //TODO HELP I DONT KNOW WHY I NEED TO DO THIS IT BREAKS OTHERWISE
            loadPreset(preset);
            loadPreset(preset);
        }, value -> Component.empty()) {
            //? if >=1.21.11 {
            @Override
            protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                extractDefaultSprite(graphics);
                extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
                if (isMouseOver(mouseX, mouseY)) {
                    hoveredPreset = preset;
                }
            }
            //?} else {
            
            /*@Override
            protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                boolean hovered = isMouseOver(mouseX, mouseY);
                graphics.fill(getX(), getY(), getX() + width, getY() + height, hovered ? 0x66FFFFFF : 0x66000000);
                graphics.centeredText(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFFFF);
                if (hovered) {
                    hoveredPreset = preset;
                }
            }
            *///?}
        });
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(parent);
    }
    //?} else {
    /*public void addButton(int y, Preset preset) {
        buttons.add(new PresetButton(preset, width / 32, y, width / 2, 18));
    }

    private class PresetButton extends ButtonWidget {
        private final Preset preset;

        PresetButton(Preset preset, int x, int y, int width, int height) {
            super(preset.ordinal(), x, y, width, height, preset.meow.getString());
            this.preset = preset;
        }

        @Override
        public void render(Minecraft minecraft, int mouseX, int mouseY) {
            if (!visible) return;
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            fill(x, y, x + width, y + height, hovered ? 0x66FFFFFF : 0x66000000);
            centeredText(minecraft.textRenderer, message, x + width / 2, y + (height - 8) / 2, 0xFFFFFFFF);
            if (hovered) {
                hoveredPreset = preset;
            }
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button instanceof PresetButton presetButton) {
            loadPreset(presetButton.preset);
        }
    }

    @Override
    protected void keyPressed(char chr, int key) {
        // escape
        if (key == 1) {
            minecraft.openScreen(parent);
            return;
        }
        super.keyPressed(chr, key);
    }
    *///?}

    //? if >=26.1 {
    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        float centerX = (width / 6F) * 5F;
        float centerY = height / 2F;
        for (Preset preset : Preset.values()) {
            if (preset != Preset.CURRENT_CONFIG) {
                presetVals[preset.ordinal()] = (float) CustomBlockHighlight.ease(presetVals[preset.ordinal()], hoveredPreset == preset ? 0 : 1, 15);
                //? if >=1.21.8 {
                int previewCenterX = (int) (centerX - presetVals[preset.ordinal()] * 100);
                int previewCenterY = (int) (centerY + (preset.ordinal() - tweener.getF()) * PREVIEW_SLOT_SWAP_DISTANCE);
                graphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(xAngleTweener.getF(), yAngleTweener.getF(), preset, previewCenterX - PREVIEW_HALF_SIZE, previewCenterY - PREVIEW_HALF_SIZE, previewCenterX + PREVIEW_HALF_SIZE, previewCenterY + PREVIEW_HALF_SIZE, 50F + (50 * (1 - presetVals[preset.ordinal()])), null));
                //?}
            }
        }
        graphics.centeredText(Minecraft.getInstance().font, firstTime ? "Welcome to the CBH config! Would you like to try a preset to get started?" : "Presets", width / 2, height / 8, 0xFFFFFFFF);
        graphics.centeredText(Minecraft.getInstance().font, "(Preview does not fully reflect preset settings.)", width / 2, (height / 8) + (int) (font.lineHeight * 1.5), 0x80808080);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        tweener.update();
        xAngleTweener.update();
        yAngleTweener.update();

        float centerX = (width / 6F) * 5F;
        float centerY = height / 2F;

        xAngle = (float) Math.atan((centerX - Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow())) / 40.0F);
        yAngle = (float) Math.atan((centerY - Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().getWindow())) / 40.0F);

        graphics.centeredText(Minecraft.getInstance().font, firstTime ? "Welcome to the CBH config! Would you like to try a preset to get started?" : "Presets", width / 2, height / 8, 0xFFFFFFFF);
        graphics.centeredText(Minecraft.getInstance().font, "(Preview does not fully reflect preset settings.)", width / 2, (height / 8) + (int) (font.lineHeight * 1.5), 0x80808080);

        for (Preset preset : Preset.values()) {
            if (preset != Preset.CURRENT_CONFIG) {
                presetVals[preset.ordinal()] = (float) CustomBlockHighlight.ease(presetVals[preset.ordinal()], hoveredPreset == preset ? 0 : 1, 15);
                //? if >=1.21.8 {
                int previewCenterX = (int) (centerX - presetVals[preset.ordinal()] * 100);
                int previewCenterY = (int) (centerY + (preset.ordinal() - tweener.getF()) * PREVIEW_SLOT_SWAP_DISTANCE);
                graphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(xAngleTweener.getF(), yAngleTweener.getF(), preset, previewCenterX - PREVIEW_HALF_SIZE, previewCenterY - PREVIEW_HALF_SIZE, previewCenterX + PREVIEW_HALF_SIZE, previewCenterY + PREVIEW_HALF_SIZE, 50F + (50 * (1 - presetVals[preset.ordinal()])), null));
                //?}
            }
        }
        graphics.nextStratum();
        super.extractBackground(graphics, mouseX, mouseY, a);
    }
    //?} elif >1.8.9 {
    /*@Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.render(graphics, mouseX, mouseY, a);
        graphics.centeredText(Minecraft.getInstance().font, firstTime ? "Welcome to the CBH config! Would you like to try a preset to get started?" : "Presets", width / 2, height / 8, 0xFFFFFFFF);
        graphics.centeredText(Minecraft.getInstance().font, "(Preview does not fully reflect preset settings.)", width / 2, (int) (height / 8F + (font.lineHeight * 1.5F)), 0x808080);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        tweener.update();
        xAngleTweener.update();
        yAngleTweener.update();
        //? if >=1.21.8 {
        xAngle = (float) Math.atan((((width / 6F) * 5F) - Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow())) / 40.0F);
        yAngle = (float) Math.atan(((height / 2F) - Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().getWindow())) / 40.0F);
        //?} else {
        /^xAngle = (float) Math.atan((((width / 6F) * 5F) - Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().getWindow().getGuiScaledWidth() / Minecraft.getInstance().getWindow().getScreenWidth()) / 40.0F);
        yAngle = (float) Math.atan(((height / 2F) - Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().getWindow().getGuiScaledHeight() / Minecraft.getInstance().getWindow().getScreenHeight()) / 40.0F);
        ^///?}
        super.extractBackground(graphics, mouseX, mouseY, a);
        //? if >=1.21.8
        graphics.nextStratum();
        for (Preset preset : Preset.values()) {
            if (preset != Preset.CURRENT_CONFIG) {
                presetVals[preset.ordinal()] = (float) CustomBlockHighlight.ease(presetVals[preset.ordinal()], hoveredPreset == preset ? 0 : 1, 15);
                int previewCenterX = (int) (((width / 6F) * 5F) - presetVals[preset.ordinal()] * 100);
                int previewCenterY = (int) ((height / 2F) + (preset.ordinal() - tweener.getF()) * PREVIEW_SLOT_SWAP_DISTANCE);
                //? if >=1.21.8
                graphics.guiRenderState.addPicturesInPictureState(new EvilRenderState(xAngleTweener.getF(), yAngleTweener.getF(), preset, previewCenterX - PREVIEW_HALF_SIZE, previewCenterY - PREVIEW_HALF_SIZE, previewCenterX + PREVIEW_HALF_SIZE, previewCenterY + PREVIEW_HALF_SIZE, 50F + (50 * (1 - presetVals[preset.ordinal()])), null));
                //? if <1.21.8
                //renderLegacyPreviewCube(graphics, preset, previewCenterX, previewCenterY, 50F + (50 * (1 - presetVals[preset.ordinal()])), xAngleTweener.getF(), yAngleTweener.getF());
            }
        }
    }
    *///?} else {
    /*@Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        tweener.update();
        xAngleTweener.update();
        yAngleTweener.update();
        xAngle = (float) Math.atan((((width / 6F) * 5F) - mouseX) / 40.0F);
        yAngle = (float) Math.atan(((height / 2F) - mouseY) / 40.0F);
        renderBackground();
        for (Preset preset : Preset.values()) {
            if (preset != Preset.CURRENT_CONFIG) {
                presetVals[preset.ordinal()] = (float) CustomBlockHighlight.ease(presetVals[preset.ordinal()], hoveredPreset == preset ? 0 : 1, 15);
                int previewCenterX = (int) (((width / 6F) * 5F) - presetVals[preset.ordinal()] * 100);
                int previewCenterY = (int) ((height / 2F) + (preset.ordinal() - tweener.getF()) * PREVIEW_SLOT_SWAP_DISTANCE);
                renderLegacyPreviewCube(preset, previewCenterX, previewCenterY, 50F + (50 * (1 - presetVals[preset.ordinal()])), xAngleTweener.getF(), yAngleTweener.getF());
            }
        }
        GlStateManager.clear(256);
        super.render(mouseX, mouseY, tickDelta);
        centeredText(textRenderer, firstTime ? "Welcome to the CBH config! Would you like to try a preset to get started?" : "Presets", width / 2, height / 8, 0xFFFFFFFF);
        centeredText(textRenderer, "(Preview does not fully reflect preset settings.)", width / 2, (int) (height / 8F + (textRenderer.fontHeight * 1.5F)), 0x808080);
    }
    *///?}

    //? if <1.21.8 && >1.8.9 {
    /*public static void renderLegacyPreviewCube(GuiGraphicsExtractor graphics, Preset preset, float translateX, float translateY, float scale, float xAngle, float yAngle) {
        if (!shouldRender(preset)) return;
        graphics.pose().pushPose();
        graphics.pose().translate(translateX, translateY, 200);
        graphics.pose().scale(scale, scale, -scale);
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 30.0F * (float) (Math.PI / 180.0));
        xRotation.rotateLocalY(-xAngle * 30.0F * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);
        graphics.pose().mulPose(rotation);

        Lighting.setupFor3DItems();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        graphics.pose().pushPose();
        graphics.pose().translate(-0.5F, -0.5F, -0.5F);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(preset.block.defaultBlockState(), graphics.pose(), bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        graphics.pose().popPose();

        PreviewOutline.draw(graphics.pose(), bufferSource, preset);

        Lighting.setupForFlatItems();
        graphics.pose().popPose();
    }
    *///?} elif =1.8.9 {
    /*public static void renderLegacyPreviewCube(Preset preset, float translateX, float translateY, float scale, float xAngle, float yAngle) {
        if (!shouldRender(preset)) return;
        Minecraft minecraft = Minecraft.getInstance();
        GlStateManager.pushMatrix();
        // light directions are transformed by the current matrix, set them up before rotating like vanilla screens do
        Lighting.turnOnGui();
        GlStateManager.translatef(translateX, translateY, 200);
        GlStateManager.scalef(scale, scale, -scale);
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yAngle * 30.0F * (float) (Math.PI / 180.0));
        xRotation.rotateLocalY(-xAngle * 30.0F * (float) (Math.PI / 180.0));
        rotation.mul(xRotation);
        AxisAngle4f axisAngle = new AxisAngle4f(rotation);
        GlStateManager.rotatef((float) Math.toDegrees(axisAngle.angle), axisAngle.x, axisAngle.y, axisAngle.z);

        GlStateManager.enableDepthTest();
        GlStateManager.clear(256);

        ItemStack stack = new ItemStack(preset.block, 1, preset.metadata);
        BakedModel model = minecraft.getItemRenderer().getModelShaper().getModel(stack);
        minecraft.getTextureManager().bind(TextureAtlas.BLOCKS_LOCATION);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableLighting();
        GlStateManager.pushMatrix();
        // renderItem draws blocks at half size around the origin
        GlStateManager.scalef(2.0F, 2.0F, 2.0F);
        minecraft.getItemRenderer().renderItem(stack, model);
        GlStateManager.popMatrix();
        GlStateManager.disableAlphaTest();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        Lighting.turnOff();

        PreviewOutline.draw(new PoseStack(), Renderer.legacyBuffer, preset);

        GlStateManager.popMatrix();
    }
    *///?}

    public enum Preset {
        //? if >1.8.9 {
        VANILLA("vanilla", Blocks.COBBLESTONE),
        SWEAT("sweat", Blocks.SMITHING_TABLE),
        TRANS("trans", Blocks.AMETHYST_BLOCK),
        CLASSIC("classic", Blocks.OAK_PLANKS),
        FANCY("fancy", Blocks.DARK_OAK_LOG),
        CURRENT_CONFIG("current", Blocks.GRASS_BLOCK),
        //?} else {
        /*VANILLA("vanilla", Blocks.COBBLESTONE, 0),
        SWEAT("sweat", Blocks.CRAFTING_TABLE, 0),
        // purple stained clay
        TRANS("trans", Blocks.STAINED_HARDENED_CLAY, 10),
        CLASSIC("classic", Blocks.PLANKS, 0),
        // dark oak log
        FANCY("fancy", Blocks.LOG2, 1),
        CURRENT_CONFIG("current", Blocks.GRASS, 0),
        *///?}
        ;

        public final String name;
        public final Component meow;
        public final Block block;
        //? if =1.8.9
        //public final int metadata;
        public final Supplier<Pair<List<CBHLineRenderInfo>, CBHFillRenderInfo>> renderInfo;

        //? if >1.8.9 {
        Preset(String name, Block block) {
        //?} else {
        /*Preset(String name, Block block, int metadata) {
            this.metadata = metadata;
        *///?}
            this.name = name;
            this.block = block;
            this.meow = Component.translatable("cbh.presets." + name);
            renderInfo = () -> {
                BlockHighlightConfig cfg = name.equals("current") ? BlockHighlightConfig.getActiveInstance() : ConfigManager.getPreset(this);
                float[] fillArr = new float[6];
                Arrays.fill(fillArr, !cfg.fillEnabled ? 0 : cfg.fillCol.alpha);
                CBHFillRenderInfo fillInfo = new CBHFillRenderInfo(cfg.fillCol.getColors(false, Color.WHITE), fillArr, cfg.fillDepthTest, cfg.fillExpandBlocks, cfg.fillExpandPercent);
                ArrayList<CBHLineRenderInfo> info = new ArrayList<>();
                for (var lineConfig : cfg.lineConfigs()) {
                    if (lineConfig.enabled && cfg.primary.enabled) {
                        float[] arr = new float[6];
                        Arrays.fill(arr, lineConfig.color.alpha);
                        info.add(new CBHLineRenderInfo(Shapes.block().move(-0.5F, -0.5F, -0.5F), lineConfig.color.getColors(false, Color.WHITE), arr, lineConfig.lineWidth, lineConfig.lineDepthTest, lineConfig.cutFromCenter, lineConfig.cutFromCorner, lineConfig.outerThicknessMult, lineConfig.innerThicknessMult, lineConfig.lineExpandBlocks, lineConfig.lineExpandPercentage));
                    }
                }
                return Pair.of(info, fillInfo);
            };
        }
    }

    public static boolean shouldRender(Preset preset) {
        return (preset == Preset.CURRENT_CONFIG && BlockHighlightConfig.getActiveInstance().enableModRendering) || (preset != null && preset != Preset.CURRENT_CONFIG);
    }
}
