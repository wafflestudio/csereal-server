package com.wafflestudio.csereal.core.reservation.database

enum class ReservationType {
    AD_HOC,
    REGULAR
}

fun resolveRequestReservationType(reservationType: ReservationType?, recurringWeeks: Int): ReservationType {
    require(recurringWeeks > 0) { "Request recurrence must be positive before resolving its type" }
    return reservationType ?: if (recurringWeeks == 1) ReservationType.AD_HOC else ReservationType.REGULAR
}

fun resolvePersistedReservationType(
    reservationType: ReservationType?,
    recurringWeeks: Int
): ReservationType? {
    return reservationType ?: when {
        recurringWeeks == 1 -> ReservationType.AD_HOC
        recurringWeeks > 1 -> ReservationType.REGULAR
        else -> null
    }
}
