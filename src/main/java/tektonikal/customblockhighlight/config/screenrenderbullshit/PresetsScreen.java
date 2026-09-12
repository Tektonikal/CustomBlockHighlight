package tektonikal.customblockhighlight.config.screenrenderbullshit;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;
//? if >=1.21.11 {
import org.jspecify.annotations.NonNull;
//?}
//? if <1.21.8 {
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
        super(Component.translatable("cbh.presets.screenTitle"));
        this.firstTime = firstTime;
        this.parent = parent;
    }

    public static void loadPreset(Preset preset) {
        BlockHighlightConfig.ACTIVE_INSTANCE = ConfigManager.loadPreset(preset.name);
    }

    @Override
    protected void init() {
        for (Preset preset : Preset.values()) {
            if (preset != Preset.CURRENT_CONFIG) {
                addButton(height / 4 + (height / 8) * preset.ordinal(), preset);
            }
        }
    }

    public void addButton(int y, Preset preset) {
        addRenderableWidget(new Button(width / 32, y, width / 2, 18, preset.meow, button -> loadPreset(preset), value -> Component.empty()) {
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

    //? if >=26.1 {
    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(Minecraft.getInstance().font, firstTime ? "Welcome to the CBH config! Would you like to try a preset to get started?" : "Presets", width / 2, height / 8, 0xFFFFFFFF);
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

        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.nextStratum();

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
    }
    //?} else {
    /*@Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.render(graphics, mouseX, mouseY, a);
        graphics.centeredText(Minecraft.getInstance().font, firstTime ? "Welcome to the CBH config! Would you like to try a preset to get started?" : "Presets", width / 2, height / 8, 0xFFFFFFFF);
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
    *///?}

    //? if <1.21.8 {
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
    *///?}

    public enum Preset {
        VANILLA("vanilla", Blocks.COBBLESTONE),
        SWEAT("sweat", Blocks.SMITHING_TABLE),
        TRANS("trans", Blocks.AMETHYST_BLOCK),
        CLASSIC("classic", Blocks.OAK_PLANKS),
        FANCY("fancy", Blocks.DARK_OAK_LOG),
        CURRENT_CONFIG("current", Blocks.GRASS_BLOCK),
        ;

        public final String name;
        public final Component meow;
        public final Block block;
        public final Supplier<Pair<List<CBHLineRenderInfo>, CBHFillRenderInfo>> renderInfo;

        Preset(String name, Block block) {
            this.name = name;
            this.block = block;
            this.meow = Component.translatable("cbh.presets." + name);

            BlockHighlightConfig cfg = name.equals("current") ? BlockHighlightConfig.getActiveInstance() : ConfigManager.getPreset(this);
            renderInfo = () -> {
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
