package com.ustadmobile.lib.annotationprocessor.core;


import com.ustadmobile.core.impl.UmCallback;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * DaoMethodInfo is a convenience wrapper class that can work out information we frequently need
 * when generating DAO method implementations, e.g. is the method asynchronous? What is the result
 * type?
 *
 * Terminology:
 *
 * The ResultType refers to the value returned by the method, or used for a result callback
 * The EntityType refers to the POJO class annotated with @UmEntity being used on an insert, update,
 * or delete method
 */
public class DaoMethodInfo {

    private ProcessingEnvironment processingEnv;

    private ExecutableElement method;

    private TypeElement daoClass;

    TypeElement umCallbackTypeElement;

    /**
     * Wrapper constructor
     *
     * @param method The method that is to be generated (e.g. abstract method originating from a DAO
     *               class or interface, whether it is inherited or not)
     * @param daoClass The DAO class that is being generated. Where a method is being implemented from
     *                 a typed interface of superclass, this is used to resolve the type arguments.
     * @param processingEnv Annotation processor processing environment
     */
    public DaoMethodInfo(ExecutableElement method, TypeElement daoClass,
                         ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.method = method;
        this.daoClass = daoClass;
        umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
    }

    private VariableElement getFirstParam() {
        return method.getParameters().get(0);
    }

    /**
     * Determines if the first parameter of this method is a List
     *
     * @return true if the first parameter is a list, false otherwise
     */
    public boolean hasEntityListParam() {
        TypeMirror firstParam = getFirstParam().asType();
        return processingEnv.getElementUtils().getTypeElement(List.class.getName()).equals(
                processingEnv.getTypeUtils().asElement(firstParam));
    }

    /**
     * Determine if the first parameter of this method is an array
     *
     * @return true if the first parameter is an array, false otherwise
     */
    public boolean hasEntityArrayParam() {
        return getFirstParam().asType().getKind().equals(TypeKind.ARRAY);
    }

    /**
     * Find the method parameter that is an async callback (e.g. UmCallback)
     *
     * @return index of the parameter that is a UmCallback, -1 if there is no such parameter
     */
    public int getAsyncParamIndex() {
        return getMethodParametersAsElements().indexOf(umCallbackTypeElement);
    }

    /**
     * Determine what type of entity parameter is being used for an Insert, Update, or Delete method
     * (where the entity is a parameter of the method). This will figure out the actually entity
     * TypeMirror, regardless of whether the parameter is specified directly, or if the method accepts
     * a list or array of the entity.
     *
     * @return TypeMirror representing the entity type
     */
    public TypeMirror resolveEntityParameterType() {
        TypeMirror entityTypeMirror;
        if(hasEntityListParam()) {
            entityTypeMirror = ((DeclaredType)getFirstParam().asType()).getTypeArguments().get(0);
        }else if(hasEntityArrayParam()) {
            entityTypeMirror = ((ArrayType)getFirstParam().asType()).getComponentType();
        }else {
            entityTypeMirror = getFirstParam().asType();
        }

        entityTypeMirror = AbstractDbProcessor.resolveDeclaredType(entityTypeMirror, daoClass,
                (TypeElement)method.getEnclosingElement(), processingEnv);

        return entityTypeMirror;
    }

    /**
     * Determine the result type of the method. The result is either the return value, or the callback
     * result type. This will also resolve type variables.
     *
     * @return TypeMirror representing the result type as above.
     */
    public TypeMirror resolveResultType() {
        int asyncParamIndex = getAsyncParamIndex();
        TypeMirror resultType;
        if(asyncParamIndex != -1) {
            DeclaredType callbackDeclaredType = (DeclaredType)method.getParameters()
                    .get(asyncParamIndex).asType();
            resultType = callbackDeclaredType.getTypeArguments().get(0);
        }else {
            resultType = method.getReturnType();
        }

        return AbstractDbProcessor.resolveDeclaredType(resultType, daoClass,
                (TypeElement)method.getEnclosingElement(), processingEnv);
    }

    /**
     * As per resolveResultType, but returning an element. Synonymous to
     *  daoMethodInfo.resolveReturnType()
     *
     * @return ResultType as an element
     */
    public Element resolveResultAsElement() {
        return processingEnv.getTypeUtils().asElement(resolveResultType());
    }

    /**
     * Resolve the return type of the method.
     *
     * @return TypeMirror for the return type of the method, with any type variables resolved
     */
    public TypeMirror resolveReturnType() {
        return AbstractDbProcessor.resolveDeclaredType(method.getReturnType(), daoClass,
                (TypeElement)method.getEnclosingElement(), processingEnv);
    }

    protected List<Element> getMethodParametersAsElements() {
        List<? extends VariableElement> variableElementList = method.getParameters();
        List<Element> variableTypeElements = new ArrayList<>();
        for(VariableElement variableElement : variableElementList) {
            variableTypeElements.add(processingEnv.getTypeUtils().asElement(variableElement.asType()));
        }

        return variableTypeElements;
    }


}