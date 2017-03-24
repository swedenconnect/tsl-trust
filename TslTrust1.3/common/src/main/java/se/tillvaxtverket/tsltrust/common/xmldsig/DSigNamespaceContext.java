/***********************************************************
 * $Id$
 * 
 * Tapestry support for the austrian security card layer.
 * 
 * Copyright (C) 2007 ev-i Informationstechnologie GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 * Created: Apr 6, 2007
 *
 * Author: wglas
 * 
 ***********************************************************/

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