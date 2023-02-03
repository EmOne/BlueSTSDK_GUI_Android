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
package com.st.STM32WB.p2pDemo;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.NavUtils;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.NodeConnectionService;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.STM32WB.p2pDemo.feature.FeatureControlLed;
import com.st.STM32WB.p2pDemo.feature.FeatureProtocolRadioReboot;
import com.st.STM32WB.p2pDemo.feature.FeatureSwitchStatus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@DemoDescriptionAnnotation(name="Led Control",
        requareAll = {FeatureSwitchStatus.class,FeatureControlLed.class})
public class LedButtonControlFragment extends RssiDemoFragment {

    private static final String DEVICE_ID_KEY = LedButtonControlFragment.class.getName()+".DEVICE_ID_KEY";
    private static final String LED_STATUS_KEY = LedButtonControlFragment.class.getName()+".LED_STATUS_KEY";
    private static final String ALARM_STATUS_KEY = LedButtonControlFragment.class.getName()+".ALARM_STATUS_KEY";
    private static final String ALARM_TEXT_KEY = LedButtonControlFragment.class.getName()+".ALARM_TEXT_KEY";
    private static final SimpleDateFormat ALARM_FORMATTER = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private TextView mDeviceName;
    private TextView mInstructionText;
    private ImageView mLedImage;

    private boolean mLedStatus=false;

    private Group mAlarmViewGroup;
    private Group mLedViewGroup;
    private TextView mAlarmText;
    private ImageView mAlarmImage;


    private RadioGroup mCurrentModeRadioGroup;

    private RadioButton mCurrentModeSingleRadioButton,  mCurrentModeRampRadioButton, mCurrentModeStepRadioButton;

    private EditText mCurrentSourceInputText;
    private ToggleButton mCurrentSourceEnableToggleButton;
    private ToggleButton mCurrentSinkEnableToggleButton;

    private FeatureSwitchStatus mButtonFeature;
    private FeatureControlLed mLedControlFeature;

    private Peer2PeerDemoConfiguration.DeviceID mCurrentDevice;


    //we cant initialize the listener here because we need to wait that the fragment is attached
    // to an activity
    private Feature.FeatureListener mButtonListener = new  Feature.FeatureListener () {

        @Override
        public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
            if(mCurrentDevice==null){ //first time
                mCurrentDevice = FeatureSwitchStatus.getDeviceSelection(sample);
                updateGui(()-> showDeviceDetected(mCurrentDevice));
            }

            final String eventDate = ALARM_FORMATTER.format(new Date(sample.notificationTime));
            int isSelected = FeatureSwitchStatus.isSwitchOn(sample) ? 1 : 0;
            updateGui(() -> {
                mAlarmText.setText(getString(R.string.stm32wb_single_eventFormat,eventDate, isSelected));
                animateAlarmColor();
            });
        }//on update
    };

    private void animateAlarmColor(){
        int initialColor = getResources().getColor(R.color.colorAccent);
        int finalColor = getResources().getColor(R.color.colorGrey);
        int duration = getResources().getInteger(R.integer.stm32wb_single_alarmBlinkDuration);
        ValueAnimator colorAnimation;
        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), initialColor,finalColor);
        colorAnimation.setDuration(duration);
        colorAnimation.addUpdateListener(animator ->
                mAlarmImage.setColorFilter((int) animator.getAnimatedValue())
        );
        colorAnimation.start();
    }

    public LedButtonControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getRssiLabelId() {
        return R.id.stm32wb_single_rssiText;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stm32wb_led_single_control, container, false);

        mLedImage = root.findViewById(R.id.stm32wb_single_ledImage);

        mLedImage.setOnClickListener(v -> {
            if(mCurrentDevice!=null && mLedControlFeature !=null){
                mLedStatus = !mLedStatus;
                changeLedStatusImage(mLedStatus);
                changeRemoteLedStatus(mLedStatus);
            }
        });
        mInstructionText = root.findViewById(R.id.stm32wb_single_instruction);
        mLedViewGroup = root.findViewById(R.id.stm32wb_single_ledView);
        mAlarmViewGroup = root.findViewById(R.id.stm32wb_single_alarmView);
        mAlarmImage = root.findViewById(R.id.stm32wb_single_alarmImage);
        mAlarmText = root.findViewById(R.id.stm32wb_single_alarmText);
        mDeviceName = root.findViewById(R.id.stm32wb_single_titleText);
        mAlarmText.setText(getResources().getString(R.string.stm32wb_single_alarm_caption));
        mAlarmImage.setColorFilter(getResources().getColor(R.color.colorGrey));

        mCurrentModeRadioGroup = root.findViewById(R.id.radio_current_source_mode);
        mCurrentModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
