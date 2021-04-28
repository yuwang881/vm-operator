package vmware.CPBU.test;



import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import vmware.CPBU.utils.Constants;
import vmware.CPBU.utils.KubernetesHelper;

import java.util.Base64;

public class KubernetesTest {
    public static void test() throws Exception {
        KubernetesClient k8sCoreClient = new DefaultKubernetesClient();
        String cluster = k8sCoreClient.getMasterUrl().toString();
        String namespace = k8sCoreClient.getNamespace();
        System.out.println("Get K8S Cluster info: ");
        System.out.println("Master URL: "+ cluster);
        System.out.println("Namespace: "+namespace);

        if (namespace == null) {
            System.out.println("Setting the namespace to vmoperator");
            namespace = "vmoperator";
        }

        String datacenter = KubernetesHelper.getStringValueFromConfigMap(k8sCoreClient,namespace, Constants.CONFIGMAP_NAME,"datacenter");
        String clusterStr = KubernetesHelper.getStringValueFromConfigMap(k8sCoreClient,namespace, Constants.CONFIGMAP_NAME,"cluster");
        String initData = KubernetesHelper.getStringValueFromConfigMap(k8sCoreClient,"default","ubuntu-cloud-init","guestinfo.userdata");
        String vcurl = KubernetesHelper.getStringValueFromSecret(k8sCoreClient,namespace,Constants.SECRET_NAME,"vcurl");
        String username = KubernetesHelper.getStringValueFromSecret(k8sCoreClient,namespace,Constants.SECRET_NAME,"username");
        String passwd = KubernetesHelper.getStringValueFromSecret(k8sCoreClient,namespace,Constants.SECRET_NAME,"password");

        System.out.println("DATACENTER: "+datacenter);
        System.out.println("CLUSTER: "+clusterStr);
        System.out.println("INITDATA: "+initData);
        System.out.println("VCURL: "+new String(Base64.getDecoder().decode(vcurl)));
        System.out.println("USERNAME: "+new String(Base64.getDecoder().decode(username)));
        System.out.println("PASSWORD: "+new String(Base64.getDecoder().decode(passwd)));
    }
}
