/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ocrexample;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;

import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.Text.Element;
import com.google.mlkit.vision.text.Text.Line;
import com.google.mlkit.vision.text.Text.TextBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends GraphicOverlay.Graphic {

    private final Paint rectPaint;
    private final Paint textPaint;
    private final Text text;
    public ColorDetector.ImageData lastestBitmap;

    public TextGraphic(GraphicOverlay overlay, ColorDetector.ImageData lastestBitmap, Text text) {
        super(overlay);
        this.lastestBitmap = lastestBitmap;
        this.text = text;
        rectPaint = new Paint();
        textPaint = new Paint();
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        String txt = "";
        List<TextElementData> cache = new ArrayList<>();
        for (TextBlock textBlock : text.getTextBlocks()) {
            for (Line line : textBlock.getLines()) {
                for (Element element : line.getElements()) {
                    txt = txt + element.getText() + " ";
                    TextElementData td = new TextElementData(element);
                    cache.add(td);
                    canvas.save();

                    canvas.translate(td.centerX, td.centerY);
                    canvas.rotate(element.getAngle() + 90, 0, 0);

                    RectF r2 = new RectF(-td.width / 2 - 20, -td.height / 2 - 20, td.width / 2 + 20, td.height / 2 + 20);
                    rectPaint.setColor(Color.rgb(td.primaryColor[0], td.primaryColor[1], td.primaryColor[2]));
                    canvas.drawRect(r2, rectPaint);
                    canvas.restore();
                }
            }
        }
        String transcribed = PhoneticTranscription.handleText(txt);
        String[] tro = txt.split(" ");
        String[] tra = transcribed.split(" ");
        int i = 0;
        for (TextElementData td : cache) {
            try {
                float w = textPaint.measureText(tra[i]);
                float w2 = textPaint.measureText(tro[i]);

                Paint tp = new TextPaint();
                tp.setColor(Color.rgb(td.secondaryColor[0], td.secondaryColor[1], td.secondaryColor[2]));

                float mw = td.height;

                if (w2 / w < 1) {   // if transcribed text longer than original, reduce font size
                    mw = mw * (w2 / w);
                }

                tp.setTextSize(mw * 0.95f);

                canvas.save();
                canvas.translate(td.centerX, td.centerY);
                canvas.rotate(td.element.getAngle() + 90, 0, 0);
                canvas.drawText(tra[i], -td.width / 2, td.height / 2, tp);
                canvas.restore();
                i++;
            } catch (Exception e) {
                Log.e("TextGraphics", e.getMessage(), e);
            }
        }

    }

    private class TextElementData {
        private final float centerX;
        private final float centerY;
        private final float width;
        private final float height;
        private final int[] primaryColor;
        private final int[] secondaryColor;

        private final Element element;

        public TextElementData(Element e) {
            element = e;
            RectF rect = new RectF(element.getBoundingBox());

            Point[] cp = element.getCornerPoints();
            if (cp != null) {
                width = (float) Math.sqrt(Math.pow(cp[0].x - cp[1].x, 2) + Math.pow(cp[0].y - cp[1].y, 2));
                height = (float) Math.sqrt(Math.pow(cp[0].x - cp[3].x, 2) + Math.pow(cp[0].y - cp[3].y, 2));
            }
            else {   // just in case, but shouldn't occur
                width = rect.width();
                height = rect.height();
            }

            centerX = rect.right - ((rect.right - rect.left) / 2) - 25; // for some reason boundingbox is always shifted

            centerY = rect.bottom - ((rect.bottom - rect.top) / 2);


            if (lastestBitmap != null) {
                Rect orect = element.getBoundingBox();
                int[][] colors = lastestBitmap.getDominantColors(orect);
                primaryColor = colors[0];
                secondaryColor = colors[1];
            } else {
                primaryColor = new int[]{255, 255, 255};
                secondaryColor = new int[]{0, 0, 0};
            }
        }
    }
}
