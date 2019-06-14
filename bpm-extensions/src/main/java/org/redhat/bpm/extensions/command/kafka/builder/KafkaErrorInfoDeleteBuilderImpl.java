package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.executor.impl.jpa.ErrorInfoDeleteBuilderImpl;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.query.jpa.data.QueryWhere;
import org.jbpm.services.task.audit.service.AuditTaskDeleteBuilderImpl;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.executor.api.ErrorInfo;
import org.kie.internal.query.ParametrizedUpdate;
import org.kie.internal.task.api.AuditTask;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;
import org.redhat.bpm.extensions.command.kafka.producer.JsonProducer;

import java.util.List;
import java.util.Properties;

public class KafkaErrorInfoDeleteBuilderImpl extends ErrorInfoDeleteBuilderImpl {

    private static String ERROR_INFO_LOG_SELECT = "SELECT * \nFROM ErrorInfo l\n";

    public KafkaErrorInfoDeleteBuilderImpl(CommandExecutor cmdExecutor) {
        super(cmdExecutor);
    }

    public KafkaErrorInfoDeleteBuilderImpl(JPAAuditLogService jpaAuditService) {
        super(jpaAuditService);
    }

    @Override
    public ParametrizedUpdate build() {
        return new ParametrizedUpdate() {
            private QueryWhere queryWhere = new QueryWhere(KafkaErrorInfoDeleteBuilderImpl.this.getQueryWhere());

            public int execute() {
                List<ErrorInfo> result4Kafka = getJpaAuditLogService().doQuery(ERROR_INFO_LOG_SELECT, this.queryWhere, KafkaErrorInfoDeleteBuilderImpl.this.getQueryType(), KafkaErrorInfoDeleteBuilderImpl.this.getSubQuery());

                //send result to kafka
                if(result4Kafka != null && !result4Kafka.isEmpty()) {
                    JsonProducer<ErrorInfo> jsonProducer = new JsonProducer<>();
                    Properties properties = new Properties();
                    properties.put("valueSerializer", "org.redhat.bpm.extensions.command.kafka.serializer.ErrorInfoJsonSerializer");
                    properties.put("topic", "bpm-errorinfo");
                    jsonProducer.start(properties);
                    jsonProducer.sendRecordsSync(result4Kafka);
                }


                int result = getJpaAuditLogService().doDelete(KafkaErrorInfoDeleteBuilderImpl.this.getQueryBase(), this.queryWhere, KafkaErrorInfoDeleteBuilderImpl.this.getQueryType(), KafkaErrorInfoDeleteBuilderImpl.this.getSubQuery());
                return result;
            }
        };
    }

    protected JPAExtendedAuditLogService getJpaAuditLogService() {
        JPAExtendedAuditLogService jpaAuditLogService = new JPAExtendedAuditLogService();
        return jpaAuditLogService;
    }




}
