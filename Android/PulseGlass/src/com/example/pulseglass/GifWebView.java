package com.example.pulseglass;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.SystemClock;
import android.view.View;

public class GifWebView extends View {
    private Movie mMovie;
    InputStream mStream;
    long mMoviestart;

    public GifWebView(Context context, InputStream stream) {
        super(context);
        mStream = stream;
        mMovie = Movie.decodeStream(mStream);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        super.onDraw(canvas);
        final long now = SystemClock.uptimeMillis();

        if (mMoviestart == 0) {
            mMoviestart = now;
        }
        final int relTime = (int) ((now - mMoviestart) % mMovie.duration());
        mMovie.setTime(relTime);
        mMovie.draw(canvas, 10, 10);
        this.invalidate();
    }
}