package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.executor.impl.jpa.RequestInfoDeleteBuilderImpl;
import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.query.jpa.data.QueryWhere;
import org.kie.api.runtime.CommandExecutor;
import org.kie.internal.executor.api.RequestInfo;
import org.kie.internal.query.ParametrizedUpdate;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;
import org.redhat.bpm.extensions.command.kafka.producer.JsonProducer;

import java.util.List;
import java.util.Properties;

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
                List<RequestInfo> result4Kafka = getJpaAuditLogService().doQuery(REQUEST_INFO_LOG_SELECT, this.queryWhere, KafkaRequestInfoDeleteBuilderImpl.this.getQueryType(), KafkaRequestInfoDeleteBuilderImpl.this.getSubQuery());

                //send result to kafka
                if(result4Kafka != null && !result4Kafka.isEmpty()) {
                    JsonProducer<RequestInfo> jsonProducer = new JsonProducer<>();
                    Properties properties = new Properties();
                    properties.put("valueSerializer", "org.redhat.bpm.extensions.command.kafka.serializer.RequestInfoJsonSerializer");
                    properties.put("topic", "bpm-requestinfo");
                    jsonProducer.start(properties);
                    jsonProducer.sendRecordsSync(result4Kafka);
                }


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
