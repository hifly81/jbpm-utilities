package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.executor.impl.jpa.RequestInfoDeleteBuilderImpl;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.services.task.audit.service.AuditTaskDeleteBuilderImpl;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.query.ParametrizedUpdate;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;

import java.util.List;

public class KafkaRequestInfoDeleteBuilderImpl extends RequestInfoDeleteBuilderImpl {

    private static String REQUEST_INFO_LOG_SELECT = "SELECT * \nFROM RequestInfo l\n";

    public KafkaRequestInfoDeleteBuilderImpl(CommandExecutor cmdExecutor) {
        super(cmdExecutor);
    }

    public KafkaRequestInfoDeleteBuilderImpl(JPAAuditLogService jpaAuditService) {
        super(jpaAuditService);
    }

    @Override
    public ParametrizedUpdate build() {
        return new ParametrizedUpdate() {
            private QueryWhere queryWhere = new QueryWhere(KafkaRequestInfoDeleteBuilderImpl.this.getQueryWhere());

            public int execute() {
                List result4Kafka = getJpaAuditLogService().doQuery(REQUEST_INFO_LOG_SELECT, this.queryWhere, KafkaRequestInfoDeleteBuilderImpl.this.getQueryType(), KafkaRequestInfoDeleteBuilderImpl.this.getSubQuery());

                //TODO send to kafka topic


                int result = getJpaAuditLogService().doDelete(KafkaRequestInfoDeleteBuilderImpl.this.getQueryBase(), this.queryWhere, KafkaRequestInfoDeleteBuilderImpl.this.getQueryType(), KafkaRequestInfoDeleteBuilderImpl.this.getSubQuery());
                return result;
            }
        };
    }

    protected JPAExtendedAuditLogService getJpaAuditLogService() {
        JPAExtendedAuditLogService jpaAuditLogService = new JPAExtendedAuditLogService();
        return jpaAuditLogService;
    }




}
