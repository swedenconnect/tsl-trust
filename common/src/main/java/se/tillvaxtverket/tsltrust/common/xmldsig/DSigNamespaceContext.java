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
package se.tillvaxtverket.tsltrust.common.xmldsig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

/**
 * @author wglas
 *
 */
public class DSigNamespaceContext implements NamespaceContext
{
    private Map<String,String> pfxToUri;
    private Map<String,String> uriToPfx;
    
    DSigNamespaceContext()
    {
        this.pfxToUri = new HashMap<String, String>();
        this.uriToPfx = new HashMap<String, String>();
    }
    
    /**
     * Add a prefix to namespace nmapping to the sotred list.
     * 
     * @param prefix
     * @param namespaceURI
     */
    public void addNamespace(String prefix, String namespaceURI)
    {
        this.pfxToUri.put(prefix,namespaceURI);
        this.uriToPfx.put(namespaceURI, prefix);
    }
    
    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix)
    {
        return this.pfxToUri.get(prefix);
    }

    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String namespaceURI)
    {
        return this.uriToPfx.get(namespaceURI);
    }

    /* (non-Javadoc)
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator getPrefixes(String namespaceURI)
    {
        return this.pfxToUri.keySet().iterator();
    }

}