package com.stockbroker.backend.enums;

/**
 * Intended delivery channel. Only IN_APP is actually delivered in this
 * implementation - SMS/EMAIL/PUSH have no real gateway wired up (see
 * NotificationService), they are recorded so a real gateway can be
 * plugged in later without changing calling code.
 */
public enum NotificationChannel {
    IN_APP,
    EMAIL,
    SMS,
    PUSH
}
