package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Metro;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Dedaulus
 * Date: 16.09.11
 * Time: 19:14
 */
public class ActivitiesStateRestorer {
    public static final Map<String, ActivityState> BLANK_STATES = new HashMap<String, ActivityState>();
    
    private File statesFile;
    private ApplicationSettings settings;

    public ActivitiesStateRestorer(File statesFile, ApplicationSettings settings) {
        this.statesFile = statesFile;
        this.settings = settings;
    }

    public Map<String, ActivityState> getStates() {
        InputStream is;
        try {
            is = new FileInputStream(statesFile);
        } catch (FileNotFoundException e) {
            return BLANK_STATES;
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            Map<String, Metro> metros = new HashMap<String, Metro>();
            for (Cinema cinema : settings.getCinemas().values()) {
                for (Metro metro : cinema.getMetros()) {
                    metros.put(metro.getName(), metro);
                }
            }

            ActivitiesStateHandler handler = new ActivitiesStateHandler(
                    settings.getCinemas(),
                    settings.getMovies(),
                    settings.getDirectors(),
                    settings.getActors(),
                    settings.getGenres(),
                    metros);
            parser.parse(is, handler);
            return handler.getStates();
        } catch (Exception e) {
            return BLANK_STATES;
        }
    }
}
