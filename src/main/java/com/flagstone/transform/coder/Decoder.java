/*
 * Decoder.java
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

package com.flagstone.transform.coder;

import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;

/**
 * Decoder provides a set of method for decoding data that is not byte-ordered,
 * specifically bit fields and strings.
 */
public class Decoder extends Coder {
    /**
     * Creates a Decoder object initialised with the data to be decoded.
     *
     * @param data
     *            an array of bytes to be decoded.
     */
    public Decoder(final byte[] bytes) {
        super();
        data = new byte[bytes.length];
        index = 0;
        offset = 0;
        pointer = 0;
        end = bytes.length << 3;
        data = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Read a bit field.
     *
     * @param numberOfBits
     *            the number of bits to read.
     *
     * @param signed
     *            indicates whether the integer value read is signed.
     *
     * @return the value read.
     */
    public final int readBits(final int numberOfBits, final boolean signed) {
        int value = 0;

        pointer = (index << 3) + offset;

        if (numberOfBits > 0) {

            for (int i = BITS_PER_INT; (i > 0)
                    && (index < data.length); i -= 8) {
                value |= (data[index++] & UNSIGNED_BYTE_MASK) << (i - 8);
            }

            value <<= offset;

            if (signed) {
                value >>= BITS_PER_INT - numberOfBits;
            } else {
                value >>>= BITS_PER_INT - numberOfBits;
            }

            pointer += numberOfBits;
            index = pointer >>> BITS_TO_BYTES;
            offset = pointer & 7;
        }

        return value;
    }

    /**
     * Read an unsigned byte.
     *
     * @return an 8-bit unsigned value.
     */
    public final int readByte() {
        return data[index++] & UNSIGNED_BYTE_MASK;
    }

    /**
     * Reads an array of bytes.
     *
     * @param bytes
     *            the array that will contain the bytes read.
     *
     * @return the array of bytes.
     */
    public final byte[] readBytes(final byte[] bytes) {
        System.arraycopy(data, index, bytes, 0, bytes.length);
        index += bytes.length;
        return bytes;
    }

    /**
     * Read a string using the default character set defined in the decoder.
     *
     * @param numberOfBytes
     *            the number of bytes to read.
     *
     * @return the decoded string.
     */
    public final String readString(final int numberOfBytes) {
        try {
            return new String(readBytes(new byte[numberOfBytes]), 0,
                    numberOfBytes, encoding);
        } catch (final java.io.UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(encoding); //NOPMD
        }
    }

    /**
     * Read a null-terminated string using the default character set defined in
     * the decoder.
     *
     * @return the decoded string.
     */
    public final String readString() {
        String value;

        int mark = index;
        int length = 0;

        while (data[mark++] != 0) {
            length += 1;
        }

        value = readString(length);
        index++;

        return value;
    }

    /**
     * Searches for a bit pattern, returning true and advancing the pointer to
     * the location if a match was found. If the bit pattern cannot be found
     * then the method returns false and the position of the internal pointer is
     * not changed.
     *
     * @param value
     *            an integer containing the bit patter to search for.
     * @param numberOfBits
     *            least significant n bits in the value to search for.
     * @param step
     *            the increment in bits to add to the internal pointer as the
     *            buffer is searched.
     *
     * @return true if the pattern was found, false otherwise.
     */
    public final boolean findBits(final int value, final int numberOfBits,
            final int step) {
        boolean found;
        final int mark = getPointer();

        while (getPointer() + numberOfBits <= end) {
            if (readBits(numberOfBits, false) == value) {
                adjustPointer(-numberOfBits);
                break;
            }
            adjustPointer(step - numberOfBits);
        }

        if (getPointer() + numberOfBits > end) {
            found = false;
            setPointer(mark);
        } else {
            found = true;
        }

        return found;
    }

}
