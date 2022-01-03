package com.codelog.schyfts.util

import com.codelog.schyfts.api.Doctor
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

class ObservableDoctorValue: ObservableValue<Doctor> {
    override fun addListener(listener: ChangeListener<in Doctor>?) {

    }

    override fun addListener(listener: InvalidationListener?) {
        TODO("Not yet implemented")
    }

    override fun removeListener(listener: InvalidationListener?) {
        TODO("Not yet implemented")
    }

    override fun removeListener(listener: ChangeListener<in Doctor>?) {
        TODO("Not yet implemented")
    }

    override fun getValue(): Doctor {
        TODO("Not yet implemented")
    }
}