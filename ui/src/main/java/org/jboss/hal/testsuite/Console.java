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
package org.jboss.hal.testsuite;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.container.HalContainer;
import org.jboss.hal.testsuite.container.WildFlyContainer;
import org.jboss.hal.testsuite.fragment.AddResourceDialogFragment;
import org.jboss.hal.testsuite.fragment.ConfirmationDialogFragment;
import org.jboss.hal.testsuite.fragment.DialogFragment;
import org.jboss.hal.testsuite.fragment.FooterFragment;
import org.jboss.hal.testsuite.fragment.HeaderFragment;
import org.jboss.hal.testsuite.fragment.VerticalNavigationFragment;
import org.jboss.hal.testsuite.fragment.WizardFragment;
import org.jboss.hal.testsuite.fragment.finder.FinderFragment;
import org.jboss.hal.testsuite.fragment.finder.FinderPath;
import org.jboss.hal.testsuite.fragment.finder.FinderSegment;
import org.jboss.hal.testsuite.model.Library;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatException;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.jboss.arquillian.graphene.Graphene.waitGui;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static org.jboss.hal.resources.CSS.alertDanger;
import static org.jboss.hal.resources.CSS.alertDismissable;
import static org.jboss.hal.resources.CSS.alertSuccess;
import static org.jboss.hal.resources.CSS.navPfVerticalHal;
import static org.jboss.hal.resources.CSS.navbar;
import static org.jboss.hal.resources.CSS.toastNotificationsListPf;
import static org.jboss.hal.resources.UIConstants.MEDIUM_TIMEOUT;
import static org.jboss.hal.testsuite.page.Places.finderPlace;
import static org.junit.Assert.assertEquals;

public class Console {

    private static final String DOT = ".";

    @Drone private WebDriver browser;
    private final String url;
    private final TokenFormatter tokenFormatter;

    public Console() {
        url = HalContainer.instance().url() + "?connect=" + WildFlyContainer.instance().managementEndpoint();
        tokenFormatter = new HalTokenFormatter();
    }

    // ------------------------------------------------------ navigation

    /**
     * Navigates to the place request and waits until the id {@link Ids#ROOT_CONTAINER} is present.
     */
    public void navigate(PlaceRequest request) {
        navigate(request, By.id(Ids.ROOT_CONTAINER));
    }

    /**
     * Navigates to the place request and waits until the selector is present.
     */
    public void navigate(PlaceRequest request, By selector) {
        browser.navigate().to(url + "#" + tokenFormatter.toPlaceToken(request));
        waitModel().until().element(selector).is().present();
        browser.manage().window().maximize();
    }

    public void reload() {
        browser.navigate().refresh();
        waitModel().until().element(By.id(Ids.ROOT_CONTAINER)).is().present();
    }

    public void verify(PlaceRequest placeRequest) {
        String expected = tokenFormatter.toPlaceToken(placeRequest);
        String actual = StringUtils.substringAfter(browser.getCurrentUrl(), "#");
        assertEquals(expected, actual);
    }

    // ------------------------------------------------------ notification

    /**
     * Waits until all notifications are gone.
     */
    public void waitNoNotification() {
        List<WebElement> dismissibleNotifications = By.cssSelector(DOT + alertDismissable).findElements(browser);
        for (int remainingExpected = dismissibleNotifications.size(); !dismissibleNotifications.isEmpty()
                && remainingExpected > 0; remainingExpected--) {

            WebElement button = dismissibleNotifications.get(0).findElement(By.cssSelector("button.close"));
            if (button != null) {
                button.click();
            }
            dismissibleNotifications = By.cssSelector(DOT + alertDismissable).findElements(browser);
        }
        waitModel().until().element(By.cssSelector(DOT + toastNotificationsListPf + ":empty")).is().present();
    }

    /**
     * Verifies that a success notification is visible
     */
    public void verifySuccess() {
        verifyNotification(alertSuccess);
    }

    /**
     * Verifies that an error notification is visible
     */
    public void verifyError() {
        verifyNotification(alertDanger);
    }

