/*
 * DefineText2.java
 * Transform
 *
 * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
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

package com.flagstone.transform.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.DefineTag;
import com.flagstone.transform.SWF;

import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.MovieTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.datatype.CoordTransform;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * DefineText2 defines one or more lines of transparent text.
 *
 * <p>
 * It extends the functionality provided by the DefineText class by encoding the
 * alpha channel of the colour objects used to set the text color.
 * </p>
 *
 * <p>
 * DefineText2 class acts as a container for the text. The bounding rectangle
 * and transform controls how the text is laid out. Each TextSpan object
 * specifies an offset from the left and bottom edges of the bounding rectangle,
 * allowing successive lines of text to be arranged as a block or paragraph. The
 * coordinate transform can be used to control the size and orientation of the
 * text when it is displayed.
 * </p>
 *
 * @see TextSpan
 * @see DefineText
 */
//TODO(class)
public final class DefineText2 implements DefineTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineText2: { identifier=%d;"
    		+ " bounds=%s; transform=%s; objects=%s }";

    /** The unique identifier for this object. */
    private int identifier;
    private Bounds bounds;
    private CoordTransform transform;
    private List<TextSpan> objects;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;
    private transient int glyphBits;
    private transient int advanceBits;

    /**
     * Creates and initialises a DefineText2 object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @param context
     *            a Context object used to manage the decoders for different
     *            type of object and to pass information on how objects are
     *            decoded.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    // TODO(optimise)
    public DefineText2(final SWFDecoder coder, final Context context)
            throws IOException {
        length = coder.readUnsignedShort() & SWFDecoder.LENGTH_FIELD;
        if (length == SWFDecoder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        bounds = new Bounds(coder);

        /*
         * This code is used to get round a bug in Flash - sometimes 16, 8-bit
         * zeroes are written out before the transform. The root cause in Flash
         * is unknown but seems to be related to the bounds not being set - all
         * values are zero.
         */
        if (coder.count(0) == 16) {
            coder.skip(16);
        }

        transform = new CoordTransform(coder);

        glyphBits = coder.readByte();
        advanceBits = coder.readByte();

        context.put(Context.TRANSPARENT, 1);
        context.put(Context.GLYPH_SIZE, glyphBits);
        context.put(Context.ADVANCE_SIZE, advanceBits);

        objects = new ArrayList<TextSpan>();

        while (coder.scanByte() != 0) {
            objects.add(new TextSpan(coder, context));
        }

        coder.readByte();

