package cs_26_sw_3_10.P3Project.platform.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import cs_26_sw_3_10.P3Project.platform.sql.data.TableData;

public class SqlHandler {
    // Get the configured datasourse
    static DataSource dataSource = SQLConfigDataSource.source();

    /**
     * Execute a sql "Insert" command. Uses children of {@link TableData} as sql
     * table templates, and data containers.
     * 
     * <pre>
     * //Example of {@link Users}
     * import {@link cs_26_sw_3_10.P3Project.platform.sql.data.Users};
     * Insert(new Users("6b895175-e9d...", "Foo", "03ac674216f3e15c7...", "secret"));
     * </pre>
     * 
     * @return Amount of rows inserted
     * @param data Accepts any class that inherits from {@link TableData}
     * 
     */
    public static <T extends TableData> int Insert(T data) throws SQLException {
        Connection connection = null;
        try {
            // Attempt for connection to MySQL
            connection = dataSource.getConnection();

            try {
                String table = data.getClass().getSimpleName();
                Map<String, Object> map = DataClassToMap(data);
                if (map.size() <= 0)
                    throw new SQLException("No data has been given!");
                Object[] values = map.values().toArray();
                Set<String> keys = map.keySet();

                String fillInBlanks = "?,".repeat(keys.size());
                fillInBlanks = fillInBlanks.substring(0, fillInBlanks.length() - 1);
                String cmd = String.format("insert into \"%s\" (%s) values (%s)", table, String.join(",", keys),
                        fillInBlanks);
                PreparedStatement prepStatement = connection.prepareStatement(cmd);
                for (int i = 0; i < values.length; i++) {
                    prepStatement.setObject(i + 1, values[i]);
                }
                return prepStatement.executeUpdate();
            } catch (IllegalAccessException e) {
                System.err.println(e);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
                System.out.println("disconnected");
            }
        }
        return 0;
    }

    public static <T extends TableData> ArrayList<T> Select(Class<T> type, String condition) throws SQLException {
        Connection connection = null;
        try {
            String table = type.getSimpleName();
            // Attempt for connection to MySQL
            connection = dataSource.getConnection();

            String cmd = String.format("SELECT * FROM \"%s\"", table);
            if (condition.length() > 0)
                cmd += "WHERE " + condition;
            System.out.print(cmd + "\n");
            PreparedStatement prepStatement = connection.prepareStatement(cmd);
            ResultSet resultSet = prepStatement.executeQuery();
            return ResultSetToArrayList(resultSet, type);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
                System.out.println("disconnected");
            }
        }
        // return results;
    }

    public static <T extends TableData> int Update(T data, String condition) throws SQLException {
        Connection connection = null;
        if (condition.length() <= 0)
            throw new SQLException("condition cannot be null!");
        try {
            // Attempt for connection to MySQL
            connection = dataSource.getConnection();

            String table = data.getClass().getSimpleName();
            try {
                var map = DataClassToMap(data);
                if (map.size() <= 0)
                    throw new SQLException("No data has been given!");
                Object[] values = map.values().toArray();
                Set<String> keys = map.keySet();
                String cmd = String.format("update \"%s\" set %s = ? where %s", table, String.join("= ?, ", keys),
                        condition);
                PreparedStatement prepStatement = connection.prepareStatement(cmd);
                for (int i = 0; i < values.length; i++) {
                    prepStatement.setObject(i + 1, values[i]);
                }
                return prepStatement.executeUpdate();
            } catch (IllegalAccessException e) {
                System.err.println(e);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
                System.out.println("disconnected");
            }
        }
        return 0;
    }

    public static <T extends TableData> int Delete(Class<T> type, String condition) throws SQLException {
        Connection connection = null;

        if (condition.length() <= 0)
            throw new SQLException("condition cannot be null!");
        try {
            // Attempt for connection to MySQL
            connection = dataSource.getConnection();

            // Class<T> dataClass = (Class<T>) data.getClass();
            String table = type.getSimpleName();
            String cmd = String.format("delete from \"%s\" where %s", table, condition);
            System.out.print(cmd + "\n");
            PreparedStatement prepStatement = connection.prepareStatement(cmd);
            System.out.println(prepStatement.toString());
            return prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
                System.out.println("disconnected");
            }
        }
    }

    static <T extends TableData> Map<String, Object> DataClassToMap(T data) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<String, Object>();
        Class<T> dataClass = (Class<T>) data.getClass();
        Field[] fields = dataClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            Object value = fields[i].get(data);
            if (value == null || name == null)
                continue;

            System.out.println(name);
            System.out.println(value);
            map.put(name, value);
        }
        return map;
    }

    static <T extends TableData> ArrayList<T> ResultSetToArrayList(ResultSet resultSet, Class<T> type)
            throws SQLException {
        ArrayList<T> resultList = new ArrayList<T>();
        while (resultSet.next()) {
            try {
                T resultContainer = type.getDeclaredConstructor().newInstance();
                Field[] fields = type.getFields();
                for (int i = 0; i < fields.length; i++) {
                    String name = fields[i].getName();
                    Object value = resultSet.getObject(name);
                    fields[i].set(resultContainer, value);
                }
                resultList.add(resultContainer);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        return resultList;
    }

}