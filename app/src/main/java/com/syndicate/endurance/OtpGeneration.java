package com.syndicate.endurance;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OtpGeneration extends AppCompatActivity {
    public static final String NUMBER_EXTRA = "e164number";
    public static final String MASTER_SECRET_EXTRA = "master_secret";
    public static final String GCM_SUPPORTED_EXTRA = "gcm_supported";

    private static final int FOCUSED_COLOR = Color.parseColor("#ff333333");
    private static final int UNFOCUSED_COLOR = Color.parseColor("#ff808080");

    private LinearLayout registrationLayout;
    private LinearLayout verificationFailureLayout;
    private LinearLayout connectivityFailureLayout;
    private RelativeLayout timeoutProgressLayout;

    private ProgressBar registrationProgress;
    private ProgressBar connectingProgress;
    private ProgressBar verificationProgress;
    private ProgressBar generatingKeysProgress;
    private ProgressBar gcmRegistrationProgress;


    private ImageView connectingCheck;
    private ImageView verificationCheck;
    private ImageView generatingKeysCheck;
    private ImageView gcmRegistrationCheck;

    private TextView connectingText;
    private TextView verificationText;
    private TextView registrationTimerText;
    private TextView generatingKeysText;
    private TextView gcmRegistrationText;

    private Button verificationFailureButton;
    private Button connectivityFailureButton;
    private Button callButton;
    private Button verifyButton;

    private EditText codeEditText;

    private MasterSecret masterSecret;
    private boolean gcmSupported;
    private volatile boolean visible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_generation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        initializeResources();
        initializeLinks();
        initializeServiceBinding();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeResources() {
        this.masterSecret = getIntent().getParcelableExtra(MASTER_SECRET_EXTRA);
        this.gcmSupported = getIntent().getBooleanExtra(GCM_SUPPORTED_EXTRA, true);
        this.registrationLayout = (LinearLayout) findViewById(R.id.registering_layout);
        this.verificationFailureLayout = (LinearLayout) findViewById(R.id.verification_failure_layout);
        this.connectivityFailureLayout = (LinearLayout) findViewById(R.id.connectivity_failure_layout);
        this.registrationProgress = (ProgressBar) findViewById(R.id.registration_progress);
        this.connectingProgress = (ProgressBar) findViewById(R.id.connecting_progress);
        this.verificationProgress = (ProgressBar) findViewById(R.id.verification_progress);
        this.generatingKeysProgress = (ProgressBar) findViewById(R.id.generating_keys_progress);
        this.gcmRegistrationProgress = (ProgressBar) findViewById(R.id.gcm_registering_progress);
        this.connectingCheck = (ImageView) findViewById(R.id.connecting_complete);
        this.verificationCheck = (ImageView) findViewById(R.id.verification_complete);
        this.generatingKeysCheck = (ImageView) findViewById(R.id.generating_keys_complete);
        this.gcmRegistrationCheck = (ImageView) findViewById(R.id.gcm_registering_complete);
        this.connectingText = (TextView) findViewById(R.id.connecting_text);
        this.verificationText = (TextView) findViewById(R.id.verification_text);
        this.registrationTimerText = (TextView) findViewById(R.id.registration_timer);
        this.generatingKeysText = (TextView) findViewById(R.id.generating_keys_text);
        this.gcmRegistrationText = (TextView) findViewById(R.id.gcm_registering_text);
        this.verificationFailureButton = (Button) findViewById(R.id.verification_failure_edit_button);
        this.connectivityFailureButton = (Button) findViewById(R.id.connectivity_failure_edit_button);
        this.callButton = (Button) findViewById(R.id.call_button);
        this.verifyButton = (Button) findViewById(R.id.verify_button);
        this.codeEditText = (EditText) findViewById(R.id.telephone_code);
        this.timeoutProgressLayout = (RelativeLayout) findViewById(R.id.timer_progress_layout);
        Button editButton = (Button) findViewById(R.id.edit_button);

        editButton.setOnClickListener(new EditButtonListener());
        this.verificationFailureButton.setOnClickListener(new EditButtonListener());
        this.connectivityFailureButton.setOnClickListener(new EditButtonListener());
    }

    private class EditButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            /*shutdownService();*/

            Intent activityIntent = new Intent(OtpGeneration.this, LoginActivity.class);
            activityIntent.putExtra(OtpGeneration.MASTER_SECRET_EXTRA, masterSecret);
            activityIntent.putExtra(OtpGeneration.GCM_SUPPORTED_EXTRA, gcmSupported);
            startActivity(activityIntent);
            finish();
        }
    }

    private void initializeLinks() {
        TextView        failureText     = (TextView) findViewById(R.id.sms_failed_text);
        String          pretext         = getString(R.string.registration_progress_activity__signal_timed_out_while_waiting_for_a_verification_sms_message);
        String          link            = getString(R.string.RegistrationProblemsActivity_possible_problems);
        SpannableString spannableString = new SpannableString(pretext + " " + link);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                new AlertDialog.Builder(OtpGeneration.this)
                        .setTitle(R.string.RegistrationProblemsActivity_possible_problems)
                        .setView(R.layout.registration_problems)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }, pretext.length() + 1, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        failureText.setText(spannableString);
        failureText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initializeServiceBinding() {
        Intent intent = new Intent(this, RegistrationService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
}