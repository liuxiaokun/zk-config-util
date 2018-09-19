package com.cloudoer.config.configconsole.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Set;

/**
 * @author liuxiaokun
 * @version 0.0.1
 * @since 2018/9/18
 */
@Slf4j
public class YamlUtil {


    /**
     * 用于把yml文件中的配置数据导入zookeeper分布式配置中心
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Yaml yaml = new Yaml();
        Map ret = yaml.load(YamlUtil.class.getClassLoader()
                .getResourceAsStream("application.yml"));

        String defaultRootNode = "/config/";
        String profile = "dev";
        String applicationName = "config-zookeeper";
        createNode(ret.entrySet(), defaultRootNode + applicationName
                + "/application" + (StringUtils.isEmpty(profile) ? "" : ("," + profile)));
    }


    /**
     * 根据yml文件创建zookeeper node
     *
     * @param entrySet   一个节点下的Map.entry集合
     * @param prefixPath 前缀，云朵约定配置中心都在config根目录下
     * @throws Exception
     */
    private static void createNode(Set<Map.Entry> entrySet, String prefixPath) throws Exception {

        for (Map.Entry temp : entrySet) {

            if (temp.getValue() instanceof String || temp.getValue() instanceof Integer || temp.getValue() instanceof Boolean) {
                log.info("node:{}, value:{}", temp.getKey(), temp.getValue());
                new ZkUtil().init().create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT)
                        .forPath(prefixPath + "/" + temp.getKey(), String.valueOf(temp.getValue()).getBytes());
            } else if (temp.getValue() instanceof Map) {
                String newPath = prefixPath + (("/".equals(prefixPath)) ? "" : "/") + temp.getKey();
                createNode(((Map) temp.getValue()).entrySet(), newPath);
            } else {
                throw new IllegalStateException("unknown value type.");
            }
        }
    }
}
