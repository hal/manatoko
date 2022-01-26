/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.manatoko;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatException;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

public class HalContainer extends GenericContainer<HalContainer> {

    private static final int PORT = 9090;
    private static final String IMAGE = "quay.io/halconsole/hal";
    private static final Logger LOGGER = LoggerFactory.getLogger(HalContainer.class);
    private static HalContainer currentInstance = null;

    public static HalContainer newInstance() {
        currentInstance = new HalContainer().withNetwork(Network.INSTANCE)
                .withNetworkAliases(Network.HAL).withExposedPorts(PORT)
                .waitingFor(Wait.forListeningPort());
        return currentInstance;
    }

    public static HalContainer currentInstance() {
        return currentInstance;
    }

    private String managementEndpoint;
    private final TokenFormatter tokenFormatter;

    private HalContainer() {
        super(DockerImageName.parse(IMAGE));
        this.tokenFormatter = new HalTokenFormatter();
    }

    /**
     * Tells the HAL standalone console to use the management endpoint of the specified WildFly instance.
     */
    public void connectTo(final WildFlyContainer wildFly) {
        this.managementEndpoint = wildFly.managementEndpoint();
    }

    String url(PlaceRequest placeRequest) {
        try {
            String fragment = tokenFormatter.toPlaceToken(placeRequest);
            String query = managementEndpoint != null ? "connect=" + managementEndpoint : null;
            return new URI("http", null, Network.HAL, PORT, "/", query, fragment).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    String.format("Unable to create URL for '%s': %s", placeRequest, e.getMessage()), e);
        }
    }

    // ------------------------------------------------------ navigation

    public void navigate(WebDriver driver, String nameToken) {
        navigate(driver, new PlaceRequest.Builder().nameToken(nameToken).build());
    }

    public void navigate(WebDriver driver, PlaceRequest placeRequest) {
        try {
            String fragment = tokenFormatter.toPlaceToken(placeRequest);
            String query = managementEndpoint != null ? "connect=" + managementEndpoint : null;
            String url = new URI("http", null, Network.HAL, PORT, "/", query, fragment).toString();
            LOGGER.debug("Navigate to {}", url);
            driver.get(url);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to navigate to '{}': {}", placeRequest, e.getMessage(), e);
        }
    }

    // ------------------------------------------------------ token formatter

    private static class HalTokenFormatter implements TokenFormatter {

        @Override
        public String toHistoryToken(List<PlaceRequest> placeRequestHierarchy) throws TokenFormatException {
            throw new UnsupportedOperationException();
        }

        @Override
        public PlaceRequest toPlaceRequest(String placeToken) throws TokenFormatException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<PlaceRequest> toPlaceRequestHierarchy(String historyToken) throws TokenFormatException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toPlaceToken(PlaceRequest placeRequest) throws TokenFormatException {
            StringBuilder builder = new StringBuilder();
            builder.append(placeRequest.getNameToken());
            Set<String> params = placeRequest.getParameterNames();
            if (params != null) {
                for (String param : params) {
                    builder.append(";").append(param).append("=").append(placeRequest.getParameter(param, null));
                }
            }
            return builder.toString();
        }
    }
}