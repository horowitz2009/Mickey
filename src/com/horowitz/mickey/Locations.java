package com.horowitz.mickey;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public class Locations {

  public static int        RAIL1           = 100;
  /**
   * @deprecated
   */
  public static int[]      RAILS2          = { RAIL1, RAIL1 + 8 };
  /**
   * @deprecated
   */
  public static int[]      RAILS4A         = { RAIL1, RAIL1 + 4, RAIL1 + 7, RAIL1 + 11 };                                                            // home
                                                                                                                                                      // and
                                                                                                                                                      // mahatma
                                                                                                                                                      // small
  /**
   * @deprecated
   */
  public static int[]      RAILS4_WOLF     = { RAIL1 - 6, RAIL1 - 2, RAIL1 + 3, RAIL1 + 8 };                                                         // wolfgang
                                                                                                                                                      // +
                                                                                                                                                      // maglev
                                                                                                                                                      // small
  /**
   * @deprecated
   */
  public static int[]      RAILS4_SMIX     = { RAIL1 - 2, RAIL1, RAIL1 + 3, RAIL1 + 8, RAIL1 + 11, RAIL1 + 16, RAIL1 + 21, RAIL1 + 26, RAIL1 + 31 }; // small
                                                                                                                                                      // mix

  /**
   * @deprecated
   */
  public static int[]      RAILS4          = { RAIL1, RAIL1 + 7, RAIL1 + 13, RAIL1 + 20 };                                                           // home
                                                                                                                                                      // and
                                                                                                                                                      // mahatma
                                                                                                                                                      // big
  /**
   * @deprecated
   */
  public static int[]      RAILS4_WOLF_BIG = { RAIL1 + 27, RAIL1 + 37, RAIL1 + 46, RAIL1 + 56 };                                                     // wolfgang
                                                                                                                                                      // +
                                                                                                                                                      // maglev
                                                                                                                                                      // small
  /**
   * @deprecated
   */
  public static int[]      RAILS4_BMIX     = { RAIL1, RAIL1 + 7, RAIL1 + 13, RAIL1 + 20, RAIL1 + 27, RAIL1 + 37, RAIL1 + 46, RAIL1 + 56 };

  /**
   * @deprecated
   */
  public static float      RAIL_Y_OFFSET   = 4f;
  /**
   * @deprecated
   */
  public static int[]      RAILS6OLD       = { RAIL1, RAIL1 + 4, RAIL1 + 8, RAIL1 + 12, RAIL1 + 19, RAIL1 + 23 };
  /**
   * @deprecated
   */
  public static int[]      RAILS6          = { RAIL1 - 6, RAIL1 - 2, RAIL1 + 3, RAIL1 + 8, RAIL1 + 15, RAIL1 + 19 };                                 // wolfgang
                                                                                                                                                      // +
                                                                                                                                                      // maglev
  /**
   * @deprecated
   */
  public static int        STREET2         = 132;
  /**
   * @deprecated
   */
  public static int        STREET2HARBOUR  = 125;
  /**
   * @deprecated
   */
  public static int        STREET4         = 128;
  /**
   * @deprecated
   */
  public static int        STREET6         = 143;
  /**
   * @deprecated
   */
  public static int        STREET6THEME    = 148;

  public static Location   LOC_0MIN        = new Location(" 0 ", 0, new Point(-196, 487), 1);
  public static Location   LOC_6MIN        = new Location(" 6m ", 6, new Point(-103, 267), 1);
  public static Location   LOC_10MIN       = new Location(" 10m ", 10, new Point(121, 267), 1);
  public static Location   LOC_30MIN       = new Location(" 30m ", 30, new Point(341, 267), 1);
  public static Location   LOC_1HOUR       = new Location(" 1h ", 60, new Point(-103, 267 + 169), 1);
  public static Location   LOC_2HOURS      = new Location(" 2h ", 120, new Point(121, 267 + 169), 1);
  public static Location   LOC_3HOURS      = new Location(" 3h ", 180, new Point(341, 267 + 169), 1);

  public static Location   LOC_4HOURS      = new Location(" 4h ", 4 * 60, new Point(-103, 267), 2);
  public static Location   LOC_6HOURS      = new Location(" 6h ", 6 * 60, new Point(121, 267), 2);
  public static Location   LOC_8HOURS      = new Location(" 8h ", 8 * 60, new Point(341, 267), 2);
  public static Location   LOC_10HOURS     = new Location("10h", 10 * 60, new Point(-103, 267 + 169), 2);
  public static Location   LOC_12HOURS     = new Location("12h", 12 * 60, new Point(121, 267 + 169), 2);
  public static Location   LOC_18HOURS     = new Location("18h", 18 * 60, new Point(341, 267 + 169), 2);

  public static Location   LOC_1DAY        = new Location(" 1d ", 24 * 60, new Point(-103, 267), 3);
  public static Location   LOC_2DAYS       = new Location(" 2d ", 48 * 60, new Point(121, 267), 3);
  public static Location   LOC_1WEEK       = new Location(" 1w ", 168 * 60, new Point(341, 267), 3);

  public static Location[] LOC_PAGE1       = new Location[] { LOC_0MIN, LOC_6MIN, LOC_10MIN, LOC_30MIN, };
  public static Location[] LOC_PAGE2       = new Location[] { LOC_1HOUR, LOC_2HOURS, LOC_3HOURS, };
  public static Location[] LOC_PAGE3       = new Location[] { LOC_4HOURS, LOC_6HOURS, LOC_8HOURS, };
  public static Location[] LOC_PAGE4       = new Location[] { LOC_10HOURS, LOC_12HOURS, LOC_18HOURS, };

  /**
   * @deprecated
   */
  public static Dimension  ARROW_LR_SIZE   = new Dimension(65, 33);

  // this is minimum size. DO NOT USE IT DIRECTLY!
  /**
   * @deprecated
   */
  public static Dimension  GAME_SIZE       = new Dimension(775, 585);

  /**
   * @deprecated
   */
  public static Rectangle  LEFTARROW_ZONE  = new Rectangle(0, -RAIL1 - ARROW_LR_SIZE.height * 3, ARROW_LR_SIZE.width + 45, ARROW_LR_SIZE.height * 3);

  /**
   * @deprecated
   */
  public static Rectangle getAbsoluteArea(int x, int y, Rectangle area) {
    Rectangle res = new Rectangle(x + area.x, y + area.y, area.width, area.height);
    return res;
  }

}
