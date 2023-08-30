/*
 * Copyright 2023 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.secretpad.common.factory;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Yaml property source factory
 *
 * @author yansi
 * @date 2023/5/10
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    /**
     * Create a new property source
     *
     * @param name
     * @param encodedResource
     * @return PropertySource
     * @throws IOException
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource)
            throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        Properties properties = null;
        try {
            properties = factory.getObject();
        } catch (IllegalStateException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            }
        }
        return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
    }
}