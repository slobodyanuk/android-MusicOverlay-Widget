package com.musicoverlaywidget.managers;

/**
 * Created by Serhii Slobodianiuk on 27.02.2017.
 */

public class TouchManager {


   public interface BoundsChecker {

        float stickyLeftSide(float screenWidth);

        float stickyRightSide(float screenWidth);

        float stickyTopSide(float screenHeight);

        float stickyBottomSide(float screenHeight);
    }
}
