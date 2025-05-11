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
package org.exbin.bined.javafx.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.annotation.ParametersAreNonnullByDefault;
import org.exbin.auxiliary.binary_data.ByteArrayEditableData;
import org.exbin.bined.javafx.basic.CodeArea;

/**
 * BinEd component usage example.
 *
 * @author ExBin Project (https://exbin.org)
 */
@ParametersAreNonnullByDefault
public class BinEdExample extends Application {

    public static void main(String[] args) {
        // You can use this component for your own project using one of the
        // following options:
        //
        // - Download library and include it with your project
        // - Download sources and modify it for your needs
        // - Import library using Maven
        //
        // Libraries (groupId:artifactId:version):
        //   org.exbin.bined:bined-core:0.2.2
        //   org.exbin.bined:bined-javafx:0.2.2
        //   org.exbin.auxiliary:binary_data:0.2.2
        //   org.exbin.auxiliary:binary_data-array:0.2.2

        BinEdExample.launch(args);
    }

    private void init(Stage stage) {
        // Create component instance
        CodeArea codeArea = new CodeArea();

        // Fill it with some data
        codeArea.setContentData(new ByteArrayEditableData(new byte[]{1, 2, 3, 0x45, 0x58, 0x41, 0x4D, 0x50, 0x4C, 0x45}));

        // Add it to frame to display it
        stage.setTitle("BinEd Example");
        stage.setOnCloseRequest((WindowEvent e) -> {
            Platform.exit();
            System.exit(0);
        });

        Scene scene = new Scene(codeArea, 1000, 600);
        stage.setScene(scene);
    }

    @Override
    public void start(Stage stage) throws Exception {
        init(stage);
        stage.show();
    }
}
