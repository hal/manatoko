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
package org.jboss.hal.testsuite.test.configuration.iiop;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.page.configuration.IIOPPage;
import org.jboss.hal.testsuite.test.Manatoko;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CLIENT_SSL_CONTEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.IDENTITY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.IIOP_OPENJDK;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAMING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NONE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SECURITY_DOMAIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_SSL_CONTEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRANSACTIONS;
import static org.jboss.hal.testsuite.container.WildFlyConfiguration.FULL;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.AUTH_METHOD;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.CALLER_PROPAGATION;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.DEFAULT_ROOT_CONTEXT;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.EXPORT_CORBALOC;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.HIGH_WATER_MARK;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.IIOP;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.PERSISTENT_SERVER_ID;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.ROOT_CONTEXT;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.SPEC;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.SUBSYSTEM_ADDRESS;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.SUPPORTED;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.SUPPORT_SSL;
import static org.jboss.hal.testsuite.fixtures.IIOPFixtures.USERNAME_PASSWORD;
import static org.jboss.hal.testsuite.page.configuration.IIOPPage.IIOP_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Manatoko
@Testcontainers
class IIOPTest {

    @Container static WildFlyContainer wildFly = WildFlyContainer.standalone(FULL);
    private static Operations operations;

    @BeforeAll
    static void setupModel() throws Exception {
        OnlineManagementClient client = wildFly.managementClient();
        operations = new Operations(client);
        operations.undefineAttribute(SUBSYSTEM_ADDRESS, PERSISTENT_SERVER_ID);
        operations.undefineAttribute(SUBSYSTEM_ADDRESS, EXPORT_CORBALOC);
        operations.writeAttribute(SUBSYSTEM_ADDRESS, SECURITY, IDENTITY);
        operations.writeAttribute(SUBSYSTEM_ADDRESS, TRANSACTIONS, SPEC);
        operations.undefineAttribute(SUBSYSTEM_ADDRESS, AUTH_METHOD);
        operations.undefineAttribute(SUBSYSTEM_ADDRESS, CALLER_PROPAGATION);
        operations.undefineAttribute(SUBSYSTEM_ADDRESS, HIGH_WATER_MARK);
        operations.undefineAttribute(SUBSYSTEM_ADDRESS, ROOT_CONTEXT);
        operations.undefineAttribute(SUBSYSTEM_ADDRESS, PROPERTIES);
        operations.undefineAttribute(SUBSYSTEM_ADDRESS, SUPPORT_SSL);
        operations.undefineAttribute(SUBSYSTEM_ADDRESS, SECURITY_DOMAIN);
    }

    @Inject Console console;
    @Inject CrudOperations crud;
    @Page IIOPPage page;
    FormFragment form;

    @BeforeEach
    void prepare() {
        page.navigate();
    }

