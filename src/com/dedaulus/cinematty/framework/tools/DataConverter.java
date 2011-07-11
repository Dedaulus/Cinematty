package com.dedaulus.cinematty.framework.tools;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Pair;
import com.dedaulus.cinematty.R;
import com.dedaulus.cinematty.framework.MovieActor;
import com.dedaulus.cinematty.framework.MovieGenre;

import java.util.ArrayList;
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
                    int endPosition = outdateEndIndex >= str.length() ? str.length() : outdateEndIndex + 1;
                    str.setSpan(new StrikethroughSpan(), 0, endPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    str.setSpan(new ForegroundColorSpan(Color.rgb(192, 192, 192)), 0, endPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                return str;
            }
        }

        return new SpannableString("");
    }

    public static SpannableString actorsToSpannableString(Collection<MovieActor> actors) {
        if (actors != null) {
            StringBuilder actorsStr = new StringBuilder();
            List<Pair<Integer, Integer>> favActorsPoints = new ArrayList<Pair<Integer, Integer>>(actors.size());
            for (MovieActor actor : actors) {
                int start = actorsStr.length();
                actorsStr.append(actor.getActor() + ", ");
                if (actor.getFavourite() != 0) {
                    int end = actorsStr.length() - 2;
                    favActorsPoints.add(new Pair<Integer, Integer>(start, end));
                }
            }

            if (actorsStr.length() != 0) {
                actorsStr.delete(actorsStr.length() - 2, actorsStr.length());
                SpannableString str = new SpannableString(actorsStr.toString());
                for (Pair<Integer, Integer> pt : favActorsPoints) {
                    str.setSpan(new ForegroundColorSpan(Color.rgb(255, 204, 0)), pt.first, pt.second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                return str;
            }
        }

        return new SpannableString("");
    }

    public static String timeToTimeLeft(Context context, Calendar time) {
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

        StringBuffer buffer = new StringBuffer();
        if (minute > 0) {
            buffer.append(" " + Integer.toString(minute) + context.getString(R.string.minute));
        }
        if (hour > 0) {
            buffer.insert(0, " " + Integer.toString(hour) + context.getString(R.string.hour));
        }

        buffer.insert(0, context.getString(R.string.schedule_start_in));
        return buffer.toString();

    }

    public static String timeInMinutesToTimeHoursAndMinutes(Context context, int minutes) {
        StringBuffer buffer = new StringBuffer();
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

    public static String metersToDistance(Context context, int meters) {
        if (meters > 1000) {
            int km = meters / 1000;
            long m = meters - km * 1000;
            m = Math.round((double)m / 100);
            if (m == 10) {
                m = 0;
                ++km;
            }

            //if (m == 0) {
                //return Integer.toString(km) + context.getString(R.string.km);
            //} else {
                return Integer.toString(km) + "." + Long.toString(m) + context.getString(R.string.km);
            //}
        } else {
            return Integer.toString(meters) + context.getString(R.string.m);
        }
    }
}
