package org.redhat.bpm.extensions.command.kafka.builder;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.services.task.audit.service.AuditTaskDeleteBuilderImpl;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.query.ParametrizedUpdate;
import org.kie.internal.task.api.AuditTask;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;
import org.redhat.bpm.extensions.command.kafka.producer.JsonProducer;

import java.util.List;
import java.util.Properties;

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
                List<AuditTask> result4Kafka = getJpaAuditLogService().doQuery(AUDIT_TASK_IMPL_SELECT, this.queryWhere, KafkaAuditTaskDeleteBuilderImpl.this.getQueryType(), KafkaAuditTaskDeleteBuilderImpl.this.getSubQuery());

                //send result to kafka
                if(result4Kafka != null && !result4Kafka.isEmpty()) {
                    JsonProducer<AuditTask> jsonProducer = new JsonProducer<>();
                    Properties properties = new Properties();
                    properties.put("valueSerializer", "org.redhat.bpm.extensions.command.kafka.serializer.AuditTaskJsonSerializer");
                    properties.put("topic", "bpm-audit-task");
                    jsonProducer.start(properties);
                    jsonProducer.sendRecordsSync(result4Kafka);
                }


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
