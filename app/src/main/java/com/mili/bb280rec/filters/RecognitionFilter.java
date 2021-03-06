package com.mili.bb280rec.filters;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


import com.mili.bb280rec.R;

import org.opencv.features2d.DMatch;

import org.opencv.android.Utils;
import org.opencv.core.*;
//import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
//import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mili on 5/5/16.
 */
public class RecognitionFilter implements Filter {
    private final static String TAG = "RecognitionFilter";
    private AssetManager mAssets;

    private List<String> DESCRIPTOR_FOLDERS = Arrays.asList(
            "sift",
            "surf",
            "orb"
    );

    // The reference imgaes;
    private List<Mat> mReferenceImgages = new ArrayList<>();

    // The reference image (this detector's target).
    private Mat mReferenceImage;

    // Descriptors of the reference image's features.
    private List<Mat> mReferenceDescriptors = new ArrayList<>();

    // Features of the scene (the current frame).
    private final MatOfKeyPoint mSceneKeypoints = new MatOfKeyPoint();
    // Descriptors of the scene's features.
    private final Mat mSceneDescriptor = new Mat();

    // Tentative matches of scene features and reference features.
    private final MatOfDMatch mMatches = new MatOfDMatch();

    // A feature detector, which finds features in images.
    public FeatureDetector mFeatureDetector;
    // A descriptor extractor, which creates descriptors of features.
    public DescriptorExtractor mDescriptorExtractor;
    // A descriptor matcher, which matches features based on their descriptors.
    public DescriptorMatcher mDescriptorMatcher;

    // The colors.
    private final Scalar mGreenColor = new Scalar(0, 255, 0);
    private final Scalar mBlueColor = new Scalar(255, 0, 0);
    private final Scalar mRedColor = new Scalar(0, 0, 255);


    public RecognitionFilter(Context context, int featureMode) throws IOException {
        // Load the reference orb descriptors
        mAssets = context.getAssets();
        String[] descriptorNames;
        try {
            descriptorNames = mAssets.list(DESCRIPTOR_FOLDERS.get(featureMode));
            Log.i(TAG, "Found " + descriptorNames.length + " files");
        } catch (IOException ioe) {
            Log.e(TAG, "Could not list assets", ioe);
            return;
        }

        // Load the reference image from the app's resources.
        // It is loaded in BGR (blue, green, red) format.
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame1, 1));
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame2, 1));
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame3, 1));
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame4, 1));
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame5, 1));
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame6, 1));
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame7, 1));
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame8, 1));
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame9, 1));
        mReferenceImgages.add(Utils.loadResource(context, R.drawable.frame10, 1));
//
//        if (featureMode == 0) {
//            //sift
////            mFeatureDetector = FeatureDetector.create(FeatureDetector.SIFT);
////            mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
////            mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
//
//        } else if (featureMode == 1) {
//            //surf
////            mFeatureDetector = FeatureDetector.create(FeatureDetector.SURF);
////            mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
////            mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
//
//        } else if (featureMode == 2) {
            //orb
            mFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);
            mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
            mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);


//        }
        for (int i = 0; i < 10; i++) {

            MatOfKeyPoint mReferenceKeypoints = new MatOfKeyPoint();
            Mat mReferenceDescriptor = new Mat();
            Mat referenceImageGray = new Mat();
            Imgproc.cvtColor(mReferenceImgages.get(i), referenceImageGray,
                    Imgproc.COLOR_BGR2GRAY);
            mFeatureDetector.detect(referenceImageGray, mReferenceKeypoints);
            mDescriptorExtractor.compute(referenceImageGray, mReferenceKeypoints,
                    mReferenceDescriptor);
            mReferenceDescriptors.add(mReferenceDescriptor);
            Log.d(TAG, String.valueOf(mReferenceDescriptor.rows()));
        }

    }

    @Override
    public int apply(Mat src, Mat dst) {
        Mat mGraySrc = new Mat();
        // Convert the scene to grayscale.
        Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY);

        // Detect the scene features, compute their descriptors,
        // and match the scene descriptors to reference descriptors.
        mFeatureDetector.detect(mGraySrc, mSceneKeypoints);
        mDescriptorExtractor.compute(mGraySrc, mSceneKeypoints,
                mSceneDescriptor);
        Features2d.drawKeypoints(mGraySrc, mSceneKeypoints, dst);

        int matchIndex = -1;
        int matchSize = 0;
        for (int i = 0; i < 10 ; i++) {

            mDescriptorMatcher.match(mSceneDescriptor,
                    mReferenceDescriptors.get(i), mMatches);

            // Calculate the max and min distances between keypoints.
            double maxDist = 0.0;
            double minDist = Double.MAX_VALUE;
            List<DMatch> matchesList =  mMatches.toList();
            for (DMatch match : matchesList) {
                double dist = match.distance;
                if (dist < minDist) {
                    minDist = dist;
                }
                if (dist > maxDist) {
                    maxDist = dist;
                }
            }

            Log.d(TAG, maxDist + " / " + minDist);
            // The thresholds for minDist are chosen subjectively
            // based on testing. The unit is not related to pixel
            // distances; it is related to the number of failed tests
            // for similarity between the matched descriptors.
            if (minDist > 50.0) {
                // The target is completely lost.

                return -1 ;
            }

            // Identify "good" keypoints based on match distance.
            int goodNum = 0;
            double maxGoodMatchDist = 2.0 * minDist;
            for (DMatch match : matchesList) {
                if (match.distance < maxGoodMatchDist) {
                    goodNum ++;
                }
            }

            Log.d(TAG, i + ": " + goodNum);

            if ( goodNum > matchSize) {
                matchIndex = i;
                matchSize = goodNum;
            }

        }



        return matchIndex;
    }

}
