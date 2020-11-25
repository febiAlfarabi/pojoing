package io.github.febialfarabi.processor;

import com.squareup.javapoet.*;
import hindia.Sumatera;
import io.toolisticon.annotationprocessortoolkit.ToolingProvider;
import io.toolisticon.annotationprocessortoolkit.tools.TypeUtils;
import org.modelmapper.ModelMapper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes({"hindia.*"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JavaBuilderProcessor extends AbstractProcessor {

    public static final String CLASS_SUFFIX = "Dto";

    public JavaBuilderProcessor(){
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        ToolingProvider.setTooling(processingEnv);
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Sumatera.class);

        List<String> uniqueIdCheckList = new ArrayList<>();

        for (Element element : elements) {
            Sumatera sumatera = element.getAnnotation(Sumatera.class);
            info("Class yang dianotasi ### "+element.getSimpleName(), element);
            boolean error = false;
            String className = element.getSimpleName()+CLASS_SUFFIX;
            if(!sumatera.alias().isEmpty()){
                className = sumatera.alias();
            }

            if (uniqueIdCheckList.contains(className)) {
                error("Nama Dto "+className+" sudah digunakan, ganti dengan nama alias untuk membedakan class yang akan digenerate", element);
                error = true;
            }

            if (!error) {
                uniqueIdCheckList.add(element.getSimpleName().toString());
                try {
                    generateJavaClass(className, element);
                } catch (Exception e) {
                    error(e.getMessage(), null);
                }
            }
        }
        return false;
    }

    public void generateJavaClass(String className, Element element) throws Exception{
        Modifier[] modifiers = new Modifier[element.getModifiers().size()];
        int i = 0 ;
        for (Modifier modifier : element.getModifiers()) {
            modifiers[i] = modifier;
            i++;
        }
        FieldInfo fieldInfo = FieldInfo.get(element);
        Set<FieldSpec> fieldSpecSet = new HashSet<>();
        fieldInfo.getFields().forEach(
                (s, field) -> {
                    FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(field.getTypeMirror()), s, field.getArrayModifiers());
                    field.getAnnotationSpecs().forEach(annotationSpec -> {
                        fieldSpecBuilder.addAnnotation(annotationSpec);
                    });
                    fieldSpecSet.add(fieldSpecBuilder.build());
                }
        );

        Set<MethodSpec> methodSpecSet = new HashSet<>();
        fieldInfo.getFields().forEach(
                (s, field) -> {
                    methodSpecSet.add(MethodSpec.methodBuilder("set" + Character.toUpperCase(s.charAt(0)) + s.substring(1))
                            .addParameter(TypeName.get(field.getTypeMirror()), s).addModifiers(Modifier.PUBLIC).addStatement("this.$N = $N", s, s).build());
                    if(TypeName.get(field.getTypeMirror()).equals(TypeName.BOOLEAN)){
                        methodSpecSet.add(MethodSpec.methodBuilder("is" + Character.toUpperCase(s.charAt(0)) + s.substring(1))
                                .returns(TypeName.get(field.getTypeMirror())).addModifiers(Modifier.PUBLIC).addStatement("return this.$N", s).build());
                    }else{
                        methodSpecSet.add(MethodSpec.methodBuilder("get" + Character.toUpperCase(s.charAt(0)) + s.substring(1))
                                .returns(TypeName.get(field.getTypeMirror())).addModifiers(Modifier.PUBLIC).addStatement("return this.$N", s).build());
                    }
                }
        );

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        fieldInfo.getFields().forEach((s, field) -> {
            constructorBuilder.addParameter(TypeName.get(field.getTypeMirror()), s).addStatement("this.$N = $N", s, s);
        });
        MethodSpec methodSpec = constructorBuilder.build();

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className) ;

        TypeElement typeElement = TypeUtils.TypeRetrieval.getTypeElement(element.asType().toString());
        TypeMirror inheritanceMirror = typeElement.getSuperclass();
        info("INHERITANCE MIRROR ### "+inheritanceMirror.toString(), element);
        if(inheritanceMirror!=null && !inheritanceMirror.toString().equals("java.lang.Object")){
            TypeElement inheritanceElement = TypeUtils.TypeRetrieval.getTypeElement(inheritanceMirror);
            Boolean annotated = inheritanceElement.getAnnotation(Sumatera.class)!=null;
            if(!annotated){
                error("Class yang diinheritance / diextends harus di anotasi dengan @Dto", element);
                return;
            }
            Sumatera sumatera = inheritanceElement.getAnnotation(Sumatera.class);
            String inheritanceClassName = inheritanceElement.getSimpleName()+CLASS_SUFFIX;
            if(!sumatera.alias().isEmpty()){
                className = sumatera.alias();
            }

            typeSpecBuilder.superclass(ClassName.bestGuess(inheritanceClassName));
        }
        typeSpecBuilder.addSuperinterface(Serializable.class);
        for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            if(annotationMirror.getAnnotationType().toString().contains("ToString")){
                typeSpecBuilder.addAnnotation(AnnotationSpec.get(annotationMirror));
            }
            if(annotationMirror.getAnnotationType().toString().contains("EqualsAndHashCode")){
                typeSpecBuilder.addAnnotation(AnnotationSpec.get(annotationMirror));
            }
        }

        typeSpecBuilder = typeSpecBuilder.addModifiers(modifiers)
                .addField(FieldSpec.builder(ModelMapper.class, "mapper", Modifier.STATIC).build())
                .addStaticBlock(CodeBlock.of("mapper = new $N();", ModelMapper.class.getSimpleName()))
                .addFields(fieldSpecSet)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                .addMethod(methodSpec)
                .addMethods(methodSpecSet)
                .addMethod(
                        MethodSpec.methodBuilder("from").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(TypeName.get(element.asType()), getTypeName(element))
                                .returns(ClassName.get(getPackageName(element), className))
                                .addStatement("return mapper.map($N, $N)", getTypeName(element), className+".class").build())
                .addMethod(
                        MethodSpec.methodBuilder("to").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(ClassName.get(getPackageName(element), className), getTypeName(element).toLowerCase())
                                .returns(TypeName.get(element.asType()))
                                .addStatement("return mapper.map($N, $N)", getTypeName(element).toLowerCase(), element.getSimpleName()+".class").build());

        TypeSpec typeSpec = typeSpecBuilder.build();

        String pkg = getPackageName(element);
        JavaFile.Builder javaFileBuilder = JavaFile.builder(pkg , typeSpec);
        JavaFile javaFile = javaFileBuilder.build();

        String content = javaFile.toString();
        String[] contentSplit = content.split(System.lineSeparator(), 2);
        StringBuilder stringBuilder= new StringBuilder();
        stringBuilder.append(contentSplit[0]).append(System.lineSeparator()).append(System.lineSeparator());
        if(inheritanceMirror!=null && !inheritanceMirror.toString().equals("java.lang.Object")){
            TypeElement inheritanceElement = TypeUtils.TypeRetrieval.getTypeElement(inheritanceMirror);
            stringBuilder.append("import").append(" ").append(getPackageName(inheritanceElement)+"."+getTypeName(inheritanceElement)+"Dto").append(";");
        }

        stringBuilder.append(contentSplit[1]);

        generateClass(pkg + "." + className, stringBuilder.toString());

    }

    private String getPackageName(Element element) {
        List<PackageElement> packageElements = ElementFilter.packagesIn(Arrays.asList(element.getEnclosingElement()));

        Optional<PackageElement> packageElement = packageElements.stream().findAny();
        return packageElement.isPresent() ? packageElement.get().getQualifiedName().toString() : null;
    }

    private void generateClass(String qfn, String end) throws Exception{
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qfn);
        Writer writer = sourceFile.openWriter();
        writer.write(end);
        writer.close();
    }

    /**
     * Checking if the class to be generated is a valid java identifier
     * Also the name should be not same as the target interface
     */
    private boolean checkIdValidity(String name, Element e) {
        boolean valid = true;
        for (int i = 0; i < name.length(); i++) {
            if (i == 0 ? !Character.isJavaIdentifierStart(name.charAt(i)) : !Character.isJavaIdentifierPart(name.charAt(i))) {
                error("AutoImplement#as should be valid java identifier for code generation: " + name, e);
                valid = false;
            }
        }
        if (name.equals(getTypeName(e))) {
            error("AutoImplement#as should be different than the Interface name ", e);
        }
        return valid;
    }

    /**
     * Get the simple name of the TypeMirror
     */
    private static String getTypeName(Element e) {
        TypeMirror typeMirror = e.asType();
        String[] split = typeMirror.toString().split("\\.");
        return split.length > 0 ? split[split.length - 1] : null;
    }

    private void error(String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    private void info(String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg, e);
    }

}
