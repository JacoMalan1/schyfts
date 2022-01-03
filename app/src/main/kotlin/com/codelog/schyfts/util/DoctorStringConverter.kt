package com.codelog.schyfts.util

import com.codelog.schyfts.api.Doctor
import com.codelog.schyfts.getFullName
import javafx.util.StringConverter
import java.util.ArrayList

class DoctorStringConverter: StringConverter<Doctor>() {
    override fun toString(doctor: Doctor): String = doctor.getFullName()

    override fun fromString(string: String): Doctor {
        val results = Doctor.searchDoctors(string.split(" ")[1]) ?: ArrayList()
        for (res in results)
            if (res.getFullName() == string)
                return res
        return Doctor(0, "", "", "", "")
    }
}