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
package se.tillvaxtverket.tsltrust.weblogic.models;

import com.aaasec.lib.aaacert.AaaCertificate;
import se.tillvaxtverket.tsltrust.common.jsonobjects.DiscoveryData.DisplayNameData;
import se.tillvaxtverket.tsltrust.common.utils.core.DerefUrl;
import se.tillvaxtverket.tsltrust.common.utils.general.FileOps;
import se.tillvaxtverket.tsltrust.weblogic.db.AuthDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.db.LogDbUtil;
import se.tillvaxtverket.tsltrust.weblogic.db.TslCertDb;
import se.tillvaxtverket.tsltrust.weblogic.db.TslCertDbSqlite;
import se.tillvaxtverket.tsltrust.weblogic.content.TTConstants;
import se.tillvaxtverket.tsltrust.weblogic.db.ValPoliciesDbUtil;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import se.tillvaxtverket.tsltrust.common.config.ConfigData;
import se.tillvaxtverket.tsltrust.common.jsonobjects.DiscoveryData;
import se.tillvaxtverket.tsltrust.common.jsonobjects.RequestPassword;

/*
 * This model class holds data and objects that are used as application resources
 * resources found in objects of this class is common to all users and sessions.
 * Typical data hold by this class is information about Trust Sercice Lists,
 * information about identity providers and database resources.
 */
public class TslTrustModel implements TTConstants {

    private static final Logger LOG = Logger.getLogger(TslTrustModel.class.getName());
    private TslCertDb tslCertDb;
    private AuthDbUtil authDb;
    private LogDbUtil logDb;
    private ValPoliciesDbUtil policyDb;
    private String mode, dataLocation;
    private int tslRefreshDelay;
    private List<AaaCertificate> lotlSigCerts;
    private boolean validLotl = true;
    private Gson gson = new Gson();
    private List<DiscoveryData> discoData;
    private String discoFeedUrl;
    private static Type discoDataType = new TypeToken<List<DiscoveryData>>() {
    }.getType();
    private ConfigData conf;
    private String lotlUrl;

    public TslTrustModel(ConfigData conf, String dataLocation, String mode, String tSLrecacheTime, String maxConsoleLogSize,String maxMajorLogAge, String lotlUrl, String discoFeedUrl ) {

        int tslRefreshDelay;
        try {
            tslRefreshDelay = Integer.parseInt(tSLrecacheTime) * 1000 * 60 * 60;
        } catch (Exception ex) {
            tslRefreshDelay = 1000 * 60 * 60 * 24;
        }


        this.mode = mode;
        this.dataLocation = dataLocation;
        this.tslRefreshDelay = tslRefreshDelay;
        this.discoFeedUrl = discoFeedUrl;
        this.lotlUrl = lotlUrl;
        this.conf = conf;
        tslCertDb = new TslCertDbSqlite(dataLocation);
        authDb = new AuthDbUtil(dataLocation);
        logDb = new LogDbUtil(maxConsoleLogSize, maxMajorLogAge);
        policyDb = new ValPoliciesDbUtil();        

        loadDiscoFeed();
    }

    public ConfigData getConf() {
        return conf;
    }

    public void setConf(ConfigData conf) {
        this.conf = conf;
    }

    
    public AuthDbUtil getAuthDb() {
        return authDb;
    }

    public String getDataLocation() {
        return dataLocation;
    }

    public String getMode() {
        return mode;
    }

    public TslCertDb getTslCertDb() {
        return tslCertDb;
    }

    public LogDbUtil getLogDb() {
        return logDb;
    }

    public ValPoliciesDbUtil getPolicyDb() {
        return policyDb;
    }

    /**
     * Time between server updates with regard to TSL re-cache and policy
     * certificate updates
     *
     * @return Number of milliseconds between server updates
     */
    public int getTslRefreshDelay() {
        return tslRefreshDelay;
    }

    public List<AaaCertificate> getLotlSigCerts() {
        return lotlSigCerts;
    }

    public void setLotlSigCerts(List<AaaCertificate> lotlSigCert) {
        this.lotlSigCerts = lotlSigCert;
    }

    public boolean isValidLotl() {
        return validLotl;
    }

    public void setValidLotl(boolean validLotl) {
        this.validLotl = validLotl;
    }

    public String getLotlUrl() {
        return lotlUrl;
    }

    

    private void loadDiscoFeed() {
        if (discoFeedUrl == null || discoFeedUrl.length() == 0) {
            return;
        }
        try {
            URL url = new URL(discoFeedUrl);
            String jsonData = stripJsonp(DerefUrl.getData(url));
            discoData = gson.fromJson(jsonData, discoDataType);

        } catch (Exception ex) {
            return;
        }
    }

    public String getTempDataLocation() {
        return dataLocation + "TempFiles/";
    }

    public Map<String, String> getIdpDisplayNames() {
        return getIdpDisplayNames(Locale.ENGLISH);
    }

