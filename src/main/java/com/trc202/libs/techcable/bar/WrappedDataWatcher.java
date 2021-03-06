package com.trc202.libs.techcable.bar;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.trc202.libs.techcable.Reflection.*;

/**
* Created by Nicholas Schlabach on 4/16/2015.
*/
public class WrappedDataWatcher {
    //Fields
    private final static Class<?> clazz = getNmsClass("DataWatcher");
    private final static Constructor constructor = makeConstructor(clazz, getNmsClass("Entity"));
    private static Field valueMap;
    private static Field typeMap;
    private static Field lockField;
    static {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().equals(Map.class)) {
                if (Modifier.isStatic(field.getModifiers())) {
                    typeMap = field;
                } else {
                    valueMap = field;
                }
            } else if (field.getType().isAssignableFrom(ReentrantReadWriteLock.class)) {
                lockField = field;
            }
        }
    }

    public List<Object> toHandleList() {
        ReadWriteLock lock = getField(lockField, getHandle());
        lock.readLock().lock();
        List<Object> handleList = new ArrayList<Object>();
        try {
            Map<Integer, Object> valueMap = getField(this.valueMap, getHandle());
            for (Object handle : valueMap.values()) {
                handleList.add(handle);
            }
            return handleList;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static int getTypeId(Class<?> clazz) {
        Map map = getField(typeMap, null);
        return (Integer) map.get(clazz);
    }

    public WrappedDataWatcher() {
        this.handle = callConstructor(constructor, new Object[] {null});
    }

    public boolean hasIndex(int index) {
        return getObject(index) != null;
    }

    public int getInt(int index) {
        return (Byte) getObject(index);
    }

    public void setInt(int index, int value) {
        setObject(index, value);
    }

    public byte getByte(int index) {
        return (Byte) getObject(index);
    }

    public void setByte(int index, byte value) {
        setObject(index, value);
    }

    public void setFloat(int index, float value) {
        setObject(index, value);
    }

    public void setString(int index, String value) {
        setObject(index, value);
    }

    private Object getObject(int i) {
        Map valueMap = getField(this.valueMap, getHandle());
        Object handle = valueMap.get(i);
        if (handle == null) return null;
        return new WrappedWatchableObject(handle).getValue();
    }

    private void setObject(int i, Object value) {
        Map valueMap = getField(this.valueMap, getHandle());
        WrappedWatchableObject watcher = new WrappedWatchableObject(i, value);
        valueMap.put(i, watcher.getHandle());
    }

    private final Object handle;
    public Object getHandle() {
        return handle;
    }

    private static class WrappedWatchableObject {
        static {
            for (Field field : getHandleClass().getDeclaredFields()) {
                if (field.getType().equals(Object.class)) {
                    valueField = field;
                }
            }
        }

        public WrappedWatchableObject(Object handle) {
            this.handle = handle;
        }

        private static final Constructor constructor = makeConstructor(getHandleClass(), int.class, int.class, Object.class);

        public WrappedWatchableObject(int index, Object value) {
            int typeId = getTypeId(value.getClass());
            handle = callConstructor(this.constructor, typeId, index, value);
        }

        private final Object handle;

        private static Field valueField;
        public Object getHandle() {
            return handle;
        }

        public Object getValue() {
            return getField(valueField, getHandle());
        }

        public void setValue(Object value) {
            setField(valueField, getHandle(), value);
        }

        public static Class<?> getHandleClass() {
            Class<?> clazz = getNmsClass("DataWatcher$WatchableObject");
            if (clazz == null) {
                clazz = getNmsClass("WatchableObject");
            }
            return clazz;
        }
    }
}
