1. Install Helm 3 from the website. On Mac, just need to run with:

  ```
  $ brew install helm

  $ helm version
  version.BuildInfo{Version:"v3.1.0", GitCommit:"b29d20baf09943e134c2fa5e1e1cab3bf93315fa", GitTreeState:"clean", GoVersion:"go1.13.8"}
  ```
2. Make sure your kubectl can connect your kubernetes cluster.

  ```
  $ kubectl cluster-info
  Kubernetes master is running at https://10.117.233.3:6443
  KubeDNS is running at https://10.117.233.3:6443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

  ```

3. Download the [Helm Chart][71855504] and unzip it to a directory as you wish

  ```
  $ tar -xvzf vmoperator-0.1.0.tgz
  x vmoperator/Chart.yaml
  x vmoperator/values.yaml
  x vmoperator/templates/NOTES.txt
  x vmoperator/templates/_helpers.tpl
  x vmoperator/templates/configmaps.yaml
  x vmoperator/templates/deployment.yaml
  x vmoperator/templates/rolebindings.yaml
  x vmoperator/templates/roles.yaml
  x vmoperator/templates/secrets.yaml
  x vmoperator/templates/serviceaccount.yaml
  x vmoperator/.helmignore
  x vmoperator/crds/vmoperator.wangyu.cpbu.vmware_contentlibraries.yaml
  x vmoperator/crds/vmoperator.wangyu.cpbu.vmware_virtualmachineclasses.yaml
  x vmoperator/crds/vmoperator.wangyu.cpbu.vmware_virtualmachineimages.yaml
  x vmoperator/crds/vmoperator.wangyu.cpbu.vmware_virtualmachines.yaml
  x vmoperator/crds/vmoperator.wangyu.cpbu.vmware_virtualmachinesetresourcepolicies.yaml
  ```
  
4. If your cluster cannot access the internet, you should pull the image to your local desktop and push it to your private image repos:

  ```
  $ docker pull yuwang881/vmoperator
  $ docker tag yuwang881/vmoperator /your/repo/url/tag
  $ docker push /your/repo/url/tag
  ```
5.Create a config file with whatever name you want, modify the content for your platform.

  ```
  $ more config.yaml

  vc:
    datacenter: VCP
    cluster: WCP
    vcurl: pek2-skevin-vc01.eng.vmware.com
    username: administrator@vsphere.local
    password: Admin!23

  image:
    repository: yuwang881/vmoperator
  ```

6. Create a new namespace for this vmoperator:

  ```
  $ kubectl create namespace vmoperator
  ```

7. Install the Helm Chart:

  ```
  $ helm install -f config.yaml vmoperator ./vmoperator --namespace vmoperator
  NAME: vmoperator
  LAST DEPLOYED: Thu Sep 10 12:56:06 2020
  NAMESPACE: vmoperator
  STATUS: deployed
  REVISION: 1
  TEST SUITE: None
  NOTES:
  To test if the VMOperator is running:
    kubectl --namespace vmoperator get pods
  ```

8. Monitor the Kubernetes to make sure that vmoperator pod is started without errors:

  ```
  $ kubectl --namespace vmoperator get pods
  NAME                         READY   STATUS    RESTARTS   AGE
  vmoperator-7d687f6cc-pxxhx   1/1     Running   0          119s
  ```


[71855504]: ../deployment/vmoperator-0.1.0.tgz "Helm Chart"
