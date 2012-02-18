package com.dedaulus.cinematty.framework.tools;

import com.dedaulus.cinematty.ApplicationSettings;
import com.dedaulus.cinematty.CinemattyApplication;

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
 * Date: 21.12.11
 * Time: 19:06
 */
public class ConnectRestorer {
    public static final Map<String, String> BLANK_CONNECT = new HashMap<String, String>();

    private File connectFile;

    public ConnectRestorer(File connectFile) {
        this.connectFile = connectFile;
    }

    public Map<String, String> getConnect() {
        InputStream is;
        try {
            is = new FileInputStream(connectFile);
        } catch (FileNotFoundException e) {
            return BLANK_CONNECT;
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ConnectHandler handler = new ConnectHandler();
            parser.parse(is, handler);
            return handler.getConnect();
        } catch (Exception e) {
            return BLANK_CONNECT;
        }
    }
}
