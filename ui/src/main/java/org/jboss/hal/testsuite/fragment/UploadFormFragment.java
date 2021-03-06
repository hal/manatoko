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
package org.jboss.hal.testsuite.fragment;

import java.io.File;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.fragment.Root;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.arquillian.graphene.Graphene.waitGui;

public class UploadFormFragment {

    @Root private WebElement root;
    @FindBy(css = "label[for='" + Ids.UPLOAD_FILE_INPUT + "']") private WebElement uploadFileLabel;
    @FindBy(id = Ids.UPLOAD_FILE_INPUT) private WebElement fileInput;

    public void uploadFile(File fileToUpload) {
        fileInput.sendKeys(fileToUpload.getAbsolutePath());
        Graphene.waitGui().until().element(uploadFileLabel).text().equalTo(fileToUpload.getName());
    }

    public static UploadFormFragment getUploadForm(WebElement serchContext) {
        WebElement formElement = serchContext.findElement(By.cssSelector("form." + CSS.upload));
        waitGui().until().element(formElement).is().visible();
        return Graphene.createPageFragment(UploadFormFragment.class, formElement);
    }
}
