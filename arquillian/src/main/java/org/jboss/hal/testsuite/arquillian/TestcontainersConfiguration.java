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
package org.jboss.hal.testsuite.arquillian;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.DroneConfiguration;

public class TestcontainersConfiguration implements DroneConfiguration<TestcontainersConfiguration> {

    private static final String CONFIGURATION = "testcontainers-configuration";

    @Override
    public String getConfigurationName() {
        return CONFIGURATION;
    }

    @Override
    @Deprecated
    public TestcontainersConfiguration configure(final ArquillianDescriptor descriptor,
            final Class<? extends Annotation> qualifier) {
        return new TestcontainersConfiguration();
    }
}
