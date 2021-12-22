package com.codelog.schyfts

import com.codelog.schyfts.api.StatisticEntry
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import javafx.util.StringConverter
import java.net.URL
import java.util.*

open class StatisticsViewer: Initializable {
    companion object {
        var statistics: List<StatisticEntry> = ArrayList()
        const val calls = 3
    }

    @FXML
    lateinit var tblStatistics: TableView<StatisticEntry>

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val v = StatisticEntry("Malan, WA", HashMap<Int, Int>(), 32)

        val clmDoctorName: TableColumn<StatisticEntry, String> = TableColumn("Doctor")
        clmDoctorName.cellValueFactory = PropertyValueFactory("doctorName")

        val clmLists: TableColumn<StatisticEntry, Int> = TableColumn("Lists")
        clmLists.cellValueFactory = PropertyValueFactory("lists")

        val clmCalls: TableColumn<StatisticEntry, String> = TableColumn("Weekend Calls")

        for (i in 1..calls) {
            val clmCall: TableColumn<StatisticEntry, Map<Int, Int>> = TableColumn("Call $i")
            clmCall.userData = i
            clmCall.cellValueFactory = PropertyValueFactory("weekendCalls")

            // Create Cell Factory to convert our Map object to a string
            clmCall.cellFactory = Callback {
                TextFieldTableCell(object: StringConverter<Map<Int, Int>>() {
                    override fun fromString(string: String): Map<Int, Int> = HashMap()

                    override fun toString(`object`: Map<Int, Int>?): String =
                        if (it.userData != null && it.userData is Int) {
                            (`object`?.get(it.userData as Int) ?: "N/A").toString()
                        } else {
                            ""
                        }
                })
            }

            clmCalls.columns.add(clmCall)
        }

        tblStatistics.columns?.addAll(clmDoctorName, clmLists, clmCalls)

        for (stat in statistics)
            tblStatistics.items?.add(stat)
    }
}