package com.trevorjd.rwtpa;

import net.risingworld.api.gui.GuiImage;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.gui.GuiPanel;
import net.risingworld.api.gui.PivotPosition;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.ImageInformation;

import static com.trevorjd.rwtpa.rwtpa.*;

public class rwtpaGUI
{
    //This method can be called by e.g. a PlayerCommandEvent, or a PlayerKeyEvent to show the GUI, then you can call it with showhide-false to hide the GUI after the player has clicked something on it via the PlayerGuiClickEvent
    public static void showHideMainGui(Player player, boolean showHide){
        GuiPanel personalMainPanel = (GuiPanel) player.getAttribute("MainPanel");
        GuiLabel personalHeaderLabel = (GuiLabel) player.getAttribute("HeaderLabel");
        GuiLabel personalRequestorLabel = (GuiLabel) player.getAttribute("RequestorLabel");
        GuiImage personalConfirmationGraphic = (GuiImage) player.getAttribute("ConfirmationGraphic");
        personalMainPanel.setVisible(showHide);
        personalHeaderLabel.setVisible(showHide);
        personalRequestorLabel.setVisible(showHide);
        personalConfirmationGraphic.setVisible(showHide);
    }

    //This method should be called in the PlayerSpawnEvent
    public static void setMainPanelAttributes(Player player)
    {
        GuiPanel personalMainPanel = createMainPanel();
        GuiLabel personalHeaderLabel = createHeaderLabel();
        GuiLabel personalRequestorLabel = createRequestorLabel();
        GuiImage personalConfirmationGraphic = createConfirmationGraphic();
        //if your element is a label or child to the panel you also need to call the .addChild Method here e.g.
        personalMainPanel.addChild(personalHeaderLabel);
        personalMainPanel.addChild(personalRequestorLabel);
        personalMainPanel.addChild(personalConfirmationGraphic);
        player.setAttribute("MainPanel", personalMainPanel);
        player.setAttribute("HeaderLabel", personalHeaderLabel);
        player.setAttribute("RequestorLabel", personalRequestorLabel);
        player.setAttribute("ConfirmationGraphic", personalConfirmationGraphic);
        player.addGuiElement((GuiPanel) player.getAttribute("MainPanel"));
        player.addGuiElement((GuiLabel) player.getAttribute("HeaderLabel"));
        player.addGuiElement((GuiLabel) player.getAttribute("RequestorLabel"));
        player.addGuiElement((GuiImage) player.getAttribute("ConfirmationGraphic"));
    }

    public static GuiPanel createMainPanel(){
        GuiPanel mainPanel = new GuiPanel(0.9f, 0.5f, true, 300, 100, false);
        mainPanel.setColor(0.2f, 0.2f, 0.2f, 0.7f);
        mainPanel.setPivot(PivotPosition.CenterRight);
        mainPanel.setBorderColor(0.6f, 0.6f, 0.6f, 1.0f);
        mainPanel.setBorderThickness(2, false);
        mainPanel.setVisible(false); //I use this as false so that the elements don't appear one I add them to the player but only when I call my show/hide function

        return mainPanel;
    }

    public static GuiLabel createHeaderLabel(){
        GuiLabel headerLabel = new GuiLabel(GUI_LABEL_TITLE, 0.5f, 1.0f, true);
        headerLabel.setPivot(PivotPosition.CenterTop);
        headerLabel.setFontSize(20);
        headerLabel.setFontColor(0.7f, 0.7f, 0.7f, 1.0f);
        headerLabel.setColor(0.0f, 0.0f, 0.0f, 0.0f);
        headerLabel.setVisible(false);

        return headerLabel;
    }

    public static GuiLabel createRequestorLabel(){
        GuiLabel requestorLabel = new GuiLabel(DEFAULT_REQUESTOR_TEXT,0.5f, 0.8f, true);
        requestorLabel.setPivot(PivotPosition.CenterTop);
        requestorLabel.setFontSize(30);
        requestorLabel.setFontColor(1.0f, 1.0f, 1.0f, 1.0f);
        requestorLabel.setColor(0.0f, 0.0f, 0.0f, 0.0f);
        requestorLabel.setVisible(false);

        return requestorLabel;
    }

    public static GuiImage createConfirmationGraphic(){
        ImageInformation confirmationGraphicFile = new ImageInformation(plugin, FILE_CONFIRMATION_GRAPHIC);
        GuiImage confirmationGraphic = new GuiImage(confirmationGraphicFile, 0.5f, 0.2f, true, 80, 32, false);
        confirmationGraphic.setPivot(PivotPosition.CenterBottom);
        confirmationGraphic.setVisible(false);

        return confirmationGraphic;
    }
}
