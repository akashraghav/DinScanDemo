package com.google.firebase.samples.apps.mlkit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

public class FrameProcessor {

    private FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    private boolean isProcessing;
    private Pattern pattern;
    private TextView tvProcessedText;

    public void runTextRecognition(Bitmap bitmap) {
        isProcessing = true;
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        recognizer.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText texts) {
                                isProcessing = false;
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                isProcessing = false;
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Log.i("text data","No text found");
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    builder.append(elements.get(k).getText()).append(" ");
                }
                builder.append("\n");
            }
            builder.append("\n");
        }
        findDin(builder.toString());
    }

    private void findDin(String block) {
        Log.d("text data", block);
        tvProcessedText.setText(block);
        Matcher matcher = pattern.matcher(block);
        if (matcher.find()) {
            tvProcessedText.setText(block.substring(matcher.start(), matcher.end()));
            isProcessing = true;
            Log.d("text data", "----->" + matcher.group(1));
        }
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public static FrameProcessor getInstance(TextView tvProcessedText) {
        FrameProcessor processor = new FrameProcessor();
        processor.tvProcessedText = tvProcessedText;
        processor.pattern = Pattern.compile("\\s[D][I][N](\\s|:|-|)[0-9]{8}\\s");
        return processor;
    }

}