//                    case R.id.radio_set:
//                        break;
//                    case R.id.radio_ramp:
//                        break;
//                    case R.id.radio_step:
//                        break;
                }
            }
        });
        mCurrentModeRadioGroup.check(R.id.radio_set);

        mCurrentSourceEnableToggleButton = root.findViewById(R.id.stm32wb_current_sourceImage);
        mCurrentSourceEnableToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((ToggleButton) v).isChecked();
            }
        });
        mCurrentSinkEnableToggleButton = root.findViewById(R.id.stm32wb_current_sinkImage);
        mCurrentSinkEnableToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((ToggleButton) v).isChecked();
            }
        });

        if(savedInstanceState!=null &&
                savedInstanceState.containsKey(DEVICE_ID_KEY)){
            Peer2PeerDemoConfiguration.DeviceID device = (Peer2PeerDemoConfiguration.DeviceID)
                    savedInstanceState.getSerializable(DEVICE_ID_KEY);
            if(device!=null)
                showDeviceDetected(device);

            mLedStatus = savedInstanceState.getBoolean(LED_STATUS_KEY,false);
            changeLedStatusImage(mLedStatus);
        }

       return root;
    }

    private void changeRemoteLedStatus(boolean newState){
        if(mLedControlFeature ==null)
            return;
        if(newState){
            mLedControlFeature.switchOnLed(mCurrentDevice);
        }else{
            mLedControlFeature.switchOffLed(mCurrentDevice);
        }
    }

    private void changeLedStatusImage(boolean newState){
        if(newState){
            mLedImage.setImageResource(R.drawable.stm32wb_led_on);
        }else{
            mLedImage.setImageResource(R.drawable.stm32wb_led_off);
        }
    }

    private static final int ENABLE_REBOOT_THREAD_ADVERTISE_MASK = 0x00004000;

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Node node = getNode();
        if(node!=null && (node.getProtocolVersion()==1)) {
            if ((node.getAdvertiseBitMask() & ENABLE_REBOOT_THREAD_ADVERTISE_MASK) != 0) {
                inflater.inflate(R.menu.stm32wb_radio_reboot, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.stm32wb_single_switchRadio){
            switchProtocolRadio();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchProtocolRadio() {
        Node node = getNode();
        if(node!=null) {
            FeatureProtocolRadioReboot reboot = node.getFeature(FeatureProtocolRadioReboot.class);
            if(reboot!=null) {
                reboot.rebootToNewProtocolRadio(mCurrentDevice, () -> {
                    //disconnect and close the demo
                    NodeConnectionService.disconnect(requireContext(), node);
                    NavUtils.navigateUpFromSameTask(requireActivity());
                });
            }
        }
    }

    private void showDeviceDetected(@NonNull Peer2PeerDemoConfiguration.DeviceID currentDevice){
        mCurrentDevice = currentDevice;
        mDeviceName.setText(getString(R.string.stm32wb_deviceNameFormat,currentDevice.getId()));
        mLedViewGroup.setVisibility(View.VISIBLE);
        mInstructionText.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mCurrentDevice!=null){
            outState.putSerializable(DEVICE_ID_KEY,mCurrentDevice);
            outState.putBoolean(LED_STATUS_KEY,mLedStatus);
            outState.putInt(ALARM_STATUS_KEY,mAlarmViewGroup.getVisibility());
            outState.putString(ALARM_TEXT_KEY,mAlarmText.getText().toString());
        }
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        super.enableNeededNotification(node);
        mButtonFeature = node.getFeature( FeatureSwitchStatus.class);
        mLedControlFeature = node.getFeature(FeatureControlLed.class);

        mButtonFeature = node.getFeature(FeatureSwitchStatus.class);
        if (mButtonFeature != null) {
            mButtonFeature.addFeatureListener(mButtonListener);
            node.enableNotification(mButtonFeature);
            node.readFeature(mButtonFeature);
        }

        mCurrentDevice = Peer2PeerDemoConfiguration.DeviceID.fromBoardId(node.getTypeId());
        if(mCurrentDevice!=null){
            showDeviceDetected(mCurrentDevice);
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        super.disableNeedNotification(node);
        if(mButtonFeature!=null){
            mButtonFeature.removeFeatureListener(mButtonListener);
            node.disableNotification(mButtonFeature);
        }
    }

//    public void onRadioButtonClicked(View view) {
//        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();
//
//        // Check which radio button was clicked
////        switch(view.getId()) {
////            case R.id.radio_set:
////                if (checked)
////                    // Pirates are the best
////                    break;
////            case R.id.radio_ramp:
////                if (checked)
////                    // Ninjas rule
////                    break;
////            case R.id.radio_step:
////                if (checked)
////                    // Ninjas rule
////                    break;
////        }
//    }
}

