package com.goalapp.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Milliseconds'ı epoch day'e çevirir (UTC gününün başlangıcından bu yana geçen gün sayısı)
 */
fun Long.toEpochDay(): Long {
    val zone = ZoneId.systemDefault()
    return Instant.ofEpochMilli(this)
        .atZone(zone)
        .toLocalDate()
        .toEpochDay()
}

/**
 * Epoch day'i milisaniyeye çevirir (UTC'de günün başlangıcı)
 */
fun Long.toMillisFromEpochDay(): Long {
    val zone = ZoneId.systemDefault()
    return LocalDate.ofEpochDay(this)
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()
}

/**
 * Epoch day'i UTC başlangıç milisaniyesine çevirir (DatePicker için)
 */
fun Long.toUtcStartOfDayMillis(): Long =
    LocalDate.ofEpochDay(this).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/**
 * UTC milisaniyeyi epoch day'e çevirir (DatePicker'dan gelen değerler için)
 */
fun Long.toEpochDayFromUtcDate(): Long =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()