        context.remove(Context.TRANSPARENT);
        context.put(Context.GLYPH_SIZE, 0);
        context.put(Context.ADVANCE_SIZE, 0);
        coder.unmark(length);
    }

    /**
     * Creates a DefineText2 object with the specified bounding rectangle,
     * coordinate transform and text records.
     *
     * @param uid
     *            the unique identifier for this object. Must be in the range
     *            1..65535
     * @param box
     *            the bounding rectangle enclosing the text. Must not be null.
     * @param aTransform
     *            an CoordTransform to change the size and orientation of the
     *            text. Must not be null.
     * @param spans
     *            an array of TextSpan objects that define the text to be
     *            displayed. Must not be null.
     */
    public DefineText2(final int uid, final Bounds box,
            final CoordTransform aTransform, final List<TextSpan> spans) {
        setIdentifier(uid);
        setBounds(box);
        setTransform(aTransform);
        setObjects(spans);
    }

    /**
     * Creates and initialises a DefineText2 object using the values copied
     * from another DefineText2 object.
     *
     * @param object
     *            a DefineText2 object from which the values will be
     *            copied.
     */
    public DefineText2(final DefineText2 object) {
        identifier = object.identifier;
        bounds = object.bounds;
        transform = object.transform;
        objects = new ArrayList<TextSpan>(object.objects.size());
        for (final TextSpan span : object.objects) {
            objects.add(span.copy());
        }
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

    /**
     * Get the width of the text block in twips.
     *
     * @return the width of the text.
     */
    public int getWidth() {
        return bounds.getWidth();
    }

    /**
     * Get the height of the text block in twips.
     *
     * @return the height of the text.
     */
    public int getHeight() {
        return bounds.getHeight();
    }

    /**
     * Add a TextSpan object to the array of text spans.
     *
     * @param obj
     *            an TextSpan object. Must not be null.
     * @return this object.
     */
    public DefineText2 add(final TextSpan obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        objects.add(obj);
        return this;
    }

    /**
     * Returns the bounding rectangle that completely encloses the text to be
     * displayed.
     *
     * @return the bounding rectangle of the text.
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Get the coordinate transform that controls the size, location and
     * orientation of the text when it is displayed.
     *
     * @return the coordinate transform used to position the text.
     */
    public CoordTransform getTransform() {
        return transform;
    }

    /**
     * Get the array of text spans that define the text to be displayed.
     *
     * @return the list of text blocks.
     */
    public List<TextSpan> getObjects() {
        return objects;
    }

    /**
     * Sets the bounding rectangle that encloses the text being displayed.
     *
     * @param aBounds
     *            the bounding rectangle enclosing the text. Must not be null.
     */
    public void setBounds(final Bounds aBounds) {
        if (aBounds == null) {
            throw new IllegalArgumentException();
        }
        bounds = aBounds;
    }

    /**
     * Sets the coordinate transform that changes the orientation and size of
     * the text displayed.
     *
     * @param aTransform
     *            an CoordTransform to change the size and orientation of the
     *            text. Must not be null.
     */
    public void setTransform(final CoordTransform aTransform) {
        if (aTransform == null) {
            throw new IllegalArgumentException();
        }
        transform = aTransform;
    }

    /**
     * Sets the array of text spans that define the text to be displayed.
     *
     * @param array
     *            an array of TextSpan objects that define the text to be
     *            displayed. Must not be null.
     */
    public void setObjects(final List<TextSpan> array) {
        if (array == null) {
            throw new IllegalArgumentException();
        }
        objects = array;
    }

    /** {@inheritDoc} */
    public DefineText2 copy() {
        return new DefineText2(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, bounds, transform, objects);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        glyphBits = calculateSizeForGlyphs();
        advanceBits = calculateSizeForAdvances();

        context.put(Context.TRANSPARENT, 1);
        context.put(Context.GLYPH_SIZE, glyphBits);
        context.put(Context.ADVANCE_SIZE, advanceBits);

        length = 2 + bounds.prepareToEncode(context);
        length += transform.prepareToEncode(context);
        length += 2;

        for (final TextSpan span : objects) {
            length += span.prepareToEncode(context);
        }

        length += 1;

        context.remove(Context.TRANSPARENT);
        context.put(Context.GLYPH_SIZE, 0);
        context.put(Context.ADVANCE_SIZE, 0);

        return (length > SWFEncoder.STD_LIMIT ? SWFEncoder.EXT_LENGTH
                : SWFEncoder.STD_LENGTH) + length;
    }

    // TODO(optimise)
    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        coder.writeHeader(MovieTypes.DEFINE_TEXT_2, length);
        coder.mark();
        coder.writeShort(identifier);
        context.put(Context.TRANSPARENT, 1);
        context.put(Context.GLYPH_SIZE, glyphBits);
        context.put(Context.ADVANCE_SIZE, advanceBits);

        bounds.encode(coder, context);
        transform.encode(coder, context);

        coder.writeByte(glyphBits);
        coder.writeByte(advanceBits);

        for (final TextSpan span : objects) {
            span.encode(coder, context);
        }

        coder.writeByte(0);

        context.remove(Context.TRANSPARENT);
        context.put(Context.GLYPH_SIZE, 0);
        context.put(Context.ADVANCE_SIZE, 0);
        coder.unmark(length);
    }

    private int calculateSizeForGlyphs() {
        int total = 0;
        int size;

        for (final TextSpan span : objects) {
            size = span.glyphBits();

            if (size > total) {
                total = size;
            }
        }

        return total;
    }

    private int calculateSizeForAdvances() {
        int total = 0;
        int size;

        for (final TextSpan span : objects) {
            size = span.advanceBits();

            if (size > total) {
                total = size;
            }
        }

        return total;
    }
}