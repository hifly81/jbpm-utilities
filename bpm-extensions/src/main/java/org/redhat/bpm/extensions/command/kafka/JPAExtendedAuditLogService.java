package org.redhat.bpm.extensions.command.kafka;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.query.jpa.data.QueryCriteria;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.query.jpa.impl.QueryAndParameterAppender;
import org.kie.internal.query.QueryParameterIdentifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;
import java.util.*;

public class JPAExtendedAuditLogService extends JPAAuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(JPAExtendedAuditLogService.class);

    public List doQuery(String queryBase, QueryWhere queryData, Class<?> resultType, String subQuery) {
        Map<String, Object> queryParams = new HashMap();
        String queryString = createQuery(queryBase, queryData, queryParams, true, subQuery);
        logger.debug("Query statement:\n {}", queryString);
        if (logger.isDebugEnabled()) {
            StringBuilder paramsStr = new StringBuilder("PARAMS:");
            Map<String, Object> orderedParams = new TreeMap(queryParams);
            Iterator var9 = orderedParams.entrySet().iterator();

            while(var9.hasNext()) {
                Map.Entry<String, Object> entry = (Map.Entry)var9.next();
                paramsStr.append("\n " + (String)entry.getKey() + " : '" + entry.getValue() + "'");
            }

            logger.debug(paramsStr.toString());
        }

        EntityManager em = this.getEntityManager();
        Object newTx = this.joinTransaction(em);
        Query query = em.createQuery(queryString);
        List result = this.executeWithParameters(queryParams, query);
        logger.debug("rows " + result);
        this.closeEntityManager(em, newTx);
        return result;
    }

    private static String createQuery(String queryBase, QueryWhere queryWhere, Map<String, Object> queryParams, boolean skipMetaParams, String subQuery) {
        StringBuilder queryBuilder = new StringBuilder(queryBase);
        QueryAndParameterAppender queryAppender = new QueryAndParameterAppender(queryBuilder, queryParams);
        boolean addLastCriteria = false;
        List<Object[]> varValCriteriaList = new ArrayList();
        List<QueryCriteria> queryWhereCriteriaList = queryWhere.getCriteria();
        checkVarValCriteria(queryWhere, varValCriteriaList);
        Iterator iter = queryWhereCriteriaList.iterator();

        while(iter.hasNext()) {
            QueryCriteria criteria = (QueryCriteria)iter.next();
            if (criteria.getListId().equals(QueryParameterIdentifiers.LAST_VARIABLE_LIST)) {
                addLastCriteria = true;
                iter.remove();
            }
        }

        Iterator var15 = queryWhere.getCriteria().iterator();

        while(var15.hasNext()) {
            QueryCriteria criteria = (QueryCriteria)var15.next();
            String listId = criteria.getListId();
            switch(criteria.getType()) {
                case NORMAL:
                    queryAppender.addQueryParameters(criteria.getParameters(), listId, (Class)criteriaFieldClasses.get(listId), (String)criteriaFields.get(listId), criteria.isUnion());
                    break;
                case RANGE:
                    queryAppender.addRangeQueryParameters(criteria.getParameters(), listId, (Class)criteriaFieldClasses.get(listId), (String)criteriaFields.get(listId), criteria.isUnion());
                    break;
                case REGEXP:
                    List<String> stringParams = castToStringList(criteria.getParameters());
                    queryAppender.addRegexQueryParameters(stringParams, listId, (String)criteriaFields.get(listId), criteria.isUnion());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown criteria type in delete query builder: " + criteria.getType().toString());
            }
        }

        while(queryAppender.getParenthesesNesting() > 0) {
            queryAppender.closeParentheses();
        }

        boolean addWhereClause = !queryAppender.hasBeenUsed();
        if (!varValCriteriaList.isEmpty()) {
            addVarValCriteria(addWhereClause, queryAppender, "l", varValCriteriaList);
            addWhereClause = false;
        }

        if (addLastCriteria) {
            addLastInstanceCriteria(queryAppender);
        }

        if (subQuery != null && !subQuery.isEmpty()) {
            queryAppender.addToQueryBuilder(subQuery, false);
        }

        return queryBuilder.toString();
    }

    private List executeWithParameters(Map<String, Object> params, Query query) {
        this.applyMetaQueryParameters(params, query);
        List result = query.getResultList();
        return result;
    }

    private void applyMetaQueryParameters(Map<String, Object> params, Query query) {
        if (params != null && !params.isEmpty()) {
            Iterator var3 = params.keySet().iterator();

            while(var3.hasNext()) {
                String name = (String)var3.next();
                Object paramVal = params.get(name);
                if (paramVal != null) {
                    if ("firstResult".equals(name)) {
                        if ((Integer)paramVal > 0) {
                            query.setFirstResult((Integer)params.get(name));
                        }
                    } else if ("maxResults".equals(name)) {
                        if ((Integer)paramVal > 0) {
                            query.setMaxResults((Integer)params.get(name));
                        }
                    } else if ("flushMode".equals(name)) {
                        query.setFlushMode(FlushModeType.valueOf((String)params.get(name)));
                    } else if (!"orderType".equals(name) && !"orderby".equals(name) && !"filter".equals(name)) {
                        query.setParameter(name, params.get(name));
                    }
                }
            }
        }

    }

    private static void addLastInstanceCriteria(QueryAndParameterAppender queryAppender) {
        String lastQueryPhrase = "(l.id IN " + "(SELECT MAX(ll.id) FROM VariableInstanceLog ll GROUP BY ll.variableId, ll.processInstanceId)" + ") ";
        queryAppender.addToQueryBuilder(lastQueryPhrase, false);
    }

    private static List<String> castToStringList(List<Object> objectList) {
        List<String> stringList = new ArrayList(objectList.size());
        Iterator var2 = objectList.iterator();

        while(var2.hasNext()) {
            Object obj = var2.next();
            stringList.add(obj.toString());
        }

        return stringList;
    }


}
