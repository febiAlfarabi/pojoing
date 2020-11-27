package io.github.febialfarabi.utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
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


}
