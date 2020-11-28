package io.github.febialfarabi.model;

import io.github.febialfarabi.model.person.Person;
import io.github.febialfarabi.model.person.PersonDto;

public class Main {

    public static void main(String[] args){
        Person person = PersonDto.to(new PersonDto());

    }

}
