/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker;

import com.kuflow.engine.client.activity.kuflow.KuFlowActivities;
import com.kuflow.engine.client.activity.kuflow.resource.CompleteProcessRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.CompleteProcessResponseResource;
import com.kuflow.engine.client.activity.kuflow.resource.CreateTaskRequestResource;
import com.kuflow.engine.client.activity.kuflow.resource.CreateTaskResponseResource;
import com.kuflow.engine.client.common.resource.WorkflowRequestResource;
import com.kuflow.engine.client.common.resource.WorkflowResponseResource;
import com.kuflow.engine.client.common.util.TemporalUtils;
import com.kuflow.engine.samples.worker.activity.TwitterActivities;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

public class SampleWorkflowImpl implements SampleWorkflow {

    private static final Logger LOGGER = Workflow.getLogger(SampleWorkflowImpl.class);

    private static final String TASK_CODE_ENVIAR_TWEET = "TW_SEND_APP";


    private final KuFlowActivities kuflowActivities;

    private final TwitterActivities twitterActivities;

    public SampleWorkflowImpl() {
        RetryOptions defaultRetryOptions = RetryOptions.newBuilder().validateBuildWithDefaults();

        ActivityOptions defaultActivityOptions = ActivityOptions
            .newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofMinutes(10))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        ActivityOptions asyncActivityOptions = ActivityOptions
            .newBuilder()
            .setRetryOptions(defaultRetryOptions)
            .setStartToCloseTimeout(Duration.ofDays(1))
            .setScheduleToCloseTimeout(Duration.ofDays(365))
            .validateAndBuildWithDefaults();

        this.kuflowActivities =
            Workflow.newActivityStub(
                KuFlowActivities.class,
                defaultActivityOptions,
                Map.of(TemporalUtils.getActivityType(KuFlowActivities.class, "createTaskAndWaitTermination"), asyncActivityOptions)
            );
        this.twitterActivities = Workflow.newActivityStub(TwitterActivities.class, defaultActivityOptions);
    }

    @Override
    public WorkflowResponseResource runWorkflow(WorkflowRequestResource request) {

        LOGGER.info("Started Twitter process {}", request.getProcessId());

        CreateTaskResponseResource taskTwitterApplication = this.createTaskEnviar_Tweet(request);

        String message = taskTwitterApplication.getTask().getElementValues().get("MESSAGE").getValueAsString();
        
        this.twitterActivities.sendTweet(message);

        CompleteProcessResponseResource completeProcess = this.completeProcess(request.getProcessId());

        LOGGER.info("Finished Twitter process {}", request.getProcessId());

        return this.completeWorkflow(completeProcess);
    }

    private WorkflowResponseResource completeWorkflow(CompleteProcessResponseResource completeProcess) {
        WorkflowResponseResource workflowResponse = new WorkflowResponseResource();
        workflowResponse.setMessage(completeProcess.getMessage());

        return workflowResponse;
    }

    private CompleteProcessResponseResource completeProcess(UUID processId) {
        CompleteProcessRequestResource request = new CompleteProcessRequestResource();
        request.setProcessId(processId);

        return this.kuflowActivities.completeProcess(request);
    }


    /**
     * Create task "Enviar Tweet" in KuFlow and wait for its completion
     *
     * @param startProcess response of start process activity
     * @return task created
     */
    private CreateTaskResponseResource createTaskEnviar_Tweet(WorkflowRequestResource workflowRequest) {
        CreateTaskRequestResource task = new CreateTaskRequestResource();
        task.setTaskId(Workflow.randomUUID());
        task.setTaskDefinitionCode(TASK_CODE_ENVIAR_TWEET);
        task.setProcessId(workflowRequest.getProcessId());

        // Create Task in KuFlow
        return this.kuflowActivities.createTaskAndWaitTermination(task);
    }

}
