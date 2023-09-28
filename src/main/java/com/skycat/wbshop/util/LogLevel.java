package com.skycat.wbshop.util;

public enum LogLevel { // From Mystical. Should be replaced by an API.
    OFF(0),
    INFO(1),
    DEBUG(2),
    WARN(3),
    ERROR(4);

    public final int intValue;

    LogLevel(int intValue) {
        this.intValue = intValue;
    }
}