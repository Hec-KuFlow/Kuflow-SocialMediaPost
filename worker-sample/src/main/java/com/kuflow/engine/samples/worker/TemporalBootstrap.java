/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.engine.samples.worker;

import com.kuflow.engine.client.activity.kuflow.KuFlowActivities;
import com.kuflow.engine.samples.worker.activity.TwitterActivities;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class TemporalBootstrap implements InitializingBean, DisposableBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    TemporalBootstrap.class
  );

  private final WorkerFactory factory;

  private final KuFlowActivities kuflowActivities;

  private final TwitterActivities twitterActivities;

  private final ApplicationProperties applicationProperties;

  public TemporalBootstrap(
    ApplicationProperties applicationProperties,
    WorkerFactory factory,
    KuFlowActivities kuflowActivities,
    TwitterActivities twitterActivities
  ) {
    this.applicationProperties = applicationProperties;
    this.factory = factory;
    this.kuflowActivities = kuflowActivities;
    this.twitterActivities = twitterActivities;
  }

  @Override
  public void afterPropertiesSet() {
    this.startWorkers();
    LOGGER.info("Temporal connection initialized");
  }

  @Override
  public void destroy() {
    this.factory.shutdown();
    this.factory.awaitTermination(1, TimeUnit.MINUTES);
    LOGGER.info("Temporal connection shutdown");
  }

  private void startWorkers() {
    Worker worker =
      this.factory.newWorker(
          this.applicationProperties.getTemporal().getKuflowQueue()
        );
    worker.registerWorkflowImplementationTypes(SampleWorkflowImpl.class);
    worker.registerActivitiesImplementations(
      this.kuflowActivities,
      this.twitterActivities
    );

    this.factory.start();
  }
}
