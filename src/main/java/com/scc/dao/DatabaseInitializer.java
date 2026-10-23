package com.scc.dao;

public class DatabaseInitializer {
    public static void initialize() {
        new Userdao();
        System.out.println("-> Database tables verified.");
    }
}
