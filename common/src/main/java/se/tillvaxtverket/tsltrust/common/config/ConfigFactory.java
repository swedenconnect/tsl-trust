/*
 * Copyright 2017 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.tillvaxtverket.tsltrust.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;

/**
 * Configuration data object factory.
 */
public class ConfigFactory<E extends Object> {

    private ConfigData confData;
    private E confDataSource;
    private File configFile;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigFactory(String dataLocation, E configData) {
        confDataSource = configData;
        confData = (ConfigData) configData;
        String fileDir = FileOps.getfileNameString(dataLocation, "cfg");
        configFile = new File(fileDir, confData.getName() + ".json");
        init();
    }

    public ConfigFactory(File configFile, E configData) {
        confDataSource = configData;
        confData = (ConfigData) configData;
        this.configFile = configFile;
        init();
    }

    private void init() {
        if (configFile.canRead()) {
            try {
                String jsonData = FileOps.readTextFile(configFile);
                confDataSource = (E) gson.fromJson(jsonData, confDataSource.getClass());
                return;
            } catch (Exception ex) {
            }
        }
        createNewConfFile();
    }

    private void createNewConfFile() {
        confData.setDefaults();
        String jsonData = gson.toJson(confData);
        try {
            new File(configFile.getParent()).mkdirs();
        } catch (Exception ex) {
        }
        FileOps.saveTxtFile(configFile, jsonData);
    }

    public E getConfData() {
        return confDataSource;
    }
}
