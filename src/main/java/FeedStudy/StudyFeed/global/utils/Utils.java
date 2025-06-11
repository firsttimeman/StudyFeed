package FeedStudy.StudyFeed.global.utils;

import java.time.LocalDate;
import java.time.Period;

public class Utils {
    public static int calculateAge(LocalDate birthday) {
        LocalDate today = LocalDate.now();
        if (birthday != null) {
            return Period.between(birthday, today).getYears();
        }
        return 0;
    }


}
