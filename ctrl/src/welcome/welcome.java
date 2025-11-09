package welcome;
/**
 *
 * @author mingdao
 */

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class welcome {
    
    /**
     * Landing page for user
     * 
     * username the user name that is supposed to be received from the text file
     */
    

    // Overloaded method that accepts a username so callers can pass the logged-in user's name
    public static void welcome_user(String name) {
        ZoneId on9 = ZoneId.of("GMT+8");
        LocalDateTime current = LocalDateTime.now(on9);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("'It is now' EEEE, MMMM d, yyyy 'at' h:mm a'.'");
        String formattedCurrent = current.format(f);
        System.out.println(formattedCurrent);
        LocalTime morning = LocalTime.of(00, 0);
        LocalTime afternoon = LocalTime.of(12, 0);
        LocalTime evening = LocalTime.of(17, 0);

        if (!current.toLocalTime().isAfter(morning) && current.toLocalTime().isBefore(afternoon)) {
            System.out.printf("Good morning, %s!", name);

        } else if (!current.toLocalTime().isAfter(afternoon) && current.toLocalTime().isBefore(evening)) {
            System.out.printf("Good afternoon, %s!", name);

        } else {
            System.out.printf("Good evening, %s!", name);
        }
    }

}
