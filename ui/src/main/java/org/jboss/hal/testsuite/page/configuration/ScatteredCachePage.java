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
package org.jboss.hal.testsuite.page.configuration;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.testsuite.fragment.EmptyState;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.SelectFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.fragment.TabsFragment;
import org.jboss.hal.testsuite.page.BasePage;
import org.jboss.hal.testsuite.page.Place;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CACHE_CONTAINER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.resources.CSS.bootstrapSelect;
import static org.jboss.hal.resources.CSS.btn;
import static org.jboss.hal.resources.CSS.btnDefault;
import static org.jboss.hal.resources.CSS.btnGroup;
import static org.jboss.hal.testsuite.Selectors.contains;

@Place(NameTokens.SCATTERED_CACHE)
public class ScatteredCachePage extends BasePage {

    public static final String FILE_STORE_WRITE_BEHAVIOUR_TAB = "scattered-cache-cache-store-file-write-tab";
    private static final String FILE_STORE_ATTRIBUTES_TAB = "scattered-cache-cache-store-file-attributes-tab";
    public static final String CUSTOM_STORE_WRITE_BEHAVIOUR_TAB = "scattered-cache-cache-store-custom-write-tab";
    private static final String CUSTOM_STORE_ATTRIBUTES_TAB = "scattered-cache-cache-store-custom-attributes-tab";
    public static final String JDBC_STORE_WRITE_BEHAVIOUR_TAB = "scattered-cache-cache-store-jdbc-write-tab";
    private static final String JDBC_STORE_ATTRIBUTES_TAB = "scattered-cache-cache-store-jdbc-attributes-tab";
    private static final String JDBC_STORE_STRING_TABLE_TAB = "scattered-cache-cache-store-jdbc-string-table-tab";
    public static final String HOTROD_STORE_WRITE_BEHAVIOUR_TAB = "scattered-cache-cache-store-hot-rod-write-tab";
    private static final String HOTROD_STORE_ATTRIBUTES_TAB = "scattered-cache-cache-store-hot-rod-attributes-tab";

    @FindBy(id = "scattered-cache-tab-container") private TabsFragment configurationTab;

    @FindBy(id = "scattered-cache-form") private FormFragment configurationForm;

    @FindBy(id = "scattered-cache-cache-component-expiration-form") private FormFragment expirationForm;

    @FindBy(id = "scattered-cache-cache-component-locking-form") private FormFragment lockingForm;

    @FindBy(id = "scattered-cache-cache-component-partition-handling-form") private FormFragment partitionHandlingForm;

    @FindBy(id = "scattered-cache-cache-component-state-transfer-form") private FormFragment stateTransferForm;

    @FindBy(id = "scattered-cache-cache-component-transaction-form") private FormFragment transactionForm;

    @FindBy(css = "label[for='scattered-cache-memory-select'] + div."
            + bootstrapSelect) private SelectFragment switchMemoryDropdown;

    @FindBy(id = "scattered-cache-cache-memory-heap-form") private FormFragment heapMemoryForm;

    @FindBy(id = "scattered-cache-cache-memory-off-heap-form") private FormFragment offHeapMemoryForm;

    @FindBy(css = "label[for='scattered-cache-store-select'] + div."
            + bootstrapSelect) private SelectFragment switchStoreDropdown;

    @FindBy(id = "scattered-cache-store-empty") private EmptyState emptyStoreForm;

    @FindBy(id = "scattered-cache-cache-store-file-tab-container") private TabsFragment fileStoreTab;

    @FindBy(id = "scattered-cache-cache-store-file-form") private FormFragment fileStoreAttributesForm;

    @FindBy(id = "scattered-cache-cache-store-file-behind-form") private FormFragment fileStoreWriteBehindForm;

    @FindBy(id = "scattered-cache-cache-store-custom-tab-container") private TabsFragment customStoreTab;

    @FindBy(id = "scattered-cache-cache-store-custom-form") private FormFragment customStoreAttributesForm;

    @FindBy(id = "scattered-cache-cache-store-custom-behind-form") private FormFragment customStoreWriteBehindForm;

    @FindBy(id = "scattered-cache-cache-store-jdbc-tab-container") private TabsFragment jdbcStoreTab;

    @FindBy(id = "scattered-cache-cache-store-jdbc-form") private FormFragment jdbcStoreAttributesForm;

    @FindBy(id = "scattered-cache-cache-store-jdbc-behind-form") private FormFragment jdbcStoreWriteBehindForm;

    @FindBy(id = "scattered-cache-cache-store-jdbc-string-table-form") private FormFragment jdbcStoreStringTableForm;

    @FindBy(id = "scattered-cache-cache-store-hot-rod-tab-container") private TabsFragment hotrodStoreTab;

    @FindBy(id = "scattered-cache-cache-store-hot-rod-form") private FormFragment hotrodStoreAttributesForm;

    @FindBy(id = "scattered-cache-cache-store-hot-rod-behind-form") private FormFragment hotrodStoreWriteBehindForm;

    @FindBy(id = "scattered-cache-backups-table_wrapper") private TableFragment backupsTable;

    @FindBy(id = "scattered-cache-backups-form") private FormFragment backupsForm;

    @Override
    public void navigate(String cacheContainer, String scatteredCache) {
        Map<String, String> params = new HashMap<>();
        params.put(CACHE_CONTAINER, cacheContainer);
        params.put(NAME, scatteredCache);
        navigate(params);
    }

