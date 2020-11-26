package io.github.febialfarabi.model;


import hindia.Sumatera;
import lombok.ToString;

;import java.util.ArrayList;
import java.util.List;

@Sumatera
@ToString(of = "address", callSuper = true)
public class Person {

    Address address ;

    List<Car> cars = new ArrayList<>();

    boolean primitiveBoolean;

    Boolean objectBoolean;



}
