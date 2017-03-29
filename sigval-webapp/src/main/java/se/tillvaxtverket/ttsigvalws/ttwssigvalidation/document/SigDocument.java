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
package se.tillvaxtverket.ttsigvalws.ttwssigvalidation.document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author stefan
 */
public class SigDocument {

    private File sigFile;
    private byte[] byteSource;
    private DocType docType;
    private DataSourceType dataSource;
    private String docName;

    public SigDocument(File sigFile, DocType docType) {
        this.sigFile = sigFile;
        this.docType = docType;
        dataSource = DataSourceType.file;
    }

    public SigDocument(byte[] byteSource, DocType docType) {
        this.byteSource = byteSource;
        this.docType = docType;
        dataSource = DataSourceType.byteArray;
    }

    public SigDocument(File sigFile) {
        this.sigFile = sigFile;
        this.dataSource = DataSourceType.file;
        this.docType = DocTypeIdentifier.getDocType(getDocInputStream());
    }

    public SigDocument(byte[] byteSource) {
        this.byteSource = byteSource;
        this.dataSource = DataSourceType.byteArray;
        this.docType = DocTypeIdentifier.getDocType(getDocInputStream());
    }

    public final InputStream getDocInputStream() {
        switch (this.dataSource) {
            case byteArray:
                return new ByteArrayInputStream(byteSource);
            case file: {
                try {
                    return new FileInputStream(sigFile);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SigDocument.class.getName()).warning(ex.getMessage());
                    return null;
                }
            }
            default:
                throw new AssertionError(this.dataSource.name());
        }
    }
    
    public byte[] getDocBytes(){
        try {
            return IOUtils.toByteArray(getDocInputStream());
        } catch (IOException ex) {
            return null;
        }
    }

    /*
     * Getters and Setters
     */

    public DocType getDocType() {
        return docType;
    }

    public DataSourceType getDataSource() {
        return dataSource;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

}
