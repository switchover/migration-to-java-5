package com.switchover.migration.java5.converter.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.switchover.migration.java5.converter.Rule;

public class EnumKeywordRule implements Rule {

    @Override
    public Boolean apply(CompilationUnit cu) {
        boolean modified = false;

        // 선언 부분
        for (VariableDeclarator varDecl : cu.findAll(VariableDeclarator.class)) {
            if ("enum".equals(varDecl.getNameAsString())) {
                modified = true;
                varDecl.setName("enums");
            }
        }

        // 이름 참조 부분
        for (NameExpr nameExpr : cu.findAll(NameExpr.class)) {
            if ("enum".equals(nameExpr.getNameAsString())) {
                modified = true;
                nameExpr.setName("enums");
            }
        }

        // 필드 참조 부분
        for (FieldAccessExpr fieldAccessExpr : cu.findAll(FieldAccessExpr.class)) {
            if ("enum".equals(fieldAccessExpr.getNameAsString())) {
                modified = true;
                fieldAccessExpr.setName("enums");
            }
        }

        return modified;
    }
}
