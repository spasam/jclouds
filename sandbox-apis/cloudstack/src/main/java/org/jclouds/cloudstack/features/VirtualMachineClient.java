/**
 *
 * Copyright (C) 2011 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.cloudstack.features;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jclouds.cloudstack.domain.AsyncCreateResponse;
import org.jclouds.cloudstack.domain.VirtualMachine;
import org.jclouds.cloudstack.options.DeployVirtualMachineOptions;
import org.jclouds.cloudstack.options.ListVirtualMachinesOptions;
import org.jclouds.concurrent.Timeout;

/**
 * Provides synchronous access to CloudStack VirtualMachine features.
 * <p/>
 * 
 * @see VirtualMachineAsyncClient
 * @see <a href="http://download.cloud.com/releases/2.2.0/api/TOC_User.html" />
 * @author Adrian Cole
 */
@Timeout(duration = 60, timeUnit = TimeUnit.SECONDS)
public interface VirtualMachineClient {
   /**
    * Lists VirtualMachines
    * 
    * @param options
    *           if present, how to constrain the list.
    * @return VirtualMachines matching query, or empty set, if no VirtualMachines are found
    */
   Set<VirtualMachine> listVirtualMachines(ListVirtualMachinesOptions... options);

   /**
    * get a specific VirtualMachine by id
    * 
    * @param id
    *           VirtualMachine to get
    * @return VirtualMachine or null if not found
    */
   VirtualMachine getVirtualMachine(long id);

   /**
    * Creates and automatically starts a virtual machine based on a service offering, disk offering,
    * and template.
    * 
    * @param zoneId
    *           availability zone for the virtual machine
    * @param serviceOfferingId
    *           the ID of the service offering for the virtual machine
    * @param templateId
    *           the ID of the template for the virtual machine
    * 
    * @return virtual machine
    */
   AsyncCreateResponse deployVirtualMachineInZone(long zoneId, long serviceOfferingId, long templateId,
            DeployVirtualMachineOptions... options);

   /**
    * Reboots a virtual machine.
    * 
    * @param id
    *           The ID of the virtual machine
    * @return job id related to destroying the VM
    */
   Long rebootVirtualMachine(long id);

   /**
    * Starts a virtual machine.
    * 
    * @param id
    *           The ID of the virtual machine
    * @return job id related to destroying the VM
    */
   Long startVirtualMachine(long id);

   /**
    * Stops a virtual machine.
    * 
    * @param id
    *           The ID of the virtual machine
    * @return job id related to destroying the VM
    */
   Long stopVirtualMachine(long id);

   /**
    * Resets the password for virtual machine. The virtual machine must be in a "Stopped" state and
    * the template must already support this feature for this command to take effect.
    * 
    * @param id
    *           The ID of the virtual machine
    * @return job id related to destroying the VM
    */
   Long resetPasswordForVirtualMachine(long id);

   /**
    * Changes the service offering for a virtual machine. The virtual machine must be in a "Stopped"
    * state for this command to take effect.
    * 
    * @param id
    *           The ID of the virtual machine
    * @return job id related to destroying the VM
    */
   Long changeServiceForVirtualMachine(long id);

   /**
    * Updates parameters of a virtual machine.
    * 
    * @param id
    *           The ID of the virtual machine
    * @return job id related to destroying the VM
    */
   Long updateVirtualMachine(long id);

   /**
    * Destroys a virtual machine. Once destroyed, only the administrator can recover it.
    * 
    * @param id
    *           vm to destroy
    * @return job id related to destroying the VM, or null if resource was not found
    */
   Long destroyVirtualMachine(long id);
}
