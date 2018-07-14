package com.trevorjd.rwtpa;

import net.risingworld.api.Plugin;
import net.risingworld.api.database.Database;
import net.risingworld.api.objects.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

class rwtpaDatabase
{
    // Globals
    static private Database db = null;

    static void initDB(Plugin plugin) {
        if (db == null)
        {
            db = plugin.getSQLiteConnection(plugin.getPath() + "/database/rwtpa_" + plugin.getWorld().getName() + ".db");
        }
        db.execute(
                "CREATE TABLE IF NOT EXISTS 'Players' ("
                + "'blocker' INTEGER, "
                + "'blocked' INTEGER"
                + ");");
    }


    static void deinit()
    {
        db.close();
        db = null;
    }

    public static ArrayList<Long> getBlockList(long blocker)
    {
        Connection con = db.getConnection();
        ArrayList<Long> blockList = new ArrayList<Long>();

        try(ResultSet result = db.executeQuery("SELECT * FROM `Players` WHERE `blocker` = '" + blocker + "' ORDER BY `blocked`;"))
        {
            while (result.next())
            {
                blockList.add(result.getLong("blocked"));
            }
        } catch (SQLException e){
        }
        return blockList;
    }

    public static void updateBlockList(Player player)
    {
        // update db with player's new data
        Connection con = db.getConnection();

        // delete existing BlockList data
        clearBlockList(player);

        // write new BlockList data
        ArrayList<Long> BlockList = (ArrayList<Long>) player.getAttribute("BlockList");
        Iterator iter = BlockList.iterator();
        while (iter.hasNext())
        {
            Long element = (long) iter.next();
            db.executeUpdate("INSERT INTO Players ('blocker','blocked') VALUES ('" + player.getUID() + "','" + element + "');");
        }
    }

    public static void clearBlockList(Player player)
    {
        // clear BlockList data
        Connection con = db.getConnection();
        db.executeUpdate("DELETE FROM Players WHERE blocker = '" + player.getUID() + "';");
    }

}
