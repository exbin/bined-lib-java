/*
 * Copyright (C) ExBin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exbin.bined.javafx.demo;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.exbin.bined.javafx.basic.CodeArea;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;

/**
 * Binary editor JavaFX examples.
 *
 * @author ExBin Project (https://exbin.org)
 */
public class BinEdDemoApplication extends Application {

    private void init(Stage stage) {
        final CodeArea codeArea = new CodeArea();
        ByteArrayEditableData data = new ByteArrayEditableData();
        try {
            data.loadFromStream(codeArea.getClass().getResourceAsStream("/org/exbin/bined/javafx/demo/resources/lorem_1.txt"));
        } catch (IOException ex) {
            Logger.getLogger(BinEdDemoApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        codeArea.setContentData(data);

        stage.setTitle("BinEd Library JavaFX Demo");
        stage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
            System.exit(0);
        });

        Scene scene = new Scene(codeArea, 600, 480);
        stage.setScene(scene);
    }

    @Override
    public void start(Stage stage) throws Exception {
        init(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