    public FormFragment getConfigurationForm() {
        configurationTab.select("scattered-cache-tab");
        return configurationForm;
    }

    public FormFragment getExpirationForm() {
        configurationTab.select("scattered-cache-cache-component-expiration-tab");
        return expirationForm;
    }

    public FormFragment getLockingForm() {
        configurationTab.select("scattered-cache-cache-component-locking-tab");
        return lockingForm;
    }

    public FormFragment getPartitionHandlingForm() {
        configurationTab.select("scattered-cache-cache-component-partition-handling-tab");
        return partitionHandlingForm;
    }

    public FormFragment getStateTransferForm() {
        configurationTab.select("scattered-cache-cache-component-state-transfer-tab");
        return stateTransferForm;
    }

    public FormFragment getTransactionForm() {
        configurationTab.select("scattered-cache-cache-component-transaction-tab");
        return transactionForm;
    }

    public TableFragment getBackupsTable() {
        return backupsTable;
    }

    public FormFragment getBackupsForm() {
        return backupsForm;
    }

    public void selectHeapMemory() {
        switchMemoryDropdown.select("Heap", "heap");
    }

    public FormFragment getHeapMemoryForm() {
        return heapMemoryForm;
    }

    public void selectOffHeapMemory() {
        switchMemoryDropdown.select("Off Heap", "off-heap");
    }

    public FormFragment getOffHeapMemoryForm() {
        return offHeapMemoryForm;
    }

    public void selectFileStoreAttributes() {
        console.waitNoNotification();
        switchStoreDropdown.select("File", "file");
        fileStoreTab.select(FILE_STORE_ATTRIBUTES_TAB);
    }

    public void selectFileStoreWriteBehaviour() {
        console.waitNoNotification();
        switchStoreDropdown.select("File", "file");
        fileStoreTab.select(FILE_STORE_WRITE_BEHAVIOUR_TAB);
    }

    public FormFragment getFileStoreAttributesForm() {
        return fileStoreAttributesForm;
    }

    public FormFragment getFileStoreWriteBehindForm() {
        return fileStoreWriteBehindForm;
    }

    public void selectCustomStoreAttributes() {
        console.waitNoNotification();
        switchStoreDropdown.select("Custom", "custom");
        customStoreTab.select(CUSTOM_STORE_ATTRIBUTES_TAB);
    }

    public void selectCustomStoreWriteBehaviour() {
        console.waitNoNotification();
        switchStoreDropdown.select("Custom", "custom");
        customStoreTab.select(CUSTOM_STORE_WRITE_BEHAVIOUR_TAB);
    }

    public FormFragment getCustomStoreAttributesForm() {
        return customStoreAttributesForm;
    }

    public FormFragment getCustomStoreWriteBehindForm() {
        return customStoreWriteBehindForm;
    }

    public void selectJdbcStoreAttributes() {
        console.waitNoNotification();
        switchStoreDropdown.selectExact("JDBC", "jdbc");
        jdbcStoreTab.select(JDBC_STORE_ATTRIBUTES_TAB);
    }

    public void selectJdbcStoreWriteBehaviour() {
        console.waitNoNotification();
        switchStoreDropdown.selectExact("JDBC", "jdbc");
        jdbcStoreTab.select(JDBC_STORE_WRITE_BEHAVIOUR_TAB);
    }

    public void selectJdbcStoreStringTable() {
        console.waitNoNotification();
        switchStoreDropdown.selectExact("JDBC", "jdbc");
        jdbcStoreTab.select(JDBC_STORE_STRING_TABLE_TAB);
    }

    public FormFragment getJdbcStoreAttributesForm() {
        return jdbcStoreAttributesForm;
    }

    public FormFragment getJdbcStoreWriteBehindForm() {
        return jdbcStoreWriteBehindForm;
    }

    public FormFragment getJdbcStoreStringTableForm() {
        return jdbcStoreStringTableForm;
    }

    public void selectHotrodStoreAttributes() {
        console.waitNoNotification();
        switchStoreDropdown.selectExact("Hot Rod", "hotrod");
        hotrodStoreTab.select(HOTROD_STORE_ATTRIBUTES_TAB);
    }

    public void selectHotrodStoreWriteBehaviour() {
        console.waitNoNotification();
        switchStoreDropdown.selectExact("Hot Rod", "hotrod");
        hotrodStoreTab.select(HOTROD_STORE_WRITE_BEHAVIOUR_TAB);
    }

    public FormFragment getHotrodStoreAttributesForm() {
        return hotrodStoreAttributesForm;
    }

    public FormFragment getHotrodStoreWriteBehindForm() {
        return hotrodStoreWriteBehindForm;
    }

    public void switchBehaviour() {
        WebElement button = browser.findElement(
                ByJQuery.selector("button." + btn + "." + btnDefault + contains("Switch Behaviour") + ":visible"));
        Graphene.waitGui().until().element(button).is().visible();
        button.click();
    }

    public SelectFragment getSelectStoreDropdown() {
        WebElement selectStoreDropdown = emptyStoreForm.getRoot()
                .findElement(By.cssSelector("div." + btnGroup + "." + bootstrapSelect));
        Graphene.waitGui().until().element(selectStoreDropdown).is().visible();
        return Graphene.createPageFragment(SelectFragment.class, selectStoreDropdown);
    }

    public EmptyState getEmptyStoreForm() {
        return emptyStoreForm;
    }
}
