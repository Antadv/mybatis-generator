package com.somelogs.plugin;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Package Structure Such as:
 *
 * com.example.dao
 *             |_mbg
 *             |   |_UserMBGMapper
 *             |   |_GoodsMBGMapper
 *             |_custom
 *                 |_UserMapper
 *                 |_GoodsMapper
 *
 *  UserMapper extends UserMBGMapper {}
 *  GoodsMapper extends GoodsMBGMapper {}
 *  ...
 *
 * 这样做的好处是：
 *  - 表增删字段，只会覆盖 MBG 下面的文件（Client 和 Xml），不会影响 Custom 下面的内容，
 *    因为 Custom 中很有可能已经存在自定义 Statement
 *  - 默认生成的 Custom 的 mapper.xml 文件中定了 base_column，引用自 MBG 中的 Base_Column_List，
 *    这样 Custom 不用关心表结构变化
 *
 * @author LBG - 2019/4/19
 */
public class RenamePlugin extends PluginAdapter {

    private String replaceStr;
    private Pattern pattern;
    private boolean replaceFlag;

    @Override
    public boolean validate(List<String> warnings) {
        // searchString 指文件名中需要替换的字符串
        String searchStr = properties.getProperty("searchString");
        replaceStr = properties.getProperty("replaceString");
        boolean valid = StringUtility.stringHasValue(searchStr) && StringUtility.stringHasValue(replaceStr);

        if (valid) {
            pattern = Pattern.compile(searchStr);
            replaceFlag = true;
        } else {
            replaceStr = "";
        }
        return true;
    }

