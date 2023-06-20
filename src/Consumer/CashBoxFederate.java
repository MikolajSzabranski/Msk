/*
 *   Copyright 2012 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL)
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
package Consumer;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class CashBoxFederate {
  /**
   * The sync point all federates will sync up on before starting
   */
  public static final String READY_TO_RUN = "ReadyToRun";

  //----------------------------------------------------------
  //                   INSTANCE VARIABLES
  //----------------------------------------------------------
  private RTIambassador rtiamb;
  private CashBoxFederateAmbassador fedamb;  // created when we connect
  private HLAfloat64TimeFactory timeFactory; // set when we join
  protected EncoderFactory encoderFactory;     // set when we join

  // caches of handle types - set once we join a federation
  protected ObjectClassHandle storageHandle;
  protected ObjectClassHandle customerHandle;
  protected InteractionClassHandle addClientToQueueHandle;
  protected InteractionClassHandle customerOutHandle;
  protected ParameterHandle addClientToQueueTypeHandle;
  protected ParameterHandle customerOutTypeHandle;
  protected AttributeHandle customerTypeHandle;
  protected AttributeHandle productsNumberHandle2;
  protected ObjectClassHandle cashBoxHandle;
  protected AttributeHandle cashBoxTypeHandle;
  protected AttributeHandle cashBoxSpeedHandle;
  protected AttributeHandle cashBoxMaxLengthHandle;
  protected AttributeHandle cashBoxQueueLenHandle;
  protected AttributeHandle storageMaxHandle;
  protected ParameterHandle productsNumberHandle;
  protected AttributeHandle storageAvailableHandle;
  protected InteractionClassHandle getClientToQueueHandle;
  public static boolean RUNNING = true;
  protected int storageMax = 0;
  protected int storageAvailable = 0;
  //----------------------------------------------------------
  //                      CONSTRUCTORS
  //----------------------------------------------------------

  //----------------------------------------------------------
  //                    INSTANCE METHODS
  //----------------------------------------------------------

  /**
   * This is just a helper method to make sure all logging it output in the same form
   */
  private void log(String message) {
    System.out.println("ConsumerFederate   : " + message);
  }

  /**
   * This method will block until the user presses enter
   */
  private void waitForUser() {
    log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    try {
      reader.readLine();
    } catch (Exception e) {
      log("Error while waiting for user input: " + e.getMessage());
      e.printStackTrace();
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  ////////////////////////// Main Simulation Method /////////////////////////
  ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the main simulation loop. It can be thought of as the main method of
   * the federate. For a description of the basic flow of this federate, see the
   * class level comments
   */
  public void runFederate(String federateName) throws Exception {
    /////////////////////////////////////////////////
    // 1 & 2. create the RTIambassador and Connect //
    /////////////////////////////////////////////////
    log("Creating RTIambassador");
    rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
    encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

    // connect
    log("Connecting...");
    fedamb = new CashBoxFederateAmbassador(this);
    rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);

    //////////////////////////////
    // 3. create the federation //
    //////////////////////////////
    log("Creating Federation...");
    // We attempt to create a new federation with the first three of the
    // restaurant FOM modules covering processes, food and drink
    try {
      URL[] modules = new URL[]{
          (new File("foms/Shop.xml")).toURI().toURL(),
      };

      rtiamb.createFederationExecution("ShopFederation", modules);
      log("Created Federation");
    } catch (FederationExecutionAlreadyExists exists) {
      log("Didn't create federation, it already existed");
    } catch (MalformedURLException urle) {
      log("Exception loading one of the FOM modules from disk: " + urle.getMessage());
      urle.printStackTrace();
      return;
    }

    ////////////////////////////
    // 4. join the federation //
    ////////////////////////////

    rtiamb.joinFederationExecution(federateName,            // name for the federate
                                   "CashBox",   // federate type
                                   "ShopFederation"     // name of federation
    );           // modules we want to add

    log("Joined Federation as " + federateName);

    // cache the time factory for easy access
    this.timeFactory = (HLAfloat64TimeFactory) rtiamb.getTimeFactory();

    rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);
    // wait until the point is announced
    while (fedamb.isAnnounced == false) {
      rtiamb.evokeMultipleCallbacks(0.1, 0.2);
    }
    waitForUser();
    rtiamb.synchronizationPointAchieved(READY_TO_RUN);
    log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
    while (fedamb.isReadyToRun == false) {
      rtiamb.evokeMultipleCallbacks(0.1, 0.2);
    }

    enableTimePolicy();
    log("Time Policy Enabled");

    publishAndSubscribe();
    log("Published and Subscribed");

    /////////////////////////////////////
    // 10. do the main simulation loop //
    /////////////////////////////////////
    for (int i = 0; i < 5; i++) {
      CashBox cb = new CashBox(CashBoxType.FAST);
      CashBox.FASTS.add(cb);
    }
    for (int i = 0; i < 5; i++) {
      CashBox cb = new CashBox(CashBoxType.STANDARD);
      CashBox.STANDARDS.add(cb);
    }
    while (fedamb.isRunning && RUNNING) {
      findLongestQueues();

      // 9.3 request a time advance and wait until we get it
      advanceTime(CashBox.TIME_TO_NEXT);
      log("Time Advanced to " + fedamb.federateTime + "");
    }


    ////////////////////////////////////
    // 12. resign from the federation //
    ////////////////////////////////////
    rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
    log("Resigned from Federation");

    ////////////////////////////////////////
    // 13. try and destroy the federation //
    ////////////////////////////////////////
    // NOTE: we won't die if we can't do this because other federates
    //       remain. in that case we'll leave it for them to clean up
    try {
      rtiamb.destroyFederationExecution("ExampleFederation");
      log("Destroyed Federation");
    } catch (FederationExecutionDoesNotExist dne) {
      log("No need to destroy federation, it doesn't exist");
    } catch (FederatesCurrentlyJoined fcj) {
      log("Didn't destroy federation, federates still joined");
    }
  }

  private void publishCashBox(CashBox cb) throws ObjectClassNotPublished, ObjectClassNotDefined, SaveInProgress, RestoreInProgress, FederateNotExecutionMember, NotConnected, RTIinternalError, AttributeNotDefined, AttributeNotOwned, ObjectInstanceNotKnown {
    ObjectInstanceHandle rtiCashBox = rtiamb.registerObjectInstance(cashBoxHandle);
    AttributeHandleValueMap map = rtiamb.getAttributeHandleValueMapFactory().create(4);
    map.put(cashBoxTypeHandle, encoderFactory.createHLAinteger32BE(cb.type.ordinal()).toByteArray());
    map.put(cashBoxSpeedHandle, encoderFactory.createHLAinteger32BE(cb.speed).toByteArray());
    map.put(cashBoxMaxLengthHandle, encoderFactory.createHLAinteger32BE(cb.maxLength).toByteArray());
    map.put(cashBoxQueueLenHandle, encoderFactory.createHLAinteger32BE(cb.queueLen).toByteArray());
    rtiamb.updateAttributeValues(rtiCashBox, map, generateTag());
  }

  private void findLongestQueues() {
    System.out.println("\tNajwiększe kolejki:");
    findLongestQueue(CashBox.FASTS);
    findLongestQueue(CashBox.STANDARDS);
  }

  private void findLongestQueue(List<CashBox> list) {
    AtomicInteger maximum = new AtomicInteger();
    list.forEach(cashBox -> {
      if (cashBox.getQueueLen() > maximum.get()) {
        maximum.set(cashBox.getQueueLen());
      }
    });
    System.out.println("\t\t" + list.get(0).type + " : " + maximum);
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////// Helper Methods //////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  /**
   * This method will attempt to enable the various time related properties for
   * the federate
   */
  private void enableTimePolicy() throws Exception {
    // NOTE: Unfortunately, the LogicalTime/LogicalTimeInterval create code is
    //       Portico specific. You will have to alter this if you move to a
    //       different RTI implementation. As such, we've isolated it into a
    //       method so that any change only needs to happen in a couple of spots
    HLAfloat64Interval lookahead = timeFactory.makeInterval(fedamb.federateLookahead);

    ////////////////////////////
    // enable time regulation //
    ////////////////////////////
    this.rtiamb.enableTimeRegulation(lookahead);

    // tick until we get the callback
    while (fedamb.isRegulating == false) {
      rtiamb.evokeMultipleCallbacks(0.1, 0.2);
    }

    /////////////////////////////
    // enable time constrained //
    /////////////////////////////
    this.rtiamb.enableTimeConstrained();

    // tick until we get the callback
    while (fedamb.isConstrained == false) {
      rtiamb.evokeMultipleCallbacks(0.1, 0.2);
    }
  }

  /**
   * This method will inform the RTI about the types of data that the federate will
   * be creating, and the types of data we are interested in hearing about as other
   * federates produce it.
   */
  private void publishAndSubscribe() throws RTIexception {
//	subscribe AddProducts Interaction
    String iname = "HLAinteractionRoot.CustomerToQueue";
    addClientToQueueHandle = rtiamb.getInteractionClassHandle(iname);
    addClientToQueueTypeHandle = rtiamb.getParameterHandle(addClientToQueueHandle, "type");
    rtiamb.subscribeInteractionClass(addClientToQueueHandle);

    iname = "HLAinteractionRoot.CustomerOut";
    customerOutHandle = rtiamb.getInteractionClassHandle(iname);
    customerOutTypeHandle = rtiamb.getParameterHandle(customerOutHandle, "type");
    rtiamb.subscribeInteractionClass(customerOutHandle);
  }

  /**
   * This method will request a time advance to the current time, plus the given
   * timestep. It will then wait until a notification of the time advance grant
   * has been received.
   */
  private void advanceTime(double timestep) throws RTIexception {
    // request the advance
    fedamb.isAdvancing = true;
    HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timestep);
    rtiamb.timeAdvanceRequest(time);

    // wait for the time advance to be granted. ticking will tell the
    // LRC to start delivering callbacks to the federate
    while (fedamb.isAdvancing) {
      rtiamb.evokeMultipleCallbacks(0.1, 0.2);
    }
  }

  private short getTimeAsShort() {
    return (short) fedamb.federateTime;
  }

  private byte[] generateTag() {
    return ("(timestamp) " + System.currentTimeMillis()).getBytes();
  }

  //----------------------------------------------------------
  //                     STATIC METHODS
  //----------------------------------------------------------
  public static void main(String[] args) {
    // get a federate name, use "exampleFederate" as default
    String federateName = "CashBox";
    if (args.length != 0) {
      federateName = args[0];
    }

    try {
      // run the example federate
      new CashBoxFederate().runFederate(federateName);
    } catch (Exception rtie) {
      // an exception occurred, just log the information and exit
      rtie.printStackTrace();
    }
  }
}