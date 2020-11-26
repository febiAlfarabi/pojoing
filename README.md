
# @Sumatera

Model converter from **ORM class to regular pojo class**, the ORM term is a modelling class that doesn't like regular pojo, such like @Entity from JPA/Hibernate and so on.

It can be used for mapping regular object, post data in request body where you cannot put ORM class directly in a parameter, or you cannot convert to json directly, or any other problem regarding it.

You don't need anymore to create duplicate Dto / Pojo class which represent ORM class, just write your ORM class and annotate it with **@Sumatera**, then the regular pojo class would be generated for you.

Example

	@Entity
	@Table
	@Sumatera
    public class Person{
    
	    String name ;
	
	    String address ;
	
	    @Nias(ignore = true)
	    String secured;
    }

The above code will automatically generate a duplicate java and also compiled class called **PersonDto** where you can find in the **generated-source** folder inside your maven project.

    public class PersonDto{
	
	    String name ;
	
	    String address ;
	
	    public String getName() {  
		    return this.name;  
	    }
	    public void setName(String name) {  
		    this.name = name;  
	    }
	    public String getAddress() {  
		    return this.address;  
	    }
	    public void setAddress(String address) {  
		    this.address = address;  
	    }
    }

By Default a class uses @Sumatera for automatically generate the duplicate class pojo and its assume that all of class field are should be generated. 

In special situation, where you need to ignore some field you can use @Nias(ignore=true) on the field.

    @Nias(ignore = true)
    String secured ;

There is also **2 static return methods** generated in your pojo class

    PersonORM personOrm = PersonDto.to(personDto);
    PersonDto personDto = PersonDto.from(personOrm);
    
The above methods would be automatically convert your type of object between any ORM from-to Dto/Pojo

> Happy Coding..!!

Thanks for

 - [x] Apache
 - [x] javapoet
 - [x] toolisticon
 - [x] modelmapper