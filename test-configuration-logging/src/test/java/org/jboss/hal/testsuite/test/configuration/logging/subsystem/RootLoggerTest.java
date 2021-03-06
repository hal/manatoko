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
package org.jboss.hal.testsuite.test.configuration.logging.subsystem;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.fixtures.LoggingFixtures;
import org.jboss.hal.testsuite.page.configuration.LoggingConfigurationPage;
import org.jboss.hal.testsuite.page.configuration.LoggingSubsystemConfigurationPage;
import org.jboss.hal.testsuite.test.Manatoko;
import org.jboss.hal.testsuite.test.configuration.logging.AbstractRootLoggerTest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.jboss.hal.testsuite.container.WildFlyConfiguration.DEFAULT;

@Manatoko
@Testcontainers
class RootLoggerTest extends AbstractRootLoggerTest {

    @Container static WildFlyContainer wildFly = WildFlyContainer.standalone(DEFAULT);

    @Inject Console console;
    @Page LoggingSubsystemConfigurationPage page;

    @Override
    protected Address rootLoggerAddress() {
        return LoggingFixtures.ROOT_LOGGER_ADDRESS;
    }

    @Override
    protected void navigateToPage() {
        page.navigate();
        console.verticalNavigation().selectPrimary("logging-root-logger-item");
    }

    @Override
    protected LoggingConfigurationPage getPage() {
        return page;
    }
}
