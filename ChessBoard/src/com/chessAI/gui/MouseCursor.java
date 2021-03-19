package com.chessAI.gui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MouseCursor {

    /*
    @param size the size of the frame (horizontal/vertical)
     * <br>
     * <b>Note</b>: maximal size is 32 pixel.
     * @param frameThickness the thickness of the frame
     * @param frameColor the color of the frame
     * @return a cursor which is a frame with the given size and color.
            */

    public static synchronized Cursor createTransparentCursor(int size, int frameThickness, Color frameColor ) {

        final int cursourSize = size + (2 * frameThickness);
        System.out.println("cursourSize: "+cursourSize);

        final BufferedImage bufferedImage = new BufferedImage( 32 + 2, 32 + 2, BufferedImage.TYPE_INT_ARGB );
        final Graphics graphic = bufferedImage.getGraphics();
        final Color colTrans = new Color( 0, 0, 0, 0 );
        for( int i = 0 ; i < cursourSize ; i++ ){
            for( int j = 0 ; j < cursourSize ; j++ ){
                if( i <= frameThickness || i > cursourSize - frameThickness -1 || j <= frameThickness
                        | j > cursourSize - frameThickness - 1 ){
                    graphic.setColor( frameColor );
                }
                else{
                    graphic.setColor( colTrans );
                }
                graphic.fillRect( i, j, 1, 1 );
            }
        }
        System.out.println("Buffered size:" +bufferedImage.getHeight() +"/"+ bufferedImage.getWidth());
        final Point hotSpot = new Point( cursourSize / 2, cursourSize / 2 );
        return Toolkit.getDefaultToolkit().createCustomCursor( bufferedImage, hotSpot, "Trans" );
    }
}
