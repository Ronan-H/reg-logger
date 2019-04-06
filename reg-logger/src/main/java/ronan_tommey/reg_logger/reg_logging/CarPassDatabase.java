package ronan_tommey.reg_logger.reg_logging;

import java.sql.*;
import javax.sql.*;

public class CarPassDatabase implements CarPassLogger {
    @Override
    public void logPass(CarPassDetails carPassDetails) {
        // TODO this method

        try{
            //MysqlDataSource mysqlDS = new MysqlDataSource();

            //mysqlDS.setURL();
           // mysqlDS.setUser("root");
            //mysqlDS.setPassword("");

            Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost/reg_logger","tommey","ronan");
            Statement myStmt = conn.createStatement();
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