package com.dedaulus.cinematty.framework.tools;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

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

    private void parseDate(String dateStr, int hours) {
        List<Integer> list = new ArrayList<Integer>();
        StringTokenizer st = new StringTokenizer(dateStr, ".");

        while (st.hasMoreTokens()) {
            list.add(Integer.parseInt(st.nextToken()));
        }

        mDate = Calendar.getInstance();

        mDate.set(Calendar.YEAR, list.get(0));
        mDate.set(Calendar.MONTH, list.get(1) - 1);
        mDate.set(Calendar.DAY_OF_MONTH, list.get(2));
        mDate.set(Calendar.HOUR_OF_DAY, list.get(3));
        mDate.set(Calendar.MINUTE, list.get(4));
        mDate.set(Calendar.SECOND, list.get(5));

        mDate.add(Calendar.HOUR_OF_DAY, hours);
    }
}
