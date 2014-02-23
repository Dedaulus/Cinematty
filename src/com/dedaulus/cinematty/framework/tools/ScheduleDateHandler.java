package com.dedaulus.cinematty.framework.tools;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * User: Dedaulus
 * Date: 26.05.11
 * Time: 21:13
 */
public class ScheduleDateHandler extends DefaultHandler {
    private static final String DATA_TAG        = "data";
    private static final String DATA_DATE_ATTR  = "date";
    private static final String DATA_LIVE_ATTR  = "live_time";
    private Calendar mDate;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equalsIgnoreCase(DATA_TAG)) {
            String date = attributes.getValue(DATA_DATE_ATTR);
            int live_hours = Integer.parseInt(attributes.getValue(DATA_LIVE_ATTR));
            parseDate(date, live_hours);
        }
    }

    public Calendar getActualDate() {
        return mDate;
    }

    private void parseDate(String dateTimeStr, int hours) {
        mDate = Calendar.getInstance();
        List<Integer> date = new ArrayList<Integer>(3);
        List<Integer> time = new ArrayList<Integer>(3);

        String[] dateTimeStrings = dateTimeStr.split("\\.");

        for (String s : dateTimeStrings[0].split("-")) {
            date.add(Integer.parseInt(s));
        }

        for (String s : dateTimeStrings[1].split(":")) {
            time.add(Integer.parseInt(s));
        }

        mDate.set(Calendar.YEAR, date.get(0));
        mDate.set(Calendar.MONTH, date.get(1) - 1);
        mDate.set(Calendar.DAY_OF_MONTH, date.get(2));
        mDate.set(Calendar.HOUR_OF_DAY, time.get(0));
        mDate.set(Calendar.MINUTE, time.get(1));
        mDate.set(Calendar.SECOND, time.get(2));

        mDate.add(Calendar.HOUR_OF_DAY, hours);
    }
}
