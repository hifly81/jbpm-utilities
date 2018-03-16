package org.redhat.bpm.query.queryparam;

import org.jbpm.services.api.query.QueryParamBuilder;
import org.jbpm.services.api.query.QueryParamBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class PotOwnedTasksByVariablesAndParamsFilterFactory implements QueryParamBuilderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PotOwnedTasksByVariablesAndParamsFilterFactory.class);
    public static final String FILTER_NAME = "potOwnedTasksByVariablesAndParamsFilter";

    @Override
    public boolean accept(String identifier) {
        return FILTER_NAME.equalsIgnoreCase(identifier);
    }

    @Override
    public QueryParamBuilder<?> newInstance(Map<String, Object> parameters) {
        LOG.debug("register query parameter builder --> {}", FILTER_NAME);
        return new PotOwnedTasksByVariablesAndParamsFilter(parameters);
    }

}
