package org.redhat.bpm.extensions.command.kafka;

import org.jbpm.executor.commands.LogCleanupCommand;
import org.jbpm.executor.impl.jpa.ExecutorJPAAuditService;
import org.jbpm.process.core.timer.DateTimeUtils;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.kie.api.executor.STATUS;
import org.redhat.bpm.extensions.command.kafka.builder.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KafkaLogCleanupCommand extends LogCleanupCommand {

    private static final Logger logger = LoggerFactory.getLogger(KafkaLogCleanupCommand.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private long nextScheduleTimeAdd = 86400000L;

    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {
        boolean skipProcessLog = ctx.getData().containsKey("SkipProcessLog") ? Boolean.parseBoolean((String)ctx.getData("SkipProcessLog")) : false;
        boolean skipTaskLog = ctx.getData().containsKey("SkipTaskLog") ? Boolean.parseBoolean((String)ctx.getData("SkipTaskLog")) : false;
        boolean skipExecutorLog = ctx.getData().containsKey("SkipExecutorLog") ? Boolean.parseBoolean((String)ctx.getData("SkipExecutorLog")) : false;
        SimpleDateFormat formatToUse = DATE_FORMAT;
        String dataFormat = (String)ctx.getData("DateFormat");
        if (dataFormat != null) {
            formatToUse = new SimpleDateFormat(dataFormat);
        }

        ExecutionResults executionResults = new ExecutionResults();
        String emfName = (String)ctx.getData("EmfName");
        if (emfName == null) {
            emfName = "org.jbpm.domain";
        }

        String singleRun = (String)ctx.getData("SingleRun");
        if ("true".equalsIgnoreCase(singleRun)) {
            this.nextScheduleTimeAdd = -1L;
        }

        String nextRun = (String)ctx.getData("NextRun");
        if (nextRun != null) {
            this.nextScheduleTimeAdd = DateTimeUtils.parseDateAsDuration(nextRun);
        }

        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(emfName);
        ExecutorJPAAuditService auditLogService = new ExecutorJPAAuditService(emf);
        String olderThan = (String)ctx.getData("OlderThan");
        String olderThanPeriod = (String)ctx.getData("OlderThanPeriod");
        String forProcess = (String)ctx.getData("ForProcess");
        String forDeployment = (String)ctx.getData("ForDeployment");
        long bamLogsRemoved;
        if (olderThanPeriod != null) {
            bamLogsRemoved = DateTimeUtils.parseDateAsDuration(olderThanPeriod);
            Date olderThanDate = new Date(System.currentTimeMillis() - bamLogsRemoved);
            olderThan = formatToUse.format(olderThanDate);
        }

        long requestInfoLogsRemoved;
        if (!skipTaskLog) {
            //create data to kafka builder
            bamLogsRemoved = (long) new KafkaAuditTaskDeleteBuilderImpl(auditLogService).processId(new String[]{forProcess}).dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan)).deploymentId(new String[]{forDeployment}).build().execute();
            logger.info("TaskAuditLogRemoved {}", bamLogsRemoved);
            executionResults.setData("TaskAuditLogRemoved", bamLogsRemoved);
            //create data to kafka builder
            requestInfoLogsRemoved = (long) new KafkaTaskEventDeleteBuilderImpl(auditLogService).dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan)).build().execute();
            logger.info("TaskEventLogRemoved {}", requestInfoLogsRemoved);
            executionResults.setData("TaskEventLogRemoved", requestInfoLogsRemoved);
        }

        if (!skipProcessLog) {
            //create data to kafka builder
            bamLogsRemoved = (long) new KafkaNodeInstanceLogDeleteBuilderImpl(auditLogService).processId(new String[]{forProcess}).dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan)).externalId(new String[]{forDeployment}).build().execute();
            logger.info("NodeInstanceLogRemoved {}", bamLogsRemoved);
            executionResults.setData("NodeInstanceLogRemoved", bamLogsRemoved);
            //create data to kafka builder
            requestInfoLogsRemoved = (long) new KafkaVariableInstanceLogDeleteBuilderImpl(auditLogService).processId(new String[]{forProcess}).dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan)).externalId(new String[]{forDeployment}).build().execute();
            logger.info("VariableInstanceLogRemoved {}", requestInfoLogsRemoved);
            executionResults.setData("VariableInstanceLogRemoved", requestInfoLogsRemoved);
            long piLogsRemoved;
            //create data to kafka builder
            piLogsRemoved = (long) new KafkaProcessInstanceLogDeleteBuilderImpl(auditLogService).processId(new String[]{forProcess}).status(new int[]{2, 3}).endDateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan)).externalId(new String[]{forDeployment}).build().execute();
            logger.info("ProcessInstanceLogRemoved {}", piLogsRemoved);
            executionResults.setData("ProcessInstanceLogRemoved", piLogsRemoved);
        }

        if (!skipExecutorLog) {
            //create data to kafka builder
            bamLogsRemoved = (long) new KafkaErrorInfoDeleteBuilderImpl(auditLogService).dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan)).build().execute();
            logger.info("ErrorInfoLogsRemoved {}", bamLogsRemoved);
            executionResults.setData("ErrorInfoLogsRemoved", bamLogsRemoved);
            //create data to kafka builder
            requestInfoLogsRemoved = (long) new KafkaRequestInfoDeleteBuilderImpl(auditLogService).dateRangeEnd(olderThan == null ? null : formatToUse.parse(olderThan)).status(new STATUS[]{STATUS.CANCELLED, STATUS.DONE, STATUS.ERROR}).build().execute();
            logger.info("RequestInfoLogsRemoved {}", requestInfoLogsRemoved);
            executionResults.setData("RequestInfoLogsRemoved", requestInfoLogsRemoved);
        }

        bamLogsRemoved = 0L;
        executionResults.setData("BAMLogRemoved", bamLogsRemoved);
        return executionResults;
    }
}
