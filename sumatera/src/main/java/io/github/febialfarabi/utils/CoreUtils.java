package io.github.febialfarabi.utils;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import io.github.febialfarabi.model.Field;
import io.github.febialfarabi.processor.FieldInfo;
import io.github.febialfarabi.processor.JavaBuilderProcessor;
import io.toolisticon.annotationprocessortoolkit.tools.TypeUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.*;

public class CoreUtils {

    public static final String CLASS_SUFFIX = "Dto";
    public static Set<Element> annotatedElements = new HashSet<>();


    /**
     * Checking if the class to be generated is a valid java identifier
     * Also the name should be not same as the target interface
     */
//    public static boolean checkIdValidity(String name, Element e) {
//        for (int i = 0; i < name.length(); i++) {
//            if (i == 0 ? !Character.isJavaIdentifierStart(name.charAt(i)) : !Character.isJavaIdentifierPart(name.charAt(i))) {
//               new Exception("AutoImplement#as should be valid java identifier for code generation: " + name).printStackTrace();
//               return false ;
//            }
//        }
//        if (name.equals(CoreUtils.getTypeName(e))) {
//            new Exception("AutoImplement#as should be different than the Interface name ").printStackTrace();
//            return false ;
//        }
//        return true;
//    }

    public static String getPackageName(Element element) {
        List<PackageElement> packageElements = ElementFilter.packagesIn(Arrays.asList(element.getEnclosingElement()));
        Optional<PackageElement> packageElement = packageElements.stream().findAny();
        return packageElement.isPresent() ? packageElement.get().getQualifiedName().toString() : null;
    }

    public static String getTypeName(Element e) {
        TypeMirror typeMirror = e.asType();
        String[] split = typeMirror.toString().split("\\.");
        return split.length > 0 ? split[split.length - 1] : null;
    }

    public static String getFullname(Element e) {
        TypeMirror typeMirror = e.asType();
        String name = typeMirror.toString();
        if(name.indexOf("<")>0){
            name = name.substring(0, name.indexOf("<"));
        }
        return name;
    }

    public static String getNormalGeneric(Element e) {
        String name = getGenericTypeName(e);
        String[] split = name.split("\\.");
        name =  split.length > 0 ? split[split.length - 1] : null;
        return "<"+name+">";
    }

    public static String getPackageGeneric(Element e) {
        return "<"+getGenericTypeName(e)+">";
    }