    /**
     * Verify there is no error notification
     */
    public boolean verifyNoError() {
        return By.cssSelector(DOT + toastNotificationsListPf + " ." + alertDanger).findElements(browser).isEmpty();
    }

    private void verifyNotification(String css) {
        waitModel().until() // use waitModel() since it might take some time until the notification is visible
                .element(By.cssSelector(DOT + toastNotificationsListPf + " ." + css))
                .is().visible();
    }

    // ------------------------------------------------------ fragment access (a-z)

    /**
     * Returns the currently opened add resource dialog.
     */
    public AddResourceDialogFragment addResourceDialog() {
        return dialog(AddResourceDialogFragment.class);
    }

    /**
     * Returns the currently opened confirmation dialog.
     */
    public ConfirmationDialogFragment confirmationDialog() {
        return dialog(ConfirmationDialogFragment.class);
    }

    /**
     * Returns the currently opened dialog.
     */
    public DialogFragment dialog() {
        return dialog(DialogFragment.class);
    }

    public <T extends DialogFragment> T dialog(Class<T> dialogClass) {
        Library.letsSleep(MEDIUM_TIMEOUT);
        WebElement dialogElement = browser.findElement(By.id(Ids.HAL_MODAL));
        waitGui().until().element(dialogElement).is().visible();
        return createPageFragment(dialogClass, dialogElement);
    }

    /**
     * Navigates to the specified token, creates and returns the finder fragment
     */
    public FinderFragment finder(String token) {
        return finder(token, null);
    }

    /**
     * Navigates to the specified token, selects the finder path, creates and returns the finder fragment
     */
    public FinderFragment finder(String token, FinderPath path) {
        By selector = By.id(Ids.FINDER);
        if (path != null && !path.isEmpty()) {
            FinderSegment segment = path.last();
            if (segment.getItemId() != null) {
                selector = By.id(segment.getItemId());
            } else if (segment.getColumnId() != null) {
                selector = By.id(segment.getColumnId());
            }
        }
        navigate(finderPlace(token, path), selector);
        return createPageFragment(FinderFragment.class, browser.findElement(selector));
    }

    public FooterFragment footer() {
        return createPageFragment(FooterFragment.class, browser.findElement(By.cssSelector("footer.footer")));
    }

    public HeaderFragment header() {
        return createPageFragment(HeaderFragment.class, browser.findElement(By.cssSelector("nav." + navbar)));
    }

    public VerticalNavigationFragment verticalNavigation() {
        return createPageFragment(VerticalNavigationFragment.class,
                browser.findElement(By.cssSelector(DOT + navPfVerticalHal)));
    }

    public WizardFragment wizard() {
        return wizard(WizardFragment.class);
    }

    public <T extends WizardFragment> T wizard(Class<T> wizardClass) {
        By wizardSelector = By.id(Ids.HAL_WIZARD);
        waitGui().until().element(wizardSelector).is().visible();
        return createPageFragment(wizardClass, browser.findElement(wizardSelector));
    }

    // ------------------------------------------------------ elements

    /**
     * Scrolls the specified element into the visible area of the browser window.
     *
     * @param element to scroll to
     * @param scrollIntoViewOptions - see
     *        <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollIntoView#Parameters">related js
     *        documentation</a>
     * @return provided element
     */
    public WebElement scrollIntoView(WebElement element, String scrollIntoViewOptions) {
        JavascriptExecutor js = (JavascriptExecutor) browser;
        js.executeScript("arguments[0].scrollIntoView(" + scrollIntoViewOptions + ");", element);
        return element;
    }

    /**
     * The top of the element will be aligned to the top of the visible area of the scrollable ancestor.
     *
     * @param element to scroll to
     * @return provided element
     */
    public WebElement scrollIntoView(WebElement element) {
        return scrollIntoView(element, "true");
    }

    /**
     * The bottom of the element will be aligned as much as possible to the bottom of the visible area of the scrollable
     * ancestor.
     *
     * @param element to scroll to
     * @return provided element
     */
    public WebElement scrollToBottom(WebElement element) {
        return scrollIntoView(element, "false");
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
