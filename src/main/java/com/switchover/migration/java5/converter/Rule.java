package com.switchover.migration.java5.converter;

import com.github.javaparser.ast.CompilationUnit;

@FunctionalInterface
public interface Rule {
    Boolean apply(CompilationUnit cu);
}
