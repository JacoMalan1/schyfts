package com.codelog.schyfts.util;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

public class DialogFactory {
    public static Dialog<Pair<LocalDate, LocalDate>> makeDateRangeDialog() {
        Dialog<Pair<LocalDate, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("Schedule options");
        dialog.setHeaderText("Please select a start and end date");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(20, 150, 10, 10));

        DatePicker dpStartDate = new DatePicker();
        DatePicker dpEndDate = new DatePicker();

        pane.add(new Label("Start Date:"), 0, 0);
        pane.add(dpStartDate, 1, 0);
        pane.add(new Label("End Date:"), 0, 1);
        pane.add(dpEndDate, 1, 1);

        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(dialogButton -> {
            if (!dialogButton.getButtonData().isCancelButton()) {
                return new Pair<>(dpStartDate.getValue(), dpEndDate.getValue());
            }
            return null;
        });
        return dialog;
    }

    @NotNull
    public static Dialog<LocalDate> makeDatePickerDialog(String prompt, String dateLabel) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Select a date");
        dialog.setHeaderText(prompt);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        var pane = new GridPane();
        pane.setHgap(10.0);
        pane.setVgap(10.0);
        pane.setPadding(new Insets(20.0, 150.0, 10.0, 10.0));

        var dp = new DatePicker();
        pane.add(new Label(dateLabel), 0, 0);
        pane.add(dp, 1, 0);

        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(it -> {
            if (it.getButtonData().isCancelButton())
                return null;
            return dp.getValue();
        });

        return dialog;
    }

    public static Dialog<LocalDate> makeDatePickerDialog(String prompt) {
        return makeDatePickerDialog(prompt, "Week start date:");
    }
}
