package com.appboy.ui.inappmessage.config;

public class AppboyInAppMessageParams {
  public static final double MODALIZED_IMAGE_RADIUS_DP = 9.0;
  public static final double GRAPHIC_MODAL_MAX_WIDTH_DP = 290.0;
  public static final double GRAPHIC_MODAL_MAX_HEIGHT_DP = 290.0;

  private static double sModalizedImageRadiusDp = MODALIZED_IMAGE_RADIUS_DP;
  private static double sGraphicModalMaxWidthDp = GRAPHIC_MODAL_MAX_WIDTH_DP;
  private static double sGraphicModalMaxHeightDp = GRAPHIC_MODAL_MAX_HEIGHT_DP;

  public static double getModalizedImageRadiusDp() {
    return sModalizedImageRadiusDp;
  }

  public static double getGraphicModalMaxWidthDp() {
    return sGraphicModalMaxWidthDp;
  }

  public static double getGraphicModalMaxHeightDp() {
    return sGraphicModalMaxHeightDp;
  }

  public static void setModalizedImageRadiusDp(double modalizedImageRadiusDp) {
    sModalizedImageRadiusDp = modalizedImageRadiusDp;
  }

  public static void setGraphicModalMaxWidthDp(double graphicModalMaxWidthDp) {
    sGraphicModalMaxWidthDp = graphicModalMaxWidthDp;
  }

  public static void setGraphicModalMaxHeightDp(double graphicModalMaxHeightDp) {
    sGraphicModalMaxHeightDp = graphicModalMaxHeightDp;
  }
}