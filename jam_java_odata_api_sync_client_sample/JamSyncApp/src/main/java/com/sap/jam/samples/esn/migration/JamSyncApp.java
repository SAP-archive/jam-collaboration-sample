package com.sap.jam.samples.esn.migration;

import client.SyncClient;

public class JamSyncApp {

    public static void main(String[] args) {

        System.out.println("[JAMSYNCAPP] Begin");

        try {
              SyncClient testClient = new SyncClient();
              testClient.Run();

         } catch (final Exception e) {
             System.out.println("Error: " + e.toString());
         }

         System.out.println("[JAMSYNCAPP] End!!");

    }

}