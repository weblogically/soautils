package weblogically.wsclient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext {

	// prefix to namespace map
    private final Map <String, String> prefixMap = new HashMap <String, String> ();
    
    public SimpleNamespaceContext() {
    }
    
    public void setNamespacePrefix(String prefix, String uri) {
    	prefixMap.put(prefix, uri);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    

    public SimpleNamespaceContext(final Map<String, String> prefixMap) {
        prefixMap.putAll(prefixMap);       
    }

    public String getNamespaceURI(String prefix) {
        return prefixMap.get(prefix);
    }

    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }

}