    public static String getGenericTypeName(Element e) {
        TypeMirror typeMirror = e.asType();
        String name = typeMirror.toString();
        if(name.indexOf("<")>0){
            name = name.substring(name.indexOf("<")+1, name.indexOf(">"));
        }
        return name;
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


    public static boolean isPrimitive(Element e) {
        return getFullname(e).indexOf(".")<0;
    }

    public static boolean isObjectOfClass(Element e) {
        return getFullname(e).indexOf(".")>0;
    }

    public static void error(ProcessingEnvironment processingEnv, String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    public static void info(ProcessingEnvironment processingEnv, String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, e);
    }

    public static Modifier translate(int mode){
        if(java.lang.reflect.Modifier.isAbstract(mode)){
            return Modifier.ABSTRACT;
        }
        if(java.lang.reflect.Modifier.isPublic(mode)){
            return Modifier.PUBLIC;
        }
        if(java.lang.reflect.Modifier.isStatic(mode)){
            return Modifier.STATIC;
        }
        if(java.lang.reflect.Modifier.isFinal(mode)){
            return Modifier.FINAL;
        }
        if(java.lang.reflect.Modifier.isNative(mode)){
            return Modifier.NATIVE;
        }
        if(java.lang.reflect.Modifier.isPrivate(mode)){
            return Modifier.PRIVATE;
        }
        if(java.lang.reflect.Modifier.isProtected(mode)){
            return Modifier.PROTECTED;
        }
        if(java.lang.reflect.Modifier.isStrict(mode)){
            return Modifier.STRICTFP;
        }
        if(java.lang.reflect.Modifier.isSynchronized(mode)){
            return Modifier.SYNCHRONIZED;
        }
        if(java.lang.reflect.Modifier.isTransient(mode)){
            return Modifier.TRANSIENT;
        }
        if(java.lang.reflect.Modifier.isVolatile(mode)){
            return Modifier.VOLATILE;
        }
        return Modifier.DEFAULT;
    }

    public static MethodSpec constructorSpec(List<String> constructorImports, ProcessingEnvironment processingEnv, TypeElement element) throws Exception{
        FieldInfo fieldInfo = FieldInfo.get(processingEnv, element);
        Set<FieldSpec> fieldSpecSet = new HashSet<>();
        fieldInfo.getFields().forEach(
                (s, field) -> {
                    FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(field.getTypeName(), s, field.getArrayModifiers());
                    for (AnnotationSpec annotationSpec : field.getAnnotationSpecs()) {
                        fieldSpecBuilder.addAnnotation(annotationSpec);
                    }
                    fieldSpecSet.add(fieldSpecBuilder.build());
                }
        );
        MethodSpec constructorSpec = null;
        final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        boolean createConstructor = false ;
        TypeMirror inheritanceMirror = element.getSuperclass();
        if (inheritanceMirror!=null && !inheritanceMirror.toString().equals(Object.class.getName())){
            StringBuilder statementBuilder = new StringBuilder();
            statementBuilder.append("super").append("(");
            statementBuilder = paramBuilder(constructorImports, processingEnv, constructorBuilder, statementBuilder, element);
            statementBuilder.append(")");
            String statement = statementBuilder.toString();
            statement = statement.replace("(,", "(");
            statement = statement.replace(",)", ")");
            constructorBuilder.addStatement(statement);
            createConstructor = true ;
        }

        if(fieldSpecSet.size()>0){
            for (Map.Entry<String, Field> stringFieldEntry : fieldInfo.getFields().entrySet()) {
                constructorBuilder.addParameter(stringFieldEntry.getValue().getTypeName(), stringFieldEntry.getKey())
                        .addStatement("this.$N = $N", stringFieldEntry.getKey(), stringFieldEntry.getKey());
            }
            createConstructor = true ;
        }
        if(createConstructor){
            constructorSpec = constructorBuilder.build();
        }
        return constructorSpec;

    }

    private static StringBuilder paramBuilder(List<String> constructorImports, ProcessingEnvironment processingEnv,
                                              MethodSpec.Builder constructorSpecBuilder, StringBuilder stringBuilder, TypeElement element) throws Exception{
        TypeMirror inheritanceMirror = element.getSuperclass();
        if (inheritanceMirror!=null && !inheritanceMirror.toString().equals(Object.class.getName())){
            TypeElement inheritanceElement = TypeUtils.TypeRetrieval.getTypeElement(inheritanceMirror);
            stringBuilder = paramBuilder(constructorImports, processingEnv, constructorSpecBuilder, stringBuilder, inheritanceElement);
            FieldInfo inheritanceFieldInfo = FieldInfo.get(processingEnv, inheritanceElement);
            for (Map.Entry<String, Field> stringFieldEntry : inheritanceFieldInfo.getFields().entrySet()) {
                constructorSpecBuilder.addParameter(stringFieldEntry.getValue().getTypeName(), stringFieldEntry.getKey());
                stringBuilder.append(stringFieldEntry.getKey());
                stringBuilder.append(",");
                if(JavaBuilderProcessor.wouldBeGeneratedClassNameMap.containsKey(stringFieldEntry.getValue().getTypeName().toString())){
                    constructorImports.add(JavaBuilderProcessor.wouldBeGeneratedClassNameMap.get(stringFieldEntry.getValue().getTypeName().toString()));
                }
            }
//            info(processingEnv, "inheritance mirror ###### "+inheritanceMirror.toString(), element);
        }
        return stringBuilder;

    }




}
