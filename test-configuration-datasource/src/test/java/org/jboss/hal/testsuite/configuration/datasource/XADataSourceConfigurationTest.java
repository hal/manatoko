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
package org.jboss.hal.testsuite.configuration.datasource;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.model.ResourceVerifier;
import org.jboss.hal.testsuite.page.configuration.DataSourcePage;
import org.jboss.hal.testsuite.test.Manatoko;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wildfly.extras.creaper.commands.datasources.AddXADataSource;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.XA_DATASOURCE_PROPERTIES;
import static org.jboss.hal.testsuite.container.WildFlyConfiguration.DEFAULT;
import static org.jboss.hal.testsuite.fixtures.DataSourceFixtures.URL_DELIMITER;
import static org.jboss.hal.testsuite.fixtures.DataSourceFixtures.XA_DATA_SOURCE_UPDATE;
import static org.jboss.hal.testsuite.fixtures.DataSourceFixtures.h2ConnectionUrl;
import static org.jboss.hal.testsuite.fixtures.DataSourceFixtures.xaDataSourceAddress;

@Manatoko
@Testcontainers
class XADataSourceConfigurationTest {

    @Container static WildFlyContainer wildFly = WildFlyContainer.standalone(DEFAULT);
    static OnlineManagementClient client;

    @BeforeAll
    static void setupModel() throws Exception {
        client = wildFly.managementClient();
        client.apply(new AddXADataSource.Builder<>(XA_DATA_SOURCE_UPDATE)
                .driverName("h2")
                .jndiName(Random.jndiName(XA_DATA_SOURCE_UPDATE))
                .addXaDatasourceProperty("URL", h2ConnectionUrl(XA_DATA_SOURCE_UPDATE))
                .build());
    }

    @Page DataSourcePage page;
    @Inject Console console;
    FormFragment form;

    @BeforeEach
    void prepare() {
        Map<String, String> params = new HashMap<>();
        params.put(NAME, XA_DATA_SOURCE_UPDATE);
        params.put("xa", "true");
        page.navigate(params);
    }

    @Test
    void connectionAttributesProperties() throws Exception {
        page.getXaTabs().select(Ids.build(Ids.XA_DATA_SOURCE, "connection", Ids.TAB));
        form = page.getXaConnectionForm();

        ModelNode properties = Random.properties();
        String urlDelimiter = Random.name();

        // there are two test related to xa-datasource-properties, because the backend uses a composite operation
        // to write the attribute and add the properties.
        form.edit();
        form.text(URL_DELIMITER, urlDelimiter);
        form.properties(XA_DATASOURCE_PROPERTIES).removeTags();
        form.properties(XA_DATASOURCE_PROPERTIES).add(properties);
        form.save();

        console.verifySuccess();
        new ResourceVerifier(xaDataSourceAddress(XA_DATA_SOURCE_UPDATE), client)
                .verifyAttribute(URL_DELIMITER, urlDelimiter);
        for (Property key : properties.asPropertyList()) {
            String value = key.getValue().asString();
            Address address = xaDataSourceAddress(XA_DATA_SOURCE_UPDATE).and(XA_DATASOURCE_PROPERTIES, key.getName());
            new ResourceVerifier(address, client).verifyAttribute(VALUE, value);
        }
    }

    @Test
    void connectionProperties() throws Exception {
        page.getXaTabs().select(Ids.build(Ids.XA_DATA_SOURCE, "connection", Ids.TAB));
        form = page.getXaConnectionForm();

        ModelNode properties = Random.properties();

        form.edit();
        form.properties(XA_DATASOURCE_PROPERTIES).removeTags();
        form.properties(XA_DATASOURCE_PROPERTIES).add(properties);
        form.save();

        console.verifySuccess();
        for (Property key : properties.asPropertyList()) {
            String value = key.getValue().asString();
            Address address = xaDataSourceAddress(XA_DATA_SOURCE_UPDATE).and(XA_DATASOURCE_PROPERTIES, key.getName());
            new ResourceVerifier(address, client).verifyAttribute(VALUE, value);
        }
    }

    @Test
    void connectionPropertiesSpecialCharacters() throws Exception {
        page.getXaTabs().select(Ids.build(Ids.XA_DATA_SOURCE, "connection", Ids.TAB));
        form = page.getXaConnectionForm();

        ModelNode properties = new ModelNode();
        properties.get("key1").set("jdbc:sybase:Tds:localhost:5000/mydatabase?JCONNECT_VERSION=6");
        properties.get("key2").set("jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=MyDatabase");
        properties.get("key3").set("jdbc:oracle:thin:@localhost:1521:orcalesid");
        properties.get("key4").set("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        properties.get("key5")
                .set("URL=jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=localhost)(PORT=1521))(CONNECT_DATA=(SERVER=dedicated)(SERVICE_NAME=test)))");

        form.edit();
        form.properties(XA_DATASOURCE_PROPERTIES).removeTags();
        form.properties(XA_DATASOURCE_PROPERTIES).add(properties);
        form.save();

        console.verifySuccess();
        for (Property key : properties.asPropertyList()) {
            String value = key.getValue().asString();
            Address address = xaDataSourceAddress(XA_DATA_SOURCE_UPDATE)
                    .and(XA_DATASOURCE_PROPERTIES, key.getName());
            new ResourceVerifier(address, client).verifyAttribute(VALUE, value);
        }
    }
}
