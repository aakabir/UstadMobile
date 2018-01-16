package com.ustadmobile.port.gwt.client.util;

import java.util.Date;
import java.util.Vector;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;

/**
 * Basic calendar related utility methods. These are isolated in their own class as Calendar is not
 * supported on the GWT client.
 */
public class UMCalendarUtil {

    public static final String[] HTTP_MONTH_NAMES = new String[]{"Jan", "Feb",
            "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /*
    public static final int[] HTTP_DAYS = new int[]{ Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,
            Calendar.SUNDAY
    };
    */ 

    public static final String[] HTTP_DAY_LABELS = new String[]{"Mon", "Tue",
            "Wed", "Thu", "Fri", "Sat", "Sun"};


    /**
     * Make a String for the date given by time as an HTTP Date as per
     * http://tools.ietf.org/html/rfc2616#section-3.3
     *
     * e.g.
     * Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
     *
     * @param time The time to generate the date for
     * @return A string with a properly formatted HTTP Date
     */
    public static String makeHTTPDate(long time) {
    	/*
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(new Date(time));
        StringBuffer sb = new StringBuffer();

        int val = cal.get(Calendar.DAY_OF_WEEK);
        for(int i = 0; i < HTTP_MONTH_NAMES.length; i++) {
            if(val == HTTP_DAYS[i]) {
                sb.append(HTTP_DAY_LABELS[i]).append(", ");
                break;
            }
        }
        appendTwoDigits(cal.get(Calendar.DAY_OF_MONTH), sb).append(' ');

        sb.append(HTTP_MONTH_NAMES[cal.get(Calendar.MONTH)]).append(' ');
        sb.append(checkYear(cal.get(Calendar.YEAR))).append(' ');
        appendTwoDigits(cal.get(Calendar.HOUR_OF_DAY), sb).append(':');
        appendTwoDigits(cal.get(Calendar.MINUTE), sb).append(':');
        appendTwoDigits(cal.get(Calendar.SECOND), sb).append(" GMT");

        return sb.toString();
        */
    	
        //GWT: 
        Date givenDate = new Date(time);
        DateTimeFormat dtf = DateTimeFormat.getFormat("dd MMM yyyy HH:mm:ss GMT");
        String gmtDateTimeFormat = dtf.format(givenDate, TimeZone.createTimeZone(0));
        return gmtDateTimeFormat;
        
    }

    /**
     * Parse the given http date according to :
     *  http://tools.ietf.org/html/rfc2616#section-3.3
     *
     * @param httpDate
     * @return
     */
    public static long parseHTTPDate(String httpDate) {
    	/*
        char[] delimChars = new char[]{' ', ':', '-'};

        Vector tokens = UMUtil.tokenize(httpDate, delimChars, 0, httpDate.length());
        Calendar cal = null;

        if(tokens.size() == 8) {//this includes the timezone
            cal = Calendar.getInstance(TimeZone.getTimeZone(
                    (String)tokens.elementAt(7)));
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(
                    (String)tokens.elementAt(1)));
            cal.set(Calendar.MONTH, UMUtil.getIndexInArrayIgnoreCase(
                    (String)tokens.elementAt(2), HTTP_MONTH_NAMES));
            cal.set(Calendar.YEAR, checkYear(Integer.parseInt(
                    (String)tokens.elementAt(3))));

            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(
                    (String)tokens.elementAt(4)));
            cal.set(Calendar.MINUTE, Integer.parseInt(
                    (String)tokens.elementAt(5)));
            cal.set(Calendar.SECOND, Integer.parseInt(
                    (String)tokens.elementAt(6)));
        }else if(tokens.size() == 7) {
            cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(Calendar.MONTH, UMUtil.getIndexInArrayIgnoreCase(
                    (String)tokens.elementAt(1), HTTP_MONTH_NAMES));
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(
                    (String)tokens.elementAt(2)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(
                    (String)tokens.elementAt(3)));
            cal.set(Calendar.MINUTE, Integer.parseInt(
                    (String)tokens.elementAt(4)));
            cal.set(Calendar.SECOND, Integer.parseInt(
                    (String)tokens.elementAt(5)));

            cal.set(Calendar.YEAR, checkYear(Integer.parseInt(
                    (String)tokens.elementAt(6))));
        }else {
            return 0L;
        }

        return cal.getTime().getTime();
        */
    	
        //httpDate
        DateTimeFormat dtf = DateTimeFormat.getFormat("dd MMM yyyy HH:mm:ss GMT");
        Date httpDateDate = dtf.parse(httpDate);
        long httpDateLong = httpDateDate.getTime();
        return httpDateLong;
        
        
    }

    /**
     * Appends two digits for the integer i; if i < 10; prepend a leading 0
     *
     * @param i Numbe to append
     * @param sb StringBuffer to append it two
     * @return The stringbuffer
     */
    private static StringBuffer appendTwoDigits(int i, StringBuffer sb) {
        if(i < 10) {
            sb.append('0');
        }
        sb.append(i);

        return sb;
    }

    private static int checkYear(int year) {
        if(year < 30) {
            return 2000 + year;
        }else if(year < 100) {
            return 1900 + year;
        }else {
            return year;
        }
    }

}