    /**
     * 注意：MBG 可以在 MBG_configuration.xml 中定义，做成动态设置
     */
    @Override
    public void initialized(IntrospectedTable table) {
        // Java client type
        String oldType = table.getMyBatis3JavaMapperType();
        if (replaceFlag) {
            Matcher matcher = pattern.matcher(oldType);
            oldType = matcher.replaceAll("MBG" + replaceStr);
        } else {
            oldType = oldType.replaceAll("Mapper", "MBGMapper");
        }
        int idx = oldType.lastIndexOf(".");
        if (idx > 0) {
            oldType = oldType.substring(0, idx) + ".mbg" + oldType.substring(idx);
        }
        table.setMyBatis3JavaMapperType(oldType);

        // xml mapper path
        String mapperName = table.getMyBatis3XmlMapperFileName();
        if (replaceFlag) {
            Matcher matcher = pattern.matcher(oldType);
            mapperName = matcher.replaceAll("MBG" + replaceStr);
        } else {
            mapperName = mapperName.replaceAll("Mapper", "MBGMapper");
        }
        table.setMyBatis3XmlMapperFileName(mapperName);
        String mapperPkg = table.getMyBatis3XmlMapperPackage() + File.separator + "mbg";
        table.setMyBatis3XmlMapperPackage(mapperPkg);
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable table) {
        List<GeneratedJavaFile> result = new ArrayList<>();
        GeneratedJavaFile g = null;
        for (GeneratedJavaFile f : table.getGeneratedJavaFiles()) {
            if (f.getFileName().contains("Dao") || f.getFileName().contains("Mapper")) {
                g = f;
                break;
            }
        }
        if (g != null) {
            String pkgName = g.getTargetPackage().replace("mbg", "custom");
            String className = g.getCompilationUnit().getType().getShortName().replace("MBG", "");
            Interface customInterface = new Interface(pkgName + "." + className);
            customInterface.setVisibility(JavaVisibility.PUBLIC);

            FullyQualifiedJavaType daoType = new FullyQualifiedJavaType(g.getCompilationUnit().getType().getFullyQualifiedName());
            customInterface.addSuperInterface(daoType);
            customInterface.addImportedType(daoType);
            String target = g.getTargetProject();
            String fileName = (target + File.separator + pkgName + "." + className).replace(".", File.separator);
            File file = new File(fileName + ".java");
            if (!file.exists()) {
                GeneratedJavaFile tmp = new GeneratedJavaFile(customInterface, target, context.getJavaFormatter());
                result.add(tmp);
            }
        }
        return result;
    }

    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable table) {
        List<GeneratedXmlFile> result = new ArrayList<>();
        GeneratedXmlFile mbgXml = table.getGeneratedXmlFiles().get(0);
        String projectName = mbgXml.getTargetProject();
        String packageName = mbgXml.getTargetPackage().replace("mbg", "custom");
        GeneratedJavaFile g = null;
        for (GeneratedJavaFile f : table.getGeneratedJavaFiles()) {
            if (f.getFileName().contains("Dao") || f.getFileName().contains("Mapper")) {
                g = f;
                break;
            }
        }
        if (g != null) {
            Document document = new Document(XmlConstants.MYBATIS3_MAPPER_CONFIG_PUBLIC_ID,
                                             XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);
            XmlElement root = new XmlElement("mapper");
            String mbgMapperReference = g.getTargetPackage() + "." + g.getFileName().replace(".java", "");

            String className = g.getFileName().replace("MBG", "");
            String fileName = g.getFileName().replace("MBG", "").replace(".java", ".xml");
            String pkgName = g.getTargetPackage().replace(".mbg", ".custom");
            String mapperReference = pkgName + "." + className.replace(".java", "");
            Attribute attribute = new Attribute("namespace", mapperReference);
            root.addAttribute(attribute);
            document.setRootElement(root);

            // 在 custom 中添加 base_column, 引用 MBG 中的 Base_Column_List
            XmlElement baseColumnEle = new XmlElement("sql");
            Attribute baseColumnIdAttr = new Attribute("id", "base_column");
            baseColumnEle.addAttribute(baseColumnIdAttr);

            XmlElement baseColumnRef = new XmlElement("include");
            Attribute baseColumnIdRefVal = new Attribute("refid", mbgMapperReference + ".Base_Column_List");
            baseColumnRef.addAttribute(baseColumnIdRefVal);
            baseColumnEle.addElement(baseColumnRef);
            root.addElement(baseColumnEle);

            // 在 custom 中添加 base_column_prefix, 引用 MBG 中的 base_column_prefix_List
            XmlElement baseColumnPrefixEle = new XmlElement("sql");
            Attribute baseColumnPrefixIdAttr = new Attribute("id", "base_column_prefix");
            baseColumnPrefixEle.addAttribute(baseColumnPrefixIdAttr);

            XmlElement baseColumnPrefixRef = new XmlElement("include");
            Attribute baseColumnPrefixIdRefVal = new Attribute("refid", mbgMapperReference + ".Base_Column_Prefix_List");
            baseColumnPrefixRef.addAttribute(baseColumnPrefixIdRefVal);
            baseColumnPrefixEle.addElement(baseColumnPrefixRef);
            root.addElement(baseColumnPrefixEle);

            // 在 custom 中添加 baseResultMap, 继承 MBG 中的 BaseResultMap
            XmlElement baseResultMapEle = new XmlElement("resultMap");
            Attribute baseResultMapId = new Attribute("id", "baseResultMap");
            baseResultMapEle.addAttribute(baseResultMapId);
            Attribute baseResultMapType = new Attribute("type", getFullEntityClassName(table));
            baseResultMapEle.addAttribute(baseResultMapType);
            Attribute baseResultMapExtend = new Attribute("extends", mbgMapperReference + ".BaseResultMap");
            baseResultMapEle.addAttribute(baseResultMapExtend);
            root.addElement(baseResultMapEle);

            // com.xx.xx transfer to com\xx\xx
            String pathName = packageName.replace(".", File.separator);
            File file = new File(projectName + File.separator + pathName + File.separator + fileName);
            if (!file.exists()) {
                GeneratedXmlFile gxf = new GeneratedXmlFile(document, fileName, packageName,
                        projectName, false, context.getXmlFormatter());
                result.add(gxf);
            }
        }
        return result;
    }

    private String getFullEntityClassName(IntrospectedTable table) {
        return table.getGeneratedJavaFiles().get(0).getCompilationUnit().getType().getFullyQualifiedName();
    }
}
