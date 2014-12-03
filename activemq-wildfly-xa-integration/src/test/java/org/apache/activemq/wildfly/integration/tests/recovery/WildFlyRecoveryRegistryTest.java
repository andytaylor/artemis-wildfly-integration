/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.wildfly.integration.tests.recovery;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.ra.ActiveMQResourceAdapter;
import org.apache.activemq.ra.recovery.RecoveryManager;
import org.apache.activemq.service.extensions.xa.recovery.XARecoveryConfig;
import org.apache.activemq.tests.integration.ra.ActiveMQRAClusteredTestBase;
import org.apache.activemq.wildfly.integration.recovery.WildFlyActiveMQRecoveryRegistry;
import org.apache.activemq.wildfly.integration.recovery.WildFlyActiveMQRegistry;
import org.apache.activemq.wildfly.integration.recovery.WildFlyRecoveryDiscovery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mtaylor
 */

public class WildFlyRecoveryRegistryTest extends ActiveMQRAClusteredTestBase
{
   @Test
   public void testWildFlyRecoveryDiscoveryIsProperlyRegistered() throws Exception
   {
      ActiveMQResourceAdapter qResourceAdapter = newResourceAdapter();
      MyBootstrapContext ctx = new MyBootstrapContext();
      qResourceAdapter.start(ctx);

      Field field = WildFlyActiveMQRecoveryRegistry.class.getDeclaredField("configSet");
      field.setAccessible(true);
      ConcurrentHashMap<XARecoveryConfig, WildFlyRecoveryDiscovery> map = (ConcurrentHashMap) field.get(WildFlyActiveMQRecoveryRegistry.getInstance());
      assertTrue(map.size() == 1);
   }

   @Test
   public void testRecoveryManagerUsesWildFlyRecoveryRegistry() throws Exception
   {
      RecoveryManager recoveryManager = new RecoveryManager();

      Method method = RecoveryManager.class.getDeclaredMethod("locateRecoveryRegistry");
      method.setAccessible(true);
      method.invoke(recoveryManager);

      Field field = RecoveryManager.class.getDeclaredField("registry");
      field.setAccessible(true);
      assertTrue(field.get(recoveryManager) instanceof WildFlyActiveMQRegistry);
   }

   @Test
   public void testXAResourcesAreRegisteredDuringDiscovery() throws Exception
   {
      ActiveMQResourceAdapter qResourceAdapter = newResourceAdapter();
      MyBootstrapContext ctx = new MyBootstrapContext();
      qResourceAdapter.start(ctx);

      qResourceAdapter.setConnectorClassName(INVM_CONNECTOR_FACTORY);
      qResourceAdapter.setConnectionParameters("server-id=1");

      assertTrue(WildFlyActiveMQRecoveryRegistry.getInstance().getXAResources().length == 2);
   }
}
