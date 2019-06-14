package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.query.NodeInstanceLogDeleteBuilderImpl;
import org.jbpm.query.jpa.data.QueryWhere;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.manager.audit.NodeInstanceLog;
import org.kie.internal.executor.api.ErrorInfo;
import org.kie.internal.query.ParametrizedUpdate;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;
import org.redhat.bpm.extensions.command.kafka.producer.JsonProducer;

import java.util.List;
import java.util.Properties;

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
                List<NodeInstanceLog> result4Kafka = getJpaAuditLogService().doQuery(NODE_INSTANCE_LOG_SELECT, this.queryWhere, KafkaNodeInstanceLogDeleteBuilderImpl.this.getQueryType(), KafkaNodeInstanceLogDeleteBuilderImpl.this.getSubQuery());

                //send result to kafka
                if(result4Kafka != null && !result4Kafka.isEmpty()) {
                    JsonProducer<NodeInstanceLog> jsonProducer = new JsonProducer<>();
                    Properties properties = new Properties();
                    properties.put("valueSerializer", "org.redhat.bpm.extensions.command.kafka.serializer.NodeInstanceLogJsonSerializer");
                    properties.put("topic", "bpm-nodeinstancelog");
                    jsonProducer.start(properties);
                    jsonProducer.sendRecordsSync(result4Kafka);
                }


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
