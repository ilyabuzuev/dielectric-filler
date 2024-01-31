package filler.utils;

public class GCodeUtils {
   public static final GCodeUtils G_CODE_UTILS = new GCodeUtils();
   private final int movementSpeedFast = 1500;
   private final int movementSpeedSlow = 4600;
   private final double matrialCount = 0.0328D;
   private double gcodeAValue = 0.01D;

   private GCodeUtils() {
   }

   public int getMovement_speed(int z) {
      return z > 2 ? this.movementSpeedFast : this.movementSpeedSlow;
   }

   public double getMaterialCount() {
      return this.matrialCount;
   }

   public double getAAxisValue() {
      return this.gcodeAValue;
   }

   public double getAAxisValue(double add) {
      this.gcodeAValue += add;
      return this.gcodeAValue;
   }

   public void setAAxisValue(double gcodeAValue) {
      this.gcodeAValue = gcodeAValue;
   }

   public void setDefaultAValue() {
      this.gcodeAValue = 0.01D;
   }
}
