/*
 * DefineJPEGImage4.java
 * Transform
 *
 * Copyright (c) 2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.flagstone.transform.image;

import java.io.IOException;
import java.util.Arrays;

import com.flagstone.transform.SWF;

import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.MovieTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

public final class DefineJPEGImage4 implements ImageTag {

    private static final float SCALE_8 = 256.0f;

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineJPEGImage4: { identifier=%d;"
            + "deblocking=%f; image=%d; alpha=%d }";

    /** The unique identifier for this object. */
    private int identifier;
    private int deblocking;
    /** The JPEG encoded image. */
    private byte[] image;
    /** The zlib compressed transparency values for the image. */
    private byte[] alpha;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;
    /** The width of the image in pixels. */
    private transient int width;
    /** The height of the image in pixels. */
    private transient int height;

    /**
     * Creates and initialises a DefineJPEGImage4 object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public DefineJPEGImage4(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & SWFDecoder.LENGTH_FIELD;
        if (length == SWFDecoder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        final int size = coder.readInt();
        deblocking = coder.readSignedShort();
        image = coder.readBytes(new byte[size]);
        alpha = coder.readBytes(new byte[length - size - 8]);
        decodeInfo();
        coder.unmark(length);
    }

    /**
     * Creates a DefineJPEGImage4 object with the specified deblocking,
     * image data, and alpha channel data.
     *
     * @param uid
     *            the unique identifier for this object. Must be in the range
     *            1..65535.
     * @param level
     *            the level of deblocking used for the image.
     * @param img
     *            the JPEG encoded image data. Must not be null.
     * @param transparency
     *            byte array containing the zlib compressed alpha channel data.
     *            Must not be null.
     */
    public DefineJPEGImage4(final int uid, final float level,
            final byte[] img, final byte[] transparency) {
        setIdentifier(uid);
        setDeblocking(level);
        setImage(img);
        setAlpha(transparency);
    }

    /**
     * Creates and initialises a DefineJPEGImage3 object using the values copied
     * from another DefineJPEGImage3 object.
     *
     * @param object
     *            a DefineJPEGImage3 object from which the values will be
     *            copied.
     */
    public DefineJPEGImage4(final DefineJPEGImage4 object) {
        identifier = object.identifier;
        width = object.width;
        height = object.height;
        image = object.image;
        alpha = object.alpha;
    }

    /** {@inheritDoc} */
    public int getIdentifier() {
        return identifier;
    }

    /** {@inheritDoc} */
    public void setIdentifier(final int uid) {
        if ((uid < SWF.MIN_IDENTIFIER) || (uid > SWF.MAX_IDENTIFIER)) {
            throw new IllegalArgumentRangeException(
                    SWF.MIN_IDENTIFIER, SWF.MAX_IDENTIFIER, uid);
        }
        identifier = uid;
    }

    public float getDeblocking() {
        return deblocking / SCALE_8;
    }

    public void setDeblocking(final float level) {
        deblocking = (int) (level * SCALE_8);
    }

    /**
     * Get the width of the image in pixels (not twips).
     *
     * @return the width of the image.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the image in pixels (not twips).
     *
     * @return the height of the image.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get a copy of the image.
     *
     * @return  a copy of the data.
     */
    public byte[] getImage() {
        return Arrays.copyOf(image, image.length);
    }

    /**
     * Get the alpha channel data.
     *
     * @return a copy of the alpha data.
     */
    public byte[] getAlpha() {
        return Arrays.copyOf(alpha, alpha.length);
    }

    /**
     * Sets the image data.
     *
     * @param bytes
     *            an array of bytes containing the image table. Must not be
     *            null.
     */
    public void setImage(final byte[] bytes) {
        image = Arrays.copyOf(bytes, bytes.length);
        decodeInfo();
    }

    /**
     * Sets the alpha channel data with the zlib compressed data.
     *
     * @param bytes
     *            array of bytes containing zlib encoded alpha channel. Must not
     *            be null.
     */
    public void setAlpha(final byte[] bytes) {
        alpha = Arrays.copyOf(bytes, bytes.length);
    }

    /** {@inheritDoc} */
    public DefineJPEGImage4 copy() {
        return new DefineJPEGImage4(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, identifier, getDeblocking(),
                image.length, alpha.length);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 8;
        length += image.length;
        length += alpha.length;

        return (length > SWFEncoder.STD_LIMIT ? SWFEncoder.EXT_LENGTH
                : SWFEncoder.STD_LENGTH) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        coder.writeHeader(MovieTypes.DEFINE_JPEG_IMAGE_4, length);
        coder.mark();
        coder.writeShort(identifier);
        coder.writeInt(image.length);
        coder.writeShort(deblocking);
        coder.writeBytes(image);
        coder.writeBytes(alpha);
        coder.unmark(length);
    }

    private void decodeInfo() {
        final JPEGInfo info = new JPEGInfo();
        info.decode(image);
        width = info.getWidth();
        height = info.getHeight();
    }
}
