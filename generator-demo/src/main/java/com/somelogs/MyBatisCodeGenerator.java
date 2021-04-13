package com.somelogs;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 本类不用修改，运行就行
 * 具体配置，请修改 src/test/resources/MBG_configuration.xml
 */
public class MyBatisCodeGenerator {

    public static void main(String[] args) throws Exception {
        System.out.println("+++++++++generate begin++++++++++");
        List<String> warnings = new ArrayList<String>();
        //String url = MyBatisCodeGenerator.class.getResource("/MBG_configuration.xml").getFile();
        String url = MyBatisCodeGenerator.class.getResource("/fcrm_MBG_configuration.xml").getFile();
        File configFile = new File(url);
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(configFile);
        DefaultShellCallback callback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
        System.out.println("+++++++++generate writer end+++++++++++");
    }
}
