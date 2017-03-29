/*
 * Copyright 2013 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *   Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 *   European Commission - subsequent versions of the EUPL (the "Licence");
 *   You may not use this work except in compliance with the Licence. 
 *   You may obtain a copy of the Licence at:
 * 
 *   http://joinup.ec.europa.eu/software/page/eupl 
 * 
 *   Unless required by applicable law or agreed to in writing, software distributed 
 *   under the Licence is distributed on an "AS IS" basis,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 *   implied.
 *   See the Licence for the specific language governing permissions and limitations 
 *   under the Licence.
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
