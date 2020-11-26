package io.github.febialfarabi.processor;

import com.squareup.javapoet.*;
import hindia.Sumatera;
import io.github.febialfarabi.model.Field;
import io.github.febialfarabi.utils.CoreUtils;
import io.toolisticon.annotationprocessortoolkit.ToolingProvider;
import io.toolisticon.annotationprocessortoolkit.tools.TypeUtils;
import org.modelmapper.ModelMapper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes({"hindia.*"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class JavaBuilderProcessor extends AbstractProcessor {


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
        CoreUtils.annotatedElements.clear();
        CoreUtils.annotatedElements.addAll(elements);
        List<String> uniqueIdCheckList = new ArrayList<>();

        for (Element element : elements) {
            Sumatera sumatera = element.getAnnotation(Sumatera.class);
            CoreUtils.info(processingEnv, "Class yang dianotasi ### "+element.getSimpleName(), element);
            boolean error = false;
            String className = element.getSimpleName()+CoreUtils.CLASS_SUFFIX;

            if (uniqueIdCheckList.contains(className)) {
                CoreUtils.error(processingEnv, "Nama Dto "+className+" sudah digunakan, ganti dengan nama alias untuk membedakan class yang akan digenerate", element);
                error = true;
            }

            if (!error) {
                uniqueIdCheckList.add(element.getSimpleName().toString());
                try {
                    generateJavaClass(className, element);
                } catch (Exception e) {
                    e.printStackTrace();
                    CoreUtils.error(processingEnv, e.getMessage(), null);
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
        FieldInfo fieldInfo = FieldInfo.get(processingEnv, element);
        Set<FieldSpec> fieldSpecSet = new HashSet<>();
        Set<Field> importFieldSet = new HashSet<>();
        fieldInfo.getFields().forEach(
                (s, field) -> {
                    FieldSpec.Builder fieldSpecBuilder = null ;
                    fieldSpecBuilder = FieldSpec.builder(field.getTypeName(), s, field.getArrayModifiers());
                    if(field.isNeedAdditionalImport()){
                        importFieldSet.add(field);
                    }
                    for (AnnotationSpec annotationSpec : field.getAnnotationSpecs()) {
                        fieldSpecBuilder.addAnnotation(annotationSpec);
                    }
                    fieldSpecSet.add(fieldSpecBuilder.build());
                }
        );

        Set<MethodSpec> methodSpecSet = new HashSet<>();
        fieldInfo.getFields().forEach(
                (s, field) -> {
                    methodSpecSet.add(MethodSpec.methodBuilder("set" + Character.toUpperCase(s.charAt(0)) + s.substring(1))
                            .addParameter(field.getTypeName(), s).addModifiers(Modifier.PUBLIC).addStatement("this.$N = $N", s, s).build());

                    if(field.getTypeName().equals(TypeName.BOOLEAN)){
                        methodSpecSet.add(MethodSpec.methodBuilder("is" + Character.toUpperCase(s.charAt(0)) + s.substring(1))
                                .returns(field.getTypeName()).addModifiers(Modifier.PUBLIC).addStatement("return this.$N", s).build());
                    }else{
                        methodSpecSet.add(MethodSpec.methodBuilder("get" + Character.toUpperCase(s.charAt(0)) + s.substring(1))
                                .returns(field.getTypeName()).addModifiers(Modifier.PUBLIC).addStatement("return this.$N", s).build());
                    }
                }
        );

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        fieldInfo.getFields().forEach((s, field) -> {
            constructorBuilder.addParameter(field.getTypeName(), s).addStatement("this.$N = $N", s, s);

        });
        MethodSpec methodSpec = constructorBuilder.build();

        TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className) ;

        TypeElement typeElement = TypeUtils.TypeRetrieval.getTypeElement(element.asType().toString());
        TypeMirror inheritanceMirror = typeElement.getSuperclass();
        if(inheritanceMirror!=null && !inheritanceMirror.toString().equals("java.lang.Object")){
            TypeElement inheritanceElement = TypeUtils.TypeRetrieval.getTypeElement(inheritanceMirror);
            Boolean annotated = inheritanceElement.getAnnotation(Sumatera.class)!=null;
            if(!annotated){
                CoreUtils.error(processingEnv, "Class yang diinheritance / diextends harus di anotasi dengan @Dto", element);
                return;
            }
            String inheritanceClassName = inheritanceElement.getSimpleName()+CoreUtils.CLASS_SUFFIX;

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
//                .addField(FieldSpec.builder(Gson.class, "gson", Modifier.STATIC).build())
//                .addStaticBlock(CodeBlock.of("gson = new $N();", Gson.class.getSimpleName()))
                .addFields(fieldSpecSet)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
                .addMethod(methodSpec)
                .addMethods(methodSpecSet)
                .addMethod(
                        MethodSpec.methodBuilder("from").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(TypeName.get(element.asType()),  CoreUtils.getTypeName(element))
                                .returns(ClassName.get(CoreUtils.getPackageName(element), className))
                                .addStatement("return mapper.map($N, $N)", CoreUtils.getTypeName(element), className+".class").build())
                .addMethod(
                        MethodSpec.methodBuilder("to").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(ClassName.get(CoreUtils.getPackageName(element), className), CoreUtils.getTypeName(element).toLowerCase())
                                .returns(TypeName.get(element.asType()))
                                .addStatement("return mapper.map($N, $N)", CoreUtils.getTypeName(element).toLowerCase(), element.getSimpleName()+".class").build());
//                .addMethod(
//                        MethodSpec.methodBuilder("fromJson").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                                .addParameter(String.class,  "jsonString")
//                                .returns(ClassName.get(CoreUtils.getPackageName(element), className))
//                                .addStatement("return gson.fromJson($N, $N)", "jsonString", className+".class").build())
//                .addMethod(
//                        MethodSpec.methodBuilder("toJson").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                                .addParameter(ClassName.get(CoreUtils.getPackageName(element), className), CoreUtils.getTypeName(element).toLowerCase())
//                                .returns(String.class)
//                                .addStatement("return gson.toJson($N)", CoreUtils.getTypeName(element).toLowerCase()).build());

        TypeSpec typeSpec = typeSpecBuilder.build();

        String pkg = CoreUtils.getPackageName(element);
        JavaFile.Builder javaFileBuilder = JavaFile.builder(pkg , typeSpec);
        JavaFile javaFile = javaFileBuilder.build();

        String content = javaFile.toString();
        for (Map.Entry<String, Field> stringFieldEntry : fieldInfo.getFields().entrySet()) {
            if(stringFieldEntry.getValue().isNeedReplaceImport()){
                CoreUtils.info(processingEnv, stringFieldEntry.getValue().getReplaceImport()+"="+stringFieldEntry.getValue().getImportReplacer(), element);
                content = content.replace(stringFieldEntry.getValue().getReplaceImport(), stringFieldEntry.getValue().getImportReplacer());
            }
            if(stringFieldEntry.getValue().isNeedReplaceGeneric()){
                String replaceGeneric = stringFieldEntry.getValue().getReplaceGeneric();
                String genericReplacer = stringFieldEntry.getValue().getGenericReplacer();
                CoreUtils.info(processingEnv, replaceGeneric+"="+genericReplacer, element);
                content = content.replace(replaceGeneric, genericReplacer);
            }
        }
        String[] contentSplit = content.split(System.lineSeparator(), 2);
        StringBuilder stringBuilder= new StringBuilder();
        stringBuilder.append(contentSplit[0]).append(System.lineSeparator()).append(System.lineSeparator());
        if(inheritanceMirror!=null && !inheritanceMirror.toString().equals(Object.class.getName())){
            TypeElement inheritanceElement = TypeUtils.TypeRetrieval.getTypeElement(inheritanceMirror);
            stringBuilder.append("import").append(" ").append(CoreUtils.getPackageName(inheritanceElement)+"."+CoreUtils.getTypeName(inheritanceElement)+CoreUtils.CLASS_SUFFIX).append(";");
        }
        importFieldSet.forEach(imporField -> {
            stringBuilder.append(System.lineSeparator()).append("import").append(" ").append(imporField.getImportPackage()).append(";");
        });
        for (Map.Entry<String, Field> stringFieldEntry : fieldInfo.getFields().entrySet()) {
            if(stringFieldEntry.getValue().isNeedReplaceImport()){
                if(!content.contains(stringFieldEntry.getValue().getImportReplacer())){
                    stringBuilder.append(System.lineSeparator()).append("import").append(" ").append(stringFieldEntry.getValue().getImportReplacer()).append(";");
                }
            }
        }

        stringBuilder.append(contentSplit[1]);

        generateClass(pkg + "." + className, stringBuilder.toString());

    }

    private void generateClass(String qfn, String end) throws Exception{
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qfn);
        Writer writer = sourceFile.openWriter();
        writer.write(end);
        writer.close();
    }


}
