## 1. Background

The vSphere Automation SDKs are very powerful client libraries for accessing new vSphere features such as using content libraries, and existing features such as virtual machine configuration and management. We are building a VM Operator (for Kubernetes) to show the powers the vSphere Automation SDK to intergrate the vSphere virtualization technology into your cloud platforms.

The VM Operator is a advanced feature of WCP (VM Operator: Engineering) which is using kubernetes standard protocol (kubectl) to manage vsphere virtual machines in the Kubernetes world. The VM Operator has two parts: the APIs are open sourced at (https://github.com/vmware-tanzu/vm-operator-api/tree/master/api/v1alpha1), the implementation is at internal gitlab.

This sample application is using the open sourced VM Operator API to show the simplified version of the VM Operator in WCP.

## 2. Libraries and languages

- vSphere Automation Java SDK 7.0 (https://code.vmware.com/web/sdk/7.0/vsphere-automation-java)
- fabric8 java client for Kubernets (https://github.com/fabric8io/kubernetes-client)

## 3. CRDs and Controllers
- virtualmachine
- virtualmachineclass
- virtualmachineimage
- contentlibrary
- virtualmachinesetresourcepolicy

## 4. Quick Started Guide
- [How to Setup][ba122188]
- [How to Use][8634b678]

  [ba122188]: Docs/GetStarted.md "Get Started"
  [8634b678]: Docs/HowToUse.md "How to Use"

## 5. Q & A
1. **The vSphere Automation SDKs already contain sample code and applications, why another one?**

  This demo application is more comprehensive and combing multiple APIs into one complicated application. It is more like real one. Also it is the first time to show the vsphere SDK usages in the Kubenetes platform.

2. **Why choose the Kubernetes Operator as the demo application?**

  Kubernetes is getting more and more popular to run all kinds of workloads. There are some cases that our users run the SDKs in the Kubernetes platform.

3. **What's differences between the original VM Operator and this one?**

  This demo application is simplified version of the original VM Operator. It has only 5 controllers (virtualmachineimage, virtualmachineclass ,virtualmachine,contentlibrary and virtualmachinesetresourcepolicy), doesn't have controllers of (virtualmachineservice, volume and others from the original VM Operator).  This demo application could be run at the any Kubernetes Clusters (including vsphere 6 and 7 or non-vSphere clusters).  The original VM Operator needs WCP and Nsxt environments.

4. **What's the usage of this demo  application?**

  CMP (Cloud Management Platform) is the target usage for this demo application.

5. **Why using Java instead of Go?**

  Go client is the best Kubernetes client to build controllers and operators. But the vSphere Automation SDKs don't support go language. The java clients (like fabric) are also matured enough to build Kubernetes Operators.

6. **Do you copy  some code from the original VM Operator?**

  No, the original VM Operator is written in Go, this application is in Java.
