/*
 * Copyright (C) 2015-2017 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.phoenicis.javafx.views.mainwindow.apps;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import org.phoenicis.apps.dto.ApplicationDTO;
import org.phoenicis.apps.dto.ScriptDTO;
import org.phoenicis.javafx.views.common.ErrorMessage;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.phoenicis.javafx.views.common.ThemeManager;
import org.phoenicis.settings.Setting;
import org.phoenicis.settings.Settings;
import org.phoenicis.settings.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.function.Consumer;

final class AppPanel extends VBox {
    private final Logger LOGGER = LoggerFactory.getLogger(AppPanel.class);

    public void setOnScriptInstall(Consumer<ScriptDTO> onScriptInstall) {
        this.onScriptInstall = onScriptInstall;
    }

    private Consumer<ScriptDTO> onScriptInstall = (script) -> {};

    public AppPanel(ApplicationDTO applicationDTO, ThemeManager themeManager, SettingsManager settingsManager) {
        super();
        this.getStyleClass().addAll("rightPane", "appPresentation");
        this.setPadding(new Insets(10));

        final VBox descriptionWidget = new VBox();
        Label appName = new Label(applicationDTO.getName());
        appName.getStyleClass().add("descriptionTitle");
        WebView appDescription = new WebView();
        VBox.setVgrow(appDescription, Priority.ALWAYS);
        appDescription.getEngine().loadContent("<body>" + applicationDTO.getDescription() + "</body>");
        final URL style = getClass().getResource(String.format("/org/phoenicis/javafx/themes/%s/description.css", themeManager.getCurrentTheme().getShortName()));
        appDescription.getEngine().setUserStyleSheetLocation(style.toString());
        Label installers = new Label("Installers");
        installers.getStyleClass().add("descriptionTitle");

        GridPane grid = new GridPane();
        grid.setHgap(100);
        int row = 0;
        for (ScriptDTO script: applicationDTO.getScripts()) {
        	Label scriptName;
        	if (settingsManager.isViewScriptSource()) {
        		scriptName = new Label(String.format("%s (Source: %s)", script.getName(), script.getScriptSource()));
        	} else {
        		scriptName = new Label(script.getName());
        	}
            scriptName.getStyleClass().add("descriptionText");
            Button installButton = new Button("Install");
            installButton.setOnMouseClicked(evt -> {
                try {
                    onScriptInstall.accept(script);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("Failed to get script", e);
                    new ErrorMessage("Error while trying to download the installer", e).show();
                }
            });
            grid.addRow(row,scriptName, installButton);
            row++;
        }

        descriptionWidget.getChildren().addAll(appName, appDescription, installers, grid);

        final HBox miniaturesPane = new HBox();
        miniaturesPane.getStyleClass().add("appPanelMiniaturesPane");

        final ScrollPane miniaturesPaneWrapper = new ScrollPane(miniaturesPane);
        miniaturesPaneWrapper.getStyleClass().add("appPanelMiniaturesPaneWrapper");

        for (byte[] miniatureBytes : applicationDTO.getMiniatures()) {
            Image image = new Image(new ByteArrayInputStream(miniatureBytes));
            ImageView imageView = new ImageView(image);
            imageView.fitHeightProperty().bind(miniaturesPaneWrapper.heightProperty().multiply(0.8));
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);
            miniaturesPane.getChildren().add(imageView);
        }

        getChildren().addAll(descriptionWidget, miniaturesPaneWrapper);
    }
}
