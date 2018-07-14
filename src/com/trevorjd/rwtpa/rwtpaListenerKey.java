package com.trevorjd.rwtpa;

import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.player.PlayerKeyEvent;
import net.risingworld.api.objects.Player;
import net.risingworld.api.utils.KeyInput;

public class rwtpaListenerKey implements Listener
{
    @EventMethod
    public void onPlayerKeyEvent(PlayerKeyEvent event)
    {
        Player player = event.getPlayer();
        if(event.isPressed() && event.getKeyCode() == KeyInput.KEY_Y)
        {
            if (player.hasAttribute("tpa_request"))
            {
                player.setAttribute("tpa_permission", true);
            }
        }
        if(event.isPressed() && event.getKeyCode() == KeyInput.KEY_N)
        {
            if (player.hasAttribute("tpa_request"))
            {
                player.setAttribute("tpa_permission", false);
            }
        }
    }
}
