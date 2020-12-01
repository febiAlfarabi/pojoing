package io.github.febialfarabi.model;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import hindia.Nias;
import hindia.Sumatera;
import io.github.febialfarabi.utils.CoreUtils;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Field {

    public static final String[] ACCEPTABLE_ANNOTATIONS = {"JsonIgnore",
            "JsonIgnoreProperties",
            "JsonProperty",
            "JsonSerialize",
            "JsonDeserialize",
            "SerializedName"};
    public static final Class[] FILTERED_ANNOTATIONS = {Sumatera.class, Nias.class};

    String importPackage ;
    String replaceImport ;
    String replaceGeneric ;

    TypeName typeName ;
    List<Modifier> modifiers = new ArrayList<>();
    Set<AnnotationSpec> annotationSpecs = new HashSet<>() ;

    Object defaultValue ;

    public TypeName getTypeName() {
        return typeName;
    }

    public void setTypeName(TypeName typeName) {
        this.typeName = typeName;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public void setAnnotationSpecs(Set<AnnotationSpec> annotationSpecs) {
        this.annotationSpecs = annotationSpecs;
    }

    public Set<AnnotationSpec> getAnnotationSpecs() {
        return annotationSpecs;
    }

    public void setImportPackage(String importPackage) {
        this.importPackage = importPackage;
    }

    public String getImportPackage() {
        return importPackage;
    }

    public void setReplaceImport(String replaceImport) {
        this.replaceImport = replaceImport;
    }

    public String getReplaceImport() {
        return replaceImport;
    }

    public void setReplaceGeneric(String replaceGeneric) {
        this.replaceGeneric = replaceGeneric;
    }

    public String getReplaceGeneric() {
        return replaceGeneric;
    }

    public boolean isNeedAdditionalImport(){
        return importPackage!=null && !importPackage.isEmpty();
    }
    public boolean isNeedReplaceImport(){
        return replaceImport!=null && !replaceImport.isEmpty();
    }

    public String getImportReplacer(){
        if(replaceImport==null || replaceImport.isEmpty()){
            return null;
        }
        return replaceImport+ CoreUtils.CLASS_SUFFIX;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isNeedReplaceGeneric(){
        return replaceGeneric!=null && !replaceGeneric.isEmpty();
    }
    public String getGenericReplacer(){
        if(replaceGeneric==null || replaceGeneric.isEmpty()){
            return null;
        }
        return "<"+replaceGeneric.substring(replaceGeneric.indexOf("<")+1, replaceGeneric.indexOf(">"))+CoreUtils.CLASS_SUFFIX+">";
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
