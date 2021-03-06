package com.cloudoer.config.configconsole.utils;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
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
     * @throws Exception
     */
    public static void yaml2zookeeper(String filePath, String applicationName, String moduleName,String profile) throws Exception {
        Yaml yaml = new Yaml();
        Map ret = yaml.load(YamlUtil.class.getClassLoader()
                .getResourceAsStream(filePath));

        //新增父项目名，项目结构为 /cloudoer/父项目名/子模块名/application,profile/配置数据
        String defaultRootNode = "/cloudoer/";
        createNode(ret.entrySet(), defaultRootNode + applicationName +"/"+ moduleName
                + "/application" + (StringUtils.isEmpty(profile) ? "" : ("," + profile)));
    }


    /**
     * 根据yml文件创建zookeeper node
     *
     * @param entrySet   一个节点下的Map.entry集合
     * @param prefixPath 前缀，云朵约定配置中心都在cloudoer根目录下
     * @throws Exception
     */
    private static void createNode(Set<Map.Entry> entrySet, String prefixPath) throws Exception {

        for (Map.Entry temp : entrySet) {

            if (temp.getValue() instanceof String || temp.getValue() instanceof Integer || temp.getValue() instanceof Boolean) {
                log.info("node:{}, value:{}", temp.getKey(), temp.getValue());
                ZkUtil.getInstance().getClient().create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT)
                        .forPath(prefixPath + "/" + temp.getKey(), String.valueOf(temp.getValue()).getBytes());
            } else if (temp.getValue() instanceof Map) {
                String newPath = prefixPath + (("/".equals(prefixPath)) ? "" : "/") + temp.getKey();
                createNode(((Map) temp.getValue()).entrySet(), newPath);
            } else {
                throw new IllegalStateException("unknown value type.");
            }
        }
    }


    public static void main(String[] args) throws Exception {
        //dump2Json("bdp/config-zookeeper", "dev");
        //yaml2zookeeper("application.yml","bbb","bbb-module","dev");
        import2Zk("{\"/p1\":\"1111\"}","{\"/p1\":\"1\",\"/p2\":\"2\"}");
    }

    private static String dump2Json(String projectName, String profile) throws Exception {

        String defaultRootNode = "/cloudoer/" + projectName + "/";

        List<String> strings = ZkUtil.getInstance().getClient().getChildren().forPath(defaultRootNode + "application," + profile);
        Map<String, String> result = new HashMap<>();
        dg(defaultRootNode + "application," + profile, strings, result);

        System.out.println(new Gson().toJson(result));
        return new Gson().toJson(result);
    }


    private static void dg(String prefix, List<String> nodes, Map<String, String> result) throws Exception {

        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        for (String node : nodes) {

            log.info("node:{}", node);
            List<String> childrens = ZkUtil.getInstance().getClient().getChildren().forPath(prefix + node);

            if (null == childrens || childrens.size() == 0) {
                result.put(prefix + node, new String(ZkUtil.getInstance().getClient().getData().forPath(prefix + node)));
            } else {
                dg(prefix + node, childrens, result);
            }
        }
    }

    /**
     * 把数据库中某个版本的data快照，导入zk中
     *
     * @param targetJson
     * @param sourceJson
     * @throws Exception
     */
    private static void import2Zk(String targetJson, String sourceJson) throws Exception {

        Map<String, String> targetMap = new Gson().fromJson(targetJson, Map.class);
        Map<String, String> sourceMap = new Gson().fromJson(sourceJson, Map.class);

        for (Map.Entry<String, String> temp : targetMap.entrySet()) {
            String key = temp.getKey();
            String value = temp.getValue();

            Stat stat = ZkUtil.getInstance().getClient().checkExists().forPath(key);

            if (null == stat) {
                ZkUtil.getInstance().getClient().create().creatingParentsIfNeeded().forPath(key, value.getBytes());
            } else {
                byte[] bytes = ZkUtil.getInstance().getClient().getData().forPath(key);

                if (!new String(bytes).equals(value)) {
                    ZkUtil.getInstance().getClient().setData().forPath(key, value.getBytes());
                }
            }
            sourceMap.remove(key);
        }

        for (Map.Entry<String, String> temp : sourceMap.entrySet()) {

            String key = temp.getKey();
            ZkUtil.getInstance().getClient().delete().forPath(key);
        }
    }
}
