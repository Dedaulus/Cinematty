package com.dedaulus.cinematty.framework.tools;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class DataConverter {
    public static String genresToString(Collection<MovieGenre> genres) {
        if (genres.size() != 0) {
            StringBuilder genresString = new StringBuilder();
            for (MovieGenre genre : genres) {
                genresString.append(genre.getGenre() + "/");
            }
            genresString.delete(genresString.length() - 1, genresString.length());
            return genresString.toString();
        } else {
            return "";
        }
    }

    public static String actorsToString(Collection<MovieActor> actors) {
        if (actors.size() != 0) {
            StringBuilder actorsString = new StringBuilder();
            for (MovieActor genre : actors) {
                actorsString.append(genre.getActor() + ", ");
            }
            actorsString.delete(actorsString.length() - 2, actorsString.length());
            return actorsString.toString();
        } else {
            return "";
        }
    }

    public static String showTimesToString(List<Calendar> showTimes) {
        if (showTimes != null) {
            Calendar now = Calendar.getInstance();
            StringBuffer times = new StringBuffer();

            for (Calendar showTime : showTimes) {
                if (now.before(showTime)) {
                    String hours = Integer.toString(showTime.get(Calendar.HOUR_OF_DAY));
                    if (hours.length() == 1) {
                        hours = "0" + hours;
                    }

                    String minutes = Integer.toString(showTime.get(Calendar.MINUTE));
                    if (minutes.length() == 1) {
                        minutes = "0" + minutes;
                    }

                    times.append(hours + ":" + minutes + ", ");
                }
            }

            if (times.length() != 0) {
                times.delete(times.length() - 2, times.length());
                return times.toString();
            }
        }

        return "";
    }

    public static SpannableString showTimesToSpannableString(List<Calendar> showTimes) {
        if (showTimes != null) {
            boolean allOutdateFound = false;
            int outdateEndIndex = 0;
            Calendar now = Calendar.getInstance();

            StringBuffer times = new StringBuffer();
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

                times.append(hours + ":" + minutes + ", ");
            }

            if (lastShowTime != null) {
                if (now.after(lastShowTime)) {
                    outdateEndIndex = times.length() - 2;
                }

                times.delete(times.length() - 2, times.length());
                SpannableString str = new SpannableString(times.toString());

                if (outdateEndIndex != 0) {
                    str.setSpan(new StrikethroughSpan(), 0, outdateEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                return str;
            }
        }

        return new SpannableString("");
    }
}
