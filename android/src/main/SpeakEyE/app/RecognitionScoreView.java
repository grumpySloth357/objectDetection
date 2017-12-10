/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package main.SpeakEyE.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import main.SpeakEyE.app.Classifier.Recognition;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class RecognitionScoreView extends View implements ResultsView {
  private static final float TEXT_SIZE_DIP = 24;
  private List<Recognition> results;
  private final float textSizePx;
  private final Paint fgPaint;
  private final Paint bgPaint;
  private static final String FLAG_FILE = "file:///android_asset/flags.txt";


  public RecognitionScoreView(final Context context, final AttributeSet set) {
    super(context, set);

    textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    fgPaint = new Paint();
    fgPaint.setTextSize(textSizePx);

    bgPaint = new Paint();
    bgPaint.setColor(0xccd8bfd8);
  }

  @Override
  public void setResults(final List<Recognition> results) {
    this.results = results;
    /*String str = "";
    for (Iterator<Recognition> i = results.iterator(); i.hasNext();) {
      Classifier.Recognition item = i.next();
      str += item.getTitle() + " ";
    }
    Output.SetAudio(str);*/
    postInvalidate();
  }

  @Override
  public void onDraw(final Canvas canvas) {
    final int x = 10;
    int y = (int) (fgPaint.getTextSize() * 1.5f);
    Paint boxPaint = new Paint();
    boxPaint.setColor(Color.BLACK);
    canvas.drawPaint(bgPaint);

    if (results != null) {
      HashSet <String> obj_map = new HashSet<String>(20);
      for (final Recognition recog : results) {
        String title = recog.getTitle();
        canvas.drawText(title + ": " + recog.getConfidence(), x, y, fgPaint);
        obj_map.add(title);
        y += fgPaint.getTextSize() * 1.5f;
        /*Get location*/
        RectF location = recog.getLocation();
        System.out.println("VIEW: "+title+":"+location+ ", area: "+recog.getArea());
        //canvas.drawRect(location,boxPaint);
      }

      /*Say stuff*/
      String speak_txt = TextUtils.join(" ", obj_map);
      Output.SetAudio(speak_txt);
    }
  }
}
