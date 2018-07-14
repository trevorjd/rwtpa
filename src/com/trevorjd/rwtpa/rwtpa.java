package com.trevorjd.rwtpa;

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

import java.io.*;
import java.util.Locale;
import java.util.Properties;

import static com.trevorjd.rwtpa.rwtpaGUI.setMainPanelAttributes;
import static com.trevorjd.rwtpa.rwtpaGUI.showHideMainGui;
import static jdk.nashorn.internal.runtime.JSType.toInteger;

/*
    Author: trevorjd https://github.com/trevorjd
    Thanks: Minotorious for assistance with GUI

    TODO add permissions support
    TODO add localization
    TODO immediate action on keypress
 */

public class rwtpa extends Plugin implements Listener
{
    static Plugin plugin;
    private Server myServer = getServer();
    private static Properties properties = new Properties();

    // GLOBAL CONFIGS
    public static String COMMAND_PREFIX;
    public static String COMMAND_SEND;
    public static String COMMAND_BLOCK;
    public static String COMMAND_UNBLOCK;
    public static String COMMAND_CLEAR;
    public static String TELEPORT_TIMER_DURATION;
    public static Locale locale;
    public static String defaultLocale = "en";

    // DEFAULT MESSAGES
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
    public static final String MSG_PLAYER_ALREADY_BLOCKLISTED = "is already on your Block List";

    public static final String GUI_LABEL_TITLE = "Teleport Request";
    public static final String DEFAULT_REQUESTOR_TEXT = "none";
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

        initPlugin();
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

    void initPlugin()
    {
        // test for existence of setting.properties file
        File propertiesFile = new File(getPath() + "/settings.properties");
        if (!propertiesFile.exists())
        {
            writeDefaultPropertiesFile();
        }

        InputStream input = null;
        FileInputStream in;
        try {
            input = new FileInputStream(propertiesFile);
            properties.load(input);
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally
        {
            if (input != null)
            {
                try
                {
                    input.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            assignProperties(properties);
        }
    }

    private void assignProperties(Properties properties)
    {
        COMMAND_PREFIX = properties.getProperty("COMMAND_PREFIX");
        COMMAND_SEND = properties.getProperty("COMMAND_SEND");
        COMMAND_BLOCK = properties.getProperty("COMMAND_BLOCK");
        COMMAND_UNBLOCK = properties.getProperty("COMMAND_UNBLOCK");
        COMMAND_CLEAR = properties.getProperty("COMMAND_CLEAR");
        TELEPORT_TIMER_DURATION = properties.getProperty("TELEPORT_TIMER_DURATION");
    }

    private void writeDefaultPropertiesFile()
    {
        try {
            Properties defaultProperties = defaultProperties();
            File file = new File(getPath() + "/settings.properties");
            FileOutputStream out = new FileOutputStream(file);
            defaultProperties.store(out,"rwtpa properties");
            out.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Properties defaultProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("COMMAND_PREFIX","/tpa");
        properties.setProperty("COMMAND_SEND","send");
        properties.setProperty("COMMAND_BLOCK","block");
        properties.setProperty("COMMAND_UNBLOCK","unblock");
        properties.setProperty("COMMAND_CLEAR","clear");
        properties.setProperty("TELEPORT_TIMER_DURATION","5");
        return properties;
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
        Timer timer = new Timer (toInteger(TELEPORT_TIMER_DURATION), 0f, 1, ()->
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
        sourcePlayer.sendTextMessage(MSG_PLAYER_SUCCESS_SOURCE);
    }

    public Player getPlayerbyName(String playerName)
    {
        Player player = myServer.getPlayer(playerName);
        return player;
    }






}