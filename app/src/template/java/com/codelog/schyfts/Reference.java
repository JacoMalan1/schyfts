package com.codelog.schyfts;

import javafx.scene.image.Image;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

public class Reference {
    public static String API_URL = "https://schyfts.uc.r.appspot.com/";
//    public static String API_URL = "http://localhost:8080/";
    public static final String GOOGLE_CLOUD_PROJECT_ID = "schyfts";
    public static final LocalDate GENESIS_TIME = LocalDate.parse("2021-04-05");
    public static final Image ICON = new Image(
            Objects.requireNonNull(Reference.class.getClassLoader().getResourceAsStream("nelanest.ico")));

    public static final Image LOGO = new Image(
            Objects.requireNonNull(Reference.class.getClassLoader().getResourceAsStream("nelanest.png"))
    );

    public static final String VERSION_STRING = "${version}";
    public static final String DEFAULT_BROWSER_PATH = "/usr/bin/chromium";
    public static final String BROWSER_PATH_CONFIG_KEY = "/usr/bin/chromium";
}
