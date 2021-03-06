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
package org.jboss.hal.testsuite.test.configuration.transaction;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.command.AddLocalSocketBinding;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.fixtures.TransactionFixtures;
import org.jboss.hal.testsuite.page.configuration.TransactionPage;
import org.jboss.hal.testsuite.test.Manatoko;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import static org.jboss.hal.testsuite.container.WildFlyConfiguration.DEFAULT;

@Manatoko
@Testcontainers
class RecoveryTest {

    @Container static WildFlyContainer wildFly = WildFlyContainer.standalone(DEFAULT);
    private static Operations operations;

    @BeforeAll
    static void setupModel() throws Exception {
        OnlineManagementClient client = wildFly.managementClient();
        operations = new Operations(client);
        client.apply(new AddLocalSocketBinding(TransactionFixtures.RECOVERY_SOCKET_BINDING_CREATE));
        client.apply(new AddLocalSocketBinding(TransactionFixtures.RECOVERY_STATUS_SOCKET_BINDING));
    }

    @Inject Console console;
    @Inject CrudOperations crudOperations;
    @Page TransactionPage page;

    @BeforeEach
    void prepare() {
        page.navigate();
        console.verticalNavigation()
                .selectPrimary(Ids.build("tx", "recovery", "config", "item"));
    }

    @Test
    void editSocketBinding() throws Exception {
        crudOperations.update(TransactionFixtures.TRANSACTIONS_ADDRESS, page.getRecoveryForm(),
                TransactionFixtures.SOCKET_BINDING,
                TransactionFixtures.RECOVERY_SOCKET_BINDING_CREATE);
    }

    @Test
    void editStatusSocketBinding() throws Exception {
        crudOperations.update(TransactionFixtures.TRANSACTIONS_ADDRESS, page.getRecoveryForm(),
                TransactionFixtures.STATUS_SOCKET_BINDING,
                TransactionFixtures.RECOVERY_STATUS_SOCKET_BINDING);
    }

    @Test
    void toggleRecoveryListener() throws Exception {
        boolean recoveryListener = operations
                .readAttribute(TransactionFixtures.TRANSACTIONS_ADDRESS, TransactionFixtures.RECOVERY_LISTENER)
                .booleanValue();
        crudOperations.update(TransactionFixtures.TRANSACTIONS_ADDRESS, page.getRecoveryForm(),
                TransactionFixtures.RECOVERY_LISTENER,
                !recoveryListener);
    }
}
