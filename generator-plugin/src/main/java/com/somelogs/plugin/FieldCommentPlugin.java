package com.somelogs.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * 属性添加注释：column: remark
 * setter、getter 无注释
 *
 * @author LBG - 2019/4/19
 */
public class FieldCommentPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field,
                                       TopLevelClass topLevelClass,
                                       IntrospectedColumn column,
                                       IntrospectedTable table,
                                       ModelClassType modelClassType) {
        List<String> docLines = field.getJavaDocLines();
        docLines.clear(); // 清除掉字段注释，不管 suppressAllComments 的值。
        docLines.add(0, "/**");
        docLines.add(1, " * " + column.getActualColumnName() + ": " + column.getRemarks());
        docLines.add(2, " */");
        return super.modelFieldGenerated(field, topLevelClass, column, table, modelClassType);
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass,
                                              IntrospectedColumn column,
                                              IntrospectedTable table,
                                              ModelClassType modelClassType) {
        method.getJavaDocLines().clear();
        return super.modelGetterMethodGenerated(method, topLevelClass, column, table, modelClassType);
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        method.getJavaDocLines().clear();
        return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }
}
