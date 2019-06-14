package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.query.ProcessInstanceLogDeleteBuilderImpl;
import org.jbpm.process.audit.query.VarInstanceLogDeleteBuilderImpl;
import org.jbpm.query.jpa.data.QueryWhere;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.query.ParametrizedUpdate;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;

import java.util.List;

public class KafkaProcessInstanceLogDeleteBuilderImpl extends ProcessInstanceLogDeleteBuilderImpl {

    private static String PROCESS_INSTANCE_LOG_SELECT = "SELECT * \nFROM ProcessInstanceLog l\n";

    public KafkaProcessInstanceLogDeleteBuilderImpl(CommandExecutor cmdExecutor) {
        super(cmdExecutor);
    }

    public KafkaProcessInstanceLogDeleteBuilderImpl(JPAAuditLogService jpaAuditService) {
        super(jpaAuditService);
    }

    @Override
    public ParametrizedUpdate build() {
        return new ParametrizedUpdate() {
            private QueryWhere queryWhere = new QueryWhere(KafkaProcessInstanceLogDeleteBuilderImpl.this.getQueryWhere());

            public int execute() {
                List result4Kafka = getJpaAuditLogService().doQuery(PROCESS_INSTANCE_LOG_SELECT, this.queryWhere, KafkaProcessInstanceLogDeleteBuilderImpl.this.getQueryType(), KafkaProcessInstanceLogDeleteBuilderImpl.this.getSubQuery());

                //TODO send to kafka topic


                int result = getJpaAuditLogService().doDelete(KafkaProcessInstanceLogDeleteBuilderImpl.this.getQueryBase(), this.queryWhere, KafkaProcessInstanceLogDeleteBuilderImpl.this.getQueryType(), KafkaProcessInstanceLogDeleteBuilderImpl.this.getSubQuery());
                return result;
            }
        };
    }

    protected JPAExtendedAuditLogService getJpaAuditLogService() {
        JPAExtendedAuditLogService jpaAuditLogService = new JPAExtendedAuditLogService();
        return jpaAuditLogService;
    }




}
