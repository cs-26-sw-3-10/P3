package cs_26_sw_3_10.P3Project.platform.sql;

import java.security.KeyStore.Entry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

public class SqlHandler {

    public static int insert(String table, Map<String, String> insertData) throws Exception {
        DataSource dataSource = null;
        Connection connection = null;
        PreparedStatement prepStatement = null;

        int result = 0;
        try {

            // Get the configured datasourse
            dataSource = SQLConfigDataSource.source();
            // Attempt for connection to MySQL
            connection = dataSource.getConnection();
            prepStatement = connection.prepareStatement("insert into user (user,username,password) values (?,?,?)");
            Set<String> keys = insertData.keySet();
            Collection<String> values = insertData.values();
            String cmd = String.format("insert into %s (%s) values (%s)", table, String.join(",", keys),
                    String.join(",", values));
            System.out.print(cmd);

            // result = prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw new Exception("SQL INSERT FAILED!");
        }
        return result;
    }
}