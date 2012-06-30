package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.framework.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * User: Dedaulus
 * Date: 13.03.11
 * Time: 22:37
 */
public class ScheduleReceiver {
    private static class OutOfDateException extends Exception {
        public OutOfDateException(){}
    }

    private static final int VALID_HOURS_FOR_JUST_DOWNLOADED = 12;
    
    private URL scheduleUrl;
    private File scheduleFile;

    public ScheduleReceiver(URL scheduleUrl, File scheduleFile) {
        this.scheduleUrl = scheduleUrl;
        this.scheduleFile = scheduleFile;
    }

    public SyncStatus getSchedule(
            Map<String, Cinema> cinemas,
            Map<String, Movie> movies,
            Map<String, MovieActor> actors,
            Map<String, MovieGenre> genres,
            List<MoviePoster> posters,
            boolean local) {
        try {
            InputStream is = getActualXmlStream(local);
            if (is == null) return SyncStatus.BAD_RESPONSE;

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ScheduleHandler handler = new ScheduleHandler();
            parser.parse(is, handler);
            handler.getSchedule(cinemas, movies, actors, genres, posters);
            return SyncStatus.OK;
        } catch (FileNotFoundException e) {
            return SyncStatus.BAD_RESPONSE;
        } catch (ParserConfigurationException e) {
            return SyncStatus.BAD_RESPONSE;
        } catch (SAXException e) {
            return SyncStatus.BAD_RESPONSE;
        } catch (IOException e) {
            return SyncStatus.BAD_RESPONSE;
        } catch (OutOfDateException e) {
            return SyncStatus.OUT_OF_DATE;
        } catch (NumberFormatException e) { // In case we received bad data to parse
            return SyncStatus.BAD_RESPONSE;
        }
    }

    private InputStream getActualXmlStream(boolean local) throws OutOfDateException {
        InputStream is;
        try {
            is = new FileInputStream(scheduleFile);
            if (isActualXmlStream(is, 0)) {
                is = new FileInputStream(scheduleFile);
            } else {
                is = null;
            }
        } catch (FileNotFoundException e) {
            is = null;
        }

        if (!local) {
            if (is == null) {
                try {
                    is = dumpStream(scheduleUrl.openConnection().getInputStream());
                    if (isActualXmlStream(is, VALID_HOURS_FOR_JUST_DOWNLOADED)) {
                        is = new FileInputStream(scheduleFile);
                    } else {
                        throw new OutOfDateException();
                    }
                } catch (IOException e) {
                    is = null;
                }
            }
        }
        
        return is;
    }

    private boolean isActualXmlStream(InputStream is, int hours) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ScheduleDateHandler handler = new ScheduleDateHandler();
            parser.parse(is, handler);
    
            Calendar got = handler.getActualDate();
            got.add(Calendar.HOUR_OF_DAY, hours);
            Calendar now = Calendar.getInstance();
            return now.before(got);
        } catch (SAXException e) {
            return false;
        } catch (ParserConfigurationException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private InputStream dumpStream(InputStream is) throws IOException {
        GZIPInputStream input = new GZIPInputStream(new BufferedInputStream(is));
        OutputStream output = new FileOutputStream(scheduleFile);
        byte data[] = new byte[1024];

        int count;
        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        input.close();

        return new FileInputStream(scheduleFile);
    }
}
