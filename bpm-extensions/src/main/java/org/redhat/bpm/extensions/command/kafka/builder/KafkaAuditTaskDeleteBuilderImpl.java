package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.services.task.audit.service.AuditTaskDeleteBuilderImpl;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.query.ParametrizedUpdate;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;

import java.util.List;

public class KafkaAuditTaskDeleteBuilderImpl extends AuditTaskDeleteBuilderImpl {

    private static String AUDIT_TASK_IMPL_SELECT = "SELECT * \nFROM AuditTaskImpl l\n";

    public KafkaAuditTaskDeleteBuilderImpl(CommandExecutor cmdExecutor) {
        super(cmdExecutor);
    }

    public KafkaAuditTaskDeleteBuilderImpl(JPAAuditLogService jpaAuditService) {
        super(jpaAuditService);
    }

    @Override
    public ParametrizedUpdate build() {
        return new ParametrizedUpdate() {
            private QueryWhere queryWhere = new QueryWhere(KafkaAuditTaskDeleteBuilderImpl.this.getQueryWhere());

            public int execute() {
                List result4Kafka = getJpaAuditLogService().doQuery(AUDIT_TASK_IMPL_SELECT, this.queryWhere, KafkaAuditTaskDeleteBuilderImpl.this.getQueryType(), KafkaAuditTaskDeleteBuilderImpl.this.getSubQuery());

                //TODO send to kafka topic


                int result = getJpaAuditLogService().doDelete(KafkaAuditTaskDeleteBuilderImpl.this.getQueryBase(), this.queryWhere, KafkaAuditTaskDeleteBuilderImpl.this.getQueryType(), KafkaAuditTaskDeleteBuilderImpl.this.getSubQuery());
                return result;
            }
        };
    }

    protected JPAExtendedAuditLogService getJpaAuditLogService() {
        JPAExtendedAuditLogService jpaAuditLogService = new JPAExtendedAuditLogService();
        return jpaAuditLogService;
    }




}
