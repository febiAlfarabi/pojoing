package io.github.febialfarabi.model.person;


import hindia.Sumatera;
import io.github.febialfarabi.model.Base;
import io.github.febialfarabi.model.vehicle.CarDto;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

;

@Sumatera
@ToString(of = "address", callSuper = true)
public class Person extends Base {

    Address address ;

    List<CarDto> cars = new ArrayList<>();

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
