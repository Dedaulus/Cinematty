package com.dedaulus.cinematty.framework.tools;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Dedaulus
 * Date: 21.12.11
 * Time: 19:16
 */
public class ConnectHandler extends DefaultHandler {
    // TODO: Add live time checking
    private static final String FOLDER_TAG        = "folder";
    private static final String FOLDER_NAME_ATTR  = "name";
    private static final String FOLDER_VALUE_ATTR = "value";

    private Map<String, String> connectStrings;
    
    {
        connectStrings = new HashMap<String, String>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equalsIgnoreCase(FOLDER_TAG)) {
            String name = attributes.getValue(FOLDER_NAME_ATTR);
            String value = attributes.getValue(FOLDER_VALUE_ATTR);
            connectStrings.put(name, value);
        }
    }
    
    public Map<String, String> getConnect() {
        return connectStrings.isEmpty() ? ConnectRestorer.BLANK_CONNECT : connectStrings;
    }
}
