package com.mili.bb280rec.filters;

/**
 * Created by mili on 5/7/16.
 */
import org.opencv.core.Mat;

public interface Filter {
    public abstract int apply(final Mat src, final Mat dst);
}

