package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.services.task.audit.service.TaskEventDeleteBuilderImpl;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.query.ParametrizedUpdate;
import org.kie.internal.task.api.AuditTask;
import org.kie.internal.task.api.model.TaskEvent;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;
import org.redhat.bpm.extensions.command.kafka.producer.JsonProducer;

import java.util.List;
import java.util.Properties;

public class KafkaTaskEventDeleteBuilderImpl extends TaskEventDeleteBuilderImpl {

    private static String TASK_EVENT_IMPL_SELECT = "SELECT * \nFROM TaskEventImpl l\n";

    public KafkaTaskEventDeleteBuilderImpl(CommandExecutor cmdExecutor) {
        super(cmdExecutor);
    }

    public KafkaTaskEventDeleteBuilderImpl(JPAAuditLogService jpaAuditService) {
        super(jpaAuditService);
    }

    @Override
    public ParametrizedUpdate build() {
        return new ParametrizedUpdate() {
            private QueryWhere queryWhere = new QueryWhere(KafkaTaskEventDeleteBuilderImpl.this.getQueryWhere());

            public int execute() {
                List<TaskEvent> result4Kafka = getJpaAuditLogService().doQuery(TASK_EVENT_IMPL_SELECT, this.queryWhere, KafkaTaskEventDeleteBuilderImpl.this.getQueryType(), KafkaTaskEventDeleteBuilderImpl.this.getSubQuery());

                //send result to kafka
                if(result4Kafka != null && !result4Kafka.isEmpty()) {
                    JsonProducer<TaskEvent> jsonProducer = new JsonProducer<>();
                    Properties properties = new Properties();
                    properties.put("valueSerializer", "org.redhat.bpm.extensions.command.kafka.serializer.TaskEventJsonSerializer");
                    properties.put("topic", "bpm-taskevent");
                    jsonProducer.start(properties);
                    jsonProducer.sendRecordsSync(result4Kafka);
                }


                int result = getJpaAuditLogService().doDelete(KafkaTaskEventDeleteBuilderImpl.this.getQueryBase(), this.queryWhere, KafkaTaskEventDeleteBuilderImpl.this.getQueryType(), KafkaTaskEventDeleteBuilderImpl.this.getSubQuery());
                return result;
            }
        };
    }

    protected JPAExtendedAuditLogService getJpaAuditLogService() {
        JPAExtendedAuditLogService jpaAuditLogService = new JPAExtendedAuditLogService();
        return jpaAuditLogService;
    }




}
