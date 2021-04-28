package vmware.CPBU.utils;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Base64;

public class KubernetesHelper {

    private static String getStringValueFromConfigMap(
            String namespace,
            String configMapName,
            String key) {
        if (key.equals("datacenter")) return "VCP";
        if (key.equals("cluster")) return "WCP";
        if (key.equals(Constants.USER_DATA))
            return "I2Nsb3VkLWNvbmZpZwoKd3JpdGVfZmlsZXM6CiAgLSBwYXRoOiAvdG1wL3dhbmd5dS50ZXN0CiAg\n"
                + "ICBjb250ZW50OiB8CiAgICAgIC0tLQogICAgICBsaW5lMToKICAgICAgICBsaW5lMjoK";
        return null;
    }

    public static String getStringValueFromConfigMap(
            KubernetesClient client,
            String namespace,
            String configMapName,
            String key) {

        String value="";
        if (namespace == null || namespace.trim().isEmpty())
            namespace = "default";
        ConfigMap configMap = client
                .configMaps()
                .inNamespace(namespace)
                .withName(configMapName)
                .get();
        if (configMap != null) {
            value = configMap.getData().get(key);
        }
        return value;
    }


    private static String getStringValueFromSecret(
            String namespace,
            String secretName,
            String key) {

        if(key.equals("vcurl")) return "pek2-skevin-vc01.eng.vmware.com";
        if(key.equals("username")) return "administrator@vsphere.local";
        if(key.equals("password")) return "Admin!23";
        return null;
    }

    public static String getStringValueFromSecret(
            KubernetesClient client,
            String namespace,
            String secretName,
            String key) {

        String value="";
        if (namespace == null || namespace.trim().isEmpty())
            namespace = "default";
        Secret secret = client
                .secrets()
                .inNamespace(namespace)
                .withName(secretName)
                .get();
        if (secret != null) {
            value = secret.getData().get(key);
        }
        
        return new String(Base64.getDecoder().decode(value));
    }


    public static String getStringValueFromConfigMap(String key) {
        String namespace = Constants.NAMESPACE_NAME;
        String configmapName = Constants.CONFIGMAP_NAME;
        return getStringValueFromConfigMap(namespace,configmapName,key);
    }

    public static String getStringValueFromSecret(String key) {
        String namespace = Constants.NAMESPACE_NAME;
        String secretName = Constants.SECRET_NAME;
        return getStringValueFromSecret(namespace,secretName,key);
    }

    public static String getUserData(String namespace,String configMapName) {
        return getStringValueFromConfigMap(namespace,configMapName,Constants.USER_DATA);
    }


}
