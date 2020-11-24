package com.alfarabi.example.model;

import com.alfarabi.example.model.BaseDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.lang.Boolean;
import java.lang.String;
import java.util.Date;

public class PersonDto extends BaseDto {
  @JsonIgnore
  Date created;

  @JsonIgnore
  @JsonIgnoreProperties({
      "person",
      "person1",
      "person2"
  })
  String name;

  int count;

  Boolean verified;

  boolean locked;

  public PersonDto() {
  }

  public PersonDto(String name, Date created, Boolean verified, boolean locked, int count) {
    this.name = name;
    this.created = created;
    this.verified = verified;
    this.locked = locked;
    this.count = count;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getCount() {
    return this.count;
  }

  public void setVerified(Boolean verified) {
    this.verified = verified;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getCreated() {
    return this.created;
  }

  public Boolean getVerified() {
    return this.verified;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getName() {
    return this.name;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public boolean isLocked() {
    return this.locked;
  }
}
