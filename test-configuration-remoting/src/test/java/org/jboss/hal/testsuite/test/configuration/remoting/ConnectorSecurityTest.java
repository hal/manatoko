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
package org.jboss.hal.testsuite.test.configuration.remoting;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.RemotingPage;
import org.jboss.hal.testsuite.test.Manatoko;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.hal.testsuite.container.WildFlyConfiguration.DEFAULT;
import static org.jboss.hal.testsuite.fixtures.RemotingFixtures.CONNECTOR_SECURITY;
import static org.jboss.hal.testsuite.fixtures.RemotingFixtures.INCLUDE_MECHANISMS;
import static org.jboss.hal.testsuite.fixtures.RemotingFixtures.connectorAddress;
import static org.jboss.hal.testsuite.fixtures.RemotingFixtures.connectorSecurityAddress;
import static org.jboss.hal.testsuite.fixtures.SocketBindingFixtures.INBOUND_READ;
import static org.jboss.hal.testsuite.fixtures.SocketBindingFixtures.STANDARD_SOCKETS;
import static org.jboss.hal.testsuite.fixtures.SocketBindingFixtures.inboundAddress;

@Manatoko
@Testcontainers
@TestMethodOrder(MethodOrderer.MethodName.class)
class ConnectorSecurityTest {

    @Container static WildFlyContainer wildFly = WildFlyContainer.standalone(DEFAULT);

    @BeforeAll
    static void setupModel() throws Exception {
        OnlineManagementClient client = wildFly.managementClient();
        Operations operations = new Operations(client);
        operations.add(inboundAddress(STANDARD_SOCKETS, INBOUND_READ));
        operations.add(connectorAddress(CONNECTOR_SECURITY), Values.of(SOCKET_BINDING, INBOUND_READ));
    }

    @Inject Console console;
    @Inject CrudOperations crud;
    @Page RemotingPage page;
    TableFragment table;
    FormFragment form;

    @BeforeEach
    void prepare() {
        page.navigate();
        console.verticalNavigation().selectSecondary("remoting-remote-connector-item",
                "remoting-connector-sub-item");
        table = page.getConnectorTable();
        form = page.getConnectorSecurityForm();
        table.bind(form);
    }

    @Test
    void _0create() throws Exception {
        table.select(CONNECTOR_SECURITY);
        page.getConnectorTabs().select(Ids.REMOTING_CONNECTOR_SECURITY_TAB);
        crud.createSingleton(connectorSecurityAddress(CONNECTOR_SECURITY), form);
    }

    @Test
    void _1update() throws Exception {
        table.select(CONNECTOR_SECURITY);
        page.getConnectorTabs().select(Ids.REMOTING_CONNECTOR_SECURITY_TAB);
        crud.update(connectorSecurityAddress(CONNECTOR_SECURITY), form, INCLUDE_MECHANISMS, asList("foo", "bar"));
    }

    @Test
    void _2reset() throws Exception {
        table.select(CONNECTOR_SECURITY);
        page.getConnectorTabs().select(Ids.REMOTING_CONNECTOR_SECURITY_TAB);
        crud.reset(connectorSecurityAddress(CONNECTOR_SECURITY), form);
    }

    @Test
    void _3delete() throws Exception {
        table.select(CONNECTOR_SECURITY);
        page.getConnectorTabs().select(Ids.REMOTING_CONNECTOR_SECURITY_TAB);
        crud.deleteSingleton(connectorSecurityAddress(CONNECTOR_SECURITY), form);
    }
}
