# KuFlow - Third-Party API (Twitter) example

## What will we create?

This tutorial will guide us in building a "Social Media Posting" process using a Temporal.io worker workflow (*when we apply the Workflow as Code paradigm*), connecting our application to the Twitter API (it could also be LinkedIn or Facebook) with the purpose of filling up a text form to post a message on the timeline using the methods of the used API.

## Prerequisites

Before starting your Workflow for the first time, you must register in [KuFlow (app.kuflow.com)](https://app.kuflow.com/). After familiarizing yourself with the user interface by navigating through the menus or visiting our [Youtube channel](https://www.youtube.com/channel/UCXoRtHICa86YfX8P_wu1f6Q) with many videos that will help you in this task, you are ready to perform the necessary configurations for our Worker. To do so, click on the `Management` menu.

### Create the Credentials

#### 3rd Party API (Twitter) Credentials

To use this 3rd-party application, you must create a Twitter account and get Elevated Access through the [Twitter Developer Platform (developer.twitter.com)](https://developer.twitter.com/en/docs/twitter-api).

#### Create the credentials for the Worker

We will configure an `APPLICATION` that will provide us with the necessary credentials so that our Worker (written in Java and located in your own machine) can interface with KuFlow.

Go to the `Settings > Applications` menu and click on `Add application`. We establish the name we want and save. Next, you will get the first data needed to configure our Worker.

- **Identifier**: Unique identifier of the application. For this tutorial: *myApp*
  - Later in this tutorial, we will configure it in the `kuflow.api.client-id` property of our example.
- **Token**: Password for the application.
  - Later in this tutorial, we will configure it in the `kuflow.api.client-secret` property of our example.
- **Namespace**: Temporal's namespace.
  - Later in this tutorial, we will configure it in the `application.temporal.namespace` property of our example.

Next, we will proceed to create the certificates that will serve us to configure the Mutual TLS with which our Worker will perform the authentication against Temporal. To do this we click on "Add certificate", set the name we want, and choose the `PKCS8` type for the encryption of the private key. This is important since the example code in this tutorial works with this encoding. We will get the following:

- **Certificate**: It is the public part that is presented in the *mTLS* connection.
  - Later in this tutorial, we will configure it in the `application.temporal.mutual-tls.cert-data` property of our example.
- **Private Key**: It is the private key for *mTLS*.
  - Later in this tutorial, we will configure it in the `application.temporal.mutual-tls.key-data` property of our example.

It is also necessary to indicate the CA certificate, which is the root certificate with which all certificates are issued. It is a public certificate and for convenience you can find it in the same `Application` screen, under the name of *CA Certificate*. This certificate will always be the same between workers.

- **CA Certificate**: Root certificate with which all certificates (client and server certificates) are issued.
  - Later in this tutorial, we will configure it in the `kuflow.activity.kuflow.key-data` property of our example.

Finally, you get something like:

<div class="text--center">

![](/img//TUT-01-App.png)

</div>

## Preparing Scenario

### Create the process definition

We need to create the definition of the process that will execute our workflow. In this section, we will configure the KuFlow tasks of which it is made up as well as the information necessary to complete said tasks, the process access rules (i.e. *RBAC*), as well as another series of information. To do this we go to the `Setting > Processes` menu and create a new process.

Complete *Process Definition* with the (*recommended*) following data:

- **Process name**
    - Social Media Post
- **Description**
    - Free text description about the Workflow.
- **Workflow**
    - **Workflow Engine**
   	 - *KuFlow Engine*, because we are designing a Temporal-based Worker.
  - **Workflow Application**
  	- myApp, the application to which our Worker will connect to.
  - **Task queue**
   	- The name of the Temporal queue where the KuFlow tasks will be set. You can choose any name, later you will set this same name in the appropriate configuration in your Worker. For this tutorial: *twitterQueue*.
  - **Type**
   	- It must match the name of the Java interface of the Workflow. For this tutorial, *twitterWorker* is the name you should type in this input.
- **Permissions**
    - At least one user or group of users must have the role of `INITIATOR` to instantiate the process through the application. In this tutorial, we will allow the *“Default Group”* from this organization.

Finally, you get something like:

<div class="text--center">

![](/img/TUT01-03-Process.png)

</div>

We will define one **Task Definition** in the process as follows:

- Task one **"Send Tweet"**
    - **Description:** Free text description about the Task.
    - **Code:** TW_SEND_APP
    - **Candidates:** Default Group (*in this tutorial, we will allow all users from this organization to fill up the application form*)
    - **Elements:**
   	 - **Name:** Message
   		 - **Description:** Free text description about the element (*optional*).
   		 - **Code:** MESSAGE
   		 - **Type:** Field
   		 - **Properties:** Mandatory

You'll get something like:

<div class="text--center">

![](/img/TUT01-04-Task.png)

</div>

### Publish the process and download the template for the Workflow Worker

By clicking on the `Publish` button you’ll receive a confirmation request message, once you have confirmed, the process will be published.

<div class="text--center">

![](/img/TUT01-05-Publish_1.png)

![](/img/TUT01-05-Publish_2.png)

</div>

Now, you can download a sample Workflow Implementation from the Process Definition main page.

<div class="text--center">

![](/img/TUT01-06-Template_1.png)

![](/img/TUT01-06-Template_2.png)

</div>

This code will serve as a starting point for implementing our worker. As we'll use the Java template, the requirements for its use are the following:

- **Java JDK**
  - You need to have a Java JDK installed on your system. The current example code uses version 17, but is not required for the KuFlow SDK. You can use for example [Adoptium](https://adoptium.net/) distribution or any other. We recommend you to use a tool like [SDKMan](https://sdkman.io/jdks) to install Java SDK distributions in a comfortable way.
- **IDE**
  - An IDE with good Java support is necessary to work comfortably. You can use *VSCode*, *IntelliJ Idea,* *Eclipse* or any other with corresponding Java plugins.

### Main technologies used in the example

To make things simpler, the following technologies have been mainly used in our example:

- **Maven**
  - To build this example. It is distributed in an integrated way so it is not necessary to install it in isolation.
- **Spring Boot and Spring Cloud**
  - To wire all our integrations.
- **Temporal Java SDK**
  - To perform GRPC communications with the KuFlow temporal service.
- **KuFlow Java SDK**
  - To implement the KuFlow API Rest client, some Temporal activities and others.
- **Twitter4J**
  - To implement the Twiiter API methods.

## Implementation

**Note:** You can download the following project from our [Community Github repository](https://github.com/Hec-KuFlow), be sure to add all the tokens and secrets from your KuFlow account and 3rd party API developers.

### Resolve dependencies

We need to modify **pom.xml**, to include new dependencies:

```xml
    <dependency>
      <groupId>org.twitter4j</groupId>
      <artifactId>twitter4j-core</artifactId>
      <version>4.0.7</version>
     </dependency>
     <dependency>
      <groupId>com.twitter</groupId>
      <artifactId>twitter-api-java-sdk</artifactId>
      <version>1.1.4</version>
    </dependency>
    <dependency>
      <groupId>com.google.guava</groupId>
      <artifactId>guava</artifactId>
      <version>31.1-jre</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
      <version>2.6.4</version>
    </dependency>
```

### Using Credentials

Now, in this step we are filling up the application configuration information. You must complete all the settings and replace the example values indicated as "FILL_ME".

#### KuFlow’s Credentials

The appropriate values can be obtained from the KuFlow application. Check out the [Create the Credentials](#create-the-credentials-for-the-worker) section of this tutorial.

```yaml
# ===================================================================
# PLEASE COMPLETE ALL CONFIGURATIONS BEFORE STARTING THE WORKER
# ===================================================================

kuflow:
 api:

   # ID of the APPLICATION configured in KUFLOW.
   # Get it in "Application details" in the Kuflow APP.
   client-id: FILL_ME

   # TOKEN of the APPLICATION configured in KUFLOW.
   # Get it in "Application details" in the Kuflow APP.
   client-secret: FILL_ME

application:
 temporal:

   # Temporal Namespace. Get it in "Application details" in the KUFLOW APP.
   namespace: FILL_ME

   # Temporal Queue. Configure it in the "Process definition" in the KUFLOW APP.
   kuflow-queue: FILL_ME

   mutual-tls:
   # Client certificate
   # Get it in "Application details" in the KUFLOW APP.
   cert-data: |
   	-----BEGIN CERTIFICATE-----
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	…
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	-----END CERTIFICATE-----

   # Private key
   # Get it in "Application details" in the KUFLOW APP.
   # IMPORTANT: This example works with PKCS8, so ensure PKCS8 is selected
   #        	when you generate the certificates in the KUFLOW App
   key-data: |
   	-----BEGIN CERTIFICATE-----
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	…
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	-----END CERTIFICATE-----

   # KUFLOW Certification Authority (CA) of the certificates issued in KUFLOW
   ca-data: |
   	-----BEGIN CERTIFICATE-----
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	…
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
   	-----END CERTIFICATE-----
```

Please note that this is a YAML, respect the indentation. You'll get something like this:

<div class="text--center">

![](/img/TUT-06-Template_3.png)

</div>

#### 3rd Party (Twitter) Credentials

We create a new file called **twitter4j.properties** inside a subfolder **/resources/config**, with the content generated from the [3rd Party API (Twitter) Credentials](#3rd-party-api-twitter-credentials) section on this tutorial.

<div class="text--center">

![](/img/TUT01-06-Template_4.png)

</div>

### Define new Activities

We create a new subfolder called **activity** and inside a file called **TwitterActivities.java** with the following content:

```java
/*
 * Copyright (c) 2022-present KuFlow S.L.
 *
 * All rights reserved.
 */
package com.kuflow.engine.samples.worker.activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface TwitterActivities {
  String sendTweet(String message);
}
```

We also have to create **TwitterActivitiesImpl.java** which is the implementation of the previous activities, using the 3rd party API methods.

In summary, the next piece of code will contain the basics methods from [Twitter Developers Platform Quick start](https://developer.twitter.com/en/docs/twitter-api/tweets/manage-tweets/quick-start) modified for our use case.

- **sendTweet(...)**: This is our `main` method, which takes a string message as input and uses the Twitter4J library to access the Twitter API and post the message as a new status update on the user's Twitter account. If the updateStatus() method call throws a TwitterException, indicating that there was an error sending the tweet, the method catches the exception and throws a new ApplicationFailure exception with a message indicating the failure and the original exception as its cause.

**Remember** You can download this project from our [Community Github repository](https://github.com/Hec-KuFlow).

```java
/*
 * Copyright (c) 2022-present KuFlow S.L.
 *
 * All rights reserved.
 */
package com.kuflow.engine.samples.worker.activity;

import io.temporal.failure.ApplicationFailure;
import org.springframework.stereotype.Service;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

@Service
public class TwitterActivitiesImpl implements TwitterActivities {

  private TwitterFactory tf = new TwitterFactory();

  @Override
  public String sendTweet(String message) {
	Twitter twitter = this.tf.getInstance();

	try {
  	twitter.updateStatus(message);
	} catch (TwitterException e) {
  	throw ApplicationFailure.newFailureWithCause(
    	"Error sending tweet",
    	"TwitterFailure",
    	e
  	);
	}
	return message;
  }

}

```

### Register Activities

In order to register the recently created activity in our Application worker, we must modify **TemporalBootstrap.java** as follows:

With **//HERE** and some description before each line we'll highlight new code.

We declare the instance variable after *kuFlowActivities*:

```java
private final KuFlowActivities kuflowActivities;
//HERE Add the following line
private final TwitterActivities twitterActivities;
```

We modify the constructor as follows:

```java
	public TemporalBootstrap(
    	ApplicationProperties applicationProperties,
    	WorkerFactory factory,
    	KuFlowSyncActivities kuFlowSyncActivities,
    	KuFlowAsyncActivities kuFlowAsyncActivities,
   	 //HERE: Add the following line and the previous comma
    	TwitterActivities twitterActivities
	) {
    	this.applicationProperties = applicationProperties;
    	this.factory = factory;
    	this.kuFlowSyncActivities = kuFlowSyncActivities;
    	this.kuFlowAsyncActivities = kuFlowAsyncActivities;
   	 //HERE: Add the following line and the previous semicolon
    	this.twitterActivities = twitterActivities;
	}
```

and we add the following line to register the activity in **startWorkers()**

```java
	worker.registerActivitiesImplementations(
  	this.kuflowActivities,
  	//HERE: Add the following line and the previous comma
  	twitterActivities
	);
```

### Workflow Implementation

In this section, we will make the fundamental steps to creating the most basic workflow for this business process:

- Users in an organization can write a message and it will be posted by the Application, in a Social Media Platform, in this case: Twitter.

Open the **SampleWorkflowImpl.java** file and modify it as follows.

With **//HERE** and some description before each line we'll highlight new code.

Declaring an instance of our new activities after kuflowActivities declaration:

```java
private final KuFlowActivities kuflowActivities;
//HERE Add the following line
private final TwitterActivities twitterActivities;
```

At the end of the constructor method **SampleWorkflowImpl()** add the following to initialize the activities:

```java
...

this.kuFlowSyncActivities = Workflow.newActivityStub(KuFlowSyncActivities.class, defaultActivityOptions);

this.kuFlowAsyncActivities = Workflow.newActivityStub(KuFlowAsyncActivities.class, asyncActivityOptions);

//HERE Add the following line
this.twitterActivities = Workflow.newActivityStub(TwitterActivities.class, defaultActivityOptions);
	}

...

```

Now we modify the **runWorkflow()** method as follows:

1. Change this line to apply the model "Task" and generate a better context. (*More info about the Task model in our [REST API reference](https://docs.kuflow.com/reference#tag/task)*).
2. Create a local variable to assign the value contained in the Send Tweet form element.
3. Call the activity method in charge to excecute the task, sending the data obtained in the previous step as parameter.

```java
@Override
	public WorkflowResponse runWorkflow(WorkflowRequest request) {
    	LOGGER.info("Process {} started", request.getProcessId());

    	this.kuFlowGenerator = new KuFlowGenerator(request.getProcessId());
   	 
   	//HERE 1
    	Task taskTwitterApplication = this.createTaskSend_Tweet(request);

   	//HERE 2
	String message = taskTwitterApplication.getElementValueAsString(""MESSAGE");

   	//HERE 3
    	this.twitterActivities.sendTweet(message);

    	CompleteProcessResponse completeProcess = this.completeProcess(request.getProcessId());

    	LOGGER.info("Process {} finished", request.getProcessId());

    	return this.completeWorkflow(completeProcess);
	}
```

The final step with the code is including the imports needed for this tutorial using some feature of your IDE (*like pressing **SHIFT+ ALT + O** in Visual Studio Code*).

## Testing

We can test all that we have done by running the worker (*like pressing **F5** in Visual Studio Code*) and initiating the process in KuFlow’s UI. Then Fill out the form with the information requested and complete the task.

<div class="text--center">

![](/img/TUT01-08-Test_1.png)

</div>

Check the Social Media (Twitter) Timeline, you'll get something like this:

<div class="text--center">

![](/img/TUT01-08-Test_2.png)

</div>

## Summary

In this tutorial, we have covered the basics of creating a Temporal.io-based workflow in KuFlow using a 3rd-party API (*Twitter*). We have defined a new process definition, and we have built a workflow that contemplates the following business rules involving automated and human tasks:

1. Users in the organization can enter a new message that will be post in a Social Media Platform.
2. The worker connects to a Third-Party API and consume its methods (Connection: TwitterFactory / Message Send: updateStatus).

We have created a special video with the entire process:

Here you can watch all steps in this video:

<a href="https://youtu.be/bJiRzjqB4BQ" target="_blank" title="Play me!">
  <p align="center">
	<img width="75%" src="https://img.youtube.com/vi/bJiRzjqB4BQ/maxresdefault.jpg" alt="Play me!"/>
  </p>
</a>

We sincerely hope that this step-by-step guide will help you to understand better how KuFlow can help your business to have better and more solid business processes.
