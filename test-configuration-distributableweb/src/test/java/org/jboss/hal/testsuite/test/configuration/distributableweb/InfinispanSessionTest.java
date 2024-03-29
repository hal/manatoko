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

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.SelectFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.model.ResourceVerifier;
import org.jboss.hal.testsuite.page.configuration.DistributableWebPage;
import org.jboss.hal.testsuite.test.Manatoko;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.AFFINITY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CACHE_CONTAINER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INFINISPAN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RANKED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ROUTING;
import static org.jboss.hal.testsuite.container.WildFlyConfiguration.FULL;
import static org.jboss.hal.testsuite.fixtures.DistributableWebFixtures.ATTRIBUTE;
import static org.jboss.hal.testsuite.fixtures.DistributableWebFixtures.GRANULARITY;
import static org.jboss.hal.testsuite.fixtures.DistributableWebFixtures.INFINISPAN_SESSION_AFFINITY;
import static org.jboss.hal.testsuite.fixtures.DistributableWebFixtures.INFINISPAN_SESSION_CREATE;
import static org.jboss.hal.testsuite.fixtures.DistributableWebFixtures.INFINISPAN_SESSION_DELETE;
import static org.jboss.hal.testsuite.fixtures.DistributableWebFixtures.INFINISPAN_SESSION_UPDATE;
import static org.jboss.hal.testsuite.fixtures.DistributableWebFixtures.SESSION;
import static org.jboss.hal.testsuite.fixtures.DistributableWebFixtures.SUBSYSTEM_ADDRESS;
import static org.jboss.hal.testsuite.fixtures.DistributableWebFixtures.infinispanSessionAddress;
import static org.jboss.hal.testsuite.fixtures.InfinispanFixtures.CC_CREATE;
import static org.jboss.hal.testsuite.test.configuration.distributableweb.DistributableWebOperations.addCacheContainer;
import static org.junit.jupiter.api.Assertions.fail;

@Manatoko
@Testcontainers
class InfinispanSessionTest {

    @Container static WildFlyContainer wildFly = WildFlyContainer.standalone(FULL);
    private static OnlineManagementClient client;

    @BeforeAll
    static void setupModel() throws Exception {
        client = wildFly.managementClient();
        Operations operations = new Operations(client);
        addCacheContainer(client, operations, CC_CREATE);
        Values values = Values.of(CACHE_CONTAINER, CC_CREATE).and(GRANULARITY, SESSION);
        operations.add(SUBSYSTEM_ADDRESS.and(ROUTING, INFINISPAN), Values.of(CACHE_CONTAINER, CC_CREATE)).assertSuccess();
        operations.add(infinispanSessionAddress(INFINISPAN_SESSION_UPDATE), values).assertSuccess();
        operations.add(infinispanSessionAddress(INFINISPAN_SESSION_AFFINITY), values).assertSuccess();
        operations.add(infinispanSessionAddress(INFINISPAN_SESSION_DELETE), values).assertSuccess();
    }

    @Page DistributableWebPage page;
    @Inject CrudOperations crud;
    @Inject Console console;
    TableFragment table;
    FormFragment form;

    @BeforeEach
    void setUp() {
        page.navigate();
        console.verticalNavigation().selectPrimary("dw-infinispan-session-management-item");
        table = page.getInfinispanSessionManagementTable();
        form = page.getInfinispanSessionManagementForm();
        table.bind(form);
    }

    @Test
    void create() throws Exception {
        crud.create(infinispanSessionAddress(INFINISPAN_SESSION_CREATE), table, f -> {
            f.text(NAME, INFINISPAN_SESSION_CREATE);
            f.text(CACHE_CONTAINER, CC_CREATE);
        });
    }

    @Test
    void update() throws Exception {
        table.select(INFINISPAN_SESSION_UPDATE);
        crud.update(infinispanSessionAddress(INFINISPAN_SESSION_UPDATE), form,
                f -> f.select(GRANULARITY, ATTRIBUTE),
                verifier -> verifier.verifyAttribute(GRANULARITY, ATTRIBUTE));
    }

    @Test
    void switchAffinity() throws Exception {
        table.select(INFINISPAN_SESSION_AFFINITY);
        page.getInfinispanSessionManagementTabs().select("dw-infinispan-session-management-affinity-tab");
        SelectFragment select = page.getSwitchAffinity();
        if (select != null) {
            select.select(RANKED);
            console.verifySuccess();
            new ResourceVerifier(infinispanSessionAddress(INFINISPAN_SESSION_AFFINITY).and(AFFINITY, RANKED), client)
                    .verifyExists();
        } else {
            fail("Select control to switch affinity not found!");
        }

    }

    @Test
    void reset() throws Exception {
        table.select(INFINISPAN_SESSION_UPDATE);
        crud.reset(infinispanSessionAddress(INFINISPAN_SESSION_UPDATE), form);
    }

    @Test
    void delete() throws Exception {
        table.select(INFINISPAN_SESSION_DELETE);
        crud.delete(infinispanSessionAddress(INFINISPAN_SESSION_DELETE), table, INFINISPAN_SESSION_DELETE);
    }
}
