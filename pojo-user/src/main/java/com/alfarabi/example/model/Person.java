package com.alfarabi.example.model;


import com.alfarabi.duplicator.annotation.Dto;
import com.alfarabi.duplicator.annotation.DtoField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@Dto
public class Person extends Base {

    @JsonIgnoreProperties({"person","person1","person2"})
    @JsonIgnore
    String name ;

    @DtoField(ignore = true)
    String address ;

    @JsonIgnore
    Date created ;

    Boolean verified ;

    boolean locked ;

    int count ;

}
