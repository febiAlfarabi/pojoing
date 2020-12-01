package io.github.febialfarabi.model.person;


import hindia.Sumatera;
import io.github.febialfarabi.model.Base;
import io.github.febialfarabi.model.vehicle.CarDto;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.*;

;

@Sumatera
@ToString(of = "address", callSuper = true)
public class Person extends Base {

    Address address ;

    Set<CarDto> cars = new HashSet<>();

    boolean primitiveBoolean;

    Boolean objectBoolean;

    Date birthdate ;
    BigDecimal balance = new BigDecimal(0);
    Integer orderCount ;

    CarDto car;
    Boolean exists ;
    Boolean coba ;


    public CarDto testGetCar() {
        return car;
    }
}
