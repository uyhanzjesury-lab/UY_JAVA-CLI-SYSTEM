package com.scc.model;

public record user(int id, String username, String password , String identity, String status ) {

    @Override
    public String toString(){

        return String.format("(%d) %s (%s)- status: %s",id, username ,identity , status );


    }


}
