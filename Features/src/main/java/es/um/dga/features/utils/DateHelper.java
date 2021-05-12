/**
 * Project: DomainNameProfiler
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Helper for datetime operations.
 */
public class DateHelper {
    
    /**
     * Provides the difference between two LocalDateTime objects in human readable format.
     * @param start Start datetime.
     * @param end End datetime.
     * @return Difference according to the format '[0Y ][0M ][0D ]00:00:00'
     */
    public static String humanReadableDifference(LocalDateTime start, LocalDateTime end) {
        try {
            long years = start.until(end, ChronoUnit.YEARS);
            start = start.plusYears(years);
    
            long months = start.until(end, ChronoUnit.MONTHS);
            start = start.plusMonths(months);
    
            long days = start.until(end, ChronoUnit.DAYS);
            start = start.plusDays(days);
    
            long hours = start.until(end, ChronoUnit.HOURS);
            start = start.plusHours(hours);
    
            long minutes = start.until(end, ChronoUnit.MINUTES);
            start = start.plusMinutes(minutes);
    
            long seconds = start.until(end, ChronoUnit.SECONDS);
            
            long milliseconds = start.until(end, ChronoUnit.MILLIS);
    
            String result = "";
    
            if (years > 0) {
                result += years + "Y ";
            }
            if (months > 0) {
                result += months + "M ";
            }
            if (days > 0) {
                result += days + "D ";
            }
    
            result += String.format("%02d", hours) + ":";
            result += String.format("%02d", minutes) + ":";
            result += String.format("%02d", seconds) + ".";
            result += String.format("%010d", milliseconds);
    
            return result;
        }
        catch (Exception ex) {
            return "ER:R:O.R!";
        }
    }
}