    @Test
    void updateOrbPersistentId() throws Exception {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, "orb", Ids.TAB));
        form = page.getOrbForm();
        crud.update(SUBSYSTEM_ADDRESS, form, PERSISTENT_SERVER_ID, "23");
    }

    @Test
    void showOrbSocketBindingSensitive() {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, "orb", Ids.TAB));
        form = page.getOrbForm();
        form.showSensitive(SOCKET_BINDING);

        String val = form.value(SOCKET_BINDING);
        assertEquals(val, IIOP);
    }

    @Test
    void updateNaming() throws Exception {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, NAMING, Ids.TAB));
        form = page.getNamingForm();
        crud.update(SUBSYSTEM_ADDRESS, form, EXPORT_CORBALOC, false);
    }

    @Test
    void resetNaming() throws Exception {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, NAMING, Ids.TAB));
        form = page.getNamingForm();
        crud.reset(SUBSYSTEM_ADDRESS, form, resourceVerifier -> {
            resourceVerifier.verifyAttribute(EXPORT_CORBALOC, true);
            resourceVerifier.verifyAttribute(ROOT_CONTEXT, DEFAULT_ROOT_CONTEXT);
        });
    }

    @Test
    void resetInitializers() throws Exception {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, "initializers", Ids.TAB));
        form = page.getInitializersForm();
        crud.reset(SUBSYSTEM_ADDRESS, form, resourceVerifier -> {
            resourceVerifier.verifyAttribute(SECURITY, NONE);
            resourceVerifier.verifyAttribute(TRANSACTIONS, NONE);
        });
    }

    @Test
    void updateAsContext() throws Exception {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, "as-context", Ids.TAB));
        form = page.getAsContextForm();
        crud.update(SUBSYSTEM_ADDRESS, form,
                f -> f.select(AUTH_METHOD, NONE),
                resourceVerifier -> resourceVerifier.verifyAttribute(AUTH_METHOD, NONE));
    }

    @Test
    void resetAsContext() throws Exception {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, "as-context", Ids.TAB));
        form = page.getAsContextForm();
        crud.reset(SUBSYSTEM_ADDRESS, form,
                resourceVerifier -> resourceVerifier.verifyAttribute(AUTH_METHOD, USERNAME_PASSWORD));
    }

    @Test
    void updateSasContext() throws Exception {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, "sas-context", Ids.TAB));
        form = page.getSasContextForm();
        crud.update(SUBSYSTEM_ADDRESS, form,
                f -> f.select(CALLER_PROPAGATION, SUPPORTED),
                resourceVerifier -> resourceVerifier.verifyAttribute(CALLER_PROPAGATION, SUPPORTED));
    }

    @Test
    void updateSecurity() throws Exception {
        boolean clientRequiresSSL = operations.readAttribute(SUBSYSTEM_ADDRESS, "client-requires-ssl").booleanValue();
        boolean serverRequiresSSL = operations.readAttribute(SUBSYSTEM_ADDRESS, "client-requires-ssl").booleanValue();
        boolean supportSSL = operations.readAttribute(SUBSYSTEM_ADDRESS, "support-ssl").booleanValue();
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, SECURITY, Ids.TAB));
        form = page.getSecurityForm();
        crud.update(SUBSYSTEM_ADDRESS, form,
                formFragment -> {
                    formFragment.flip("client-requires-ssl", !clientRequiresSSL);
                    formFragment.flip("server-requires-ssl", !serverRequiresSSL);
                    formFragment.flip("support-ssl", !supportSSL);
                },
                resourceVerifier -> {
                    resourceVerifier.verifyAttribute("client-requires-ssl", !clientRequiresSSL);
                    resourceVerifier.verifyAttribute("server-requires-ssl", !serverRequiresSSL);
                    resourceVerifier.verifyAttribute("support-ssl", !supportSSL);
                });
    }

    @Test
    void updateSecurityServerRequireClientSslContext() {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, SECURITY, Ids.TAB));
        form = page.getSecurityForm();
        crud.updateWithError(form, f -> f.text(CLIENT_SSL_CONTEXT, "foo"), SERVER_SSL_CONTEXT);
    }

    @Test
    void updateTcp() throws Exception {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, "tcp", Ids.TAB));
        form = page.getTcpForm();
        crud.update(SUBSYSTEM_ADDRESS, form, HIGH_WATER_MARK, 11);
    }

    @Test
    void updateProperties() throws Exception {
        page.getTabs().select(Ids.build(IIOP_OPENJDK, PROPERTIES, Ids.TAB));
        form = page.getPropertiesForm();
        crud.update(SUBSYSTEM_ADDRESS, form, PROPERTIES, Random.properties());
    }

    @Test
    void resetProperties() throws Exception {
        page.getTabs().select(Ids.build(IIOP_OPENJDK, PROPERTIES, Ids.TAB));
        form = page.getPropertiesForm();
        crud.reset(SUBSYSTEM_ADDRESS, form,
                resourceVerifier -> resourceVerifier.verifyAttributeIsUndefined(PROPERTIES));
    }

    @Test
    void cancelEditTcp() {
        page.getTabs().select(Ids.build(IIOP_PREFIX, GROUP, "tcp", Ids.TAB));
        form = page.getTcpForm();
        form.edit();
        form.cancel();
        assertTrue(console.verifyNoError());
    }

    @Test
    void cancelEditProperties() {
        page.getTabs().select(Ids.build(IIOP_OPENJDK, PROPERTIES, Ids.TAB));
        form = page.getPropertiesForm();
        form.edit();
        form.cancel();
        assertTrue(console.verifyNoError());
    }
}
