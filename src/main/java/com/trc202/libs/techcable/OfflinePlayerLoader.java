/**
 * The MIT License
 * Copyright (c) 2014-2015 Techcable
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.trc202.libs.techcable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Loads the data of offline players
 *
 * Should be compatible with versions later than 1.6.4
 *
 * @author Techcable
 */
public class OfflinePlayerLoader {

    /**
     * Returns the given players data
     *
     * Loads the player's data from its file if it is offline
     * If the player is online the online version is returned
     *
     * Players returned by this method may or may not be spawned and should only be used to access data
     *
     * @param name the player's name
     *
     * @return a player's data
     *
     * @throws RuntimeException if the loading failed
     */
    public static Player loadPlayer(String name) {
        return loadPlayer(Bukkit.getOfflinePlayer(name));
    }

    /**
     * Returns the given players data
     *
     * Loads the player's data from its file if it is offline
     * If the player is online the online version is returned
     *
     * Players returned by this method may or may not be spawned and should only be used to access data
     *
     * @param id the player's uuid
     *
     * @return a player's data
     *
     * @throws RuntimeException if the loading failed
     */
    public static Player loadPlayer(UUID id) {
        return loadPlayer(Bukkit.getOfflinePlayer(id));
    }

    /**
     * Returns the given players data
     *
     * Loads the player's data from its file if it is offline
     * If the player is online the online version is returned
     *
     * Players returned by this method may or may not be spawned and should only be used to access data
     *
     * @param player the player
     *
     * @return a player's data
     *
     * @throws RuntimeException if the loading failed
     */
    public static Player loadPlayer(OfflinePlayer player) {
        if (player == null) return null;
        if (player instanceof Player) {
            return (Player) player;
        }
        return loadPlayer(player.getUniqueId(), player.getName());
    }

    private static Player loadPlayer(UUID id, String name) {
        Object server = getMinecraftServer();
        Object interactManager = newPlayerInteractManager();
        Object worldServer = getWorldServer();
        Object profile = newGameProfile(id, name);
        Class<?> entityPlayerClass = Reflection.getNmsClass("EntityPlayer");
        Constructor entityPlayerConstructor = Reflection.makeConstructor(entityPlayerClass, Reflection.getNmsClass("MinecraftServer"), Reflection.getNmsClass("WorldServer"), Reflection.getUtilClass("com.mojang.authlib.GameProfile"), Reflection.getNmsClass("PlayerInteractManager"));
        Object entityPlayer = Reflection.callConstructor(entityPlayerConstructor, server, worldServer, profile, interactManager);
        Player player = (Player) getBukkitEntity(entityPlayer);
        return player;
    }

    private static Object newGameProfile(UUID id, String name) {
        Class<?> gameProfileClass = Reflection.getUtilClass("com.mojang.authlib.GameProfile");
        if (gameProfileClass == null) { //Before uuids
            return name;
        }
        Constructor gameProfileConstructor = null;
        gameProfileConstructor = Reflection.makeConstructor(gameProfileClass, UUID.class, String.class);
        if (gameProfileConstructor == null) { //Verson has string constructor
            gameProfileConstructor = Reflection.makeConstructor(gameProfileClass, String.class, String.class);
            return Reflection.callConstructor(gameProfileConstructor, id.toString(), name);
        } else { //Version has uuid constructor
            return Reflection.callConstructor(gameProfileConstructor, id, name);
        }
    }

    private static Object newPlayerInteractManager() {
        Object worldServer = getWorldServer();
        Class<?> playerInteractClass = Reflection.getNmsClass("PlayerInteractManager");
        Class<?> worldClass = Reflection.getNmsClass("World");
        Constructor c = Reflection.makeConstructor(playerInteractClass, worldClass);
        return Reflection.callConstructor(c, worldServer);
    }

    private static Object getWorldServer() {
        Object server = getMinecraftServer();
        Class<?> minecraftServerClass = Reflection.getNmsClass("MinecraftServer");
        Method getWorldServer = Reflection.makeMethod(minecraftServerClass, "getWorldServer", int.class);
        return Reflection.callMethod(getWorldServer, server, 0);
    }

    //NMS Utils

    private static Object getMinecraftServer() {
        return Reflection.callMethod(Reflection.makeMethod(Reflection.getCbClass("CraftServer"), "getServer"), Bukkit.getServer());
    }

    private static Entity getBukkitEntity(Object o) {
        Method getBukkitEntity = Reflection.makeMethod(o.getClass(), "getBukkitEntity");
        return Reflection.callMethod(getBukkitEntity, o);
    }
}