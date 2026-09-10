package com.example.aimuscle.data.db

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

object LocalDateConverter {
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? =
        dateString?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? =
        date?.toString()
}

object LocalDateTimeConverter {
    @TypeConverter
    fun toLocalDateTime(dateString: String?): LocalDateTime? =
        dateString?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? =
        dateTime?.toString()
}
