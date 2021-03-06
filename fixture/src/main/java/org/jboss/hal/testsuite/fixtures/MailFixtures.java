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
package org.jboss.hal.testsuite.fixtures;

import org.jboss.hal.resources.Ids;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.model.CrudConstants;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.jboss.hal.dmr.ModelDescriptionConstants.MAIL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAIL_SESSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER;

public final class MailFixtures {

    private static final String SESSION_PREFIX = "ms";

    public static final String MAIL_SMTP = "mail-smtp";
    public static final String SECRET = "secret";

    public static final Address SUBSYSTEM_ADDRESS = Address.subsystem(MAIL);

    // ------------------------------------------------------ session

    public static final String SESSION_CREATE = Ids.build(SESSION_PREFIX, CrudConstants.CREATE, Random.name());
    public static final String SESSION_READ = Ids.build(SESSION_PREFIX, CrudConstants.READ, Random.name());
    public static final String SESSION_UPDATE = Ids.build(SESSION_PREFIX, CrudConstants.UPDATE, Random.name());
    public static final String SESSION_DELETE = Ids.build(SESSION_PREFIX, CrudConstants.DELETE, Random.name());

    public static Address sessionAddress(String name) {
        return SUBSYSTEM_ADDRESS.and(MAIL_SESSION, name);
    }

    // ------------------------------------------------------ server

    public static Address serverAddress(String session, String type) {
        return sessionAddress(session).and(SERVER, type);
    }

    private MailFixtures() {
    }
}
