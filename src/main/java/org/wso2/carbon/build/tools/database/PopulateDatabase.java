package org.wso2.carbon.build.tools.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.wso2.carbon.build.tools.main.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PopulateDatabase{
    private static final Log logger = LogFactory.getLog(PopulateDatabase.class);
    private static Connection connect = null;
    static boolean valid = false;

    public static void connect(){
        try {
            connect = DriverManager.getConnection(Constants.DATABASE_CONNECTION, Constants.MYSQL_USERNAME,
                    Constants.MYSQL_PASSWORD);
            System.out.println("Connected");
        }catch (SQLException ex) {
            logger.error("Exception occurred : " + ex.getMessage());
        }
        catch (Exception ex) {
            logger.error("Exception occurred : " + ex.getMessage());
        }
    }
    public static void insertData(String groupId, String artifactId){

        try{
            //insert into DependencyTable
            Statement stmt = connect.createStatement();
            ResultSet compareDependency = stmt.executeQuery(
                    "SELECT GroupId FROM Library_ThirdParty WHERE GroupId='" +groupId
                            +"' AND ArtifactId='"+artifactId+"'");
            if(!compareDependency.next()){
                String insertDepndencyQuery="INSERT INTO Library_ThirdParty (GroupId, ArtifactId) values(?,?)";
                PreparedStatement insertDependencySt = connect.prepareStatement(insertDepndencyQuery);
                insertDependencySt.setString(1, groupId);
                insertDependencySt.setString(2, artifactId);
                insertDependencySt.execute();
            }
            stmt.close();
        }
        catch (Exception ex)
        {
            logger.error("Exception occurred : " + ex.getMessage());
        }
    }

    public static void closeConnection(){
        try {
            connect.close();
            System.out.println("disconnected");
        }catch (SQLException ex) {
            logger.error("Exception occurred : " + ex.getMessage());
        }
        catch (Exception ex) {
            logger.error("Exception occurred : " + ex.getMessage());
        }
    }
}
