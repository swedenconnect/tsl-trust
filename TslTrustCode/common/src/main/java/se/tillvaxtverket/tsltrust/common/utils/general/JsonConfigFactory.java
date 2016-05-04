/*
 * Copyright 2012 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
 *  		 
 * Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations 
 * under the Licence.
 */
package se.tillvaxtverket.tsltrust.common.utils.general;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;

/**
 * Factory class for generating a JSON config data instance
 */
public class JsonConfigFactory<E extends Object> {

    private JsonConfigData confData;
    private E confDataSource;
    private File configFile;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public JsonConfigFactory(String dataLocation, E configData) {
        confDataSource = configData;
        confData = (JsonConfigData) configData;
        String fileDir = FileOps.getfileNameString(dataLocation, "conf");
        configFile = new File(fileDir, confData.getName() + ".json");
//        configFile = new File(fileDir, "config.json");
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
