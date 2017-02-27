package com.musicoverlaywidget.managers;

abstract class BoundsCheckerWithOffset implements TouchManager.BoundsChecker {

        private int offsetX, offsetY;

        public BoundsCheckerWithOffset(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        @Override
        public final float stickyLeftSide(float screenWidth) {
            return stickyLeftSideImpl(screenWidth) + offsetX;
        }

        @Override
        public final float stickyRightSide(float screenWidth) {
            return stickyRightSideImpl(screenWidth) - offsetX;
        }

        @Override
        public final float stickyTopSide(float screenHeight) {
            return stickyTopSideImpl(screenHeight) + offsetY;
        }

        @Override
        public final float stickyBottomSide(float screenHeight) {
            return stickyBottomSideImpl(screenHeight) - offsetY;
        }

        protected abstract float stickyLeftSideImpl(float screenWidth);
        protected abstract float stickyRightSideImpl(float screenWidth);
        protected abstract float stickyTopSideImpl(float screenHeight);
        protected abstract float stickyBottomSideImpl(float screenHeight);
    }