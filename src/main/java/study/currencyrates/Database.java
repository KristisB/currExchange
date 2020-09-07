package study.currencyrates;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private BasicDataSource dataSource;

    //constructor
    public Database(String databaseName) {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
//        dataSource.setUsername("root");
//        dataSource.setPassword("");
//        dataSource.setUrl("jdbc:mysql://localhost:3306/" + databaseName + "?useUnicode=yes&characterEncoding=UTF-8");
        dataSource.setUsername("ba02ebd004a098");
        dataSource.setPassword("14428edb");
        dataSource.setUrl("jdbc:mysql://eu-cdbr-west-03.cleardb.net:3306/" + databaseName + "?useUnicode=yes&characterEncoding=UTF-8");

        dataSource.setValidationQuery("SELECT 1");
    }

    public double getRate(String curr, String fxDate) {
        String query = "SELECT rate FROM ex_rates WHERE curr_code=? and date=? ; ";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, curr);
            statement.setString(2, fxDate);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("rate");
            } else {
                return 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public void addLog(String log) {
        String query = "INSERT INTO log (log_time, info) VALUES (?,?);";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            long timeStamp = System.currentTimeMillis();
            statement.setLong(1, timeStamp);
            statement.setString(2, log);
            statement.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadFxRates(String date) {
//        String query = "INSERT INTO ex_rates (curr_code, rate, date) VALUES ('EUR', 1, '2020-09-06'); ";
        try (Connection connection = dataSource.getConnection();) {
            PreparedStatement statement = null;
            HashMap<String,Double> fxRates=new HashMap<>();
            LbConnect lbConnect = new LbConnect();
            fxRates= lbConnect.getFxRates(date);

            System.out.println(fxRates.toString());

            //constructing query for inserting multiple records into db
            StringBuffer query = new StringBuffer("INSERT INTO ex_rates (curr_code, rate, date) values (?, ?, ?)");
            for (int i = 0; i < fxRates.size() - 1; i++) {
                query.append(", (?, ?, ?)");
            }
            statement=connection.prepareStatement(query.toString());

            int indexNo=1;
            for (Map.Entry<String,Double> entry:fxRates.entrySet()){
                statement.setString(indexNo,entry.getKey());
                statement.setString(indexNo+1,entry.getValue().toString());
                statement.setString(indexNo+2,date);
                indexNo=indexNo+3;
            }
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean areRatesOnDate(String date){
        String query = "SELECT * FROM ex_rates WHERE date=?; ";
        boolean result= false;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, date);
            ResultSet resultSet = statement.executeQuery();
            result= resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
