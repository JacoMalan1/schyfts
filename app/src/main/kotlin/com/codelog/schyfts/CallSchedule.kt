package com.codelog.schyfts

import com.codelog.schyfts.api.APIException
import com.codelog.schyfts.api.APIRequest
import com.codelog.schyfts.api.Doctor
import com.codelog.schyfts.util.AlertFactory
import com.codelog.schyfts.util.DoctorStringConverter
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.control.cell.ChoiceBoxTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.KeyCode
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import javafx.util.Callback
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*
import kotlin.random.Random
import com.codelog.schyfts.util.KLoggerContext as Logger

data class CallEntry(val calls: Array<Doctor>, val dow: DayOfWeek) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CallEntry

        if (!calls.contentEquals(other.calls)) return false
        if (dow != other.dow) return false

        return true
    }

    override fun hashCode(): Int {
        var result = calls.contentHashCode()
        result = 31 * result + dow.hashCode()
        return result
    }

    fun serialize(): JSONObject {
        val returnVal = JSONObject()
        val callsArray = JSONArray()

        for (doctor in calls) {
            callsArray.put(doctor.id)
        }

        returnVal.put("calls", callsArray)
        returnVal.put("dow", dow.ordinal)

        return returnVal
    }
}

fun Doctor.getFullName(reverse: Boolean = false): String =
    if (reverse)
        "$surname, $name"
    else
        "$name $surname"

class CallSchedule: Initializable {
    companion object {
        const val CALLS = 3
        var stage: Stage? = null

        private var instance: CallSchedule? = null

        fun registerEventListeners() {
            stage?.scene?.setOnKeyPressed {
                if (it.isAltDown && it.code == KeyCode.R) {
                    val rand = Random(System.nanoTime())

                    if (instance != null) {
                        val notNullInstance = instance ?: CallSchedule()
                        for (entry in notNullInstance.tblCalls.items) {
                            for (i in 0..2) {
                                val selection = rand.nextInt(0, notNullInstance.doctors.size)
                                entry.calls[i] = notNullInstance.doctors[selection]
                            }
                        }

                        notNullInstance.tblCalls.refresh()
                        notNullInstance.isSaved = false
                    }
                }
            }
        }
    }

    @FXML
    lateinit var tblCalls: TableView<CallEntry>
    lateinit var doctors: List<Doctor>

    var isSaved = false

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        doctors = Doctor.getAllDoctors()
        val entries = ArrayList<CallEntry>()
        tblCalls.isEditable = true

        for (day in DayOfWeek.values())
            entries.add(CallEntry(Array(3) { Doctor(0, "", "", "", "") }, day))

        val clmDay: TableColumn<CallEntry, DayOfWeek> = TableColumn("Date")
        clmDay.cellValueFactory = PropertyValueFactory("dow")
        tblCalls.columns.add(clmDay)

        for (i in 1..CALLS) {
            val clmCall = TableColumn<CallEntry, Doctor>("Call $i")
            clmCall.cellValueFactory = Callback {
                return@Callback object: ObservableValue<Doctor> {
                    override fun addListener(listener: ChangeListener<in Doctor>?) {
                    }

                    override fun addListener(listener: InvalidationListener?) {
                    }

                    override fun removeListener(listener: InvalidationListener?) {
                    }

                    override fun removeListener(listener: ChangeListener<in Doctor>?) {
                    }

                    override fun getValue(): Doctor {
                        return it.value.calls[i - 1]
                    }
                }
            }

            val doctorsObservable: ObservableList<Doctor> = FXCollections.observableArrayList()
            for (d in doctors)
                doctorsObservable.add(d)

            val doctorsSorted = doctorsObservable.sorted { o1, o2 ->
                o1.getFullName(true).compareTo(o2.getFullName(true))
            }

            clmCall.cellFactory = ChoiceBoxTableCell.forTableColumn(DoctorStringConverter(), doctorsSorted)
            clmCall.isEditable = true

            clmCall.setOnEditCommit {
                it.rowValue.calls[i - 1] = it.newValue
                isSaved = false
            }

            tblCalls.columns.add(clmCall)
        }

        tblCalls.items.addAll(entries)

        instance = this


    }

    fun mnuSaveClick() {
        val dialog: Dialog<LocalDate?> = Dialog()
        dialog.title = "Select a date"
        dialog.headerText = "Please select a date on which this week starts."
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        val pane = GridPane()
        pane.hgap = 10.0
        pane.vgap = 10.0
        pane.padding = Insets(20.0, 150.0, 10.0, 10.0)

        val dp = DatePicker()
        pane.add(Label("Week start date:"), 0, 0)
        pane.add(dp, 1, 0)

        dialog.dialogPane.content = pane
        dialog.setResultConverter {
            if (it.buttonData.isCancelButton)
                return@setResultConverter null
            return@setResultConverter dp.value
        }

        val result = dialog.showAndWait()
        if (result.isEmpty)
            return

        if (result.get().dayOfWeek != DayOfWeek.MONDAY) {
            AlertFactory.showAndWait(Alert.AlertType.ERROR, "That date is not a Monday!")
            return
        }

        val jsonData = JSONObject()
        jsonData.put("date", result.get().toString())
        val entryArray = JSONArray()
        for (entry in tblCalls.items) {
            entryArray.put(entry.serialize())
        }
        jsonData.put("entries", entryArray)

        val request = APIRequest("updateCallRegistry", true, "date", "entries")
        try {
            val response = request.send(result.get().toString(), entryArray)
            if (response.getString("status") == "ok") {
                AlertFactory.showAndWait("Done!")
                isSaved = true
            } else {
                throw APIException("Couldn't save to database!", response)
            }
        } catch (e: Exception) {
            Logger.error("Couldn't save call schedule!")
            Logger.exception(e)

            AlertFactory.showAndWait(Alert.AlertType.ERROR, "Couldn't save to database!");
        }
    }

    fun mnuClearClick() {
        if (!isSaved) {
            val dialog = Dialog<Boolean?>()
            dialog.title = "Save to database?"
            dialog.headerText = "The schedule has not been saved yet. Would you like to save it now?"
            dialog.dialogPane.buttonTypes.addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL)

            dialog.setResultConverter {
                when (it) {
                    ButtonType.CANCEL -> null
                    ButtonType.YES -> true
                    ButtonType.NO -> false
                    else -> null
                }
            }

            val result = dialog.showAndWait()
            if (result.isEmpty)
                return
            if (result.get())
                mnuSaveClick()
        }

        val entries = ArrayList<CallEntry>()
        for (day in DayOfWeek.values()) {
            entries.add(
                CallEntry(
                    Array(3) {
                        Doctor(0, "", "", "", "")
                    }, day
                )
            )
        }

        tblCalls.items.clear()
        tblCalls.items.addAll(entries)
        tblCalls.refresh()
        isSaved = false
    }
}