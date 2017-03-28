/*
 * Copyright 2015 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
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
package se.tillvaxtverket.tsltrust.weblogic.issuestack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import se.tillvaxtverket.tsltrust.common.utils.general.EuropeCountry;
import java.lang.reflect.Type;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;

/**
 * This class provides a stack for pushing TSL issues. The data gathered here
 * can be used by a reporting system, like an e-mail notification system to
 * notify selected recipients of various problems.
 */
public class TSLIssueStack {

    private static Map<EuropeCountry, Map<TSLIssueID, TslIssue>> tslIssueMap = new EnumMap<EuropeCountry, Map<TSLIssueID, TslIssue>>(EuropeCountry.class);
    private static File issueStackFile;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Type issueMapType = new TypeToken<Map<EuropeCountry, Map<TSLIssueID, TslIssue>>>() {
    }.getType();

    public static void push(EuropeCountry country, TSLIssueID tslIssueID, TSLIssueSubcode tslIssueSubcode, String parameter) {
        if (!tslIssueMap.containsKey(country)) {
            tslIssueMap.put(country, new EnumMap<TSLIssueID, TslIssue>(TSLIssueID.class));
        }
        Map<TSLIssueID, TslIssue> countryIssueMap = tslIssueMap.get(country);

        if (!countryIssueMap.containsKey(tslIssueID)) {
            countryIssueMap.put(tslIssueID, new TslIssue(tslIssueID));
        }
        TslIssue issue = countryIssueMap.get(tslIssueID);

        //check if issue is new
        if (issue.getNextNotification() == null) {
            issue.setNextNotification(new Date());
        }
        issue.setSubcode(tslIssueSubcode);
        if (parameter != null) {
            Map<String, String> paramMap = new HashMap<String, String>();
            if (tslIssueID.equals(TSLIssueID.unavailable)) {
                paramMap.put("Attempted location", parameter);
                issue.setParamMap(paramMap);
            }
        }

        backupIssueMap();
    }

    public static void backupIssueMap() {
        if (issueStackFile != null) {
            String issueJson = gson.toJson(tslIssueMap, issueMapType);
            FileOps.saveTxtFile(issueStackFile, issueJson);
        }
    }

    public static void clear(EuropeCountry country, TSLIssueID tslIssueID) {
        if (tslIssueMap.containsKey(country)) {
            Map<TSLIssueID, TslIssue> countryIssueMap = tslIssueMap.get(country);
            if (countryIssueMap.containsKey(tslIssueID)) {
                countryIssueMap.remove(tslIssueID);
            }
            if (countryIssueMap.isEmpty()) {
                tslIssueMap.remove(country);
            }
        }
    }

    public static Map<EuropeCountry, Map<TSLIssueID, TslIssue>> getTslIssueMap() {
        return tslIssueMap;
    }

    public static void clearAll() {
        tslIssueMap = new EnumMap<EuropeCountry, Map<TSLIssueID, TslIssue>>(EuropeCountry.class);
    }

    public static void initStack(File issueStackFile) {
        TSLIssueStack.issueStackFile = issueStackFile;
        if (issueStackFile.canRead()) {
            String issueDataJson = FileOps.readTextFile(issueStackFile);
            tslIssueMap = gson.fromJson(issueDataJson, issueMapType);
        }
    }

}
