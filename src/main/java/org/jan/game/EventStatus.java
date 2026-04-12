package org.jan.game;

public enum EventStatus {
    OPEN,             // posted, waiting for a challenger
    PENDING_APPROVAL, // challenger applied, waiting for creator to approve
    IN_PROGRESS,      // approved — players heading to the location
    FINISHED,         // both players submitted the same result → scores counted
    DISPUTED,         // both players submitted different results → scores NOT counted
    CANCELLED
}