    public Map<String, String> getIdpDisplayNames(Locale locale) {
        Map<String, String> idpDisplayNames = new HashMap<String, String>();
        if (discoData == null) {
            return idpDisplayNames;
        }


        for (DiscoveryData dData : discoData) {
            try {
                String entityId = dData.EntityID;
                if (dData.EntityID.length() > 0) {
                    String dispName = getDisplayName(dData.DisplayNames, entityId, locale);
                    idpDisplayNames.put(entityId, dispName);
                }
            } catch (Exception ex) {
                LOG.warning(ex.getMessage());
            }
        }
        return idpDisplayNames;
    }

    private String stripJsonp(String jsonp) {
        if (jsonp == null || jsonp.length() < 2) {
            return "";
        }
        String inp = jsonp.trim();
        int first = inp.indexOf("(");
        int last = inp.lastIndexOf(")");
        //If string ends with )
        if (last == inp.length() - 1) {
            if (first == -1 || first > last) {
                //Illegal json and jsonp
                return "";
            }
            //Strinp callback
            return inp.substring(first + 1, last);
        }
        //String was not jsonp, return original
        return inp;
    }

    private String getDisplayName(List<DiscoveryData.DisplayNameData> dispNames, String entityId, Locale locale) {
        if (dispNames == null) {
            return (entityId == null) ? "" : entityId;
        }

        String locDispName = "";
        String defDispName = "";
        String langCode = locale.getLanguage();

        for (DisplayNameData dnData : dispNames) {
            try {
                if (dnData.lang.equalsIgnoreCase(langCode)) {
                    locDispName = dnData.value;
                }
                if (dnData.lang.equalsIgnoreCase("en")) {
                    defDispName = dnData.value;
                }
            } catch (Exception ex) {
                return entityId;
            }
        }
        if (locDispName.length() > 0) {
            return locDispName;
        }
        if (defDispName.length() > 0) {
            return defDispName;
        }
        return entityId;
    }

    public List<String> getCurrentRequestPassword() {
        List<RequestPassword> reqPassList = new ArrayList<RequestPassword>();
        long currentTime = System.currentTimeMillis();
        long minVal = 1000 * 60 * 60 * 24 * 2; // 2 days validity is minimum, else a new pw is created
        long maxVal = 1000 * 60 * 60 * 24 * 3; // New pw is created with 3 days validity
        List<String> currentPassw = new ArrayList<String>();
        File reqPwFile = new File(FileOps.getfileNameString(dataLocation, "cfg"), "rplist");
        if (!reqPwFile.canRead()) {
            reqPwFile.getParentFile().mkdirs();
        }
        long recentExp = 0;
        String recentPw = "";
        try {
            String jsonData = FileOps.readTextFile(reqPwFile);
            reqPassList = gson.fromJson(jsonData, new TypeToken<List<RequestPassword>>() {
            }.getType());
            for (int i = reqPassList.size() - 1; i >= 0; i--) {
                RequestPassword rp = reqPassList.get(i);
                try {
                    if (rp.exp < currentTime) {
                        reqPassList.remove(i);
                        continue;
                    }
                    if (rp.exp > recentExp) {
                        recentExp = rp.exp;
                        recentPw = rp.rp;
                    }
                } catch (Exception ex) {
                    reqPassList.remove(i);
                }
            }
        } catch (Exception ex) {
            reqPassList = new ArrayList<RequestPassword>();
        }

        if (recentExp < (currentTime + minVal)) {
            String pw = getNewPW();
            RequestPassword rp = new RequestPassword();
            rp.rp = pw;
            rp.exp = currentTime + maxVal;
            reqPassList.add(rp);
            currentPassw.add(pw);
            currentPassw.add(TIME_FORMAT.format(new Date(currentTime + maxVal)));
        } else {
            currentPassw.add(recentPw);
            currentPassw.add(TIME_FORMAT.format(new Date(recentExp)));
        }
        FileOps.saveTxtFile(reqPwFile, gson.toJson(reqPassList));
        return currentPassw;
    }

    public boolean checkRequestPw(String inpPwd) {
        List<RequestPassword> reqPassList = new ArrayList<RequestPassword>();
        long currentTime = System.currentTimeMillis();
        File reqPwFile = new File(dataLocation + "cfg/rplist");
        if (!reqPwFile.canRead()) {
            return false;
        }
        try {
            String jsonData = FileOps.readTextFile(reqPwFile);
            reqPassList = gson.fromJson(jsonData, new TypeToken<List<RequestPassword>>() {
            }.getType());
        } catch (Exception ex) {
        }

        for (int i = reqPassList.size() - 1; i >= 0; i--) {
            RequestPassword rp = reqPassList.get(i);
            try {
                if (rp.rp.equals(inpPwd)) {
                    if (rp.exp > currentTime) {
                        return true;
                    }
                }
            } catch (Exception ex) {
            }
        }
        return false;
    }

    private String getNewPW() {
        char[] symbols = new char[36];
        for (int idx = 0; idx < 10; ++idx) {
            symbols[idx] = (char) ('0' + idx);
        }
        for (int idx = 10; idx < 36; ++idx) {
            symbols[idx] = (char) ('a' + idx - 10);
        }

        Random random = new Random(System.currentTimeMillis());
        char[] buf;

        int length = 8;
        buf = new char[length];

        for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }
}
