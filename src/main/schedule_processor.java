package main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class schedule_processor {
    

  /**
   *  Takes a given year in int form and generates an ArrayList of String[]
   *  containing the start and end date of every smaller group used for batch 
   *  processing of season data. Note: this function starts processing on March 15
   *  and ends November 5 of any given year and works in 3 day groups.
   *
   *  @param year : the year being processed
   */
    public static String[][] generateSeasonDates(int year) {
        LocalDate seasonStart = LocalDate.of(year, 3, 15); // March 15
        LocalDate seasonEnd = LocalDate.of(year, 11, 5); // November 5

        List<String[]> weeks = new ArrayList<>();

        LocalDate currentStart = seasonStart;
        while(!currentStart.isAfter(seasonEnd)) {
            LocalDate currentEnd = currentStart.plusDays(3);
            if (currentEnd.isAfter(seasonEnd)) {
                currentEnd = seasonEnd;
            }
            weeks.add(new String[]{currentStart.toString(), currentEnd.toString()});
            currentStart = currentStart.plusDays(4);
        }
        return weeks.toArray(new String[0][0]);
    }



    public static void main(String[] args) {
        // Testing for this class
        //int year = 2023;
        //String[][] seasonInWeeks = generateSeasonDates(year);
        //
        //for (String[] week : seasonInWeeks) {
        //    System.out.println("Start: " + week[0] + " End: " + week[1]);
        //}
    }

}
