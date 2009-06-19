package com.flagstone.transform.coder;

/**
 * The FLVFactory interface is used to generate objects when decoding a video.
 */
public interface FLVFactory<T> {

    /** TODO(method). */
    FLVFactory<T> copy();

    /** TODO(method). */
    T getObject(final FLVDecoder coder) throws CoderException;
}