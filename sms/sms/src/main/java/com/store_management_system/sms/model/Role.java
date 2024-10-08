package com.store_management_system.sms.model;

public class Role {
    private Long id;
    private String name;
    
    public boolean isEmpty(){
        return (id==null && (name==null || name.isEmpty()) );
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
