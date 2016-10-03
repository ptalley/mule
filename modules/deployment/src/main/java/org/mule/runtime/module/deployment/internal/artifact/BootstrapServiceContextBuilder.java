/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.runtime.module.deployment.internal.artifact;

import static org.mule.runtime.core.config.bootstrap.ClassLoaderRegistryBootstrapDiscoverer.BOOTSTRAP_PROPERTIES;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.config.bootstrap.BootstrapService;
import org.mule.runtime.core.config.bootstrap.BootstrapServiceDiscoverer;
import org.mule.runtime.core.config.bootstrap.PropertiesBootstrapService;
import org.mule.runtime.core.config.bootstrap.PropertiesBootstrapServiceDiscoverer;
import org.mule.runtime.core.config.builders.AbstractConfigurationBuilder;
import org.mule.runtime.core.util.PropertiesUtils;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPlugin;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class BootstrapServiceContextBuilder extends AbstractConfigurationBuilder {

  private final List<ArtifactPlugin> artifactPlugins;

  public BootstrapServiceContextBuilder(List<ArtifactPlugin> artifactPlugins) {
    this.artifactPlugins = artifactPlugins;
  }

  @Override
  protected void doConfigure(MuleContext muleContext) throws Exception {
    final PropertiesBootstrapServiceDiscoverer propertiesBootstrapServiceDiscoverer =
        new PropertiesBootstrapServiceDiscoverer(this.getClass().getClassLoader());

    List<BootstrapService> bootstrapServices = new LinkedList<>();
    bootstrapServices.addAll(propertiesBootstrapServiceDiscoverer.discover());

    for (ArtifactPlugin artifactPlugin : artifactPlugins) {
      final URL localResource = artifactPlugin.getArtifactClassLoader().findResource(BOOTSTRAP_PROPERTIES);

      if (localResource != null) {
        final Properties properties = PropertiesUtils.loadProperties(localResource);
        final BootstrapService pluginBootstrapService =
            new PropertiesBootstrapService(artifactPlugin.getArtifactClassLoader().getClassLoader(), properties);

        bootstrapServices.add(pluginBootstrapService);
      }
    }

    BootstrapServiceDiscoverer bootstrapServiceDiscoverer = () -> bootstrapServices;

    muleContext.setBootstrapServiceDiscoverer(bootstrapServiceDiscoverer);

  }
}
