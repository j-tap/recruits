package com.talhanation.recruits.client.gui.team;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.inventory.TeamCreationContainer;
import com.talhanation.recruits.network.MessageCreateTeam;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;


@OnlyIn(Dist.CLIENT)
public class TeamCreationScreen extends ScreenBase<TeamCreationContainer> {
    private static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(Main.MOD_ID,"textures/gui/team/team_create_gui.png");

    private static final int fontColor = 4210752;
    private final TeamCreationContainer container;
    private final Inventory playerInventory;
    private EditBox textField;
    private ItemStack banner;
    private int teamColorIndex;
    private int teamColorId;
    private String teamColor;
    private int recruitColorIndex;
    private int recruitColorId;
    private String recruitColor;
    public static ItemStack currency;
    public static int price;
    private final ArrayList<String> TEAM_COLORS = new ArrayList<>(
            Arrays.asList("white", "aqua", "black", "blue", "dark_aqua", "dark_blue", "dark_gray", "dark_green", "dark_purple", "dark_red", "gold", "green", "light_purple", "red", "yellow"));


    private final ArrayList<String> RECRUIT_COLORS = new ArrayList<>(
            Arrays.asList("white", "black",
                    "light_gray", "gray", "dark_gray",
                    "light_blue", "blue","dark_blue",
                    "light_green", "green", "dark_green",
                    "light_red", "red", "dark_red",
                    "light_brown", "brown", "dark_brown",
                    "light_cyan", "cyan", "dark_cyan",
                    "yellow","orange", "magenta", "purple", "gold"));
    private final ArrayList<Integer> TeamColorID = new ArrayList<>(
            Arrays.asList(16777215, 5636095, 0, 5592575, 43690, 170, 5592405, 43520, 11141290, 11141120, 16755200, 5635925, 16733695, 16733525, 16777045));

    private final ArrayList<Integer> RecruitColorID = new ArrayList<>(
            Arrays.asList(16777215, 0,
                    16711935, 10141901, 16776960,
                    12582656, 16738740, 8421504,
                    13882323, 65535, 10494192,
                    255, 9127187, 65280,
                    16711680, 0, 0,
                    0, 0, 0,
                    0,0,0,0));


    public TeamCreationScreen(TeamCreationContainer container, Inventory playerInventory, Component title) {
        super(RESOURCE_LOCATION, container, playerInventory, Component.literal(""));
        this.playerInventory = playerInventory;
        this.container = container;
        this.banner = null;
        imageWidth = 176;
        imageHeight = 225;
    }

    @Override
    protected void init() {
        super.init();

        currency.setCount(price);

        this.refreshSelectedColorRecruit();
        this.refreshSelectedColorTeam();

        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        //Main.LOGGER.debug("Hello from Screen");

        if(playerInventory.player.getTeam() == null) {
            textField = new EditBox(font, leftPos + 18, topPos + 50, 140, 15, Component.literal(""));
            textField.setTextColor(-1);
            textField.setTextColorUneditable(-1);
            textField.setBordered(true);
            textField.setMaxLength(24);
            addRenderableWidget(textField);
            setInitialFocus(textField);

            cycleButtonLeftTeamColor(leftPos + 60, topPos + 69);
            cycleButtonRightTeamColor(leftPos + 60 + 85, topPos + 69);

            cycleButtonLeftRecruitColor(leftPos + 60, topPos + 83);
            cycleButtonRightRecruitColor(leftPos + 60 + 85, topPos + 83);

            String create = "Create   ";
            addRenderableWidget(new Button(leftPos + 18, topPos + 99, 140, 20, Component.literal(create),
                    button -> {
                        this.banner = container.getBanner();
                        if (!banner.equals(ItemStack.EMPTY)) {
                            Main.SIMPLE_CHANNEL.sendToServer(new MessageCreateTeam(this.getCorrectFormat(textField.getValue().strip()), banner, teamColor, recruitColorIndex));
                            this.onClose();
                        }
                    }));
        }
    }

    protected void containerTick() {
        super.containerTick();
        if(textField != null) textField.tick();
    }


    public boolean mouseClicked(double p_100753_, double p_100754_, int p_100755_) {
        if (this.textField != null && this.textField.isFocused()) {
            this.textField.mouseClicked(p_100753_, p_100754_, p_100755_);
        }
        return super.mouseClicked(p_100753_, p_100754_, p_100755_);
    }

    private Button cycleButtonLeftTeamColor(int x, int y){
        return addRenderableWidget(new ExtendedButton(x, y, 12, 12, Component.literal("<"),
                button -> {
                    if(this.teamColorIndex > 0){
                        this.teamColorIndex--;
                        this.refreshSelectedColorTeam();
                    }
                }
        ));
    }

    private Button cycleButtonRightTeamColor(int x, int y){
        return addRenderableWidget(new ExtendedButton(x, y, 12, 12, Component.literal(">"),
                button -> {
                    if(this.teamColorIndex + 1 != TEAM_COLORS.size()){
                        this.teamColorIndex++;
                        this.refreshSelectedColorTeam();
                    }
                }
        ));
    }
    private void refreshSelectedColorTeam() {
        this.teamColor = TEAM_COLORS.get(teamColorIndex);
        this.teamColorId = TeamColorID.get(teamColorIndex);
    }

    private Button cycleButtonLeftRecruitColor(int x, int y){
        return addRenderableWidget(new ExtendedButton(x, y, 12, 12, Component.literal("<"),
                button -> {
                    if(this.recruitColorIndex > 0){
                        this.recruitColorIndex--;
                        this.refreshSelectedColorRecruit();
                    }
                }
        ));
    }

    private Button cycleButtonRightRecruitColor(int x, int y){
        return addRenderableWidget(new ExtendedButton(x, y, 12, 12, Component.literal(">"),
                button -> {
                    if(this.recruitColorIndex + 1 != RECRUIT_COLORS.size()){
                        this.recruitColorIndex++;
                        this.refreshSelectedColorRecruit();
                    }
                }
        ));
    }
    private void refreshSelectedColorRecruit() {
        this.recruitColor = RECRUIT_COLORS.get(recruitColorIndex);
        this.recruitColorId = RecruitColorID.get(recruitColorIndex);
    }
    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);

        font.draw(matrixStack, "Create a Team:", 18  , 11, fontColor);

        font.draw(matrixStack, playerInventory.getDisplayName().getVisualOrderText(), 8, 128 + 2, fontColor);

        font.draw(matrixStack, teamColor, 77, 69 + 2, teamColorId);

        font.draw(matrixStack, "Team Color:",18, 69 + 2, fontColor);

        font.draw(matrixStack, recruitColor, 77, 83 + 2, fontColor);//change to recruitcolorid

        font.draw(matrixStack, "Unit Color:",18, 83 + 2, fontColor);

        if(price > 0 && currency != null){
            itemRenderer.renderGuiItem(currency, 120, this.imageHeight - 125);
            itemRenderer.renderGuiItemDecorations(font, currency, 120, this.imageHeight - 125);
        }
    }

    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(matrixStack, partialTicks, mouseX, mouseY);

    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        setFocused(textField);

        return textField.keyPressed(key, a, b) || textField.canConsumeInput() || super.keyPressed(key, a, b);
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    private String getCorrectFormat(String input) {
        input = input.replaceAll(" ", "");
        input = input.replaceAll("[^a-zA-Z0-9\\s]+", "");

        return input;
    }

}
