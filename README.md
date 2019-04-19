# 快速上手

下载项目，首先稍微修改 generator-demo 模块中 /src/main/resources/MGB_configuration.xml

## 修改数据源

```xml
<!-- 生发环境数据库配置:修改配置 -->
<jdbcConnection driverClass="com.mysql.jdbc.Driver"
                connectionURL="jdbc:mysql://localhost:3306/test"
                userId="root" password="8089">
</jdbcConnection>
```

## 修改 Package 和文件生成路径

主要两个位置：

- targetPackage：包名
- targetProject：文件生成的绝对路径

```xml
<!-- Entity -->
<javaModelGenerator targetPackage="com.somelogs.entity"
                    targetProject="D:/imant/project/generator/generator-demo/src/main/java">
    <property name="enableSubPackages" value="true"/>
    <property name="trimStrings" value="true"/>
</javaModelGenerator>
<!-- Mapper Xml -->
<sqlMapGenerator targetPackage="com.somelogs.dao"
                 targetProject="D:/imant/project/generator/generator-demo/src/main/resources">
    <property name="enableSubPackages" value="true"/>
</sqlMapGenerator>
<!-- Mapper Interface -->
<javaClientGenerator type="XMLMAPPER" targetPackage="com.somelogs.dao"
                     targetProject="D:/imant/project/generator/generator-demo/src/main/java">
    <property name="enableSubPackages" value="true"/>
</javaClientGenerator>
```

## 修改表信息

指定表名（tableName）和生成的实体名称（domainObjectName）

```xml
<!--生成对应表和实体类名称   根据需要修改tableName    domainObjectName-->
<table tableName="t_goods" domainObjectName="Goods" enableCountByExample="false"
       enableUpdateByExample="false" enableDeleteByExample="false"
       enableSelectByExample="false" selectByExampleQueryId="false"
       enableInsert="true" enableUpdateByPrimaryKey="true"
       enableDeleteByPrimaryKey="true">
    <property name="useActualColumnNames" value="false"/>
    <columnOverride column="LONG_VARCHAR_FIELD" jdbcType="VARCHAR"/>
</table>
```

## 执行

上面修改完后，执行`MyBatisCodeGenerator`类中的 main 方法即可。

# 项目结构

生成项目结构如下

```
dao
   |_mbg
   |   |_GoodsMBGMapper
   |   |_UserMBGMapper
   |_custom
       |_GoodsMapper
       |_UserMapper
```
之所以要分为 MBG 和 custom，是因为表结构变化，Mapper Xml 会直接被覆盖，那之前自定义的 Statement 全得丢失。
现在的做法是把自定义的接口放在 Custom 中。重新生成的只覆盖 MBG 中的文件，不会影响 Custom 中的内容。

# 插件

自定义了有两个插件

- RenamePlugin：把文件分文 MBG 和 Custom 两类，Custom 继承自 MBG。
- FieldCommentPlugin：生成的实体中字段添加注释，系统自动生成的注释一丢废话。。。