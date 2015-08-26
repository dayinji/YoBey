package com.badprinter.yobey.utils;

import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;

/**
 * Created by root on 15-8-13.
 */
public class MyEvalucatorUtil {
    public static class JellyAnim extends IntEvaluator {
        private double amp = 0.06;
        private double freq = 1.5;
        private double decay = 2.0;
        /*The first time reach the endValue
        it must less than duration!
        And It is recommend to be 0.2 ~ 0.5(200ms ~ 500ms)
         */
        private double firstTime = 0.3;
        /*The duration must equal to ValueAnimation.getDuration();
        it must larger than firstTime!
        And It is recommend to be 1.0 ~ 2.0(1000ms ~ 2000ms)
         */
        private double duration;

        public void setAmp(double amp) { this.amp = amp; }
        public double getAmp() { return amp; }

        public void setFreq(double freq) { this.freq = freq; }
        public double getFreq() { return freq; }

        public void setDecay(double decay) { this.decay = decay; }
        public double getDecay() { return decay; }

        public void setFirstTime(int firstTime) { this.firstTime = firstTime/1000.0; }
        public double getFirstTime() { return firstTime*1000; }

        public void setDuration(int time) { this.duration = time/1000.0; }
        public double getDuration() { return duration*1000; }

        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            double value;
            if (duration*fraction >= firstTime) {
                double t = duration*fraction - firstTime;
                double v = (endValue - startValue) / firstTime;
                value = endValue + v*amp*Math.sin(freq*t*2*Math.PI)/Math.exp(decay*t);
            }
            else {
                value = startValue + (endValue - startValue) * fraction * duration / firstTime;
            }

            return (int)value;
        }
    }
    public static class JellyFloatAnim extends FloatEvaluator {
        private double amp = 0.06;
        private double freq = 1.5;
        private double decay = 2.0;
        /*The first time reach the endValue
        it must less than duration!
        And It is recommend to be 0.2 ~ 0.5(200ms ~ 500ms)
         */
        private double firstTime = 0.3;
        /*The duration must equal to ValueAnimation.getDuration();
        it must larger than firstTime!
        And It is recommend to be 1.0 ~ 2.0(1000ms ~ 2000ms)
         */
        private double duration;

        public void setAmp(double amp) { this.amp = amp; }
        public double getAmp() { return amp; }

        public void setFreq(double freq) { this.freq = freq; }
        public double getFreq() { return freq; }

        public void setDecay(double decay) { this.decay = decay; }
        public double getDecay() { return decay; }

        public void setFirstTime(int firstTime) { this.firstTime = firstTime/1000.0; }
        public double getFirstTime() { return firstTime*1000; }

        public void setDuration(int time) { this.duration = time/1000.0; }
        public double getDuration() { return duration*1000; }

        @Override
        public Float evaluate(float fraction, Number startValue, Number endValue) {
            float value;
            if (duration*fraction >= firstTime) {
                double t = duration*fraction - firstTime;
                double v = (endValue.floatValue() - startValue.floatValue()) / firstTime;
                value = (float)(endValue.floatValue() + v*amp*Math.sin(freq*t*2*Math.PI)/Math.exp(decay*t));
            }
            else {
                value = (float)(startValue.floatValue() + (endValue.floatValue() - startValue.floatValue()) * fraction * duration / firstTime);
            }

            return value;
        }
    }

    public static class GravityAnim extends IntEvaluator {

        double g = 2000;
        double duration;
        double decay = 0.5;
        public void setGravity(double g) { this.g = g; }
        public double getGravity() { return g; }

        public void setDuration(double duration) { this.duration = duration/1000; }
        public double getDuration() { return duration*1000; }

        public void setDecay(double decay) { this.decay = decay; }
        public double getDecay() { return decay; }

        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            double t0 = Math.pow(2*Math.abs(endValue - startValue)/g, 1.0/2.0);
            double currentTime = duration*fraction;
            double offset;
            double value;
            int times = 0;
            if (currentTime <= t0) {
                if (startValue < endValue)
                    value = startValue + g*currentTime*currentTime/2;
                else
                    value = startValue - g*currentTime*currentTime/2;
            } else {
                currentTime -= t0;
                t0 *= decay;
                //times <= 18 for preventing an endless loop
                while(currentTime > t0 && times <= 18) {
                    currentTime = currentTime - t0;
                    times++;
                    if (times % 2 == 0)
                        t0 *= decay;
                }
                if (times > 18)
                    offset = 0;
                else if (times % 2 == 0) {
                    offset = t0*t0*g/2 - g*(t0 - currentTime)*(t0 - currentTime)/2;
                } else {
                    offset = t0*t0*g/2 - g*currentTime*currentTime/2;
                }
                if (startValue < endValue)
                    value = endValue - offset;
                else
                    value = endValue + offset;

            }
            return (int)value;
        }
    }
    public static class SLowOutAnim extends IntEvaluator {
        @Override
        public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
            fraction = getSlowFraction(fraction);
            return (int)(startValue + fraction * (endValue - startValue));
        }
        private float getSlowFraction(float fraction) {
            return fraction*fraction*fraction - 3*fraction*fraction + 3*fraction;
        }
    }
}
