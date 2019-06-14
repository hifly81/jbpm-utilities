package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.query.NodeInstanceLogDeleteBuilderImpl;
import org.jbpm.query.jpa.data.QueryWhere;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.query.ParametrizedUpdate;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;

import java.util.List;

public class KafkaNodeInstanceLogDeleteBuilderImpl extends NodeInstanceLogDeleteBuilderImpl {

    private static String NODE_INSTANCE_LOG_SELECT = "SELECT * \nFROM NodeInstanceLog l\n";

    public KafkaNodeInstanceLogDeleteBuilderImpl(CommandExecutor cmdExecutor) {
        super(cmdExecutor);
    }

    public KafkaNodeInstanceLogDeleteBuilderImpl(JPAAuditLogService jpaAuditService) {
        super(jpaAuditService);
    }

    @Override
    public ParametrizedUpdate build() {
        return new ParametrizedUpdate() {
            private QueryWhere queryWhere = new QueryWhere(KafkaNodeInstanceLogDeleteBuilderImpl.this.getQueryWhere());

            public int execute() {
                List result4Kafka = getJpaAuditLogService().doQuery(NODE_INSTANCE_LOG_SELECT, this.queryWhere, KafkaNodeInstanceLogDeleteBuilderImpl.this.getQueryType(), KafkaNodeInstanceLogDeleteBuilderImpl.this.getSubQuery());

                //TODO send to kafka topic


                int result = getJpaAuditLogService().doDelete(KafkaNodeInstanceLogDeleteBuilderImpl.this.getQueryBase(), this.queryWhere, KafkaNodeInstanceLogDeleteBuilderImpl.this.getQueryType(), KafkaNodeInstanceLogDeleteBuilderImpl.this.getSubQuery());
                return result;
            }
        };
    }

    protected JPAExtendedAuditLogService getJpaAuditLogService() {
        JPAExtendedAuditLogService jpaAuditLogService = new JPAExtendedAuditLogService();
        return jpaAuditLogService;
    }




}
