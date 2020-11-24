package com.alfarabi.example.model;


import java.lang.Long;

public class BaseDto {
  private Long id;

  public BaseDto() {
  }

  public BaseDto(Long id) {
    this.id = id;
  }

  public Long getId() {
    return this.id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
