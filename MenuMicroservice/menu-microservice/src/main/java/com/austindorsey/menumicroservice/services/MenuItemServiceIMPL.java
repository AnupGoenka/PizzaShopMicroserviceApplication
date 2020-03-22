package com.austindorsey.menumicroservice.services;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import com.austindorsey.menumicroservice.models.CreateMenuItemRequest;
import com.austindorsey.menumicroservice.models.MenuItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:database.properties")
public class MenuItemServiceIMPL implements MenuItemService {

    
    @Value("${mysql.host}")
    private String dbHost;
    @Value("${mysql.port}")
    private String dbPort;
    @Value("${mysql.user}")
    private String dbUserName;
    @Value("${mysql.password}")
    private String dbPassword;
    @Value("${mysql.database}")
    private String dbName;
    @Value("${mysql.tableName.menuItem}")
    private String tableName;
    @Value("${mysql.tableName.menuItemHistory}")
    private String historyTableName;
    private Connection mysql;

    @Autowired private DriverManagerWrapper driverManagerWrapped;

    @Override
    public MenuItem[] getMenuItems() throws SQLException, ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
            mysql = driverManagerWrapped.getConnection(url, dbUserName, dbPassword);
            Statement statement = mysql.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " + tableName + ";");
            ArrayList<MenuItem> list = new ArrayList<>();
            while (result.next()) {
                int id = result.getInt("id");
                String catagory = result.getString("catagory");
                String itemName = result.getString("itemName");
                String discription = result.getString("discription");
                Number cost = result.getDouble("cost");
                Date revisionDate = result.getDate("revisionDate");
                MenuItem item = new MenuItem(id, catagory, itemName, discription, cost, revisionDate);
                list.add(item);
            }
            return list.toArray(new MenuItem[list.size()]);
        } finally {
            if (mysql != null) {
                mysql.close();
            }
        }
    }

    @Override
    public MenuItem getMenuItemByID(int id) throws SQLException, ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
            mysql = driverManagerWrapped.getConnection(url, dbUserName, dbPassword);
            MenuItem item = null;
            Statement statement = mysql.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " + tableName + " WHERE id='" + id + "';");
            if (result.next()) {
                String catagory = result.getString("catagory");
                String itemName = result.getString("itemName");
                String discription = result.getString("discription");
                Number cost = result.getDouble("cost");
                Date revisionDate = result.getDate("revisionDate");

                item = new MenuItem(id, catagory, itemName, discription, cost, revisionDate);
            }
            return item;
        } finally {
            mysql.close();
        }
    }

    @Override
    public MenuItem[] getMenuItemHistoryById(int origenalId) throws SQLException, ClassNotFoundException {
        ArrayList<MenuItem> history = new ArrayList<>();
        MenuItem curentItem = getMenuItemByID(origenalId);
        if (curentItem == null) {
            return null;
        } else {
            history.add(getMenuItemByID(origenalId));
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
            mysql = driverManagerWrapped.getConnection(url, dbUserName, dbPassword);
            Statement statement = mysql.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " + historyTableName + " WHERE origenalId='" + origenalId + "';");
            while (result.next()) {
                String catagory = result.getString("catagory");
                String itemName = result.getString("itemName");
                String discription = result.getString("discription");
                Number cost = result.getDouble("cost");
                Date revisionDate = result.getDate("revisionDate");
                MenuItem item = new MenuItem(origenalId, catagory, itemName, discription, cost, revisionDate);
                history.add(item);
            }
            return history.toArray(new MenuItem[history.size()]);
        } finally {
            mysql.close();
        }
    }

    @Override
    public MenuItem[] searchMenuItemsByName(String search) throws SQLException, ClassNotFoundException {
        MenuItem[] allItems = getMenuItems();
        String[] regexStrings = {"(?i)^\\b%s", "(?i)\\b %s", "(?i)%s"};
        ArrayList<ArrayList<MenuItem>> searchResaults = new ArrayList<ArrayList<MenuItem>>(regexStrings.length);
        for (int i = 0; i < regexStrings.length; i++) {
            searchResaults.add(new ArrayList<MenuItem>());
        }

        for(int i = 0; i < allItems.length; i++) {
            for (int j = 0; j < regexStrings.length; j++) {
                String regex = "(.*)" + String.format(regexStrings[j], search) + "(.*)";
                boolean matches = allItems[i].getName().matches(regex);
                if (matches) {
                    searchResaults.get(j).add(allItems[i]);
                    j = regexStrings.length;
                }
            }
        }

        ArrayList<MenuItem> finalResaults = new ArrayList<MenuItem>();
        for (int i = 0; i < searchResaults.size(); i++) {
            if (searchResaults.get(i).size() > 0) {
                finalResaults.addAll(searchResaults.get(i));
            }
        }
        return finalResaults.toArray(new MenuItem[finalResaults.size()]);
    }

    @Override
    public MenuItem createNewMenuItem(CreateMenuItemRequest item) throws SQLException, ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
            mysql = driverManagerWrapped.getConnection(url, dbUserName, dbPassword);
            Statement statement = mysql.createStatement();
            statement.executeUpdate("INSERT INTO " + tableName + " (catagory, itemName, discription, cost) VALUES ('" + 
                                    item.getCatagory() + "', '" + item.getName() + "', '" + item.getDiscription() + "', " + item.getCost().doubleValue() + ");");
            ResultSet result = statement.executeQuery("SELECT * FROM " + tableName + " WHERE catagory='" + item.getCatagory() +
                                                                                      "' AND itemName='" + item.getName() +
                                                                                      "' AND discription='" + item.getDiscription() +
                                                                                      "' AND cost="  + item.getCost().doubleValue() + ";");
            MenuItem newItem = null;
            if (result.next()) {
                int id = result.getInt("id");
                String catagory = result.getString("catagory");
                String itemName = result.getString("itemName");
                String discription = result.getString("discription");
                Number cost = result.getDouble("cost");
                Date revisionDate = result.getDate("revisionDate");

                newItem = new MenuItem(id, catagory, itemName, discription, cost, revisionDate);
            }
            return newItem;
        } finally {
            mysql.close();
        }
    }

    @Override
    public MenuItem updateMenuItem(int id, Map<String,Object> updatePairs) throws SQLException, ClassNotFoundException {
        try {
            if (updatePairs.size() > 0) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
                mysql = driverManagerWrapped.getConnection(url, dbUserName, dbPassword);
                Statement statement = mysql.createStatement();

                String sql = "UPDATE " + tableName + " SET ";
                System.out.println(updatePairs.keySet());
                for (String key : updatePairs.keySet()) {
                    sql = sql.replace(";", ", ");
                    if (updatePairs.get(key) instanceof String) {
                        sql += key + "='" + updatePairs.get(key) + "';";
                    } else {
                        sql += key + "=" + updatePairs.get(key) + ";";
                    }
                }
                statement.executeUpdate(sql);
            }
            return getMenuItemByID(id);
        } finally {
            mysql.close();
        }
    }
}