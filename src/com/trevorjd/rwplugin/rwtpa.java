package com.trevorjd.rwplugin;

import net.risingworld.api.Plugin;
import net.risingworld.api.Server;
import net.risingworld.api.Timer;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.events.player.PlayerSpawnEvent;
import net.risingworld.api.gui.GuiLabel;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.KeyInput;
import net.risingworld.api.utils.Vector3f;

import static com.trevorjd.rwplugin.rwtpaGUI.setMainPanelAttributes;
import static com.trevorjd.rwplugin.rwtpaGUI.showHideMainGui;

/*
    Author: trevorjd https://github.com/trevorjd
    Thanks: Minotorious for assistance with GUI

    TODO add permissions support
    TODO add config file
    TODO add localization
    TODO immediate action on keypress
 */

public class rwtpa extends Plugin implements Listener
{
    static Plugin plugin;
    public Server myServer = getServer();

    // CONFIG CONSTANTS
    public static final String COMMAND_PREFIX = "/tpa";
    public static final String COMMAND_SEND_PREFIX = "send";
    public static final String COMMAND_BLOCK_PREFIX = "block";
    public static final String COMMAND_UNBLOCK_PREFIX = "unblock";
    public static final String COMMAND_CLEAR_PREFIX = "clear";

    public static final float TELEPORT_TIMER_DURATION = 5;

    // LOCALIZABLE_MESSAGES
    public static final String MSG_PLAYER_USAGE_1 = "Usage";
    public static final String MSG_PLAYER_USAGE_2 = "player name";
    public static final String MSG_PLAYER_NOT_FOUND = "Player not found.";
    private static final String MSG_PLAYER_WORD_PLAYER = "Player";
    private static final String MSG_PLAYER_SUCCESS_DESTINATION = "has been teleported to you.";
    private static final String MSG_PLAYER_SUCCESS_SOURCE = "You have been teleported.";
    private static final String MSG_PLAYER_RECEIVER_REQUEST = "Teleport request received.";
    private static final String MSG_PLAYER_SOURCE_REQUEST = "TPA request sent to:";
    private static final String MSG_PLAYER_PERMISSION_UNAVAILABLE = "Unable to get permission for teleport.";
    public static final String MSG_PLAYER_PLEASE_WAIT = "You have an outstanding teleport request. Try again in 10 seconds.";
    public static final String MSG_PLAYER_RECEIVER_BUSY = "Player is busy. Try again in 10 seconds.";
    public static final String MSG_SENDER_RECEIVER_UNAVAILABLE = "Unable to send TP request to this player.";
    public static final String MSG_PLAYER_CLEAR_1 = "This command will remove ALL entries from your block list.";
    public static final String MSG_PLAYER_CLEAR_2 = "Repeat '/tpa clear' to confirm.";
    public static final String MSG_PLAYER_CLEAR_DONE = "TPA BlockList cleared.";
    public static final String MSG_PLAYER_BLOCKLIST_ADDED = "Player added to blocklist";
    public static final String MSG_PLAYER_BLOCKLIST_REMOVED = "Player removed from blocklist";

    public static final String GUI_LABEL_TITLE = "Teleport Request";
    public static final String DEFAULT_REQUESTOR_TEXT = "No one here but us chickens.";
    public static final String FILE_CONFIRMATION_GRAPHIC = "/resources/images/confirmation.png";

    // Listeners
    rwtpaListenerCommand rwtpaListenerCommandL = new rwtpaListenerCommand();
    rwtpaListenerKey rwtpaListenerKeyL = new rwtpaListenerKey();

    @Override
    public void onEnable()
    {
        rwtpaDatabase.initDB(this);

        plugin = this;                    // make the plug-in instance available to other package classes
        registerEventListener(this);
        registerEventListener(rwtpaListenerCommandL);
        registerEventListener(rwtpaListenerKeyL);
    }

    @Override
    public void onDisable()
    {
        unregisterEventListener(rwtpaListenerCommandL);
        unregisterEventListener(rwtpaListenerKeyL);
        rwtpaDatabase.deinit();
    }

    @EventMethod
    public static void onPlayerSpawn(PlayerSpawnEvent event)
    {
        Player player = event.getPlayer();
        player.setAttribute("BlockList", rwtpaDatabase.getBlockList(player.getUID()));
        // Prepare GUI
        setMainPanelAttributes(player);
    }

    @EventMethod
    public void onPlayerConnect(PlayerConnectEvent event){
        // Register keys for rwtpaListenerKey
        Player player = event.getPlayer();
        player.registerKeys(KeyInput.KEY_Y);
        player.registerKeys(KeyInput.KEY_N);
        player.setListenForKeyInput(true);
    }

    public static void requestPermission(Player sender, Player receiver)
    {
        // Validation should occur outside this method

        // Send notifcations and set attributes to indicate pending teleport
        receiver.sendTextMessage(MSG_PLAYER_RECEIVER_REQUEST);
        sender.sendTextMessage(MSG_PLAYER_SOURCE_REQUEST + " " + receiver.getName());
        receiver.setAttribute("tpa_request", sender.getUID());
        sender.setAttribute("tpa_request", receiver.getUID());

        // Add name of requesting player to receiving player GUI, then display GUI
        GuiLabel personalRequestorLabel = (GuiLabel) receiver.getAttribute("RequestorLabel");
        personalRequestorLabel.setText(sender.getName());
        showHideMainGui(receiver, true); // trigger Minotorious' gui

        //define 5 second timer, rwtpaListernerKey will listen for a response
        Timer timer = new Timer (TELEPORT_TIMER_DURATION, 0f, 1, ()->
            {
                if (receiver.hasAttribute("tpa_permission")) // this should be set by rwtpaListenerKey
                {
                    if (receiver.getAttribute("tpa_permission").equals(true))
                    {
                        long sourcePlayerUID = (long) sender.getAttribute("tpa_request");
                        long destinationPlayerUID = receiver.getUID();
                        teleportPlayer(sourcePlayerUID, destinationPlayerUID);
                    } else
                    {
                        sender.sendTextMessage(MSG_PLAYER_PERMISSION_UNAVAILABLE);
                    }

                } else
                {
                    sender.sendTextMessage(MSG_PLAYER_PERMISSION_UNAVAILABLE);
                }

                rwtpaCleanUp(sender, receiver);
            }
        );
        timer.start();
    }

    public static void rwtpaCleanUp(Player sender, Player receiver)
    {
        // clean up attributes and hide gui
        sender.deleteAttribute("tpa_request");
        receiver.deleteAttribute("tpa_request");
        receiver.deleteAttribute("tpa_permission");
        showHideMainGui(receiver, false);
    }

    public static void teleportPlayer(long sourcePlayerUID, long destinationPlayerUID)
    {
        Server server = plugin.getServer();
        Player sourcePlayer = server.getPlayer(sourcePlayerUID);
        Player destinationPlayer = server.getPlayer(destinationPlayerUID);
        Vector3f destination = destinationPlayer.getPosition();
        destination.setY(destination.getY()+1); // Small difference on Y axis to avoid possible problems with two players occupying the same position.
        sourcePlayer.setPosition(destination);
        destinationPlayer.sendTextMessage(MSG_PLAYER_WORD_PLAYER + " " + sourcePlayer.getName() + " " + MSG_PLAYER_SUCCESS_DESTINATION);
        sourcePlayer.sendTextMessage(MSG_PLAYER_SUCCESS_SOURCE + " " + destinationPlayer.getName());
    }

    public Player getPlayerbyName(String playerName)
    {
        Player player = myServer.getPlayer(playerName);
        return player;
    }






}