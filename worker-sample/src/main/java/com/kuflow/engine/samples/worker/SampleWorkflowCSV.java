/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker;

import com.kuflow.engine.client.common.resource.WorkflowRequestResource;
import com.kuflow.engine.client.common.resource.WorkflowResponseResource;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface SampleWorkflowCSV {
    @WorkflowMethod(name = "twitterAppWorkerCSV")
    WorkflowResponseResource runWorkflow(WorkflowRequestResource request);
}
