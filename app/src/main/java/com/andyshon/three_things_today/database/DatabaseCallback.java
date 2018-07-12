package com.andyshon.three_things_today.database;

import java.io.File;


public interface DatabaseCallback {
    void onGetTasks(String... s);
    void onExportFile(File tempFile);
}
