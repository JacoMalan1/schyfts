package com.codelog.schyfts.api

import com.codelog.schyfts.util.KLoggerContext as Logger

data class Statistics(val scheduleState: Map<Int, List<Map<String, String>>>, val doctors: List<Doctor>) {
    fun calculateStatistics(callPrefix: String): List<StatisticEntry> {
        val entries: MutableList<StatisticEntry> = ArrayList(doctors.size)

        for (d in doctors) {
            var lists = 0
            val calls = HashMap<Int, Int>()
            for (key in scheduleState.keys) {

                val items: List<Map<String, String>> = scheduleState[key] ?: emptyList()
                if (items.isEmpty())
                    return emptyList()

                for (item in items) {

                    for (clm in item.keys) {
                        if (item[clm] == null)
                            continue

                        if (item[clm]?.isEmpty() == true)
                            continue

                        if (item[clm]!![0] != '#')
                            lists++

                        if (clm.contains(callPrefix)) {
                            try {
                                calls[clm[callPrefix.length].digitToInt()]
                            } catch (e: NumberFormatException) {
                                Logger.exception(e)
                                Logger.error("Number format exception! Couldn't figure out which call we're dealing with.")
                            }
                        }
                    }

                }

            }

            entries.add(StatisticEntry(d.toString(), calls, lists))
        }
        return entries
    }
}