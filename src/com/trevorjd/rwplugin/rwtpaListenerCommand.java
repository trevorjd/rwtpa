package com.trevorjd.rwplugin;

import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.objects.Player;

import java.util.ArrayList;
import static com.trevorjd.rwplugin.rwtpa.*;

public class rwtpaListenerCommand implements Listener
{
    @EventMethod
    public void onPlayerCommand(PlayerCommandEvent event)
    {
        Player player = event.getPlayer();
        Player target;
        String[] cmd = event.getCommand().split(" ");
        // check for tpa request
        if (cmd[0].equals(COMMAND_PREFIX))
        {
            // Less than 2 arguments.
            if (cmd.length < 3 || cmd.length > 3)
            {
                if (cmd.length == 2)
                {
                    // process "/tpa clear"
                    if (cmd[1].equals(COMMAND_CLEAR))
                    {
                        if (player.hasAttribute("ClearPending"))
                        {
                            player.deleteAttribute("ClearPending");
                            rwtpaDatabase.clearBlockList(player);
                            player.sendTextMessage(MSG_PLAYER_CLEAR_DONE);
                        } else
                        {
                            player.setAttribute("ClearPending",true);
                            player.sendTextMessage(MSG_PLAYER_CLEAR_1);
                            player.sendTextMessage(MSG_PLAYER_CLEAR_2);
                        }
                    } else
                    {
                        // this will catch commands missing a target, or /tpa help
                        player.sendTextMessage(MSG_PLAYER_USAGE_1 + ": " + COMMAND_PREFIX + " " + COMMAND_SEND + " <" + MSG_PLAYER_USAGE_2 + ">");
                    }
                } else
                {
                    // send usage instructions
                    player.sendTextMessage(MSG_PLAYER_USAGE_1 + ": " + COMMAND_PREFIX + " " + COMMAND_SEND + " <" + MSG_PLAYER_USAGE_2 + ">");
                }

            }

            if (cmd.length == 3)
            {
                rwtpa myrwtpa = new rwtpa();
                if (null != myrwtpa.getPlayerbyName(cmd[2]))
                {
                    target = myrwtpa.getPlayerbyName(cmd[2]);
                    ArrayList<Long> playerBlockList = (ArrayList<Long>) player.getAttribute("BlockList");

                    if (cmd[1].equals(COMMAND_SEND))
                    {
                        // player, send, target
                        // Validate receiver name.
                        target = myrwtpa.getPlayerbyName(cmd[2]);
                        // check if the sender has an outstanding request
                        if (player.hasAttribute("tpa_request"))
                        {
                            player.sendTextMessage(MSG_PLAYER_PLEASE_WAIT);
                        } else
                        {
                            // check if sender is on the receiver's block list
                            boolean playerBlocked = false;
                            ArrayList<Long> targetBlockList = (ArrayList<Long>) target.getAttribute("BlockList");
                            if (target.hasAttribute("BlockList"))
                            {
                                playerBlocked = targetBlockList.contains(Long.valueOf(player.getUID()));
                            }

                            if (playerBlocked)
                            {
                                player.sendTextMessage(MSG_SENDER_RECEIVER_UNAVAILABLE);
                            } else
                            {
                                // Check if receiver has a pending request
                                if (target.hasAttribute("tpa_request"))
                                {
                                    player.sendTextMessage(MSG_PLAYER_RECEIVER_BUSY);
                                } else
                                {
                                    // Request permission for teleport.
                                    requestPermission(player, target);
                                }
                            }
                        }
                    } else
                        if (cmd[1].equals(COMMAND_BLOCK))
                        {
                            // player, block, target
                            target = myrwtpa.getPlayerbyName(cmd[2]);
                            if (playerBlockList.contains(target.getUID()))
                            {
                                player.sendTextMessage(target.getName() + " " + MSG_PLAYER_ALREADY_BLOCKLISTED);
                            } else
                            {
                                playerBlockList.add(target.getUID());
                                player.setAttribute("BlockList", playerBlockList);
                                player.sendTextMessage(MSG_PLAYER_BLOCKLIST_ADDED + ": " + target.getName());
                                rwtpaDatabase.updateBlockList(player);
                            }

                        } else
                            if (cmd[1].equals(COMMAND_UNBLOCK))
                            {
                                // player, block, target
                                target = myrwtpa.getPlayerbyName(cmd[2]);
                                playerBlockList.remove(target.getUID());
                                player.setAttribute("BlockList", playerBlockList);
                                player.sendTextMessage(MSG_PLAYER_BLOCKLIST_REMOVED + ": " + target.getName());
                                rwtpaDatabase.updateBlockList(player);
                            } else
                            {
                                player.sendTextMessage(MSG_PLAYER_NOT_FOUND);
                            }

                }
            }
        }
    }
}

