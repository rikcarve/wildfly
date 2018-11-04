/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extension.microprofile.health;

import org.jboss.as.controller.OperationContext;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.undertow.Host;
import org.wildfly.extension.undertow.UndertowService;

import io.smallrye.health.SmallRyeHealthReporter;

/**
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2018 Red Hat inc.
 */
public class HealthContextService implements Service<HealthCheckHandler> {

    static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("extension", "health", "context");
    private static final String CONTEXT_NAME = "health";

    //private final Supplier<ExtensibleHttpManagement> extensibleHttpManagement;
    //private final Supplier<Host> host;
    private final InjectedValue<Host> host = new InjectedValue<>();
    private final InjectedValue<SmallRyeHealthReporter> healthReporter = new InjectedValue<>();

    private final boolean securityEnabled;

    static void install(OperationContext context, boolean securityEnabled) {
        HealthContextService service = new HealthContextService(securityEnabled);
        context.getServiceTarget().addService(SERVICE_NAME, service)
            .addDependency(UndertowService.virtualHostName("default-server", "default-host"), Host.class, service.host)
            .addDependency(MicroProfileHealthSubsystemDefinition.HEALTH_REPORTER_SERVICE, SmallRyeHealthReporter.class, service.healthReporter)
            .install();
    }

    HealthContextService(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    @Override
    public void start(StartContext context) {
        // access to the /health endpoint is unsecured
        host.getValue().registerHandler(CONTEXT_NAME, new HealthCheckHandler(healthReporter.getValue()));
    }

    @Override
    public void stop(StopContext context) {
        host.getValue().unregisterHandler(CONTEXT_NAME);
    }

    @Override
    public HealthCheckHandler getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }
}
