package com.codelog.schyfts;

import com.codelog.clogg.Logger;
import com.codelog.schyfts.util.AlertFactory;
import com.codelog.schyfts.util.PrintUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class PrintRoster extends TimerTask implements Initializable {
    public static URI loadURL;

    @FXML
    private WebView wvPrint;
    @FXML
    private MenuItem mnuPrint;
    @FXML
    private MenuItem mnuReloadPage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wvPrint.getEngine().load(loadURL.toASCIIString());
    }

    public void mnuPrintClick(ActionEvent actionEvent) {
        var stage = (Stage)wvPrint.getScene().getWindow();
        stage.setMaximized(true);
        try {
            PrintUtils.printNode(wvPrint);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException
                | InstantiationException e) {
            Logger.getInstance().exception(e);
        }
    }

    public void mnuReloadPageClick(ActionEvent actionEvent) {
        wvPrint.getEngine().reload();
    }

    @Override
    public void run() {
        wvPrint.getEngine().load(loadURL.toASCIIString());
    }
}
