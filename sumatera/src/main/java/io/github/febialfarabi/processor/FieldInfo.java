package io.github.febialfarabi.processor;


import hindia.Nias;
import io.github.febialfarabi.model.Field;
import com.squareup.javapoet.AnnotationSpec;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts getters to field
 */
public class FieldInfo {

    private final LinkedHashMap<String, Field> fields;
    private final List<String> mandatoryFields;

    public FieldInfo(LinkedHashMap<String, Field> fields, List<String> mandatoryFields) {

        this.fields = fields;
        this.mandatoryFields = mandatoryFields;
    }

    public LinkedHashMap<String, Field> getFields() {
        return fields;
    }

    public List<String> getMandatoryFields() {
        return mandatoryFields;
    }

    public static FieldInfo get(Element element) throws Exception{
        LinkedHashMap<String, Field> fields = new LinkedHashMap<>();
        List<String> mandatoryFields = new ArrayList<>();

        for (VariableElement variableElement : ElementFilter.fieldsIn(element.getEnclosedElements())) {

            String variableName = variableElement.getSimpleName().toString();
            if(variableElement.getModifiers().contains(Modifier.FINAL) || variableElement.getModifiers().contains(Modifier.STATIC)){
                continue;
            }
            if (variableElement.getAnnotation(Nias.class) != null) {
                Nias nias = variableElement.getAnnotation(Nias.class);
                if(nias.ignore()){
                    continue;
                }
                if(!nias.alias().isEmpty()){
                    variableName = nias.alias();
                }
            }
            mandatoryFields.add(variableName);
            Field field = new Field();
            field.setType(variableElement.asType().toString());
            field.setTypeMirror(variableElement.asType());
            field.setModifiers(variableElement.getModifiers().stream().collect(Collectors.toList()));
            for (Class acceptableAnnotation : Field.ACCEPTABLE_ANNOTATIONS) {
                if(variableElement.getAnnotation(acceptableAnnotation)!=null){
                    field.getAnnotationSpecs().add(AnnotationSpec.get(variableElement.getAnnotation(acceptableAnnotation)));
                }
            }
            fields.put(variableName, field);
        }

        return new FieldInfo(fields, mandatoryFields);
    }

    private static String methodToFieldName(String methodName) {
        if (methodName.startsWith("get")) {
            String str = methodName.substring(3);
            if (str.length() == 0) {
                return null;
            } else if (str.length() == 1) {
                return str.toLowerCase();
            } else {
                return Character.toLowerCase(str.charAt(0)) + str.substring(1);
            }
        }
        return null;
    }

    private static String getTypeName(Element e) {
        TypeMirror typeMirror = e.asType();
        String[] split = typeMirror.toString().split("\\.");
        return split.length > 0 ? split[split.length - 1] : null;
    }

}
