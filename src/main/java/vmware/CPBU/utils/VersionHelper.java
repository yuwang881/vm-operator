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

import com.vmware.appliance.system.Version;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.Folder;
import com.vmware.vcenter.FolderTypes;
import vmware.CPBU.exceptions.VMOperatorException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VersionHelper {

    /**
     * Returns the identifier of a folder
     *
     * Note: The method assumes that there is only one folder and datacenter
     * with the specified names.
     * 
     * @param stubFactory Stub factory for the api endpoint
     * @param sessionStubConfig stub configuration for the current session
     * @return identifier of a folder
     */
    public static String getVersion(
        StubFactory stubFactory, StubConfiguration sessionStubConfig) {

        Version versionService = stubFactory.createStub(Version.class, sessionStubConfig);
        return versionService.get().getVersion();

    }


}
