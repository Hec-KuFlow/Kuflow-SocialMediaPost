/*
 * Copyright (c) 2022-present KuFlow S.L.
 *
 * All rights reserved.
 */
package com.kuflow.engine.samples.worker.activity;

import io.temporal.failure.ApplicationFailure;

import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kuflow.engine.samples.model.TwitterMessage;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.twitter.clientlib.model.User;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Service
public class TwitterActivitiesImpl implements TwitterActivities {

  //private TwitterFactory tf = new TwitterFactory();
 

  @Override
  public String sendTweet(String message) {
    
    ConfigurationBuilder cb = new ConfigurationBuilder();

    cb.setDebugEnabled(true)
      .setOAuthConsumerKey("IIuWeFqxzyUbHfwWsIJVKkRZt")
      .setOAuthConsumerSecret("yrabCA9fJSHerZJiOtENT853bmH3x3KkU6yLgggwGyMKSxwS7G")
      .setOAuthAccessToken("1556675694402445314-fmnunqMs44HdPRQMmpAxQkKv1nca2G")
      .setOAuthAccessTokenSecret("SVAIdWSJE0Qt1qgxebEFwrdKzCXIYJ9F0QN7YluqRbHfF");

    TwitterFactory tf = new TwitterFactory(cb.build());
    Twitter twitter = tf.getInstance();
    //String destinationId = "hectess";
    
    try {
      StatusUpdate status = new StatusUpdate(message);
      //status.setMedia(new File("C:\\demo\\imagen.png"));
      //twitter.updateStatus(status);
      twitter.updateStatus(message);
      //7DirectMessage directMessage = twitter.directMessages().sendDirectMessage(destinationId, message);
      
      //Long recipientId = directMessage.getRecipientId();
      //System.out.println("Direct message successfully sent to " +
      //          twitter.showUser(recipientId).getScreenName());
    } catch (TwitterException e) {
      throw ApplicationFailure.newFailureWithCause(
        "Error sending tweet",
        "TwitterFailure",
        e
      );
    }
    return message;
  }

  @Override
  public List<TwitterMessage> readTwitterMessages() {
    String[] post;
    List<TwitterMessage> twitterMessages = new LinkedList<>();
    try (CSVReader reader = new CSVReaderBuilder(new FileReader("C:\\demo\\rrss.csv")).build();){
       	while ((post = reader.readNext()) != null) {
        	Instant instant = Instant.parse(post[0]);
          TwitterMessage twitterMessage = new TwitterMessage();
          twitterMessage.setInstant(instant);
          twitterMessage.setSocial(post[1]);
          twitterMessage.setMessage(post[2]);
          twitterMessage.setAsset(post[3]);
          twitterMessages.add(twitterMessage);
        	
			  }
    } catch (Exception e) {
		    throw new RuntimeException(e);
  	}
	  twitterMessages.sort((a,b)->{
      return a.getInstant().compareTo(b.getInstant());
	  });

    return twitterMessages;
  }
}
