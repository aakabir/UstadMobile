package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmEmbedded;
import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.database.jdbc.DbChangeListener;
import com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils;
import com.ustadmobile.lib.database.jdbc.UmJdbcDatabase;
import com.ustadmobile.lib.database.jdbc.UmLiveDataJdbc;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;

import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.tools.Diagnostic;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_JDBC_OUTPUT;

public class DbProcessorJdbc extends AbstractDbProcessor {

    private static String SUFFIX_JDBC_DBMANAGER = "_Jdbc";

    private static final String SUFFIX_JDBC_DAO = "_JdbcDaoImpl";

    private File dbTmpFile;

    //Map of fully qualified database class name to a connection that has that database
    private Map<String, DataSource> nameToDataSourceMap = new HashMap<>();

    public DbProcessorJdbc() {
        setOutputDirOpt(OPT_JDBC_OUTPUT);
    }

    public void processDbClass(TypeElement dbType, File destinationDir) throws IOException {
        String jdbcDbClassName = dbType.getSimpleName() + SUFFIX_JDBC_DBMANAGER;
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(dbType);
        ParameterizedTypeName dbChangeListenersMapType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(DbChangeListener.class),
                ClassName.get(JdbcDatabaseUtils.DbChangeListenerRequest.class));

        ClassName initialContextClassName = ClassName.get(InitialContext.class);
        TypeSpec.Builder jdbcDbTypeSpec = TypeSpec.classBuilder(jdbcDbClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(dbType))
                .addSuperinterface(ClassName.get(UmJdbcDatabase.class))
                .addJavadoc("Generated code - DO NOT EDIT!\n")
                .addField(ClassName.get(Object.class), "_context", Modifier.PRIVATE)
                .addField(ClassName.get(DataSource.class), "_dataSource", Modifier.PRIVATE)
                .addField(ClassName.get(ExecutorService.class), "_executor", Modifier.PRIVATE)
                .addField(dbChangeListenersMapType, "_dbChangeListeners", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.get(Object.class), "context")
                    .addParameter(TypeName.get(String.class), "dbName")
                    .addCode(CodeBlock.builder().add("\tthis._context = context;\n")
                        .add("this._dbChangeListeners = new $T<>();\n", HashMap.class)
                        .beginControlFlow("try ")
                            .add("$T iContext = new $T();\n", initialContextClassName, initialContextClassName)
                            .add("_executor = $T.newCachedThreadPool();\n", Executors.class)
                            .add("this._dataSource = (DataSource)iContext.lookup(\"java:/comp/env/jdbc/\"+dbName);\n")
                            .add("createAllTables();\n")
                        .endControlFlow()
                        .beginControlFlow("catch($T e)",
                                ClassName.get(NamingException.class))
                            .add("throw new RuntimeException(e);\n")
                        .endControlFlow()
                        .build())
                .build())
                .addMethod(MethodSpec.methodBuilder("getExecutor")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(ExecutorService.class))
                        .addCode("return this._executor;\n").build())
                .addMethod(MethodSpec.methodBuilder("getConnection")
                        .addException(ClassName.get(SQLException.class))
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(Connection.class))
                        .addCode("return this._dataSource.getConnection();\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("handleTablesChanged")
                        .addParameter(ArrayTypeName.of(String.class), "tablesChanged")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .varargs()
                        .addCode("$T.handleTablesChanged(_dbChangeListeners, tablesChanged);\n",
                                JdbcDatabaseUtils.class)
                        .build())

                .addMethod(MethodSpec.methodBuilder("addDbChangeListener")
                        .addParameter(ClassName.get(JdbcDatabaseUtils.DbChangeListenerRequest.class),
                                "listenerRequest")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addCode(CodeBlock.builder()
                                .add("$T.addDbChangeListener(listenerRequest, _dbChangeListeners);\n",
                                        JdbcDatabaseUtils.class)
                                .build())
                        .build())
                .addMethod(MethodSpec.methodBuilder("removeDbChangeListener")
                        .addParameter(ClassName.get(JdbcDatabaseUtils.DbChangeListenerRequest.class),
                            "listenerRequest")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addCode(CodeBlock.builder()
                            .add("$T.removeDbChangeListener(listenerRequest, _dbChangeListeners);\n",
                                    JdbcDatabaseUtils.class)
                            .build())
                        .build());

        TypeSpec.Builder factoryClassSpec = DbProcessorUtils.makeFactoryClass(dbType, jdbcDbClassName);

        addCreateTablesMethodToClassSpec(dbType, jdbcDbTypeSpec);

        for(Element subElement : dbType.getEnclosedElements()) {
            if (subElement.getKind() != ElementKind.METHOD)
                continue;

            ExecutableElement dbMethod = (ExecutableElement)subElement;
            if(dbMethod.getModifiers().contains(Modifier.STATIC))
                continue;

            if(!dbMethod.getModifiers().contains(Modifier.ABSTRACT))
                continue;


            MethodSpec.Builder overrideSpec =  MethodSpec.overriding(dbMethod);
            TypeElement returnTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(
                    dbMethod.getReturnType());

            if(dbMethod.getAnnotation(UmDbContext.class) != null) {
                overrideSpec.addCode("return _context;\n");
            }else if(dbMethod.getAnnotation(UmClearAll.class) != null) {
                addClearAllTablesCodeToMethod(dbType, overrideSpec, '`');
            }else {
                String daoFieldName = "_" + returnTypeElement.getSimpleName();
                jdbcDbTypeSpec.addField(TypeName.get(dbMethod.getReturnType()), daoFieldName, Modifier.PRIVATE);
                ClassName daoImplClassName = ClassName.get(
                        processingEnv.getElementUtils().getPackageOf(returnTypeElement).getQualifiedName().toString(),
                        returnTypeElement.getSimpleName() + SUFFIX_JDBC_DAO);

                overrideSpec.beginControlFlow("if($L == null)", daoFieldName)
                        .addCode("$L = new $T(this);\n", daoFieldName, daoImplClassName)
                    .endControlFlow()
                    .addCode("return $L;\n", daoFieldName);
            }


            jdbcDbTypeSpec.addMethod(overrideSpec.build());

        }



        JavaFile.builder(packageElement.getQualifiedName().toString(), factoryClassSpec.build())
                .indent("    ").build().writeTo(destinationDir);
        JavaFile.builder(packageElement.getQualifiedName().toString(), jdbcDbTypeSpec.build())
                .indent("    ").build().writeTo(destinationDir);

        //now create an in temporary file implementation of this database, this will be used when generating the DAOs
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dbTmpFile = File.createTempFile(dbType.getQualifiedName().toString(), ".db");

        messager.printMessage(Diagnostic.Kind.NOTE,
                "DbProcessorJdbc: creating temporary database in: " +
                        dbTmpFile.getAbsolutePath());

        dataSource.setUrl("jdbc:sqlite:" + dbTmpFile.getAbsolutePath());
        try(
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
        ) {
            nameToDataSourceMap.put(dbType.getQualifiedName().toString(), dataSource);
            for(TypeElement entityType : findEntityTypes(dbType)){
                stmt.execute(makeCreateTableStatement(entityType, '`'));
            }
            stmt.close();
        }catch(SQLException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Error attempting to create database for "
                + dbType.getQualifiedName().toString() + ": " + e.getMessage());
        }


    }

    /**
     * Generate a createAllTables method that will run the SQL required to generate all tables for
     * all entities on the given database type.
     *
     * @param dbType TypeElement representing the class annotated with @UmDatabase
     * @param classBuilder TypeSpec.Builder being used to generate the JDBC database class implementation
     */
    protected void addCreateTablesMethodToClassSpec(TypeElement dbType, TypeSpec.Builder classBuilder) {
        MethodSpec.Builder createMethod = MethodSpec.methodBuilder("createAllTables");
        CodeBlock.Builder createCb = CodeBlock.builder();
        createCb.add("try (\n").indent()
                .add("$T _connection = getConnection();\n", Connection.class)
                .add("$T _stmt = _connection.createStatement();\n", ClassName.get(Statement.class))
            .unindent().beginControlFlow(")")
                .add("$T _existingTableNames = $T.getTableNames(_connection);\n",
                        ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)),
                        ClassName.get(JdbcDatabaseUtils.class));

        for(TypeElement entityTypeElement : findEntityTypes(dbType)) {
            addCreateTableStatements(createCb, "_stmt", entityTypeElement, '`');
        }

        createCb.endControlFlow() //end try/catch control flow
                .beginControlFlow("catch($T e)\n", SQLException.class)
                .add("throw new $T(e);\n", RuntimeException.class)
                .endControlFlow();



        createMethod.addCode(createCb.build());
        classBuilder.addMethod(createMethod.build());
    }

    protected void addClearAllTablesCodeToMethod(TypeElement dbType, MethodSpec.Builder builder,
                                                 char identifierQuoteChar) {
        String identifierQuoteStr = String.valueOf(identifierQuoteChar);
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        codeBlock.add("try(\n").indent()
                .add("$T connection = getConnection();\n", Connection.class)
                .add("$T stmt = connection.createStatement();\n", Statement.class)
                .unindent().beginControlFlow(") ");

        for(TypeElement entityType : findEntityTypes(dbType)) {
            codeBlock.add("stmt.executeUpdate(\"DELETE FROM $1L$2L$1L\");\n",
                    identifierQuoteStr, entityType.getSimpleName().toString());
        }

        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
                .add("e.printStackTrace();\n")
                .endControlFlow();

        builder.addCode(codeBlock.build());
    }

    /**
     * Get the TypeElements that correspond to the entities on the @UmDatabase annotation of the given
     * TypeElement
     *
     * TODO: make sure that we check each annotation, this currently **ASSUMES** the first annotation is @UmDatabase
     *
     * @param dbTypeElement TypeElement representing the class with the @UmDatabase annotation
     * @return List of TypeElement that represents the values found on entities
     */
    private List<TypeElement> findEntityTypes(TypeElement dbTypeElement){
        List<TypeElement> entityTypeElements = new ArrayList<>();
        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationEntryMap =
                dbTypeElement.getAnnotationMirrors().get(0).getElementValues();
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotationEntryMap.entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            Object value = entry.getValue().getValue();
            if (key.equals("entities")) {
                List<? extends AnnotationValue> typeMirrors =
                        (List<? extends AnnotationValue>) value;
                for(AnnotationValue entityValue : typeMirrors) {
                    entityTypeElements.add((TypeElement) processingEnv.getTypeUtils()
                            .asElement((TypeMirror) entityValue.getValue()));
                }
            }
        }

        return entityTypeElements;
    }

    /**
     * Generates code that will execute CREATE TABLE and CREATE INDEX as required for the given
     * entity.
     *
     * @param codeBlock CodeBlock.Builder that this code will be added to
     * @param stmtVariableName Name of the SQL Statement object variable in the CodeBlock
     * @param entitySpec The TypeElement representing the entity for which the statements are being generated
     * @param quoteChar The quote char used to contain SQL table names e.g. '`' for MySQL and Sqlite
     *
     * @return SQL for table creation only, to be used within the annotation processor itself
     */
    protected void addCreateTableStatements(CodeBlock.Builder codeBlock, String stmtVariableName,
                                                 TypeElement entitySpec, char quoteChar) {

        Map<String, List<String>> indexes = new HashMap<>();
        for(Element subElement : entitySpec.getEnclosedElements()) {
            if(subElement.getKind() != ElementKind.FIELD)
                continue;

            VariableElement fieldVariable = (VariableElement)subElement;

            if(fieldVariable.getAnnotation(UmIndexField.class) != null) {
                indexes.put("index_" + entitySpec.getSimpleName() + '_' + fieldVariable.getSimpleName(),
                        Collections.singletonList(fieldVariable.getSimpleName().toString()));
            }

        }

        codeBlock.beginControlFlow("if(!_existingTableNames.contains($S))",
                entitySpec.getSimpleName().toString())
                .add("$L.executeUpdate($S);\n", stmtVariableName,
                        makeCreateTableStatement(entitySpec, quoteChar));
        for(Map.Entry<String, List<String>> index : indexes.entrySet()) {
            Map<String, String> formatArgs = new HashMap<>();
            formatArgs.put("quot", String.valueOf(quoteChar));
            formatArgs.put("index_name", index.getKey());
            formatArgs.put("table_name", entitySpec.getSimpleName().toString());
            formatArgs.put("stmt", stmtVariableName);
            boolean indexFieldCommaNeeded = false;
            StringBuffer indexFieldBuffer = new StringBuffer();
            for(String fieldName : index.getValue()) {
                if(indexFieldCommaNeeded)
                    indexFieldBuffer.append(',');

                indexFieldCommaNeeded = true;
                indexFieldBuffer.append(quoteChar).append(fieldName).append(quoteChar).append(' ');
            }

            formatArgs.put("index_fields", indexFieldBuffer.toString());
            codeBlock.addNamed("$stmt:L.executeUpdate(\"CREATE INDEX $quot:L$index_name:L$quot:L ON $quot:L$table_name:L$quot:L ($index_fields:L)\");\n",
                    formatArgs);
        }

        codeBlock.endControlFlow();
    }

    /**
     * Generate a create table statement for the given entity class
     *
     * @param entitySpec TypeElement representing the entity class to generate a create table statement for
     * @param quoteChar quoteChar used to enclose database identifiers
     *
     * @return Create table SQL as a String
     */
    private String makeCreateTableStatement(TypeElement entitySpec, char quoteChar) {
        boolean fieldVariablesStarted = false;

        StringBuffer sbuf = new StringBuffer()
                .append("CREATE TABLE IF NOT EXISTS ").append(quoteChar)
                .append(entitySpec.getSimpleName()).append(quoteChar)
                .append(" (");
        for(Element subElement : entitySpec.getEnclosedElements()) {
            if (subElement.getKind() != ElementKind.FIELD)
                continue;

            if (fieldVariablesStarted)
                sbuf.append(", ");

            VariableElement fieldVariable = (VariableElement)subElement;
            UmPrimaryKey primaryKeyAnnotation = fieldVariable.getAnnotation(UmPrimaryKey.class);

            sbuf.append(quoteChar).append(fieldVariable.getSimpleName().toString())
                    .append(quoteChar).append(' ').append(makeSqlTypeDeclaration(fieldVariable));

            if(primaryKeyAnnotation!= null) {
                sbuf.append(" PRIMARY KEY ");
                if(primaryKeyAnnotation.autoIncrement())
                    sbuf.append(" AUTOINCREMENT ");
                sbuf.append(" NOT NULL ");
            }

            fieldVariablesStarted = true;
        }

        sbuf.append(')');


        return sbuf.toString();
    }

    protected String makeSqlTypeDeclaration(VariableElement field) {
        TypeMirror fieldType = field.asType();

        switch(fieldType.getKind()) {
            case BOOLEAN:
                return "BOOL";

            case INT:
                return "INTEGER";

            case LONG:
                return "BIGINT";

            case FLOAT:
                return "FLOAT";

            case DOUBLE:
                return "DOUBLE";

            case DECLARED:
                Element typeEl = processingEnv.getTypeUtils().asElement(fieldType);
                if(processingEnv.getElementUtils().getTypeElement("java.lang.String")
                        .equals(typeEl)) {
                    return "TEXT";
                }else if(processingEnv.getElementUtils().getTypeElement("java.lang.Integer")
                    .equals(typeEl)) {
                    return "INTEGER";
                }else if(processingEnv.getElementUtils().getTypeElement("java.lang.Long")
                    .equals(typeEl)) {
                    return "BIGINT";
                }

                break;
        }

        //didn't recognize that.
        messager.printMessage(Diagnostic.Kind.ERROR,
                "Could not determine SQL data type for field: " + field.getEnclosingElement() +
                "." + field.getSimpleName().toString());

        return null;
    }


    @Override
    public void processDbDao(TypeElement daoType, TypeElement dbType, File destinationDir) throws IOException {
        String daoClassName = daoType.getSimpleName() + SUFFIX_JDBC_DAO;
        TypeSpec.Builder jdbcDaoClassSpec = TypeSpec.classBuilder(daoClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(daoType))
                .addField(ClassName.get(UmJdbcDatabase.class), "_db", Modifier.PRIVATE)
                .addJavadoc(" GENERATED CODE - DO NOT EDIT! \n")
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(UmJdbcDatabase.class), "_db")
                    .addCode("this._db = _db;\n").build());

        for(Element subElement : daoType.getEnclosedElements()) {
            if (subElement.getKind() != ElementKind.METHOD)
                continue;

            ExecutableElement daoMethod = (ExecutableElement)subElement;

            if(daoMethod.getAnnotation(UmInsert.class) != null) {
                addInsertMethod(daoMethod, jdbcDaoClassSpec, '`');
            }else if(daoMethod.getAnnotation(UmQuery.class) != null){
                addQueryMethod(daoMethod, dbType, jdbcDaoClassSpec, '`');
            }else if(daoMethod.getAnnotation(UmUpdate.class) != null) {
                addUpdateMethod(daoMethod, dbType, jdbcDaoClassSpec, '`');
            }else if(daoMethod.getAnnotation(UmDelete.class) != null) {
                addDeleteMethod(daoMethod, jdbcDaoClassSpec, '`');
            }
        }


        JavaFile.builder(processingEnv.getElementUtils().getPackageOf(daoType).toString(),
                jdbcDaoClassSpec.build()).build().writeTo(destinationDir);
    }


    public void addInsertMethod(ExecutableElement daoMethod, TypeSpec.Builder daoBuilder,
                                char identifierQuote) {
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(daoMethod);

        VariableElement insertedElement = daoMethod.getParameters().get(0);
        boolean isList = false;
        boolean isArray = false;
        TypeMirror resultType;

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
        List<Element> variableTypeElements = getMethodParametersAsElements(daoMethod);
        int asyncParamIndex = variableTypeElements.indexOf(umCallbackTypeElement);
        boolean asyncMethod = asyncParamIndex != -1;

        if(asyncMethod) {
            resultType = ((DeclaredType)daoMethod.getParameters().get(asyncParamIndex)
                    .asType()).getTypeArguments().get(0);
        }else {
            resultType = daoMethod.getReturnType();
        }

        TypeMirror insertParameter = daoMethod.getParameters().get(0).asType();

        TypeElement entityTypeElement = insertParameter.getKind().equals(TypeKind.DECLARED) ?
                (TypeElement)processingEnv.getTypeUtils().asElement(insertedElement.asType()) : null;

        if(entityTypeElement != null && entityTypeElement.equals(processingEnv.getElementUtils()
                .getTypeElement(List.class.getName()))) {
            isList = true;
            DeclaredType declaredType = (DeclaredType)daoMethod.getParameters().get(0).asType();
            entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(
                    declaredType.getTypeArguments().get(0));
        }else if(insertParameter.getKind().equals(TypeKind.ARRAY)) {
            isArray = true;
            entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(
                    ((ArrayType)insertParameter).getComponentType());
        }


        if(entityTypeElement.getAnnotation(UmEntity.class) == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, daoMethod.getEnclosingElement().getSimpleName() +
                    "." + daoMethod.getSimpleName() +
                    "@UmInsert first parameter must be an entity, array of entities, or list of entities");
            return;
        }


        String preparedStmtVarName = "_stmt";

        String identifierQuoteStr = String.valueOf(identifierQuote);
        CodeBlock.Builder codeBlock = CodeBlock.builder();


        if(asyncMethod) {
            codeBlock.beginControlFlow("_db.getExecutor().execute(() ->");
        }

        if(!isVoid(resultType)) {
            codeBlock.add("$T result = $L;\n", resultType, defaultValue(resultType));
        }

        codeBlock.add("try (\n").indent()
                    .add("$T connection = _db.getConnection();\n", Connection.class)
                    .add("$T _stmt = connection.prepareStatement(\"INSERT INTO $L$L$L (",
                            PreparedStatement.class, identifierQuoteStr,
                            entityTypeElement.getSimpleName().toString(), identifierQuoteStr);

        List<VariableElement> entityFields = new ArrayList<>();
        boolean commaRequired = false;
        for(Element fieldElement : entityTypeElement.getEnclosedElements()) {
            if(fieldElement.getKind() != ElementKind.FIELD)
                continue;

            if(commaRequired)
                codeBlock.add(", ");

            entityFields.add((VariableElement)fieldElement);
            codeBlock.add(identifierQuoteStr).add(fieldElement.getSimpleName().toString())
                    .add(identifierQuoteStr);
            commaRequired = true;
        }
        codeBlock.add(") VALUES (");
        for(int i = 0; i < entityFields.size(); i++) {
            codeBlock.add("?");
            if(i < entityFields.size() - 1)
                codeBlock.add(", ");
        }
        codeBlock.add(")\"");

        if(!isVoid(resultType)) {
            codeBlock.add(", $T.RETURN_GENERATED_KEYS", Statement.class);
        }

        codeBlock.add(");\n");

        codeBlock.unindent().beginControlFlow(")");


        if(isList || isArray) {
            codeBlock.beginControlFlow("for($T _element : $L)", entityTypeElement,
                    daoMethod.getParameters().get(0).getSimpleName().toString());
        }

        for(int i = 0; i < entityFields.size(); i++) {
            addSetPreparedStatementValueToCodeBlock(preparedStmtVarName,
                    (isList|| isArray) ? "_element" : insertedElement.getSimpleName().toString(),
                    i + 1, entityFields.get(i), codeBlock);
        }

        if(isList || isArray) {
            codeBlock.add("$L.addBatch();\n", preparedStmtVarName)
                .endControlFlow()
                .add("$L.executeBatch();\n", preparedStmtVarName);
        }else {
            codeBlock.add("$L.execute();\n", preparedStmtVarName);
        }

        /*
         * Handle getting generated primary keys (if any)
         */
        if(!isVoid(resultType)) {
            codeBlock.add("try (\n").indent()
                    .add("$T generatedKeys = _stmt.getGeneratedKeys();\n", ResultSet.class)
                .unindent().beginControlFlow(")");

            if(isList || isArray) {
                String arrayListVarName = isList ? "result" : "resultList";
                ParameterizedTypeName listTypeName =ParameterizedTypeName.get(
                        ClassName.get(ArrayList.class), ClassName.get(String.class));
                if(isArray)
                    codeBlock.add("$T ", listTypeName);

                codeBlock.add("$L = new $T();\n", arrayListVarName, listTypeName);

                TypeMirror primaryKeyType = isArray ?
                        ((DeclaredType)resultType).getTypeArguments().get(0) :
                        ((ArrayType)resultType).getComponentType();
                codeBlock.beginControlFlow("while(generatedKeys.next())")
                        .add("$L.add(generatedKeys.get$L(1));\n", arrayListVarName,
                                getPreparedStatementSetterGetterTypeName(primaryKeyType))
                    .endControlFlow();

                if(isArray) {
                    codeBlock.add("result = arrayList.toArray(new $T[arrayList.size()]);\n",
                            primaryKeyType);
                }
            }else {
                codeBlock.beginControlFlow("if(generatedKeys.next())")
                        .add("result = generatedKeys.get$L(1);\n",
                                getPreparedStatementSetterGetterTypeName(resultType))
                        .endControlFlow();
            }

            codeBlock.nextControlFlow("catch($T pkE)", SQLException.class)
                    .add("pkE.printStackTrace();\n")
                    .endControlFlow();
        }

        codeBlock.add("_db.handleTablesChanged($S);\n", entityTypeElement.getSimpleName().toString());


        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
                .add("e.printStackTrace();\n").endControlFlow();

        if(!isVoid(resultType) && !asyncMethod) {
            codeBlock.add("return result;\n");
        }

        if(asyncMethod) {
            codeBlock.add("$T.onSuccessIfNotNull($L, $L);\n", UmCallbackUtil.class,
                    daoMethod.getParameters().get(asyncParamIndex).getSimpleName().toString(),
                    isVoid(resultType) ? "null" : "result");

            codeBlock.endControlFlow(")");
        }

        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());
    }

    /**
     * Generate an implementation for methods annotated with UmUpdate
     *
     * @param daoMethod daoMethod to generate an implementation for
     * @param dbType The database class
     * @param daoBuilder The builder for the dao being generated, to which the new method will be added
     * @param identifierQuote The quote character to use to quote SQL identifiers
     */
    public void addUpdateMethod(ExecutableElement daoMethod, TypeElement dbType,
                                TypeSpec.Builder daoBuilder, char identifierQuote) {
        String identifierQuoteStr = String.valueOf(identifierQuote);
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(daoMethod);

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
        List<Element> variableTypeElements = getMethodParametersAsElements(daoMethod);
        int asyncParamIndex = variableTypeElements.indexOf(umCallbackTypeElement);
        boolean asyncMethod = asyncParamIndex != -1;

        TypeMirror resultType;
        if(asyncMethod) {
            resultType = ((DeclaredType)daoMethod.getParameters().get(asyncParamIndex)
                    .asType()).getTypeArguments().get(0);
        }else {
            resultType = daoMethod.getReturnType();
        }

        TypeMirror entityType = daoMethod.getParameters().get(0).asType();
        TypeElement entityTypeElement = entityType.getKind().equals(TypeKind.DECLARED) ?
                (TypeElement)processingEnv.getTypeUtils().asElement(entityType) : null;

        boolean isListOrArray = false;
        if(entityTypeElement != null &&
                entityTypeElement.equals(processingEnv.getElementUtils().getTypeElement(
                        List.class.getName()))) {
            entityType = ((DeclaredType)entityType).getTypeArguments().get(0);
            entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(entityType);
            isListOrArray = true;
        }else if(entityType.getKind().equals(TypeKind.ARRAY)) {
            entityType = ((ArrayType)entityType).getComponentType();
            entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(entityType);
            isListOrArray = true;
        }


        if(asyncMethod) {
            codeBlock.beginControlFlow("_db.getExecutor().execute(() ->");
        }

        codeBlock.add("$T numUpdates = 0;\n", TypeName.INT);

        codeBlock.add("try (\n").indent()
                .add("$T _connection = _db.getConnection();\n", Connection.class)
                .add("$T _stmt = _connection.prepareStatement(\"", PreparedStatement.class)
                .add("UPDATE $L$L$L SET ", identifierQuoteStr, entityTypeElement.getSimpleName().toString(),
                        identifierQuoteStr);

        Map<String, Integer> fieldNameToPositionMap = new HashMap<>();

        boolean commaRequired = false;
        int positionCounter = 1;
        Element pkElement = null;
        for(Element subElement : entityTypeElement.getEnclosedElements()) {
            if(!subElement.getKind().equals(ElementKind.FIELD))
                continue;

            if(subElement.getAnnotation(UmPrimaryKey.class) != null) {
                pkElement = subElement;
                continue;
            }


            if(commaRequired)
                codeBlock.add(", ");

            codeBlock.add("$L$L$L = ?", identifierQuoteStr, subElement.getSimpleName().toString(),
                    identifierQuoteStr);
            commaRequired = true;
            fieldNameToPositionMap.put(subElement.getSimpleName().toString(),
                    positionCounter);
            positionCounter++;
        }

        if(pkElement == null){
            String message = " UmUpdate method : on primary key field found on " +
                    entityTypeElement.getQualifiedName().toString();
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod) + ": " + message);
            throw new IllegalArgumentException(message);
        }
        fieldNameToPositionMap.put(pkElement.getSimpleName().toString(), positionCounter);

        codeBlock.add(" WHERE $L$L$L = ?\");\n", identifierQuoteStr,
                pkElement.getSimpleName().toString(), identifierQuoteStr);

        codeBlock.unindent().beginControlFlow(")");

        String entityName = isListOrArray ? "_element" :
                daoMethod.getParameters().get(0).getSimpleName().toString();

        if(isListOrArray) {
            codeBlock.beginControlFlow("for($T _element : $L)",
                    entityTypeElement, daoMethod.getParameters().get(0).getSimpleName().toString());
        }

        for(Map.Entry<String, Integer> entry : fieldNameToPositionMap.entrySet()) {
            String propertyName = entry.getKey();
            List<ExecutableElement> getterCallChain = findGetterOrSetter("get", daoMethod,
                    propertyName, entityTypeElement, new ArrayList<>(), true);
            if(getterCallChain != null && getterCallChain.size() != 1) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        formatMethodForErrorMessage(daoMethod) +
                                " could not find setter for " + propertyName);
                return;
            }

            codeBlock.add("_stmt.$L($L, $L.$L());\n",
                    getPreparedStatementSetterMethodName(getterCallChain.get(0).getReturnType()),
                    entry.getValue(),
                    entityName,
                    getterCallChain.get(0).getSimpleName().toString());
        }

        if(isListOrArray) {
            codeBlock.add("_stmt.addBatch();\n")
                .endControlFlow()
                .add("numUpdates = $T.sumUpdateTotals(_stmt.executeBatch());\n",
                        JdbcDatabaseUtils.class);
        }else {
            codeBlock.add("numUpdates = _stmt.executeUpdate();\n");
        }

        codeBlock.beginControlFlow("if(numUpdates > 0)")
                .add("_db.handleTablesChanged($S);\n", entityTypeElement.getSimpleName().toString())
                .endControlFlow();

        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
                .add("e.printStackTrace();\n")
                .endControlFlow();



        if(asyncMethod) {
            codeBlock.add("$T.onSuccessIfNotNull($L, $L);\n",
                    UmCallbackUtil.class, daoMethod.getParameters().get(asyncParamIndex)
                            .getSimpleName().toString(),
                    isVoid(resultType) ? "null" : "numUpdates");
            codeBlock.endControlFlow(")");
        }else if(!isVoid(resultType)) {
            codeBlock.add("return numUpdates;\n");
        }

        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());
    }


    public void addDeleteMethod(ExecutableElement daoMethod, TypeSpec.Builder daoBuider,
                                char identifierQuote) {
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(daoMethod);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        boolean isListOrArray = false;
        String identifierQuoteStr = String.valueOf(identifierQuote);

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
        List<Element> variableTypeElements = getMethodParametersAsElements(daoMethod);
        int asyncParamIndex = variableTypeElements.indexOf(umCallbackTypeElement);
        boolean asyncMethod = asyncParamIndex != -1;

        TypeMirror resultType;
        if(asyncMethod) {
            codeBlock.beginControlFlow("_db.getExecutor().execute(() -> ");
            DeclaredType declaredType = (DeclaredType)daoMethod.getParameters().get(asyncParamIndex)
                    .asType();
            resultType = declaredType.getTypeArguments().get(0);
        }else {
            resultType = daoMethod.getReturnType();
        }

        TypeMirror entityType = daoMethod.getParameters().get(0).asType();
        TypeElement entityTypeElement = entityType.getKind().equals(TypeKind.DECLARED) ?
                (TypeElement)processingEnv.getTypeUtils().asElement(entityType) : null;

        if(entityTypeElement != null &&
                entityTypeElement.equals(processingEnv.getElementUtils().getTypeElement(
                        List.class.getName()))) {
            entityType = ((DeclaredType)entityType).getTypeArguments().get(0);
            entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(entityType);
            isListOrArray = true;
        }else if(entityType.getKind().equals(TypeKind.ARRAY)) {
            entityType = ((ArrayType)entityType).getComponentType();
            entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(entityType);
            isListOrArray = true;
        }


        VariableElement pkElement = findPrimaryKey(entityTypeElement);
        if(pkElement == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod) + " no primary key found on" +
                    entityTypeElement.getQualifiedName());
            return;
        }

        codeBlock.add("int numDeleted = 0;\n")
                .add("try (\n").indent()
                    .add("$T connection = _db.getConnection();\n", Connection.class)
                    .add("$T stmt = connection.prepareStatement(\"", PreparedStatement.class)
                    .add("DELETE FROM $1L$2L$1L WHERE $1L$3L$1L = ?\");\n", identifierQuoteStr,
                            entityTypeElement.getSimpleName().toString(),
                            pkElement.getSimpleName().toString())
                .unindent().beginControlFlow(")");

        if(isListOrArray) {
            codeBlock.beginControlFlow("for($T _entity : $L)",
                    entityTypeElement, daoMethod.getParameters().get(0).getSimpleName().toString());
        }

        PreparedStatement stmt;
        String pkGetterMethod = pkElement.getSimpleName().toString();
        pkGetterMethod = Character.toUpperCase(pkGetterMethod.charAt(0))
                + pkGetterMethod.substring(1);
        codeBlock.add("stmt.$L(1, $L.get$L());\n",
                getPreparedStatementSetterMethodName(pkElement.asType()),
                isListOrArray ? "_entity" : daoMethod.getParameters().get(0).getSimpleName().toString(),
                pkGetterMethod);

        if(isListOrArray) {
            codeBlock.add("stmt.addBatch();\n")
                    .endControlFlow()
                    .add("numDeleted = $T.sumUpdateTotals(stmt.executeBatch());\n",
                            JdbcDatabaseUtils.class);
        }else {
            codeBlock.add("numDeleted = stmt.executeUpdate();\n");
        }

        codeBlock.beginControlFlow("if(numDeleted > 0)")
                .add("_db.handleTablesChanged($S);\n", entityTypeElement.getSimpleName().toString())
                .endControlFlow();

        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
                .add("e.printStackTrace();\n")
                .endControlFlow();

        if(asyncMethod) {
            codeBlock.add("$T.onSuccessIfNotNull($L, $L);\n",
                    UmCallbackUtil.class,
                    daoMethod.getParameters().get(asyncParamIndex).getSimpleName(),
                    isVoid(resultType) ? "null" : "numDeleted")
                    .endControlFlow(")");
        }else if(!isVoid(resultType)) {
            codeBlock.add("return numDeleted;\n");
        }

        methodBuilder.addCode(codeBlock.build());
        daoBuider.addMethod(methodBuilder.build());
    }


    public void addQueryMethod(ExecutableElement daoMethod, TypeElement dbType, TypeSpec.Builder daoBuilder,
                               char identifierQuote) {
        //we need to run the query, find the columns, and then determine the appropriate setter methods to run
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(daoMethod);
        String querySql = daoMethod.getAnnotation(UmQuery.class).value();
        String querySqlTrimmedLower = querySql.toLowerCase().trim();

        boolean isUpdateOrDelete = false;
        if(querySqlTrimmedLower.startsWith("update") || querySqlTrimmedLower.startsWith("delete")) {
            isUpdateOrDelete = true;
        }

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
        List<Element> variableTypeElements = getMethodParametersAsElements(daoMethod);
        int asyncParamIndex = variableTypeElements.indexOf(umCallbackTypeElement);
        boolean asyncMethod = asyncParamIndex != -1;


        TypeMirror resultType;
        if(asyncMethod) {
            codeBlock.beginControlFlow("_db.getExecutor().execute(() -> ");
            DeclaredType declaredType = (DeclaredType)daoMethod.getParameters().get(asyncParamIndex)
                    .asType();
            resultType = declaredType.getTypeArguments().get(0);
        }else {
            resultType = daoMethod.getReturnType();
        }

        List<String> namedParams = getNamedParameters(querySql);
        String preparedStmtSql = querySql;
        for(String paramName : namedParams) {
            preparedStmtSql = preparedStmtSql.replaceAll(":" + paramName, "?");
        }

        boolean returnsList = false;
        boolean returnsArray = false;
        boolean returnsLiveData = false;

        Element resultTypeElement = processingEnv.getTypeUtils().asElement(resultType);


        if(resultTypeElement != null && resultTypeElement.equals(processingEnv
            .getElementUtils().getTypeElement(UmLiveData.class.getName()))) {

            List<String> tableList;
            try {
                Select select = (Select) CCJSqlParserUtil.parse(querySql);
                TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                tableList = tablesNamesFinder.getTableList(select);
                codeBlock.add("// Table names = " + Arrays.toString(tableList.toArray()))
                        .add("\n");
            }catch(JSQLParserException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod) +
                        " exception parsing query to determine tables");
                return;
            }

            returnsLiveData = true;
            DeclaredType declaredResultType = (DeclaredType)resultType;
            resultType = declaredResultType.getTypeArguments().get(0);
            resultTypeElement = processingEnv.getTypeUtils().asElement(resultType);
            codeBlock.beginControlFlow("return new $T<$T>() ", UmLiveDataJdbc.class, resultType)
                .beginControlFlow("")
                    .add("super.setDatabase(_db);\n")
                    .add("super.setTablesToMonitor(");
            boolean commaRequired = false;
            for(String tableName : tableList) {
                if(commaRequired)
                    codeBlock.add(", ");
                codeBlock.add("$S", tableName);
                commaRequired = true;
            }
            codeBlock.add(");\n");

            codeBlock.endControlFlow()
                .beginControlFlow("public $T fetchValue()", resultType);

            //add code here so that we generate the next stuff in the onFetch method of UmLiveDataJdbc
        }

        if(resultType.getKind().equals(TypeKind.ARRAY)) {
            ArrayType arrayType = (ArrayType)resultType;
            resultType = arrayType.getComponentType();
            resultTypeElement = processingEnv.getTypeUtils().asElement(resultType);
            codeBlock.add("$T[] result = null;\n", resultTypeElement);
            returnsArray = true;
        }else if(resultTypeElement != null && resultTypeElement.equals(processingEnv
                .getElementUtils().getTypeElement(List.class.getCanonicalName()))) {
            DeclaredType declaredResultType = (DeclaredType)resultType;

            resultType = declaredResultType.getTypeArguments().get(0);
            resultTypeElement = processingEnv.getTypeUtils().asElement(resultType);
            returnsList = true;
            codeBlock.add("$T<$T> result = new $T<>();\n", List.class, resultTypeElement,
                    ArrayList.class);
        }else if(!isVoid(resultType)){
            codeBlock.add("$T result = $L;\n", resultType, defaultValue(resultType));
        }

        TypeName returnTypeName = TypeName.get(resultType);
        boolean primitiveOrStringReturn = returnTypeName.isPrimitive()
                || returnTypeName.isBoxedPrimitive()
                || returnTypeName.equals(ClassName.get(String.class));

        if(!isUpdateOrDelete) {
            codeBlock.add("$T resultSet = null;\n", ResultSet.class);
        }

        codeBlock.add("try (\n").indent()
            .add("$T connection = _db.getConnection();\n", Connection.class)
            .add("$T stmt = connection.prepareStatement($S);\n", PreparedStatement.class, preparedStmtSql)
            .unindent().beginControlFlow(")");


        for(int i = 0; i < namedParams.size(); i++) {
            VariableElement paramVariableElement = null;
            for(VariableElement variableElement : daoMethod.getParameters()) {
                if(variableElement.getSimpleName().toString().equals(namedParams.get(i))) {
                    paramVariableElement = variableElement;
                    break;
                }
            }

            if(paramVariableElement == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod)
                        + " has no parameter named " + namedParams.get(i));
                return;
            }

            codeBlock.add("stmt.$L($L, $L);\n",
                    getPreparedStatementSetterMethodName(paramVariableElement.asType()),  i + 1,
                    paramVariableElement.getSimpleName().toString());
        }

        if(!isUpdateOrDelete) {
            addMapResultSetToValuesToCodeBlock(dbType, daoMethod, querySql, primitiveOrStringReturn,
                    returnsList, returnsArray, resultType, resultTypeElement, codeBlock);
        }else {
            TypeMirror updateResultTypeMirror = resultType;
            if(updateResultTypeMirror.getKind().equals(TypeKind.DECLARED)) {
                TypeElement updateResultTypeElement = (TypeElement)resultTypeElement;
                if(updateResultTypeElement != null && updateResultTypeElement.equals(
                        processingEnv.getElementUtils().getTypeElement("java.lang.Void"))) {
                    updateResultTypeMirror = processingEnv.getTypeUtils().getNoType(TypeKind.VOID);
                }
            }

            if(updateResultTypeMirror.getKind().equals(TypeKind.DECLARED)) {
                try {
                    updateResultTypeMirror = processingEnv.getTypeUtils().unboxedType(resultType);
                }catch(Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            formatMethodForErrorMessage(daoMethod) + " : " +
                        "update or delete query method must return void or integer type.");
                }
            }

            if(updateResultTypeMirror.getKind().equals(TypeKind.VOID)){
                codeBlock.add("int result = stmt.executeUpdate();\n");
            }else {
                codeBlock.add("result = stmt.executeUpdate();\n");
            }

            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableNames;
            try {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(preparedStmtSql);
                if(querySqlTrimmedLower.startsWith("update")){
                    tableNames = tablesNamesFinder.getTableList((Update)statement);
                }else if(querySqlTrimmedLower.startsWith("delete")) {
                    tableNames = tablesNamesFinder.getTableList((Delete)statement);
                }else {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            formatMethodForErrorMessage(daoMethod) + ": " +
                            " query was not select, expecting update or delete statement to " +
                            " determine table changes, found something else: " + preparedStmtSql);
                    throw new IllegalArgumentException("Query must be select, update, or delete");
                }

                codeBlock.beginControlFlow("if(result > 0)")
                        .add("_db.handleTablesChanged(");
                boolean commaRequired = false;
                for(String tableName : tableNames){
                    if(commaRequired)
                        codeBlock.add(", ");
                    codeBlock.add("$S", tableName);

                    commaRequired = true;
                }
                codeBlock.add(");\n").endControlFlow();
            }catch(JSQLParserException je) {
                messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod) +
                    " exception parsing update/delete query: " + je.getMessage());
            }



        }

        codeBlock.nextControlFlow("catch($T e)", SQLException.class)
            .add("e.printStackTrace();\n");

        if(!isUpdateOrDelete) {
            codeBlock.nextControlFlow("finally")
                .add("$T.closeQuietly(resultSet);\n", JdbcDatabaseUtils.class);
        }

        codeBlock.endControlFlow();

        if(asyncMethod) {
            codeBlock.add("$T.onSuccessIfNotNull($L, $L);\n",
                    UmCallbackUtil.class,
                    daoMethod.getParameters().get(asyncParamIndex).getSimpleName().toString(),
                    !isVoid(resultType) ? "result" : "null");
            codeBlock.endControlFlow(")");
        }else if(!daoMethod.getReturnType().getKind().equals(TypeKind.VOID)){
            codeBlock.add("return result;\n");
        }

        if(returnsLiveData) {
            //end the method and inner class
            codeBlock.endControlFlow().endControlFlow().add(";\n");
        }


        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());

    }

    /**
     * Generates a block of code that will convert the JDBC ResultSet from a SELECT query into the
     * desired result, which could be a single entity / POJO object, a list of objects, or String/
     * primitive result types.
     *
     * @param dbType TypeElement representing the database class
     * @param daoMethod ExecutableElement representing the DAO Method for which an implementation is
     *                  being generated
     * @param querySql The SQL query as per the UmQuery annotation
     * @param primitiveOrStringReturn true if the method returns a primitive result or string, false
     *                                otherwise (e.g. returns a POJO/entity)
     * @param returnsList true if the method returns a java.util.List, false otherwise
     * @param returnsArray true if the method returns an Array, false otherwise
     * @param resultType The TypeMirror representing the return type.
     * @param resultTypeElement Element representation of resultType
     * @param codeBlock CodeBlock.Builder the generated code will be added to
     */
    protected void addMapResultSetToValuesToCodeBlock(TypeElement dbType, ExecutableElement daoMethod,
                                                      String querySql,
                                                      boolean primitiveOrStringReturn,
                                                      boolean returnsList, boolean returnsArray,
                                                      TypeMirror resultType,
                                                      Element resultTypeElement,
                                                      CodeBlock.Builder codeBlock) {
        codeBlock.add("resultSet = stmt.executeQuery();\n");


        try(
            Connection dbConnection = nameToDataSourceMap.get(dbType.getQualifiedName().toString())
                    .getConnection();
            Statement stmt = dbConnection.createStatement();
            ResultSet results = stmt.executeQuery(querySql);
        ) {
            ResultSetMetaData metaData = results.getMetaData();
            if(primitiveOrStringReturn && metaData.getColumnCount() != 1) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        formatMethodForErrorMessage(daoMethod) +
                                ": returns a String or primitive. SQL must have 1 column only. " +
                                "found " + metaData.getColumnCount() + " columns");
            }


            if(returnsList) {
                codeBlock.beginControlFlow("while(resultSet.next())");
            } else if (returnsArray) {
                codeBlock.add("$T<$T> resultList = new $T<>();\n", ArrayList.class, resultType,
                        ArrayList.class)
                        .beginControlFlow("while(resultSet.next())");
            } else {
                codeBlock.beginControlFlow("if(resultSet.next())");
            }

            if(!primitiveOrStringReturn){
                addCreateNewEntityFromResultToCodeBlock((TypeElement)resultTypeElement,  daoMethod,
                        "entity", "resultSet", metaData, codeBlock);
            }else{
                codeBlock.add("$T entity = resultSet.get$L(1);\n", resultType,
                        getPreparedStatementSetterGetterTypeName(resultType));
            }

            if(returnsList) {
                codeBlock.add("result.add(entity);\n")
                        .endControlFlow();
            }else if(returnsArray) {
                codeBlock.add("resultList.add(entity);\n")
                        .endControlFlow()
                        .add("result = resultList.toArray(new $T[resultList.size()]);\n",
                                resultTypeElement);
            }else {
                codeBlock.add("result = entity;\n")
                        .endControlFlow();
            }

        } catch(SQLException e) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Exception generating query method for: " +
                            formatMethodForErrorMessage(daoMethod) + ": " + e.getMessage());
        }
    }

    /**
     * Generate a block of code that will initialize a new POJO / Entity from an JDBC ResultSet.
     * It will generate code along the lines of:
     *
     *  EntityType entityVariableName = new EntityType();
     *  entityVariableName.setUid(resultSetVariableName.getInt(1));
     *  entityVariableName.setName(resultSetVariableName.getString(2));
     *  entityVariableName.setEmbeddedObject(new EmbeddedObject());
     *  entityVariableName.getEmbeddedObject().setEmbeddedValue(resultSetVariableName.getInt(3));
     *
     *
     * @param entityElement The POJO / Entity that is being initialized
     * @param daoMethodElement The DAO method this is being generated for. Used to generate useful
     *                         error messages
     * @param entityVariableName The variable name to use for the entity being initialized
     * @param resultSetVariableName The variable name of the JDBC ResultSet from which values are
     *                              to be fetched
     * @param metaData JDBC Metadata used to find the columns that are returned by running the
     *                 query, so that they can be mapped to to setters.
     * @param codeBlock CodeBlock.Builder to add the generate code block to
     */
    private void addCreateNewEntityFromResultToCodeBlock(TypeElement entityElement,
                                                         ExecutableElement daoMethodElement,
                                                         String entityVariableName,
                                                         String resultSetVariableName,
                                                         ResultSetMetaData metaData,
                                                         CodeBlock.Builder codeBlock) {
        codeBlock.add("$T $L = new $T();\n", entityElement.asType(), entityVariableName,
                entityElement.asType());
        try {
            List<String> initializedEmbeddedObjects = new ArrayList<>();
            for(int i = 0; i < metaData.getColumnCount(); i++) {
                List<ExecutableElement> callChain = findSetterMethod(daoMethodElement,
                        metaData.getColumnLabel(i + 1), entityElement, new ArrayList<>(), true);
                if(callChain == null || callChain.isEmpty()) {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            formatMethodForErrorMessage(daoMethodElement) +
                                    " : Could not find setter for field: '" +
                                    metaData.getColumnLabel(i + 1) +"' on return class " +
                                    entityElement.getQualifiedName());
                    return;
                }

                CodeBlock.Builder setFromResultCodeBlock = CodeBlock.builder()
                        .add(entityVariableName);
                String callChainStr = "";

                for(int j = 0; j < callChain.size(); j++) {
                    ExecutableElement method = callChain.get(j);
                    callChainStr += "." + method.getSimpleName() + "()";
                    if(method.getSimpleName().toString().startsWith("set")) {
                        HashMap<String, Object> paramMap = new HashMap<>();
                        paramMap.put("setterName", method.getSimpleName().toString());
                        paramMap.put("resultSetGetter",
                                "get" + getPreparedStatementSetterGetterTypeName(method.getParameters()
                                        .get(0).asType()));
                        paramMap.put("resultSetVarName", resultSetVariableName);
                        paramMap.put("index", i + 1);
                        setFromResultCodeBlock.addNamed(".$setterName:L($resultSetVarName:L.$resultSetGetter:L($index:L));\n",
                                paramMap);
                    }else if(method.getSimpleName().toString().startsWith("get")) {
                        //this is an embedded field

                        //Check if the embedded field has been initialized with a blank new object.
                        // If not, we must do so to avoid a NullPointerException
                        if(!initializedEmbeddedObjects.contains(callChainStr)) {
                            CodeBlock.Builder initBuilder = CodeBlock.builder()
                                    .add(entityVariableName);
                            for(int k = 0; k < j; k++) {
                                initBuilder.add(".$L()", callChain.get(k).getSimpleName().toString());
                            }

                            initBuilder.add(".set$L(new $T());\n",
                                    callChain.get(j).getSimpleName().toString()
                                            .substring("get".length()),
                                    callChain.get(j).getReturnType());
                            codeBlock.add(initBuilder.build());
                            initializedEmbeddedObjects.add(callChainStr);
                        }

                        setFromResultCodeBlock.add(".$L()", method.getSimpleName().toString());
                    }
                }

                codeBlock.add(setFromResultCodeBlock.build());
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * For SQL with named parameters (e.g. "SELECT * FROM Table WHERE uid = :paramName") return a
     * list of all named parameters.
     *
     * @param querySql SQL that may contain named parameters
     * @return String list of named parameters (e.g. "paramName"). Empty if no named parameters are present.
     */
    private List<String> getNamedParameters(String querySql) {
        List<String> namedParams = new ArrayList<>();
        boolean insideQuote = false;
        boolean insideDoubleQuote = false;
        char lastC = 0;
        int startNamedParam = -1;
        for(int i  = 0; i < querySql.length(); i++) {
            char c = querySql.charAt(i);
            if(c == '\'' && lastC != '\\')
                insideQuote = !insideQuote;
            if(c == '\"' && lastC != '\\')
                insideDoubleQuote = !insideDoubleQuote;

            if(!insideQuote && !insideDoubleQuote) {
                if(c == ':'){
                    startNamedParam = i;
                }else if(Character.isWhitespace(c) && startNamedParam != -1){
                    //process the parameter
                    namedParams.add(querySql.substring(startNamedParam + 1, i ));
                    startNamedParam = -1;
                }else if(i == (querySql.length()-1) && startNamedParam != -1) {
                    namedParams.add(querySql.substring(startNamedParam + 1, i +1));
                    startNamedParam = -1;
                }
            }


            lastC = c;
        }

        return namedParams;
    }


    /**
     * Find the setter method for a given row name (e.g. "fieldName") on a given Java object (e.g.
     * POJO or Entity). This method will recursively check parent classes and any field with the
     * UmEmbedded annotation.
     *
     * @see UmEmbedded
     *
     * @param methodType "get" or "set
     * @param daoMethod The daoMethod currently being generated. Used to generate meaningful error
     *                  messages.
     * @param rowName The rowName as it was returned by the query
     * @param entityElement The java object to search to find an appropriate setter method
     * @param callChain The current call chain to get to this object.
     * @param checkEmbedded If true, look through any objects annotated @UmEmbedded
     *
     * @return A list of methods that represent the chain of methods that need to be called.
     *  For simple setters that are directly on the object itself, this is simply a list with the
     *  setter method. If the field resides in an embedded object, this includes the getter methods
     *  to reach the setter method e.g. getEmbeddedObject, setField.
     */
    private List<ExecutableElement> findGetterOrSetter(String methodType,
                                                       ExecutableElement daoMethod,
                                                       String rowName,
                                                       TypeElement entityElement,
                                                       List<ExecutableElement> callChain,
                                                       boolean checkEmbedded) {
        //go through the methods on this TypeElement to find a setter
        String targetMethodName = methodType + Character.toUpperCase(rowName.charAt(0)) +
                rowName.substring(1);

        for(Element subElement : entityElement.getEnclosedElements()) {
            if(subElement.getKind() != ElementKind.METHOD)
                continue;

            if(subElement.getSimpleName().toString().equals(targetMethodName)) {
                callChain.add((ExecutableElement)subElement);
                return callChain;
            }
        }


        //check @UmEmbedded objects
        if(checkEmbedded) {
            for(Element subElement : entityElement.getEnclosedElements()) {
                if(subElement.getKind().equals(ElementKind.FIELD)
                        && subElement.getAnnotation(UmEmbedded.class) != null) {
                    VariableElement varElement = (VariableElement)subElement;
                    String getterName = subElement.getSimpleName().toString();
                    getterName = "get" + Character.toUpperCase(getterName.charAt(0)) +
                            getterName.substring(1);

                    ExecutableElement getterMethod = null;
                    for(Element subElement2 : entityElement.getEnclosedElements()){
                        if(subElement2.getSimpleName().toString().equals(getterName)
                                && subElement2.getKind().equals(ElementKind.METHOD)
                                && ((ExecutableElement)subElement2).getParameters().isEmpty()){
                            getterMethod = (ExecutableElement)subElement2;
                            break;
                        }
                    }

                    if(getterMethod == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR,
                                formatMethodForErrorMessage(daoMethod) + ": " +
                                    entityElement.getQualifiedName() + "." + subElement.getSimpleName() +
                                    " is annotated with @UmEmbedded but has no getter method");
                        return null;
                    }

                    callChain.add(getterMethod);
                    List<ExecutableElement> retVal = findGetterOrSetter(methodType,daoMethod, rowName,
                            (TypeElement)processingEnv.getTypeUtils().asElement(varElement.asType()),
                            callChain, checkEmbedded);

                    if(retVal != null)
                        return retVal;
                    else
                        callChain.remove(getterMethod);
                }
            }
        }


        //Check parent classes
        if(entityElement.getSuperclass() != null
                && !entityElement.getSuperclass().getKind().equals(TypeKind.NONE)) {
            return findGetterOrSetter(methodType, daoMethod, rowName, (TypeElement) processingEnv.getTypeUtils()
                    .asElement(entityElement.getSuperclass()), callChain, checkEmbedded);
        }else {
            return null;
        }
    }

    private List<ExecutableElement> findSetterMethod(ExecutableElement daoMethod,
                                                     String rowName,
                                                     TypeElement entityElement,
                                                     List<ExecutableElement> callChain,
                                                     boolean checkEmbedded) {
        return findGetterOrSetter("set", daoMethod, rowName, entityElement, callChain,
                checkEmbedded);
    }

    private VariableElement findPrimaryKey(TypeElement entityType) {
        for(Element subElement : entityType.getEnclosedElements()) {
            if(!subElement.getKind().equals(ElementKind.FIELD))
                continue;

            if(subElement.getAnnotation(UmPrimaryKey.class) != null)
                return (VariableElement)subElement;
        }

        return null;
    }


    private void addSetPreparedStatementValueToCodeBlock(String preparedStatementVariableName,
                                           String entityVariableName, int index,
                                           VariableElement field, CodeBlock.Builder codeBlock) {
        boolean isPkAutoIncrementField = field.getAnnotation(UmPrimaryKey.class) != null
                && field.getAnnotation(UmPrimaryKey.class).autoIncrement();

        if(isPkAutoIncrementField) {
            String pkGetterMethod = field.getSimpleName().toString();
            pkGetterMethod = "get" + pkGetterMethod.substring(0, 1).toUpperCase() +
                    pkGetterMethod.substring(1);
            codeBlock.beginControlFlow("if($L.$L() == $L)", entityVariableName,
                    pkGetterMethod, defaultValue(field.asType()))
                    .add("$L.setObject($L, null);\n", preparedStatementVariableName, index)
                    .nextControlFlow("else");
        }
        codeBlock.add(preparedStatementVariableName);
        codeBlock.add(".$L(", getPreparedStatementSetterMethodName(field.asType()));


        //TODO: add error message here if there is no match
        codeBlock.add("$L, $L.get", index, entityVariableName);
        String fieldName = field.getSimpleName().toString();

        codeBlock.add(Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1))
                .add("());\n");

        if(isPkAutoIncrementField) {
            codeBlock.endControlFlow();
        }
    }

    private String getPreparedStatementSetterMethodName(TypeMirror variableType) {
        return "set" + getPreparedStatementSetterGetterTypeName(variableType);
    }

    /**
     * Get the suffix to use on get/set methods of PreparedStatement according to the type of variable.
     * Used when generating code such as preparedStatement.setString / preparedStatement.setInt etc.
     *
     * @param variableType Variable type to set/get on a prepared statement
     * @return suffix to use e.g. "Int" for integers, "String" for Strings, etc.
     */
    private String getPreparedStatementSetterGetterTypeName(TypeMirror variableType) {
        if(variableType.getKind().equals(TypeKind.INT)) {
            return "Int";
        }else if(variableType.getKind().equals(TypeKind.LONG)) {
            return "Long";
        }else if(variableType.getKind().equals(TypeKind.FLOAT)) {
            return "Float";
        }else if(variableType.getKind().equals(TypeKind.DOUBLE)) {
            return "Double";
        }else if(variableType.getKind().equals(TypeKind.DECLARED)) {
            String className = ((TypeElement)processingEnv.getTypeUtils().asElement(variableType))
                    .getQualifiedName().toString();
            switch(className) {
                case "java.lang.String":
                    return "String";
                case "java.lang.Integer":
                    return "Int";
                case "java.lang.Long":
                    return "Long";
                case "java.lang.Float":
                    return "Float";
                case "java.lang.Double":
                    return "Double";
            }
        }

        return null;
    }

    private String formatMethodForErrorMessage(ExecutableElement element) {
        return ((TypeElement)element.getEnclosingElement()).getQualifiedName() + "." +
            element.getSimpleName();
    }

    @Override
    protected void onDone() {
        super.onDone();

        if(dbTmpFile != null && dbTmpFile.exists()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "DbProcessorJdbc: " +
                    " Cleanup db tmp file: " + dbTmpFile.getAbsolutePath() +
                    " deleted: " + dbTmpFile.delete());
        }
    }
}