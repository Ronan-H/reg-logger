package ronan_tommey.reg_logger.reg_logging;

import java.sql.*;
import javax.sql.*;

/**
 * Logs all of the data on cars that have passed into a database
 */
public class CarPassDatabase implements CarPassLogger {

    /**
     * @param carPassDetails Object with details of car that passed by
     */
    @Override
    public void logPass(CarPassDetails carPassDetails) {

        //Ensures connection to database or relays error message
        try{
            Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost/reg_logger","tommey","ronan");
            Statement myStmt = conn.createStatement();
            //Inserts data into database
            String query = "Insert into cars_data (reg_plate,date_and_time,direction,kmph_speed,pixel_speed) values('"+
                    carPassDetails.getRegText()+"',FROM_UNIXTIME("+
                    carPassDetails.getTimestamp()/1000+"),'"+
                    carPassDetails.getDirection()+"',"+
                    carPassDetails.getKmphSpeed()+","+
                    carPassDetails.getPixelSpeed()+");";

             myStmt.executeQuery(query);
        }
        catch( SQLException se){
            System.out.println(se.getMessage());
        }
    }
}