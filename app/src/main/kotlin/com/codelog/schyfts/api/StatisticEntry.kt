package com.codelog.schyfts.api

data class StatisticEntry(var doctorName: String, var weekendCalls: Map<Int, Int>, var lists: Int)