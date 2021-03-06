package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.InputStreamStreamingResponse;
import com.ustadmobile.lib.database.annotation.UmRestAuthorizedUidParam;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.UmDbWithAuthenticator;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;

import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_JERSEY_RESOURCE_OUT;

public class DbProcessorJerseyResource extends AbstractDbProcessor {

    public static final String POSTFIX_RESUORCE = "_Resource";

    public static final String FIELDNAME_SERVLET_CONTEXT = "_servletContext";

    public static final int ASYNC_TIMEOUT_DEFAULT = 5000;


    public DbProcessorJerseyResource() {
        setOutputDirOpt(OPT_JERSEY_RESOURCE_OUT);
    }

    @Override
    public void processDbClass(TypeElement dbType, String destination) throws IOException {

    }

    @Override
    public void processDbDao(TypeElement daoType, TypeElement dbType, String destination) throws IOException {
        List<Element> annotatedElementList = findRestEnabledMethods(daoType);

        TypeSpec.Builder resBuilder = TypeSpec.classBuilder(daoType.getSimpleName().toString() +
                POSTFIX_RESUORCE)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Path.class).addMember("value",
                "$S", daoType.getSimpleName().toString()).build())
                .addField(FieldSpec.builder(ClassName.get("javax.servlet","ServletContext"),
                        FIELDNAME_SERVLET_CONTEXT).addAnnotation(Context.class).build());

        TypeMirror inputStreamType = processingEnv.getElementUtils().getTypeElement(
                InputStream.class.getName()).asType();


        for(Element annotatedElement : annotatedElementList) {
            ExecutableElement methodElement = (ExecutableElement) annotatedElement;
            DaoMethodInfo methodInfo = new DaoMethodInfo(methodElement, daoType, processingEnv);
            TypeName resultTypeName = TypeName.get(methodInfo.resolveResultEntityType());
            boolean primitiveToStringResult = false;
            boolean isVoidResult = isVoid(methodInfo.resolveResultType());
            boolean isStreamResponse = false;

            if (resultTypeName.isPrimitive() || resultTypeName.isBoxedPrimitive()) {
                resultTypeName = ClassName.get(String.class);
                primitiveToStringResult = true;
            }

            if (resultTypeName.equals(ClassName.get(Void.class))) {
                resultTypeName = TypeName.VOID;
            }

            if(resultTypeName.equals(ClassName.get(InputStream.class))) {
                resultTypeName = ClassName.get(Response.class);
                isStreamResponse = true;
            }

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(
                    methodElement.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(resultTypeName)
                    .addAnnotation(AnnotationSpec.builder(Path.class).addMember("value",
                            "\"/$L\"", methodElement.getSimpleName().toString()).build());

            addJaxWsParameters(methodElement, daoType, methodBuilder, QueryParam.class, null,
                    FormDataParam.class, true);
            addJaxWsMethodAnnotations(methodElement, daoType, methodBuilder);

            ExecutableElement daoGetter = DbProcessorUtils.findDaoGetter(daoType, dbType,
                    processingEnv);
            if (daoGetter == null) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        formatMethodForErrorMessage(methodElement, daoType) + " : cannot find database getter method");
                continue;
            }

            CodeBlock.Builder codeBlock = CodeBlock.builder()
                    .add("$1T _db = $1T.getInstance($2L);\n", dbType, FIELDNAME_SERVLET_CONTEXT)
                    .add("$T _dao = _db.$L();\n", daoType,
                            daoGetter.getSimpleName());

            //TODO: check at compile time that this database implements UmDbWithAuthenticator

            if(methodInfo.getAuthorizedUidParam() != null) {
                VariableElement uidParamEl = methodInfo.getAuthorizedUidParam();
                codeBlock.beginControlFlow("if(!(($T)_db).validateAuth($L, _authHeader))",
                            UmDbWithAuthenticator.class, uidParamEl.getSimpleName())
                            .add("throw new $T($S, 403);\n", WebApplicationException.class,
                                    "Invalid authorization")
                        .endControlFlow();
            }

            String syncableDbVariableName = null;
            if(methodInfo.isUpdateOrInsert()) {
                syncableDbVariableName = "_syncableDb";
                codeBlock.add("$1T $2L = ($1T)_db;", UmSyncableDatabase.class,
                        syncableDbVariableName);
            }

            if(methodElement.getAnnotation(UmUpdate.class) != null
                    && DbProcessorUtils.entityHasChangeSequenceNumbers(
                            methodInfo.resolveEntityParameterComponentType(), processingEnv)) {
                codeBlock.add(generateUpdateSetChangeSeqNumSection(methodElement, daoType,
                        syncableDbVariableName).build());
            }


            if (methodInfo.isAsyncMethod()) {
                codeBlock.add("$1T _latch = new $1T(1);\n", CountDownLatch.class);
                if (!isVoidResult)
                    codeBlock.add("$1T<$2T> _resultRef = new $1T<>();\n", AtomicReference.class,
                            methodInfo.resolveResultType()/*,isAutoSyncInsert ? "_syncablePkResult" : ""*/);


                codeBlock.add("_dao.$L(", methodElement.getSimpleName());
                int paramCount = 0;
                for (VariableElement param : methodElement.getParameters()) {
                    if (paramCount > 0)
                        codeBlock.add(",");
                    if (!umCallbackTypeElement.equals(processingEnv.getTypeUtils()
                            .asElement(param.asType()))) {
                        codeBlock.add(param.getSimpleName().toString());
                    } else {
                        CodeBlock.Builder onSuccessCode = CodeBlock.builder();
                        if (!isVoidResult) {
                            onSuccessCode.add("_resultRef.set(_result);\n");
                        }


                        onSuccessCode.add("_latch.countDown();\n");

                        TypeSpec callbackTypeSpec = TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(UmCallback.class),
                                        TypeName.get(methodInfo.resolveResultType())))
                                .addMethod(
                                        MethodSpec.methodBuilder("onSuccess")
                                                .addParameter(TypeName.get(methodInfo.resolveResultType()), "_result")
                                                .addModifiers(Modifier.PUBLIC)
                                                .addCode(onSuccessCode.build()).build())
                                .addMethod(
                                        MethodSpec.methodBuilder("onFailure")
                                                .addModifiers(Modifier.PUBLIC)
                                                .addParameter(Throwable.class, "_throwable")
                                                .addCode("_latch.countDown();\n").build()).build();
                        codeBlock.add("$L", callbackTypeSpec);

                    }

                    paramCount++;
                }

                codeBlock.add(");\n")
                        .beginControlFlow("try")
                        .add("_latch.await($L, $T.MILLISECONDS);\n", ASYNC_TIMEOUT_DEFAULT,
                                TimeUnit.class)
                        .nextControlFlow("catch($T _e)", InterruptedException.class)
                        .endControlFlow();
                if (!isVoidResult) {
                    codeBlock.add("return ");
                    if(primitiveToStringResult)
                        codeBlock.add("String.valueOf(");

                    codeBlock.add("_resultRef.get()");
                    if(primitiveToStringResult)
                        codeBlock.add(")");

                    codeBlock.add(";\n");
                }
            }else if(methodInfo.isLiveDataReturn()) {
                TypeMirror liveDataType = methodInfo.resolveResultEntityType();
                codeBlock.add("$1T _latch = new $1T(1);\n", CountDownLatch.class)
                    .add("$1T<$2T> _resultRef = new $1T<>();\n", AtomicReference.class,
                            liveDataType)
                    .add("$T _liveData = _dao.$L",
                        methodInfo.resolveResultType(), methodElement.getSimpleName())
                    .add(makeNamedParameterMethodCall(methodElement.getParameters()))
                    .add(";\n")
                    .beginControlFlow("$T<$T> _observer = (_value) -> ",
                        UmObserver.class, liveDataType)
                        .add("_resultRef.set(_value);\n")
                        .add("_latch.countDown();\n")
                    .endControlFlow(" ")
                    .add("_liveData.observeForever(_observer);\n")
                    .beginControlFlow("try")
                        .add("_latch.await($L, $T.MILLISECONDS);\n", ASYNC_TIMEOUT_DEFAULT,
                                TimeUnit.class)
                    .nextControlFlow("catch($T _e)", InterruptedException.class)
                        .add("_e.printStackTrace();\n")
                    .endControlFlow()
                    .add("_liveData.removeObserver(_observer);\n")
                    .add("return _resultRef.get();\n");
            }else {
                boolean returnDaoResult = !isVoidResult;

                boolean daoMethodThrowsException = !methodElement.getThrownTypes().isEmpty();
                if(daoMethodThrowsException) {
                    codeBlock.beginControlFlow("try");
                }

                if(returnDaoResult)
                    codeBlock.add("return ");

                if(primitiveToStringResult)
                    codeBlock.add("String.valueOf(");

                //TODO: Set content type and size headers for attachment responses
                if(isStreamResponse) {
                    codeBlock.add("$T.ok(new $T(", Response.class,
                            InputStreamStreamingResponse.class);
                }

                codeBlock.add("_dao.$L", methodElement.getSimpleName());
                codeBlock.add(makeNamedParameterMethodCall(methodElement.getParameters()));

                if(primitiveToStringResult)
                    codeBlock.add(")");
                else if(isStreamResponse)
                    codeBlock.add(")).build()");

                codeBlock.add(";\n");

                if(daoMethodThrowsException) {
                    codeBlock.nextControlFlow("catch($T _t)", Throwable.class)
                            .add("_t.printStackTrace();\n")
                            .add("throw new $T(_t.toString(), 500);\n",
                                    WebApplicationException.class)
                            .endControlFlow();
                }

            }

            methodBuilder.addCode(codeBlock.build());
            resBuilder.addMethod(methodBuilder.build());
        }


        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(daoType);
        JavaFile databaseJavaFile = JavaFile.builder(packageElement.getQualifiedName().toString(),
                resBuilder.build())
                .indent("    ").build();
        writeJavaFileToDestination(databaseJavaFile, destination);

    }



}
