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
package org.jboss.hal.testsuite.test.configuration.modcluster;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.ModclusterPage;
import org.jboss.hal.testsuite.test.Manatoko;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Batch;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LISTENER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.testsuite.container.WildFlyConfiguration.HA;
import static org.jboss.hal.testsuite.fixtures.ModclusterFixtures.CAPACITY;
import static org.jboss.hal.testsuite.fixtures.ModclusterFixtures.LOAD_METRIC_CREATE;
import static org.jboss.hal.testsuite.fixtures.ModclusterFixtures.LOAD_METRIC_DELETE;
import static org.jboss.hal.testsuite.fixtures.ModclusterFixtures.LOAD_METRIC_UPDATE;
import static org.jboss.hal.testsuite.fixtures.ModclusterFixtures.PROXY_UPDATE;
import static org.jboss.hal.testsuite.fixtures.ModclusterFixtures.WEIGHT;
import static org.jboss.hal.testsuite.fixtures.ModclusterFixtures.loadMetricAddress;
import static org.jboss.hal.testsuite.fixtures.ModclusterFixtures.loadProviderDynamicAddress;
import static org.jboss.hal.testsuite.fixtures.ModclusterFixtures.proxyAddress;

@Manatoko
@Testcontainers
class LoadMetricTest {

    @Container static WildFlyContainer wildFly = WildFlyContainer.standalone(HA);

    @BeforeAll
    static void setupModel() throws Exception {
        OnlineManagementClient client = wildFly.managementClient();
        Operations operations = new Operations(client);
        Batch proxyAdd = new Batch();
        proxyAdd.add(proxyAddress(PROXY_UPDATE), Values.of(LISTENER, DEFAULT));
        proxyAdd.add(loadProviderDynamicAddress(PROXY_UPDATE));
        operations.batch(proxyAdd).assertSuccess();
        operations.add(loadMetricAddress(PROXY_UPDATE, LOAD_METRIC_DELETE), Values.of(TYPE, "heap")).assertSuccess();
        operations.add(loadMetricAddress(PROXY_UPDATE, LOAD_METRIC_UPDATE), Values.of(TYPE, "heap")).assertSuccess();
    }

    @Inject Console console;
    @Inject CrudOperations crud;
    @Page ModclusterPage page;
    TableFragment table;
    FormFragment form;

    @BeforeEach
    void setUp() {
        page.navigate(NAME, PROXY_UPDATE);
        console.verticalNavigation().selectPrimary("load-metrics-item");
        table = page.getLoadMetricTable();
        form = page.getLoadMetricForm();
        table.bind(form);
    }

    @Test
    void create() throws Exception {
        crud.create(loadMetricAddress(PROXY_UPDATE, LOAD_METRIC_CREATE), table, f -> {
            f.text(NAME, LOAD_METRIC_CREATE);
            f.select(TYPE, "cpu");
        },
                ver -> ver.verifyAttribute(TYPE, "cpu"));
    }

    @Test
    void reset() throws Exception {
        table.select(LOAD_METRIC_UPDATE);
        crud.reset(loadMetricAddress(PROXY_UPDATE, LOAD_METRIC_UPDATE), form);
    }

    @Test
    void update() throws Exception {
        table.select(LOAD_METRIC_UPDATE);
        crud.update(loadMetricAddress(PROXY_UPDATE, LOAD_METRIC_UPDATE), form, WEIGHT, Random.number());
    }

    @Test
    void updateCapacity() throws Exception {
        // update an attribute of type DOUBLE
        table.select(LOAD_METRIC_UPDATE);
        crud.update(loadMetricAddress(PROXY_UPDATE, LOAD_METRIC_UPDATE), form, CAPACITY, Random.numberDouble());
    }

    @Test
    void delete() throws Exception {
        crud.delete(loadMetricAddress(PROXY_UPDATE, LOAD_METRIC_DELETE), table, LOAD_METRIC_DELETE);
    }
}
