package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.query.VarInstanceLogDeleteBuilderImpl;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.services.task.audit.service.TaskEventDeleteBuilderImpl;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.query.ParametrizedUpdate;
import org.kie.internal.runtime.manager.audit.query.VariableInstanceLogDeleteBuilder;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;

import java.util.List;

public class KafkaVariableInstanceLogDeleteBuilderImpl extends VarInstanceLogDeleteBuilderImpl {

    private static String VARIABLE_INSTANCE_LOG_SELECT = "SELECT * \nFROM VariableInstanceLog l\n";

    public KafkaVariableInstanceLogDeleteBuilderImpl(CommandExecutor cmdExecutor) {
        super(cmdExecutor);
    }

    public KafkaVariableInstanceLogDeleteBuilderImpl(JPAAuditLogService jpaAuditService) {
        super(jpaAuditService);
    }

    @Override
    public ParametrizedUpdate build() {
        return new ParametrizedUpdate() {
            private QueryWhere queryWhere = new QueryWhere(KafkaVariableInstanceLogDeleteBuilderImpl.this.getQueryWhere());

            public int execute() {
                List result4Kafka = getJpaAuditLogService().doQuery(VARIABLE_INSTANCE_LOG_SELECT, this.queryWhere, KafkaVariableInstanceLogDeleteBuilderImpl.this.getQueryType(), KafkaVariableInstanceLogDeleteBuilderImpl.this.getSubQuery());

                //TODO send to kafka topic


                int result = getJpaAuditLogService().doDelete(KafkaVariableInstanceLogDeleteBuilderImpl.this.getQueryBase(), this.queryWhere, KafkaVariableInstanceLogDeleteBuilderImpl.this.getQueryType(), KafkaVariableInstanceLogDeleteBuilderImpl.this.getSubQuery());
                return result;
            }
        };
    }

    protected JPAExtendedAuditLogService getJpaAuditLogService() {
        JPAExtendedAuditLogService jpaAuditLogService = new JPAExtendedAuditLogService();
        return jpaAuditLogService;
    }




}
