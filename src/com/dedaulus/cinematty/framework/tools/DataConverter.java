package com.dedaulus.cinematty.framework.tools;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Pair;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.Cinema;
import com.dedaulus.cinematty.framework.Movie;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class DataConverter {
    public static String imdbToString(float imdb) {
        if (imdb > 0) {
            return String.format(Locale.US, "imdb: %.1f", imdb);
        }
        return "";
    }
    
    public static String directorsToString(Collection<String> directors) {
        if (!directors.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String director : directors) {
                builder.append(director).append(", ");
            }

            builder.delete(builder.length() - 2, builder.length());
            return builder.toString();
        }
        return "";
    }
    
    public static String countriesToString(Collection<String> countries) {
        if (!countries.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String country : countries) {
                builder.append(country).append(", ");
            }

            builder.delete(builder.length() - 2, builder.length());
            return builder.toString();
        }
        return "";
    }
    
    public static String favActorsToString(Collection<MovieActor> actors) {
        if (!actors.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (MovieActor actor : actors) {
                if (actor.getFavourite() != 0) {
                    builder.append(actor.getName()).append(", ");
                }
            }
            if (builder.length() != 0) {
                builder.delete(builder.length() - 2, builder.length());
                return builder.toString();
            }
        }
        return "";
    }
    
    public static String genresToString(Collection<MovieGenre> genres) {
        if (!genres.isEmpty()) {
            StringBuilder genresString = new StringBuilder();
            for (MovieGenre genre : genres) {
                genresString.append(genre.getName()).append("/");
            }

            genresString.delete(genresString.length() - 1, genresString.length());
            return genresString.toString();
        }
        return "";
    }

    public static String actorsToString(Collection<MovieActor> actors) {
        if (!actors.isEmpty()) {
            StringBuilder actorsString = new StringBuilder();
            for (MovieActor genre : actors) {
                actorsString.append(genre.getName()).append(", ");
            }

            actorsString.delete(actorsString.length() - 2, actorsString.length());
            return actorsString.toString();
        }
        return "";
    }

    //public static String showTimesToString(List<Calendar> showTimes) {
    //    return showTimesToString(showTimes, Calendar.getInstance());
    //}

    public static String showTimesToString(List<Calendar> showTimes, Pair<Calendar, Calendar> timeRange) {
        if (showTimes != null) {
            StringBuilder times = new StringBuilder();
            for (Calendar showTime : showTimes) {
                if (timeRange.first.before(showTime) && timeRange.second.after(showTime)) {
                    String hours = Integer.toString(showTime.get(Calendar.HOUR_OF_DAY));
                    if (hours.length() == 1) {
                        hours = "0" + hours;
                    }

                    String minutes = Integer.toString(showTime.get(Calendar.MINUTE));
                    if (minutes.length() == 1) {
                        minutes = "0" + minutes;
                    }

                    times.append(hours).append(":").append(minutes).append(", ");
                }
            }

            if (times.length() != 0) {
                times.delete(times.length() - 2, times.length());
                return times.toString();
            }
        }

        return "";
    }

    public static SpannableString showTimesToSpannableString(Context context, List<Calendar> showTimes) {
        if (showTimes != null) {
            boolean allOutdateFound = false;
            int outdateEndIndex = 0;
            Calendar now = Calendar.getInstance();

            StringBuilder times = new StringBuilder();
            Calendar lastShowTime = null;
            for (Calendar showTime : showTimes) {
                lastShowTime = showTime;

                if (!allOutdateFound) {
                    if (now.before(showTime)) {
                        allOutdateFound = true;
                        if (times.length() > 0) {
                            outdateEndIndex = times.length() - 2;
                        }
                    }
                }

                String hours = Integer.toString(showTime.get(Calendar.HOUR_OF_DAY));
                if (hours.length() == 1) {
                    hours = "0" + hours;
                }

                String minutes = Integer.toString(showTime.get(Calendar.MINUTE));
                if (minutes.length() == 1) {
                    minutes = "0" + minutes;
                }

                times.append(hours).append(":").append(minutes).append(", ");
            }

            if (lastShowTime != null) {
                if (now.after(lastShowTime)) {
                    outdateEndIndex = times.length() - 2;
                }

                times.delete(times.length() - 2, times.length());
                SpannableString str = new SpannableString(times.toString());

                if (outdateEndIndex != 0) {
                    int endPosition = outdateEndIndex >= str.length() ? str.length() : outdateEndIndex + 1;
                    str.setSpan(new ForegroundColorSpan(Color.GRAY), 0, endPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    str.setSpan(new StrikethroughSpan(), 0, endPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                return str;
            }
        }

        SpannableString str = new SpannableString(context.getString(R.string.unknown_schedule));
        str.setSpan(new ForegroundColorSpan(Color.rgb(40, 40, 40)), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return str;
    }

    public static String showTimesToClosestTimeString(Context context, List<Calendar> showTimes) {
        String timeLeftString;
        if (!showTimes.isEmpty()) {
            Calendar now = Calendar.getInstance();
            Calendar closestTime = getClosestTime(showTimes, now);
            if (closestTime == null) {
                timeLeftString = context.getString(R.string.no_schedule) + " ";
            } else if (closestTime.equals(now)) {
                timeLeftString = context.getString(R.string.schedule_now);
            } else {
                Calendar leftTime = (Calendar)closestTime.clone();
                leftTime.add(Calendar.HOUR_OF_DAY, -now.get(Calendar.HOUR_OF_DAY));
                leftTime.add(Calendar.MINUTE, -now.get(Calendar.MINUTE));
                timeLeftString = DataConverter.timeToTimeLeft(context, leftTime);
            }
        } else {
            timeLeftString = context.getString(R.string.no_schedule) + " ";
        }

        return timeLeftString;
    }

    public static SpannableString actorsToSpannableString(Collection<MovieActor> actors) {
        if (actors != null) {
            StringBuilder actorsStr = new StringBuilder();
            List<Pair<Integer, Integer>> favActorsPoints = new ArrayList<Pair<Integer, Integer>>(actors.size());
            for (MovieActor actor : actors) {
                int start = actorsStr.length();
                actorsStr.append(actor.getName()).append(", ");
                if (actor.getFavourite() != 0) {
                    int end = actorsStr.length() - 2;
                    favActorsPoints.add(new Pair<Integer, Integer>(start, end));
                }
            }

            if (actorsStr.length() != 0) {
                actorsStr.delete(actorsStr.length() - 2, actorsStr.length());
                SpannableString str = new SpannableString(actorsStr.toString());
                for (Pair<Integer, Integer> pt : favActorsPoints) {
                    str.setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), pt.first, pt.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                return str;
            }
        }

        return new SpannableString("");
    }

    public static String timeToTimeLeft(Context context, Calendar time) {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        StringBuilder buffer = new StringBuilder();
        if (minute > 0) {
            buffer.append(" ").append(Integer.toString(minute)).append(context.getString(R.string.minute));
        }
        if (hour > 0) {
            buffer.insert(0, " " + Integer.toString(hour) + context.getString(R.string.hour));
        }

        buffer.insert(0, context.getString(R.string.schedule_start_in));
        return buffer.toString();

    }

    public static String timeInMinutesToTimeHoursAndMinutes(Context context, int minutes) {
        StringBuilder buffer = new StringBuilder();
        if (minutes > 59) {
            int hours = minutes / 60;
            minutes = minutes - hours * 60;

            buffer.append(Integer.toString(hours)).append(context.getString(R.string.hour));
            if (minutes != 0) {
                buffer.append(" ").append(Integer.toString(minutes)).append(context.getString(R.string.minute));
            }
        } else {
            buffer.append(Integer.toString(minutes)).append(context.getString(R.string.minute));
        }

        return buffer.toString();
    }

    public static String timeInMinutesToTimeHoursAndMinutes(int minutes) {
        if (minutes > 0) {
            StringBuilder buffer = new StringBuilder();
            if (minutes > 59) {
                int hours = minutes / 60;
                minutes = minutes - hours * 60;
                buffer.append(hours).append(":");
            } else {
                buffer.append("0:");
            }
            if (minutes < 10) buffer.append("0");
            buffer.append(minutes);
            return buffer.toString();
        }

        return "";
    }

    public static String metersToDistance(Context context, int meters) {
        if (meters > 1000) {
            int km = meters / 1000;
            long m = meters - km * 1000;
            m = Math.round((double)m / 100);
            if (m == 10) {
                m = 0;
                ++km;
            }
            return Integer.toString(km) + "." + Long.toString(m) + context.getString(R.string.km);
        } else {
            return Integer.toString(meters) + context.getString(R.string.m);
        }
    }

    public static String longUrlToShort(String longUrl) {
        try {
            String data = "{\"longUrl\": \"" + longUrl + "\"}";
            URL url = new URL("https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyBO_7Q_LPxcJWfWONebA-k3yQ70QlzAYFM");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(data);
            wr.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = rd.readLine()) != null) result += line;
            wr.close();
            rd.close();

            return new JSONObject(result).getString("id");
        } catch (Exception e) {
            return null;
        }
    }

    public static Calendar getClosestTime(List<Calendar> showTimes, Calendar time) {
        int id = Collections.binarySearch(showTimes, time, new Comparator<Calendar>() {
            public int compare(Calendar o1, Calendar o2) {
                int day1 = o1.get(Calendar.DAY_OF_YEAR);
                int day2 = o2.get(Calendar.DAY_OF_YEAR);

                if (day1 < day2) return -1;
                else if (day1 > day2) return 1;
                else {
                    int hour1 = o1.get(Calendar.HOUR_OF_DAY);
                    int hour2 = o2.get(Calendar.HOUR_OF_DAY);

                    if (hour1 < hour2) return -1;
                    else if (hour1 > hour2) return 1;
                    else {
                        int minute1 = o1.get(Calendar.MINUTE);
                        int minute2 = o2.get(Calendar.MINUTE);

                        if (minute1 < minute2) return -1;
                        else if (minute1 > minute2) return 1;
                        else return 0;
                    }
                }
            }
        });

        if (id >= 0) return time;
        else {
            id = -(id + 1);
            if (id == showTimes.size()) return null;
            else return showTimes.get(id);
        }
    }

    public static List<Movie> getMoviesFromTimeRange(
            Map<String, Pair<Movie, List<Calendar>>> showTimes,
            Pair<Calendar, Calendar> timeRange) {
        List<Movie> movies = new ArrayList<Movie>(showTimes.size());
        if (timeRange.second.before(timeRange.first)) return movies;

        for (Pair<Movie, List<Calendar>> showTime : showTimes.values()) {
            Calendar c = getClosestTime(showTime.second, timeRange.first);
            if (c != null && c.before(timeRange.second)) {
                movies.add(showTime.first);
            }
        }

        return movies;
    }

    public static List<Cinema> getMovieCinemasFromTimeRange(
            Collection<Cinema> cinemas, Movie movie, int day, Pair<Calendar, Calendar> timeRange)
    {
        List<Cinema> movieCinemas = new ArrayList<Cinema>(cinemas.size());
        if (timeRange.second.before(timeRange.first)) return movieCinemas;

        for (Cinema cinema : cinemas) {
            List<Calendar> showTimes = cinema.getShowTimes(day).get(movie.getName()).second;
            Calendar c = getClosestTime(showTimes, timeRange.first);
            if (c != null && c.before(timeRange.second)) {
                movieCinemas.add(cinema);
            }
        }

        return movieCinemas;
    }

    public static Pair<Calendar, Calendar> getTimeRange(int specificTime, int day) {
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();

        from.set(Calendar.MINUTE, 0);
        to.set(Calendar.MINUTE, 0);

        int hour = from.get(Calendar.HOUR_OF_DAY);
        if (hour < Constants.LAST_SHOWTIME_HOUR) {
            from.add(Calendar.DAY_OF_MONTH, -1);
            to.add(Calendar.DAY_OF_MONTH, -1);
        }

        switch (specificTime) {
            case Constants.WHOLE_DAY:
                from.set(Calendar.HOUR_OF_DAY, Constants.IN_MORNING);
                to.set(Calendar.HOUR_OF_DAY, Constants.LAST_SHOWTIME_HOUR);
                to.add(Calendar.DAY_OF_MONTH, 1);
                break;

            case Constants.IN_MORNING:
                from.set(Calendar.HOUR_OF_DAY, Constants.IN_MORNING);
                to.set(Calendar.HOUR_OF_DAY, Constants.IN_AFTERNOON);
                break;

            case Constants.IN_AFTERNOON:
                from.set(Calendar.HOUR_OF_DAY, Constants.IN_AFTERNOON);
                to.set(Calendar.HOUR_OF_DAY, Constants.IN_EVENING);
                break;

            case Constants.IN_EVENING:
                from.set(Calendar.HOUR_OF_DAY, Constants.IN_EVENING);
                to.set(Calendar.HOUR_OF_DAY, Constants.AT_NIGHT);
                break;

            case Constants.AT_NIGHT:
                from.set(Calendar.HOUR_OF_DAY, Constants.AT_NIGHT);
                to.set(Calendar.HOUR_OF_DAY, Constants.LAST_SHOWTIME_HOUR);
                to.add(Calendar.DAY_OF_MONTH, 1);
        }

        from.add(Calendar.DAY_OF_MONTH, day);
        to.add(Calendar.DAY_OF_MONTH, day);

        if (specificTime != Constants.WHOLE_DAY) {
            Calendar now = Calendar.getInstance();
            if (now.after(from)) {
                from = now;
            }
        }

        return Pair.create(from, to);
    }
}
