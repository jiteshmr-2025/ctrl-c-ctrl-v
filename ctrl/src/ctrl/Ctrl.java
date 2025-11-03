/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package ctrl;

import org.JSON.JSONArray;
import weather.API_Get;

/**
 *
 * @author jites
 */
public class Ctrl {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        API_Get api = new API_Get();

        try {
            // --- Example GET request: Fetch latest weather forecast for Kuala Lumpur ---
            String getUrl = "https://api.data.gov.my/weather/forecast/?contains=WP%20Kuala%20Lumpur@location__location_name&sort=date&limit=1";
            String getResponse = api.get(getUrl);
            System.out.println("GET Response:\n" + getResponse);
            
//          Try to split the dictionary
//            String[] current_weather = getResponse.trim("");
//            System.out.println(Arrays.toString(current_weather));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
