/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.STM32WB.p2pDemo.feature;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.STM32WB.p2pDemo.Peer2PeerDemoConfiguration;


/**
 * Write feature used to switch on and off the board led
 */
public class FeatureControlLed extends Feature {
    public static final String FEATURE_NAME = "ControlLed";

    private static final byte TEMPERATURE_ON_COMMAND  = 0x0B;
    private static final byte TEMPERATURE_OFF_COMMAND  = 0x0A;
    private static final byte VIBRATE_ON_COMMAND  = 0x09;
    private static final byte VIBRATE_OFF_COMMAND  = 0x08;
    private static final byte VOLTAGE_ON_COMMAND  = 0x07;
    private static final byte VOLTAGE_OFF_COMMAND  = 0x06;
    private static final byte SOURCE_ON_COMMAND  = 0x05;
    private static final byte SOURCE_OFF_COMMAND  = 0x04;
    private static final byte SINK_ON_COMMAND  = 0x03;
    private static final byte SINK_OFF_COMMAND  = 0x02;
    private static final byte SWITCH_ON_COMMAND  = 0x01;
    private static final byte SWITCH_OFF_COMMAND = 0x00;

    /**
     * build a carry position feature
     * @param n node that will send data to this feature
     */
    public FeatureControlLed(Node n) {
        super(FEATURE_NAME, n, new Field[0]);
    }//FeatureControlLed

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        return new ExtractResult(new Sample(timestamp,new Number[0],getFieldsDesc()),0);
    }

    /**
     *
     * @param device device where switch on the led
     */
    public void switchOffLed(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(),SWITCH_OFF_COMMAND});
    }
    public void switchOnLed(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(),SWITCH_ON_COMMAND});
    }
    public void switchCurrentSinkOff(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(),SINK_OFF_COMMAND});
    }
    public void switchCurrentSinkOn(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(),SINK_ON_COMMAND});
    }
    public void switchCurrentSourceOff(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(), SOURCE_OFF_COMMAND});
    }
    public void switchCurrentSourceOn(Peer2PeerDemoConfiguration.DeviceID device, byte mode, byte[] val){
        writeData(new byte[]{device.getId(), SOURCE_ON_COMMAND, mode, val[0], val[1]});
    }
    public void switchVoltageSinkOff(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(), VOLTAGE_OFF_COMMAND});
    }
    public void switchVoltageSinkOn(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(), VOLTAGE_ON_COMMAND});
    }
    public void switchVibrateSinkOff(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(), VIBRATE_OFF_COMMAND});
    }
    public void switchVibrateSinkOn(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(), VIBRATE_ON_COMMAND});
    }
    public void switchTemperatureSinkOff(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(), TEMPERATURE_OFF_COMMAND});
    }
    public void switchTemperatureSinkOn(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(), TEMPERATURE_ON_COMMAND});
    }
}
