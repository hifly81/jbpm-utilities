package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.query.ProcessInstanceLogDeleteBuilderImpl;
import org.jbpm.process.audit.query.VarInstanceLogDeleteBuilderImpl;
import org.jbpm.query.jpa.data.QueryWhere;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.manager.audit.NodeInstanceLog;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.internal.query.ParametrizedUpdate;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;
import org.redhat.bpm.extensions.command.kafka.producer.JsonProducer;

import java.util.List;
import java.util.Properties;

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
                List<ProcessInstanceLog> result4Kafka = getJpaAuditLogService().doQuery(PROCESS_INSTANCE_LOG_SELECT, this.queryWhere, KafkaProcessInstanceLogDeleteBuilderImpl.this.getQueryType(), KafkaProcessInstanceLogDeleteBuilderImpl.this.getSubQuery());

                //send result to kafka
                if(result4Kafka != null && !result4Kafka.isEmpty()) {
                    JsonProducer<ProcessInstanceLog> jsonProducer = new JsonProducer<>();
                    Properties properties = new Properties();
                    properties.put("valueSerializer", "org.redhat.bpm.extensions.command.kafka.serializer.ProcessInstanceLogJsonSerializer");
                    properties.put("topic", "bpm-processinstancelog");
                    jsonProducer.start(properties);
                    jsonProducer.sendRecordsSync(result4Kafka);
                }


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
