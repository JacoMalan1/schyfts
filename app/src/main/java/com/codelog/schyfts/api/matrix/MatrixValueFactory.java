package com.codelog.schyfts.api.matrix;

import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class MatrixValueFactory implements Callback<TableColumn.CellDataFeatures<MatrixEntry, String>, ObservableValue<String>> {

    private short moduleNumber;

    public MatrixValueFactory(short module) {
        moduleNumber = module;
    }

    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<MatrixEntry, String> param) {
        var entry = param.getValue();
        return new StringBinding() {
            @Override
            protected String computeValue() {
                return entry.getModuleMap().get(moduleNumber);
            }
        };
    }
}
