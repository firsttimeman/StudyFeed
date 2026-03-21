package FeedStudy.StudyFeed.global.utils;

import java.time.LocalDate;
import java.time.Period;

public class Utils {
    public static int calculateAge(LocalDate birthday) {
        if (birthday == null) {
            throw new IllegalArgumentException("birthDate is required");
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }


}
