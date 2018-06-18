package org.redhat.bpm.query.queryparam;

import org.dashbuilder.dataset.filter.ColumnFilter;
import org.jbpm.services.api.query.QueryParamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.dashbuilder.dataset.filter.FilterFactory.*;


public class TasksByGroupsAndVariablesAndParamsNotActualOwnerFilter implements QueryParamBuilder<ColumnFilter> {

    private static final Logger LOG = LoggerFactory.getLogger(TasksByGroupsAndVariablesAndParamsNotActualOwnerFilter.class);

    private Map<String, Object> parameters;
    private boolean built = false;

    public TasksByGroupsAndVariablesAndParamsNotActualOwnerFilter(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public ColumnFilter build() {

        if (built) {
            return null;
        }

        // extract mandatory parameters
        List<String> groups = (List<String>) parameters.get("groups");
        String actualOwner = (String)parameters.get("actualOwner");

        LOG.debug("groups: {}", groups);
        LOG.debug("actualOwner: {}", actualOwner);

        // extract optional parameters
        List<String> status = (List<String>) parameters.get("status");
        Map<String, List<String>> paramsMap = (Map<String, List<String>>) parameters.get("paramsMap");
        Map<String, List<String>> variablesMap = (Map<String, List<String>>) parameters.get("variablesMap");

        LOG.debug("status: {}", status);
        LOG.debug("paramsMap: {}", paramsMap);
        LOG.debug("variablesMap: {}", variablesMap);

        ColumnFilter filter = AND(in("potOwner", groups));
        filter = AND(filter, notEqualsTo("actualowner", actualOwner));

        if (status != null) {
            filter = AND(filter, in("status", status));
        }

        if (paramsMap != null) {
            filter = AND(filter, buildMapFilter(paramsMap, "paramname", "paramvalue"));
        }

        if (variablesMap != null) {
            filter = AND(filter, buildMapFilter(variablesMap, "variablename", "variablevalue"));
        }

        built = true;

        LOG.debug("filter instance: {}", filter);
        return filter;
    }

    private ColumnFilter buildMapFilter(Map<String, List<String>> map, String name, String value) {

        return OR(map.entrySet().stream()
            .map(entry -> AND(equalsTo(name, entry.getKey()), in(value, entry.getValue()))
        ).toArray(ColumnFilter[]::new));

    }

}
