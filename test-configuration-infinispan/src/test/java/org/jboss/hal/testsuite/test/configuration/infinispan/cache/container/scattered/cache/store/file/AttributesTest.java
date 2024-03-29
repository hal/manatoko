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
package org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.scattered.cache.store.file;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.page.configuration.ScatteredCachePage;
import org.jboss.hal.testsuite.test.Manatoko;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.scattered.cache.store.StoreSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.testsuite.container.WildFlyConfiguration.FULL_HA;
import static org.jboss.hal.testsuite.fixtures.InfinispanFixtures.CC_CREATE;
import static org.jboss.hal.testsuite.fixtures.InfinispanFixtures.SC_CREATE;
import static org.jboss.hal.testsuite.fixtures.InfinispanFixtures.fileStoreAddress;

@Manatoko
@Testcontainers
class AttributesTest {

    @Container static WildFlyContainer wildFly = WildFlyContainer.standalone(FULL_HA);

    @BeforeAll
    static void setupModel() throws Exception {
        StoreSetup.setup(wildFly, operations -> operations.headers(Values.of("allow-resource-service-restart", true))
                .add(fileStoreAddress(CC_CREATE, SC_CREATE)));
    }

    @Inject Console console;
    @Inject CrudOperations crud;
    @Page ScatteredCachePage page;
    Operations operations;
    FormFragment form;

    @BeforeEach
    void prepare() {
        operations = new Operations(wildFly.managementClient());
        page.navigate(CC_CREATE, SC_CREATE);
        console.verticalNavigation().selectPrimary("scattered-cache-store-item");
        page.selectFileStoreAttributes();
        form = page.getFileStoreAttributesForm();
    }

    @Test
    void editMaxBatchSize() throws Exception {
        console.waitNoNotification();
        crud.update(fileStoreAddress(CC_CREATE, SC_CREATE), form,
                "max-batch-size", Random.number());
    }

    @Test
    void togglePassivation() throws Exception {
        console.waitNoNotification();
        boolean passivation = operations.readAttribute(fileStoreAddress(CC_CREATE, SC_CREATE),
                "passivation")
                .booleanValue(true);
        crud.update(fileStoreAddress(CC_CREATE, SC_CREATE), form,
                "passivation", !passivation);
    }

    @Test
    void togglePreload() throws Exception {
        console.waitNoNotification();
        boolean preload = operations.readAttribute(fileStoreAddress(CC_CREATE, SC_CREATE), "preload")
                .booleanValue(false);
        crud.update(fileStoreAddress(CC_CREATE, SC_CREATE), form,
                "preload", !preload);
    }

    @Test
    void editProperties() throws Exception {
        console.waitNoNotification();
        crud.update(fileStoreAddress(CC_CREATE, SC_CREATE), form,
                "properties", Random.properties());
    }

    @Test
    void togglePurge() throws Exception {
        console.waitNoNotification();
        boolean purge = operations.readAttribute(fileStoreAddress(CC_CREATE, SC_CREATE), "purge")
                .booleanValue(true);
        crud.update(fileStoreAddress(CC_CREATE, SC_CREATE), form,
                "purge", !purge);
    }

    @Test
    void toggleShared() throws Exception {
        console.waitNoNotification();
        boolean shared = operations.readAttribute(fileStoreAddress(CC_CREATE, SC_CREATE), "shared")
                .booleanValue(false);
        crud.update(fileStoreAddress(CC_CREATE, SC_CREATE), form,
                "shared", !shared);
    }
}
