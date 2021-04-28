/*
 * Copyright (c) 2019. VMware, Inc. All rights reserved. VMware Confidential.
 */

package vmware.CPBU.exceptions;


import vmware.CPBU.utils.Quantity;

/** @author <a href="mailto:yuwa@vmware.com">Yu Wang</a> */
public class VMOperatorException extends Exception {
  private ExceptionCode ecode;

  public VMOperatorException(ExceptionCode code, String... message) {
    super(String.format(code.pattern, message));
    this.ecode = code;
  }

  public int getCode() {
    return this.ecode.getCode();
  }

  public enum ExceptionCode {

    // Application Exception
    CLUSTER_NOTFOUNT_ERROR(101, "The cluster is not found!"),
    RESOURCEPOOL_DUPLICATE_ERROR(102, "The resource pool exists!"),
    DATACENTER_NOTFOUNT_ERROR(103, "The datacenter is not found!"),
    VMFOLDER_NOTFOUNT_ERROR(104, "The vmfolder is not found!"),
    QUANTITY_PARSE_ERROR(105, "Cannot parse the quantity to a long value!"),
    VM_CREATION_PROPS_ERROR(106, "Some props for VM creation are missed!"),
    VM_NOTFOUNT_ERROR(107, "The VM is not found!"),
    VMCLASS_NOTFOUNT_ERROR(108, "The VM Class is not found!"),
    VMIMAGE_NOTFOUNT_ERROR(109, "The VM Image is not found!"),
    DATASTORE_NOTFOUNT_ERROR(110, "The DataStore is not found!"),
    VMNAME_EXIST_ERROR(111,"The VM Name exists already!"),
    RESOURCEPOLICY_NOTFOUNT_ERROR(112, "The Resource Policy is not found!");

    private int code;
    private String pattern;

    ExceptionCode(int code, String pattern) {
      this.code = code;
      this.pattern = pattern;
    }

    public int getCode() {
      return this.code;
    }
  }
}
