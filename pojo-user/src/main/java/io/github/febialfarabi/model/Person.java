package io.github.febialfarabi.model;


import hindia.Sumatera;
import hindia.Nias;
;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Sumatera
@ToString(of = "id", callSuper = true)
@EqualsAndHashCode(callSuper = true, of = "id")
public class Person extends Base {

    @JsonIgnoreProperties({"person","person1","person2"})
    @JsonIgnore
    String name ;

    @Nias(ignore = true)
    String address ;

    @JsonIgnore
    Date created ;

    Boolean verified ;

    boolean locked ;

    int count ;

}
