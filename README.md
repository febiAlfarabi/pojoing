# pojoing

Model converter from ORM class to regular pojo class, ORM means a pojo that doesn't like regular pojo, such like @Entity from JPA/Hibernate and so on.

It can be used for mapping, post data in request body where the project uses spring java for building application

You don't need anymore to create duplicate Dto / Pojo class which represent ORM class, just write your ORM class and annotate it with @Sumatera, then the regular pojo class would be generated for you. 
