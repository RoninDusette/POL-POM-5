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

package org.phoenicis.javafx.controller.apps;

import javafx.application.Platform;
import org.phoenicis.apps.Repository;
import org.phoenicis.apps.dto.ApplicationDTO;
import org.phoenicis.apps.dto.CategoryDTO;
import org.phoenicis.javafx.views.common.ErrorMessage;
import org.phoenicis.javafx.views.mainwindow.apps.ViewApps;
import org.phoenicis.scripts.interpreter.ScriptInterpreter;
import org.phoenicis.settings.SettingsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppsController {
    private final ViewApps view;
    private final Repository repository;
    private final ScriptInterpreter scriptInterpreter;
    private final SettingsManager settingsManager;
    
    private Runnable onAppLoaded = () -> {};

    public AppsController(ViewApps view,
                          Repository repository,
                          ScriptInterpreter scriptInterpreter,
                          SettingsManager settingsManager) {
        this.view = view;
        this.repository = repository;
        this.scriptInterpreter = scriptInterpreter;
        this.settingsManager = settingsManager;
        
        this.view.setOnApplyFilter(filter -> {
            repository.setFilter(filter);
            repository.fetchInstallableApplications(
                    this.view::populate,
                    e -> this.view.showFailure()
            );
        });
    }

    public void loadApps() {
        this.view.showWait();
        repository.fetchInstallableApplications(
                this.view::populate,
                e -> this.view.showFailure()
        );

        this.view.setOnRetryButtonClicked(event -> {
            this.view.showWait();
            repository.fetchInstallableApplications(
                    this.view::populate,
                    e -> this.view.showFailure()
            );
        });

        this.view.setOnSelectAll(categories -> {
            List<ApplicationDTO> allApps = new ArrayList<>();
            for (CategoryDTO categoryDTO: categories) {
                if (categoryDTO.getType() == CategoryDTO.CategoryType.INSTALLERS) {
                    allApps.addAll(categoryDTO.getApplications());
                }
            }
            Collections.sort(allApps, Comparator.comparing(ApplicationDTO::getName));
            this.view.populateApps(allApps, settingsManager);
        });

        this.view.setOnSelectCategory(categoryDTO -> this.view.populateApps(categoryDTO.getApplications(), settingsManager));
        this.view.setOnSelectScript(scriptDTO -> scriptInterpreter.runScript(
                scriptDTO.getScript(),
                e -> Platform.runLater(() -> {
                    // no exception if installation is cancelled
                    if (!(e.getCause() instanceof InterruptedException)) {
                        new ErrorMessage("The script ended unexpectedly", e);
                    }
                })
        ));

        onAppLoaded.run();
    }

    public void setOnAppLoaded(Runnable onAppLoaded) {
        this.onAppLoaded = onAppLoaded;
    }

    public ViewApps getView() {
        return view;
    }
}
