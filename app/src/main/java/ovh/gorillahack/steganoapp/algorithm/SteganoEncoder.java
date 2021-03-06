package ovh.gorillahack.steganoapp.algorithm;

import android.graphics.Bitmap;
import android.graphics.Color;

import ovh.gorillahack.steganoapp.exceptions.SteganoEncodeException;
import ovh.gorillahack.steganoapp.utils.SteganoUtils;

/**
 * A steganography encoder that uses the LSB technique on picture bitmaps.
 */
public class SteganoEncoder implements EncoderInterface<Bitmap> {

    /**
     * The bitmap that will be used to hide text.
     */
    protected Bitmap pictureBitmap;

    /**
     * Constructs a steganography encoder based on copy from a given bitmap.
     *
     * @param immutableBitmap The bitmap that will be copied
     */
    public SteganoEncoder(Bitmap immutableBitmap) {

        SteganoUtils.checkBitmap(immutableBitmap);
        this.pictureBitmap = immutableBitmap.copy(immutableBitmap.getConfig(), true);
    }

    /**
     * Encode and returns a given string encoded in the picture bitmap.
     *
     * @param text The string to encode in the bitmap
     * @return The encoded bitmap that contains the text
     * @throws SteganoEncodeException If a steganography error occurred in the process
     */
    public Bitmap encode(String text) throws SteganoEncodeException {

        if (text == null || text.isEmpty()) {
            throw new SteganoEncodeException("Message cannot be empty");
        }

        int[] binarySequence = SteganoUtils.getBinarySequence(text);
        int[] marginSequence = SteganoUtils.getMarginSequence(text.length());
        int binaryPtr = 0;
        int marginPtr = 0;

        // For each pixel in the picture
        for (int y = 0; y < this.pictureBitmap.getHeight(); y++) {
            for (int x = 0; x < this.pictureBitmap.getWidth(); x++) {

                if( marginPtr < SteganoUtils.MARGIN_SIZE ) {

                    this.encodeMargin(marginSequence, marginPtr, y, x);
                    marginPtr++;
                }
                else if (binaryPtr >= binarySequence.length) {

                    // Return the new bitmap when all data is encoded
                    return this.pictureBitmap;
                }
                else {

                    // Encode the data and move the bit pointer
                    encodeData(binarySequence, binaryPtr, y, x);
                    binaryPtr++;
                }
            }
        }

        return this.pictureBitmap;
    }

    /**
     * Encode a bit from the given margin sequence using the LSB technique in a specif pixel.
     *
     * @param marginSequence The margin sequence to encode
     * @param marginPtr The current margin pointer
     * @param y The pixel 'Y' coordinate
     * @param x The pixel 'X' coordinate
     */
    private void encodeMargin(int[] marginSequence, int marginPtr, int y, int x) {

        int argbColor = this.pictureBitmap.getPixel(x, y);
        int blueColor = Color.blue(argbColor);

        if (blueColor % 2 == 1) {
            blueColor--;
        }
        blueColor += marginSequence[marginPtr];

        int newArgbColor = Color.argb(Color.alpha(argbColor),
                Color.red(argbColor),
                Color.green(argbColor),
                blueColor);
        this.pictureBitmap.setPixel(x, y, newArgbColor);
    }

    /**
     * Encode a bit from a given binary sequence using the LSB technique in a specific pixel.
     *
     * @param binarySequence The binary sequence to encode
     * @param binaryPtr The current binary data pointer
     * @param y The pixel 'Y' coordinate
     * @param x The pixel 'X' coordinate
     */
    private void encodeData(int[] binarySequence, int binaryPtr, int y, int x) {

        // Extract Least Significant Byte from pixel (blue component)
        int argbColor = this.pictureBitmap.getPixel(x, y);
        int blueColor = Color.blue(argbColor);

        // Make the last bit 0 if needed
        if (blueColor % 2 == 1) {
            blueColor--;
        }

        // There is the true encoding: injecting the data
        blueColor += binarySequence[binaryPtr];

        // Recode the color in ARGB format
        int newArgbColor = Color.argb(Color.alpha(argbColor),
                Color.red(argbColor),
                Color.green(argbColor),
                blueColor);
        this.pictureBitmap.setPixel(x, y, newArgbColor);
    }

}
