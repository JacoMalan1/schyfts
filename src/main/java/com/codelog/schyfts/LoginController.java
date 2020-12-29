package com.codelog.schyfts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private Button btnLogin;
    @FXML
    private TextField txtUname;
    @FXML
    private PasswordField pwdPass;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
    }

    public void btnLoginClick(ActionEvent actionEvent) throws IOException {

        String uname = txtUname.getText();
        String pword = pwdPass.getText();

        URL url = new URL("https://schyfts.uc.r.appspot.com/login");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

        var body = new JSONObject();
        body.put("uname", uname);
        body.put("pword", pword);

        System.out.println("Request body:");
        System.out.println(body.toString());

        writer.write(body.toString());
        writer.flush();
        writer.close();

        int responseCode = con.getResponseCode();
        System.out.println("Response code: " + responseCode);
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            builder.append(line);

        System.out.println(builder.toString());

        JSONObject response = new JSONObject(builder.toString());

        var status = response.getString("status");
        var message = response.getString("message");

        con.disconnect();

        Alert alert = new Alert(
                (status.equals("ok")) ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                message
        );

        alert.setTitle("Message");
        alert.show();

    }
}
