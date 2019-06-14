package org.redhat.bpm.extensions.command.kafka.builder;

import org.jbpm.process.audit.JPAAuditLogService;
import org.jbpm.process.audit.query.VarInstanceLogDeleteBuilderImpl;
import org.jbpm.query.jpa.data.QueryWhere;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.kie.internal.query.ParametrizedUpdate;
import org.redhat.bpm.extensions.command.kafka.JPAExtendedAuditLogService;
import org.redhat.bpm.extensions.command.kafka.producer.JsonProducer;

import java.util.List;
import java.util.Properties;

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
                List<VariableInstanceLog> result4Kafka = getJpaAuditLogService().doQuery(VARIABLE_INSTANCE_LOG_SELECT, this.queryWhere, KafkaVariableInstanceLogDeleteBuilderImpl.this.getQueryType(), KafkaVariableInstanceLogDeleteBuilderImpl.this.getSubQuery());

                //send result to kafka
                if(result4Kafka != null && !result4Kafka.isEmpty()) {
                    JsonProducer<VariableInstanceLog> jsonProducer = new JsonProducer<>();
                    Properties properties = new Properties();
                    properties.put("valueSerializer", "org.redhat.bpm.extensions.command.kafka.serializer.VariableInstanceLogJsonSerializer");
                    properties.put("topic", "bpm-variableinstancelog");
                    jsonProducer.start(properties);
                    jsonProducer.sendRecordsSync(result4Kafka);
                }


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
