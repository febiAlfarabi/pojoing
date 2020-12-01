package io.github.febialfarabi.processor;


import com.squareup.javapoet.*;
import hindia.Nias;
import hindia.Sumatera;
import io.github.febialfarabi.model.Field;
import io.github.febialfarabi.utils.CoreUtils;
import io.toolisticon.annotationprocessortoolkit.tools.TypeUtils;
import io.toolisticon.annotationprocessortoolkit.tools.generics.GenericType;
import io.toolisticon.annotationprocessortoolkit.tools.generics.GenericTypeKind;
import io.toolisticon.annotationprocessortoolkit.tools.generics.GenericTypeParameter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.*;
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

    public static FieldInfo get(ProcessingEnvironment processingEnvironment, TypeElement element) throws Exception{
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
            }
            mandatoryFields.add(variableName);
            Field field = new Field();
            Set<Element> elements = CoreUtils.annotatedElements;
            boolean isSumatera = false ;
            for (Element classElement : elements) {
                if(classElement.asType().toString().equalsIgnoreCase(variableElement.asType().toString())){
                    isSumatera = true ;
                    break;
                }
            }
            if(isSumatera){
                String className = variableElement.asType().toString()+CoreUtils.CLASS_SUFFIX;
                field.setImportPackage(className);
                field.setTypeName(ClassName.bestGuess(className));
            }else{
                try{
                    if(CoreUtils.isObjectOfClass(variableElement) && Collection.class.isAssignableFrom(Class.forName(CoreUtils.getFullname(variableElement)))){
                        field.setTypeName(TypeName.get(variableElement.asType()));
                        isSumatera = false ;
                        for (Element classElement : elements) {
                            if(classElement.asType().toString().equalsIgnoreCase(CoreUtils.getGenericTypeName(variableElement))){
                                isSumatera = true ;
                                break;
                            }
                        }
                        if(isSumatera){
                            field.setReplaceImport(CoreUtils.getGenericTypeName(variableElement));
                            field.setReplaceGeneric(CoreUtils.getNormalGeneric(variableElement));
                        }
                        if(Set.class.isAssignableFrom(Class.forName(CoreUtils.getFullname(variableElement)))){
                            field.setDefaultValue("HashSet<>");
                            field.setImportPackage(HashSet.class.getName());
                        }
                        if(List.class.isAssignableFrom(Class.forName(CoreUtils.getFullname(variableElement)))){
                            field.setDefaultValue("ArrayList<>");
                            field.setImportPackage(ArrayList.class.getName());
                        }
                        if(Map.class.isAssignableFrom(Class.forName(CoreUtils.getFullname(variableElement)))){
                            field.setDefaultValue("HashMap<>");
                            field.setImportPackage(HashMap.class.getName());
                        }

                    }else{
                        field.setTypeName(TypeName.get(variableElement.asType()));
                    }
                }catch (Exception e){
                    field.setTypeName(TypeName.get(variableElement.asType()));
                }
            }

            field.setModifiers(variableElement.getModifiers().stream().collect(Collectors.toList()));
            for (String acceptableAnnotation : Field.ACCEPTABLE_ANNOTATIONS) {
                for (AnnotationMirror annotationMirror : variableElement.getAnnotationMirrors()) {
                    if(annotationMirror.getAnnotationType().toString().contains(acceptableAnnotation)){
                        field.getAnnotationSpecs().add(AnnotationSpec.get(annotationMirror));
                    }
                }
            }
            fields.put(variableName, field);
        }

        return new FieldInfo(fields, mandatoryFields);
    }

}
