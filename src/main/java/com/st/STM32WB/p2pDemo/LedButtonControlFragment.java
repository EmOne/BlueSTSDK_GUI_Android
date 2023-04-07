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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
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
    private ToggleButton mCurrentSourceEnableToggleButton;
    private EditText mCurrentSourceInputText;
    private RadioGroup mCurrentModeRadioGroup;
    private RadioButton mCurrentModeSingleRadioButton,  mCurrentModeRampRadioButton, mCurrentModeStepRadioButton;
    private ToggleButton mCurrentSinkEnableToggleButton;
    private TextView mCurrentSinkText;
    private ToggleButton mVoltageSinkEnableToggleButton;
    private Switch mVoltageSinkModeSwitch;
    private TextView mVoltageSinkText;
    private ToggleButton mVibrateSinkEnableToggleButton;
    private TextView mVibrateSinkText;
    private ToggleButton mTemperatureSinkEnableToggleButton;
    private TextView mTemperatureSinkText;
    private Switch mTemperatureSinkModeSwitch;
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

            updateGui(() -> {
                final String eventDate = ALARM_FORMATTER.format(new Date(sample.notificationTime));
                int isSelected = FeatureSwitchStatus.isSwitchOn(sample) ? 1 : 0;
                float v = sample.data[2].floatValue();
//TODO: Classify 
                if (mCurrentSinkEnableToggleButton.isChecked())
                {
                    mCurrentSinkText.setText(getString(R.string.stm32wb_currentFormat, v));
                }
                else if (mTemperatureSinkEnableToggleButton.isChecked())
                {
                    mTemperatureSinkText.setText(getString(R.string.stm32wb_temperatureFormat, v));
                }
                else if (mVibrateSinkEnableToggleButton.isChecked())
                {
                    mVibrateSinkText.setText(getString(R.string.stm32wb_vibratingFormat, v));
                }
                else if (mVoltageSinkEnableToggleButton.isChecked())
                {
                    if(mVoltageSinkModeSwitch.isChecked()) {
                        mVoltageSinkText.setText(getString(R.string.stm32wb_voltageratioFormat, v));
                    } else {
                        mVoltageSinkText.setText(getString(R.string.stm32wb_voltageFormat, v));
                    }
                }
                else
                {

                }

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

        mCurrentSourceEnableToggleButton = root.findViewById(R.id.stm32wb_current_sourceImage);
        mCurrentSourceInputText = root.findViewById(R.id.stm32wb_current_sourceText);
        mCurrentSourceInputText.setEnabled(false);
        mCurrentSourceInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = mCurrentSourceInputText.getText().toString();
                int textlength = mCurrentSourceInputText.getText().length();

                if(text.endsWith(" ") || text.equals("0") || text.isEmpty()) {
                    return;
                }

                if(text.startsWith("."))
                {
                    mCurrentSourceInputText.setText("0.");
                    return;
                }

                float f = Float.parseFloat(text);
                if (f > 24.0f) {
                    f = 24.0f;
                    text = String.format("%.2f", f);
                    mCurrentSourceInputText.setText(text);
                }
                mCurrentSourceInputText.setSelection(mCurrentSourceInputText.getText().length());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mCurrentSourceInputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                changeCurrentSourceMode(mCurrentModeRadioGroup.getCheckedRadioButtonId());
                return true;
            }
            return false;
        });

        mCurrentModeRadioGroup = root.findViewById(R.id.radio_current_source_mode);
        mCurrentModeSingleRadioButton = root.findViewById(R.id.radio_set);
        mCurrentModeRampRadioButton = root.findViewById(R.id.radio_ramp);
        mCurrentModeStepRadioButton = root.findViewById(R.id.radio_step);
        mCurrentSourceEnableToggleButton.setOnClickListener(v ->  {
            boolean checked = ((ToggleButton) v).isChecked();
            changeCurrentSourceEnable(checked);
        });
        mCurrentModeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (mCurrentSourceEnableToggleButton.isChecked()) {
                changeCurrentSourceMode(checkedId);
            }
        });

        mCurrentSinkEnableToggleButton = root.findViewById(R.id.stm32wb_current_sinkImage);
        mCurrentSinkText = root.findViewById(R.id.stm32wb_current_sinkText);
        mCurrentSinkText.setEnabled(false);
        mCurrentSinkEnableToggleButton.setOnClickListener(v -> {
            boolean checked = ((ToggleButton) v).isChecked();
            changeCurrentSinkEnable(checked);
        });

        mVoltageSinkEnableToggleButton = root.findViewById(R.id.toggleVoltageButton);
        mVoltageSinkEnableToggleButton.setOnClickListener(v -> {
            boolean checked = ((ToggleButton) v).isChecked();
            changeVoltageSinkEnable(checked);
        });
        mVoltageSinkModeSwitch = root.findViewById(R.id.voltageswitchMode);
        mVoltageSinkModeSwitch.setOnClickListener(v -> {
            boolean checked = ((Switch) v).isChecked();
            changeVoltageMode(checked);
        });
        mVoltageSinkModeSwitch.setEnabled(false);
        mVoltageSinkText = root.findViewById(R.id.textvoltageView);
        mVoltageSinkText.setEnabled(false);

        mVibrateSinkEnableToggleButton = root.findViewById(R.id.toggleVibrateButton);
        mVibrateSinkEnableToggleButton.setOnClickListener(v -> {
            boolean checked = ((ToggleButton) v).isChecked();
            changeVibrateSinkEnable(checked);
        });
        mVibrateSinkText = root.findViewById(R.id.textVibrateView);
        mVibrateSinkText.setEnabled(false);

        mTemperatureSinkEnableToggleButton = root.findViewById(R.id.toggleTemperatureButton);
        mTemperatureSinkEnableToggleButton.setOnClickListener(v -> {
            boolean checked = ((ToggleButton) v).isChecked();
            changeTemperatureSinkEnable(checked);
        });
        mTemperatureSinkText = root.findViewById(R.id.textTemperatureView);
        mTemperatureSinkText.setEnabled(false);
        mTemperatureSinkModeSwitch = root.findViewById(R.id.switchWireNumber);
        mTemperatureSinkModeSwitch.setOnClickListener(v -> {
            boolean checked = ((Switch) v).isChecked();
            if (checked)
            {
                mTemperatureSinkModeSwitch.setText("3 wire mode");
            }
            else
            {
                mTemperatureSinkModeSwitch.setText("2/4 wire mode");
            }
            changeWireMode(checked);
        });
        mTemperatureSinkModeSwitch.setEnabled(false);

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
        if(newState) {
            mLedControlFeature.switchOnLed(mCurrentDevice);
        } else {
            mLedControlFeature.switchOffLed(mCurrentDevice);
        }
    }

    private void changeLedStatusImage(boolean newState){
        if(newState) {
            mLedImage.setImageResource(R.drawable.stm32wb_led_on);
        } else {
            mLedImage.setImageResource(R.drawable.stm32wb_led_off);
        }
    }

    private void changeCurrentSourceEnable(boolean newState)
    {
        if(mLedControlFeature ==null)
            return;

        if (newState){
            mCurrentSourceInputText.setEnabled(true);
            mCurrentModeSingleRadioButton.setEnabled(true);
            mCurrentModeRampRadioButton.setEnabled(true);
            mCurrentModeStepRadioButton.setEnabled(true);
            //TODO: send current source enable currently command -> ref. radio mode selection
            changeCurrentSourceMode(mCurrentModeRadioGroup.getCheckedRadioButtonId());
        } else {
            mCurrentSourceInputText.setEnabled(false);
            mCurrentModeSingleRadioButton.setEnabled(false);
            mCurrentModeRampRadioButton.setEnabled(false);
            mCurrentModeStepRadioButton.setEnabled(false);
            //TODO: send current source disable command
            mLedControlFeature.switchCurrentSourceOff(mCurrentDevice);
        }
    }
    private void changeVoltageMode(boolean mode)
    {
        mLedControlFeature.switchVoltageSinkOn(mCurrentDevice,
                (byte) (mode ? 0x01 : 0x00));
    }
    private void changeWireMode(boolean mode)
    {
        mLedControlFeature.switchTemperatureSinkOn(mCurrentDevice, (byte) (mode ? 0x01 : 0x00));
    }
    private void changeCurrentSourceMode(int index)
    {
        String text = mCurrentSourceInputText.getText().toString();
        if (text.isEmpty())
            text = "4";
        float f = Float.parseFloat(text);
        short b = (short) ((f) * 100.0f);
        byte[] a = { (byte)((b >> 8) & 0xff) , (byte)(b & 0xff)};

        if (mCurrentModeSingleRadioButton.getId() == index){
            mCurrentSourceInputText.setEnabled(true);
             //TODO: send current source single command
            mLedControlFeature.switchCurrentSourceOn(mCurrentDevice, (byte) 0x00, a);
        } else if (mCurrentModeRampRadioButton.getId() == index) {
            mCurrentSourceInputText.setEnabled(false);
            //TODO: send current source ramp command
            mLedControlFeature.switchCurrentSourceOn(mCurrentDevice, (byte) 0x01, a);
        } else if (mCurrentModeStepRadioButton.getId() == index) {
            mCurrentSourceInputText.setEnabled(false);
            //TODO: send current source step command
            mLedControlFeature.switchCurrentSourceOn(mCurrentDevice, (byte) 0x02, a);
        }
    }
    private void changeCurrentSinkEnable(boolean newState)
    {
        if (newState){
            mCurrentSinkText.setEnabled(true);
            //TODO: send sink enable command
            mLedControlFeature.switchCurrentSinkOn(mCurrentDevice);
        } else {
            mCurrentSinkText.setEnabled(false);
            //TODO: send sink disable command
            mLedControlFeature.switchCurrentSinkOff(mCurrentDevice);
        }
    }
    private void changeVoltageSinkEnable(boolean newState)
    {
        if (newState) {
            mVoltageSinkText.setEnabled(true);
            mVoltageSinkModeSwitch.setEnabled(true);
            mLedControlFeature.switchVoltageSinkOn(mCurrentDevice,
                    (byte) (mVoltageSinkModeSwitch.isChecked() ? 0x01 : 0x00));
        } else {
            mVoltageSinkText.setEnabled(false);
            mVoltageSinkModeSwitch.setEnabled(false);
            mLedControlFeature.switchVoltageSinkOff(mCurrentDevice);
        }
    }
    private void changeVibrateSinkEnable(boolean newState)
    {
        if (newState) {
            mVibrateSinkText.setEnabled(true);
            mLedControlFeature.switchVibrateSinkOn(mCurrentDevice);
        } else {
            mVibrateSinkText.setEnabled(false);
            mLedControlFeature.switchVibrateSinkOff(mCurrentDevice);
        }
    }
    private void changeTemperatureSinkEnable(boolean newState)
    {
        if (newState) {
            mTemperatureSinkText.setEnabled(true);
            mTemperatureSinkModeSwitch.setEnabled(true);
            mLedControlFeature.switchTemperatureSinkOn(mCurrentDevice,
                    (byte) (mTemperatureSinkModeSwitch.isChecked() ? 0x01 : 0x00));
        } else {
            mTemperatureSinkText.setEnabled(false);
            mTemperatureSinkModeSwitch.setEnabled(false);
            mLedControlFeature.switchTemperatureSinkOff(mCurrentDevice);
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
}

