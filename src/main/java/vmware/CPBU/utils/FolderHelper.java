/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.CPBU.utils;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.Folder;
import com.vmware.vcenter.FolderTypes;
import vmware.CPBU.exceptions.VMOperatorException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FolderHelper {

    /**
     * Returns the identifier of a folder
     *
     * Note: The method assumes that there is only one folder and datacenter
     * with the specified names.
     * 
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @param datacenterName name of the datacenter
     * @param folderName name of the folder
     * @return identifier of a folder
     */
    public static String getFolder(
        StubFactory stubFactory, StubConfiguration sessionStubConfig,
        String datacenterName, String folderName) throws VMOperatorException {

        // Get the folder
        Folder folderService = stubFactory.createStub(Folder.class,
            sessionStubConfig);
        Set<String> vmFolders = Collections.singleton(folderName);
        FolderTypes.FilterSpec.Builder vmFolderFilterSpecBuilder = 
            new FolderTypes.FilterSpec.Builder().setNames(vmFolders);

        if (null != datacenterName) {
            // Get the datacenter
            Set<String> datacenters = Collections
                    .singleton(DatacenterHelper.getDatacenter(stubFactory,
                        sessionStubConfig, datacenterName));
            vmFolderFilterSpecBuilder.setDatacenters(datacenters);
        }
        List<FolderTypes.Summary> folderSummaries = folderService.list(
            vmFolderFilterSpecBuilder.build());

        if (folderSummaries.size() < 1)
            throw new VMOperatorException(VMOperatorException.ExceptionCode.VMFOLDER_NOTFOUNT_ERROR);
        return folderSummaries.get(0).getFolder();
    }

    public static String getFolder(StubFactory stubFactory, StubConfiguration
        sessionStubConfig, String vmFolderName) throws VMOperatorException {
        return getFolder(stubFactory, sessionStubConfig, null, vmFolderName);
    }
}
