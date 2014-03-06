/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.jmh.generators.bytecode;

import org.objectweb.asm.ClassReader;
import org.openjdk.jmh.generators.source.ClassInfo;
import org.openjdk.jmh.generators.source.GeneratorSource;
import org.openjdk.jmh.generators.source.MetadataInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ASMGeneratorSource implements GeneratorSource {

    private final ClassInfoRepo classInfos;
    private final File targetDir;
    private final File targetSourceDir;
    private final List<SourceError> sourceErrors;

    public ASMGeneratorSource(File targetDir, File targetSourceDir) {
        this.targetDir = targetDir;
        this.targetSourceDir = targetSourceDir;
        this.sourceErrors = new ArrayList<SourceError>();
        this.classInfos = new ClassInfoRepo();
    }

    public void processClasses(Collection<File> classFiles)  throws IOException {
        for (File f : classFiles) {
            processClass(f);
        }
    }

    public void processClass(File classFile) throws IOException {
        final ASMClassInfo ci = new ASMClassInfo(classInfos);
        FileInputStream fos = null;
        try {
            fos = new FileInputStream(classFile);
            ClassReader reader = new ClassReader(fos);
            reader.accept(ci, 0);
            classInfos.put(ci.getIdName(), ci);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    @Override
    public Collection<ClassInfo> getClasses() {
        return classInfos.getInfos();
    }

    @Override
    public ClassInfo resolveClass(String className) {
        return classInfos.get(className);
    }

    @Override
    public Writer newResource(String resourcePath) throws IOException {
        String pathName = targetDir.getAbsolutePath() + "/" + resourcePath;
        new File(pathName.substring(0, pathName.lastIndexOf("/"))).mkdirs();
        return new FileWriter(new File(pathName));
    }

    @Override
    public Writer newClass(String className) throws IOException {
        String pathName = targetSourceDir.getAbsolutePath() + "/" + className.replaceAll("\\.", "/");
        new File(pathName.substring(0, pathName.lastIndexOf("/"))).mkdirs();
        return new FileWriter(new File(pathName + ".java"));
    }

    @Override
    public void printError(String message) {
        sourceErrors.add(new SourceError(message));
    }

    @Override
    public void printError(String message, MetadataInfo element) {
        sourceErrors.add(new SourceElementError(message, element));
    }

    @Override
    public void printError(String message, Throwable throwable) {
        sourceErrors.add(new SourceThrowableError(message, throwable));
    }

    public boolean hasErrors() {
        return !sourceErrors.isEmpty();
    }

    public Collection<SourceError> getErrors() {
        return sourceErrors;
    }

}
