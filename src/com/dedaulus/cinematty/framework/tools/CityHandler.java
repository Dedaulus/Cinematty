package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.City;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 28.05.11
 * Time: 15:06
 */
public class CityHandler extends DefaultHandler {
    private static final String CITY_TAG        = "city";
    private static final String CITY_ID_ATTR    = "id";
    private static final String CITY_NAME_ATTR  = "name";
    private static final String CITY_FILE_ATTR  = "file";

    private List<City> mCityList = new ArrayList<City>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equalsIgnoreCase(CITY_TAG)) {
            int id = Integer.parseInt(attributes.getValue(CITY_ID_ATTR));
            String name = attributes.getValue(CITY_NAME_ATTR);
            String file = attributes.getValue(CITY_FILE_ATTR);

            mCityList.add(new City(id, name, file));
        }
    }

    public List<City> getCityList() {
        return mCityList;
    }
}
