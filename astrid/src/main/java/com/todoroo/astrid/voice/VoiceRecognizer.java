/**
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */
package com.todoroo.astrid.voice;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;

import com.todoroo.andlib.utility.AndroidUtilities;
import com.todoroo.astrid.utility.Constants;
import com.todoroo.astrid.voice.RecognizerApi.RecognizerApiListener;

import org.tasks.R;

import java.util.List;

@TargetApi(8)
public class VoiceRecognizer {

    protected RecognizerApi recognizerApi;
    protected VoiceInputAssistant voiceInputAssistant;

    public static boolean speechRecordingAvailable(Context context) {
        return AndroidUtilities.getSdkVersion() >= 8 &&
                SpeechRecognizer.isRecognitionAvailable(context);
    }

    /**
     * Call this to see if your phone supports voiceinput in its current configuration.
     * If this method returns false, it could also mean that Google Voicesearch is simply
     * not installed.
     * If this method returns true, internal use of it enables the registered microphone-button.
     *
     * @return whether this phone supports voiceinput
     */
    public static boolean voiceInputAvailable(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return (activities.size() != 0);
    }

    private VoiceRecognizer() {
        //
    }

    private static VoiceRecognizer instance = null;

    public static VoiceRecognizer instantiateVoiceRecognizer(Activity activity, RecognizerApiListener listener) {
        synchronized(VoiceRecognizer.class) {
            if (instance == null) {
                instance = new VoiceRecognizer();
            }
        }

        if (speechRecordingAvailable(activity)) {
            if (instance.recognizerApi != null) {
                instance.recognizerApi.destroy();
            }

            instance.recognizerApi = new RecognizerApi(activity);
            instance.recognizerApi.setListener(listener);
        } else {
            instance.voiceInputAssistant = new VoiceInputAssistant(activity);
        }
        return instance;
    }

    public void startVoiceRecognition(Context context, Fragment fragment) {
        if (speechRecordingAvailable(context) && recognizerApi != null) {
            recognizerApi.start(Constants.PACKAGE, context.getString(R.string.audio_speak_now));
        } else {
            int prompt = R.string.voice_create_prompt;
            voiceInputAssistant.startVoiceRecognitionActivity(fragment, prompt);
        }
    }

    public void destroyRecognizerApi() {
        if (instance != null && instance.recognizerApi != null) {
            instance.recognizerApi.destroy();
            instance.recognizerApi = null;
        }
    }

    public void cancel() {
        if (instance != null && instance.recognizerApi != null) {
            instance.recognizerApi.cancel();
        }
    }
}
