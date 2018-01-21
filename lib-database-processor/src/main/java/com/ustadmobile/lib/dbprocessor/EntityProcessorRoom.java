package com.ustadmobile.lib.dbprocessor;

import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmRelation;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by mike on 1/20/18.
 */

public class EntityProcessorRoom {

    public static void processorDir(File inDir, File outDir) throws IOException{
        JavaClassSource classSource = null;
        if(!outDir.exists())
            outDir.mkdirs();

        for(File srcFile : inDir.listFiles()) {
            File outFile = new File(outDir, srcFile.getName());
            JavaType parsedSource = Roaster.parse(srcFile);

            if(!(parsedSource instanceof JavaClassSource)) {
                Files.write(Paths.get(outFile.toURI()), parsedSource.toString().getBytes("UTF-8"));
                continue;
            }


            classSource = (JavaClassSource)parsedSource;

            if(classSource.hasAnnotation(UmEntity.class)) {
                classSource.addAnnotation("android.arch.persistence.room.Entity");
            }

            //iterate over fields
            for(FieldSource fieldSource: classSource.getFields()) {
                if(fieldSource.hasAnnotation(UmPrimaryKey.class)) {
                    AnnotationSource umAnnotation = fieldSource.getAnnotation(UmPrimaryKey.class);
                    AnnotationSource pkAnnotation = fieldSource
                            .addAnnotation("android.arch.persistence.room.PrimaryKey");

                    String autoIncrementVal = umAnnotation.getLiteralValue("autoIncrement");
                    if(autoIncrementVal != null && autoIncrementVal.equals("true"))
                        pkAnnotation.setLiteralValue("autoGenerate", "true");
                }

                if(fieldSource.hasAnnotation(UmRelation.class)) {
                    AnnotationSource umRelationAnnotation = fieldSource.getAnnotation(UmRelation.class);
                    AnnotationSource roomRelationAnnotation = fieldSource
                            .addAnnotation("android.arch.persistence.room.Relation");
                    roomRelationAnnotation.setStringValue("parentColumn",
                            umRelationAnnotation.getStringValue("parentColumn"));
                    roomRelationAnnotation.setStringValue("entityColumn",
                            umRelationAnnotation.getStringValue("entityColumn"));
                }
            }

            String srcToWrite = "/* GENERATED FILE : DO NOT EDIT */\n\n" + classSource.toString();
            Files.write(Paths.get(outFile.toURI()), srcToWrite.getBytes("UTF-8"));
        }
    }

    public static void main(String[] args) throws Exception{
        processorDir(new File(args[0]), new File(args[1]));
    }

}