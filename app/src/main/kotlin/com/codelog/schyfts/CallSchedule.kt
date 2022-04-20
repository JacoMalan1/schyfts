package com.codelog.schyfts

import com.codelog.schyfts.api.APIException
import com.codelog.schyfts.api.APIRequest
import com.codelog.schyfts.api.Doctor
import com.codelog.schyfts.util.AlertFactory
import com.codelog.schyfts.util.DialogFactory
import com.codelog.schyfts.util.DoctorStringConverter
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.cell.ChoiceBoxTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.KeyCode
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
        val dialog = DialogFactory.makeDatePickerDialog("Please select a date on which this week starts.");

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

    private fun savePrompt(): Boolean? {
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

        return result.get()
    }

    fun mnuClearClick() {
        if (!isSaved) {
            val saveResponse = savePrompt() ?: return
            if (saveResponse)
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

    fun mnuLoadClick() {
        if (!isSaved) {
            val saveResponse = savePrompt() ?: return
            if (saveResponse)
                mnuSaveClick()
        }

        val dialog = DialogFactory.makeDatePickerDialog("Please select the Monday of the week you want to load.")
        val result = dialog.showAndWait()

        if (result.isEmpty)
            return

        if (result.get().dayOfWeek != DayOfWeek.MONDAY) {
            AlertFactory.showAlert(Alert.AlertType.WARNING, "That date is not a Monday!");
            return;
        }

        val baseDate = result.get();
        val startDate = baseDate.toString().split("T")[0]
        val endDate = baseDate.plusDays(7).toString().split("T")[0]

        val req = APIRequest("getCallData", true, "start", "end")
        try {
            val jsonArray = req.send(startDate, endDate).getJSONArray("results")
            tblCalls.items.clear();

            val entries = ArrayList<CallEntry>(jsonArray.length() / 3 + 1)

            for (dow in DayOfWeek.values()) {
                val calls = Array(3) { Doctor(0, "", "", "", "") }
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val date = LocalDate.parse(json.getString("date").split("T")[0])
                    if (date.dayOfWeek == dow) {
                        val dID = json.getInt("dID")
                        val value = json.getInt("value")
                        val doctor = doctors.find { it.id == dID } ?: Doctor(0, "", "", "", "")
                        calls[value - 1] = doctor;
                    }
                }
                entries.add(CallEntry(calls, dow))
            }

            entries.sortWith(Comparator { o1, o2 -> return@Comparator o1.dow.ordinal - o2.dow.ordinal })
            tblCalls.items.addAll(entries);
            tblCalls.refresh()
        } catch (e: APIException) {
            Logger.error("Couldn't fetch Call Data!");
            Logger.exception(e);
        }
    }
}