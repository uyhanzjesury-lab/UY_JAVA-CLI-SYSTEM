package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Userdao {

    public boolean registerUser(String username, String password, String identity) {

        String sql = "INSERT INTO users(username, password , identity,status) VALUES(?, ? , ? ,'PERNDING')";
        try(Connection conn =  ClinicdatabaseConnection.getConnection();
            PreparedStatement pstm = conn.prepareStatement(sql)){

pstm.setString(1 ,username);
pstm.setString(2 ,password);
pstm.setString(3 ,identity .toUpperCase());
pstm.executeUpdate();
System.out.println("User registered successfully in the database!");

return true;

        }catch (SQLException e){

          System.err.println("FAILED:" + e.getMessage());
          return false;
        }



}

}
