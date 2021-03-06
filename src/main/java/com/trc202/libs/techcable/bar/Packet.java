package com.trc202.libs.techcable.bar;

import com.trc202.libs.techcable.Reflection;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.trc202.libs.techcable.Reflection.*;

/**
* Created by Nicholas Schlabach on 4/16/2015.
*/
public abstract class Packet {
    private static final Field playerConnectionField = makeField(getNmsClass("EntityPlayer"), "playerConnection");
    private static final Method sendPacketMethod = makeMethod(getNmsClass("PlayerConnection"), "sendPacket", getNmsClass("Packet"));
    public void sendPacket(Player p) {
        Object entityPlayer = Reflection.getHandle(p);
        Object playerConnection = getField(playerConnectionField, entityPlayer);
        callMethod(sendPacketMethod, playerConnection, getHandle());
    }
    protected abstract Object getHandle();
}
