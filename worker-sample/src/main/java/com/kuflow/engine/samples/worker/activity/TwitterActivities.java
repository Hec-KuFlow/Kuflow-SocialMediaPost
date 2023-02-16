/*
 * Copyright (c) 2022-present KuFlow S.L.
 *
 * All rights reserved.
 */
package com.kuflow.engine.samples.worker.activity;

import java.util.List;

import com.kuflow.engine.samples.model.TwitterMessage;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface TwitterActivities {
  String sendTweet(String message);
  List<TwitterMessage> readTwitterMessages();
}

