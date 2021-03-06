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
package org.jboss.hal.testsuite.test.configuration.resourceadapter;

import java.util.List;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.dmr.ModelNode;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.model.ResourceVerifier;
import org.jboss.hal.testsuite.page.configuration.ResourceAdapterPage;
import org.jboss.hal.testsuite.test.Manatoko;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ARCHIVE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONFIG_PROPERTIES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FROM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATISTICS_ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TO;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TRANSACTION_SUPPORT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WM_SECURITY_MAPPING_GROUPS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WM_SECURITY_MAPPING_USERS;
import static org.jboss.hal.testsuite.container.WildFlyConfiguration.DEFAULT;
import static org.jboss.hal.testsuite.fixtures.ResourceAdapterFixtures.BEANVALIDATIONGROUPS;
import static org.jboss.hal.testsuite.fixtures.ResourceAdapterFixtures.BOOTSTRAP_CONTEXT;
import static org.jboss.hal.testsuite.fixtures.ResourceAdapterFixtures.LOCAL_TRANSACTION;
import static org.jboss.hal.testsuite.fixtures.ResourceAdapterFixtures.RESOURCE_ADAPTER_UPDATE;
import static org.jboss.hal.testsuite.fixtures.ResourceAdapterFixtures.WM_SECURITY;
import static org.jboss.hal.testsuite.fixtures.ResourceAdapterFixtures.WM_SECURITY_DEFAULT_GROUPS;
import static org.jboss.hal.testsuite.fixtures.ResourceAdapterFixtures.WM_SECURITY_DEFAULT_PRINCIPAL;
import static org.jboss.hal.testsuite.fixtures.ResourceAdapterFixtures.WM_SECURITY_MAPPING_REQUIRED;
import static org.jboss.hal.testsuite.fixtures.ResourceAdapterFixtures.resourceAdapterAddress;

@Manatoko
@Testcontainers
class ResourceAdapterConfigurationTest {

    @Container static WildFlyContainer wildFly = WildFlyContainer.standalone(DEFAULT);
    private static OnlineManagementClient client;

    @BeforeAll
    static void setupModel() throws Exception {
        client = wildFly.managementClient();
        Operations operations = new Operations(client);
        operations.add(resourceAdapterAddress(RESOURCE_ADAPTER_UPDATE), Values.of(ARCHIVE, Random.name()));
    }

    @Inject Console console;
    @Inject CrudOperations crud;
    @Page ResourceAdapterPage page;

    @BeforeEach
    void setUp() {
        page.navigate(NAME, RESOURCE_ADAPTER_UPDATE);
        console.verticalNavigation().selectPrimary("resource-adapter-configuration-item");
    }

    @Test
    void updateAttributes() throws Exception {
        page.getConfigurationTabs().select("resource-adapter-configuration-attributes-tab");

        List<String> groups = Random.list();
        String bootstrapContext = Random.name();
        ModelNode configProperties = Random.properties("foo", "bar");

        crud.update(resourceAdapterAddress(RESOURCE_ADAPTER_UPDATE), page.getAttributesForm(),
                form -> {
                    form.list(BEANVALIDATIONGROUPS).add(groups);
                    form.text(BOOTSTRAP_CONTEXT, bootstrapContext);
                    form.flip(STATISTICS_ENABLED, true);
                    form.select(TRANSACTION_SUPPORT, "LocalTransaction");
                    form.properties(CONFIG_PROPERTIES).add(configProperties);
                },
                resourceVerifier -> {
                    for (String group : groups) {
                        resourceVerifier.verifyListAttributeContainsValue(BEANVALIDATIONGROUPS, group);
                    }
                    resourceVerifier.verifyAttribute(BOOTSTRAP_CONTEXT, bootstrapContext);
                    resourceVerifier.verifyAttribute(STATISTICS_ENABLED, true);
                    resourceVerifier.verifyAttribute(TRANSACTION_SUPPORT, LOCAL_TRANSACTION);
                    // properties are nested resources!
                    ResourceVerifier propertyVerifier = new ResourceVerifier(
                            resourceAdapterAddress(RESOURCE_ADAPTER_UPDATE).and(CONFIG_PROPERTIES, "foo"), client);
                    propertyVerifier.verifyAttribute(VALUE, "bar");
                });
    }

    @Test
    void updateWmSecurity() throws Exception {
        page.getConfigurationTabs().select("resource-adapter-configuration-wm-security-tab");

        List<String> defaultGroups = Random.list();
        String defaultPrincipal = Random.name();

        crud.update(resourceAdapterAddress(RESOURCE_ADAPTER_UPDATE), page.getWmSecurityForm(),
                form -> {
                    form.flip(WM_SECURITY, true);
                    form.list(WM_SECURITY_DEFAULT_GROUPS).add(defaultGroups);
                    form.text(WM_SECURITY_DEFAULT_PRINCIPAL, defaultPrincipal);
                    form.flip(WM_SECURITY_MAPPING_REQUIRED, true);
                    form.properties(WM_SECURITY_MAPPING_GROUPS).add(Random.properties("group", "admin"));
                    form.properties(WM_SECURITY_MAPPING_USERS).add(Random.properties("user", "john"));
                },
                resourceVerifier -> {
                    resourceVerifier.verifyAttribute(WM_SECURITY, true);
                    for (String group : defaultGroups) {
                        resourceVerifier.verifyListAttributeContainsValue(WM_SECURITY_DEFAULT_GROUPS, group);
                    }
                    resourceVerifier.verifyAttribute(WM_SECURITY_DEFAULT_PRINCIPAL, defaultPrincipal);
                    resourceVerifier.verifyAttribute(WM_SECURITY_MAPPING_REQUIRED, true);
                    resourceVerifier.verifyAttribute(WM_SECURITY_MAPPING_GROUPS, mappings("group", "admin"));
                    resourceVerifier.verifyAttribute(WM_SECURITY_MAPPING_USERS, mappings("user", "john"));
                });
    }

    private ModelNode mappings(String from, String to) {
        ModelNode mapping = new ModelNode();
        mapping.get(FROM).set(from);
        mapping.get(TO).set(to);
        ModelNode mappings = new ModelNode();
        mappings.add(mapping);
        return mappings;
    }
}
