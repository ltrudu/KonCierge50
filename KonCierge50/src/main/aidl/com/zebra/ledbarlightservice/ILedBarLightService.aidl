// ILedBarLightService.aidl
package com.zebra.ledbarlightservice;
// Declare any non-default types here with import statements

 interface ILedBarLightService {
     /**
      * Demonstrates some basic types that you can use as parameters
      * and return values in AIDL.
      */
     void setLight(int ledId, int color);
 }
