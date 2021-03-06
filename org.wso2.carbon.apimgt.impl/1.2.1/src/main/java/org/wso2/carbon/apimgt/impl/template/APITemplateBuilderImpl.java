/*
 *  Copyright WSO2 Inc.
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

package org.wso2.carbon.apimgt.impl.template;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.dto.Environment;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Constructs API and resource configurations for the ESB/Synapse using a Apache velocity
 * templates.
 */
public class APITemplateBuilderImpl implements APITemplateBuilder {

    private static final Log log = LogFactory.getLog(APITemplateBuilderImpl.class);

    public static final String TEMPLATE_TYPE_VELOCITY = "velocity_template";

    private API api;
    private List<HandlerConfig> handlers = new ArrayList<HandlerConfig>();

    public APITemplateBuilderImpl(API api) {
        this.api = api;
    }

    @Override
    public String getConfigStringForTemplate(Environment environment) throws APITemplateException {
        StringWriter writer = new StringWriter();

        try {
            // build the context for template and apply the necessary decorators
            ConfigContext configcontext = new APIConfigContext(this.api);

            configcontext = new TransportConfigContext(configcontext, api);
            configcontext = new ResourceConfigContext(configcontext, api);
            // this should be initialised before endpoint config context.
            configcontext = new EndpointBckConfigContext(configcontext,api);
            configcontext = new EndpointConfigContext(configcontext, api);
            configcontext = new SecurityConfigContext(configcontext,api);
            configcontext = new JwtConfigContext(configcontext);
            configcontext = new ResponseCacheConfigContext(configcontext, api);
            configcontext = new HandlerConfigContex(configcontext, handlers);
            configcontext = new EnvironmentConfigContext(configcontext, environment);
            configcontext = new TemplateUtilContext(configcontext);

            //@todo: this validation might be better to do when the builder is initialized.
            configcontext.validate();

            VelocityContext context = configcontext.getContext();

            /*  first, initialize velocity engine  */
            VelocityEngine velocityengine = new VelocityEngine();
            velocityengine.init();

            Template t = velocityengine.getTemplate(this.getTemplatePath());

            t.merge(context, writer);

        } catch (Exception e) {
            log.error("Velocity Error", e);
            throw new APITemplateException("Velocity Error", e);
        }
        return writer.toString();
    }

    @Override
    public OMElement getConfigXMLForTemplate(Environment environment) throws APITemplateException {
        try {
            return AXIOMUtil.stringToOM(getConfigStringForTemplate(environment));
        } catch (XMLStreamException e) {
            String msg = "Error converting string to OMElement - String: " + getConfigStringForTemplate(environment);
            log.error(msg, e);
            throw new APITemplateException(msg, e);
        }
    }

    public void addHandler(String handlerName, Map<String, String> properties) {
        addHandlerPriority(handlerName, properties, handlers.size());
    }

    public void addHandlerPriority(String handlerName, Map<String, String> properties, int priority) {
        HandlerConfig handler = new HandlerConfig(handlerName, properties);
        handlers.add(priority, handler);
    }

    public String getTemplatePath() {
        return "repository" + File.separator + "resources" + File.separator + "api_templates" + File.separator + APITemplateBuilderImpl.TEMPLATE_TYPE_VELOCITY + ".xml";
    }

}
