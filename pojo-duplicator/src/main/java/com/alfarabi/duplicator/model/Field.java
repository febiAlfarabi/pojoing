package com.alfarabi.duplicator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.alfarabi.duplicator.annotation.Dto;
import com.alfarabi.duplicator.annotation.DtoField;
import com.squareup.javapoet.AnnotationSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class Field {

    public static Class[] ACCEPTABLE_ANNOTATIONS = {JsonIgnore.class, JsonIgnoreProperties.class, JsonSerialize.class, JsonDeserialize.class};
    public Class[] FILTERED_ANNOTATIONS = {Dto.class, DtoField.class};


    String type ;
    TypeMirror typeMirror ;
    List<Modifier> modifiers = new ArrayList<>();
    List<AnnotationSpec> annotationSpecs = new ArrayList<>() ;


    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public void setTypeMirror(TypeMirror typeMirror) {
        this.typeMirror = typeMirror;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public void setAnnotationSpecs(List<AnnotationSpec> annotationSpecs) {
        this.annotationSpecs = annotationSpecs;
    }

    public List<AnnotationSpec> getAnnotationSpecs() {
        return annotationSpecs;
    }

    public Modifier[] getArrayModifiers(){
        Modifier[] modifiers = new Modifier[this.modifiers.size()];
        int i = 0 ;
        for (Modifier modifier : this.modifiers) {
            modifiers[i] = modifier;
            i++;
        }
        return modifiers;

    }
}
