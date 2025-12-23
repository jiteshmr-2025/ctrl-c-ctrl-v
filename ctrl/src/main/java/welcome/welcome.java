package welcome;

/**
 *
 * @author mingdao
 */

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class welcome {

    public static String welcome_user(String name) {
        ZoneId zone = ZoneId.of("GMT+8");
        LocalDateTime current = LocalDateTime.now(zone);
        
        LocalTime time = current.toLocalTime();
        
        // Simplified Logic
        if (time.isBefore(LocalTime.of(12, 0))) {
            return "Good morning, " + name + ".";
        } else if (time.isBefore(LocalTime.of(17, 0))) {
            return "Good afternoon, " + name + ".";
        } else {
            return "Good evening, " + name + ".";
        }
    }
}
