/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.testsuite.test.configuration.distributableweb;

import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.command.AddRemoteSocketBinding;
import org.jboss.hal.testsuite.model.AvailablePortFinder;
import org.jboss.hal.testsuite.model.ModelNodeGenerator;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_CACHE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LOCAL_CACHE;
import static org.jboss.hal.testsuite.fixtures.InfinispanFixtures.SOCKET_BINDINGS;
import static org.jboss.hal.testsuite.fixtures.InfinispanFixtures.cacheContainerAddress;
import static org.jboss.hal.testsuite.fixtures.InfinispanFixtures.remoteCacheContainerAddress;

interface DistributableWebOperations {

    static void addCacheContainer(OnlineManagementClient client, Operations operations, String cacheContainer)
            throws Exception {
        String localCache = Random.name();
        operations.add(cacheContainerAddress(cacheContainer)).assertSuccess();
        operations.add(cacheContainerAddress(cacheContainer).and(LOCAL_CACHE, localCache)).assertSuccess();
        operations.writeAttribute(cacheContainerAddress(cacheContainer), DEFAULT_CACHE, localCache).assertSuccess();
    }

    static void addRemoteSocketBinding(OnlineManagementClient client, Operations operations, String name)
            throws Exception {
        client.apply(new AddRemoteSocketBinding(name, "localhost", AvailablePortFinder.getNextAvailableTCPPort()));
    }

    static void addRemoteCacheContainer(OnlineManagementClient client, Operations operations, String name,
            String socketBinding) throws Exception {
        String remoteCluster = Random.name();
        operations.batch(new Batch()
                .add(remoteCacheContainerAddress(name), Values.of("default-remote-cluster", remoteCluster))
                .add(remoteCacheContainerAddress(name).and("remote-cluster", remoteCluster),
                        Values.of(SOCKET_BINDINGS,
                                new ModelNodeGenerator.ModelNodeListBuilder().addAll(socketBinding).build())))
                .assertSuccess();
    }
}
