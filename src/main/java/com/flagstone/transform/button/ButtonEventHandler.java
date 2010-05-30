/*
 * ButtonEvent.java
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

package com.flagstone.transform.button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.flagstone.transform.action.Action;
import com.flagstone.transform.action.ActionData;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncodeable;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * ButtonEvent is used to define the actions that a (menu or push) button will
 * execute in response to a particular event. A button responds to:
 *
 * <table class="datasheet">
 * <tr>
 * <td valign="top">RollOver</td>
 * <td>the mouse cursor moves over the active area of a button.</td>
 * </tr>
 * <tr>
 * <td valign="top">RollOut</td>
 * <td>the mouse cursor moves out of the active area of a button.</td>
 * </tr>
 * <tr>
 * <td valign="top">Press</td>
 * <td>the mouse button is clicked while the mouse cursor is over the active
 * area of the button.</td>
 * </tr>
 * <tr>
 * <td valign="top">Release</td>
 * <td>the mouse button is clicked and released while the mouse cursor is over
 * the active area of the button.</td>
 * </tr>
 * <tr>
 * <td valign="top">DragOut</td>
 * <td>the mouse button is clicked and the mouse cursor is dragged out of the
 * active area of the button.</td>
 * </tr>
 * <tr>
 * <td valign="top">DragOver</td>
 * <td>the mouse button is clicked, the mouse cursor is dragged into the active
 * area of the button and the mouse button is released.</td>
 * </tr>
 * </table>
 *
 * <p>
 * When a button is configured as a menu item then three additional events can
 * occur:
 * </p>
 *
 * <table class="datasheet">
 * <tr>
 * <td valign="top">MenuDragOver</td>
 * <td>occurs when the mouse button is clicked and the mouse cursor is dragged
 * into the active area of the menu item.</td>
 * </tr>
 * <tr>
 * <td valign="top">MenuDragOut</td>
 * <td>occurs when the mouse button is clicked and the mouse cursor is dragged
 * out of the active area of the menu item.</td>
 * </tr>
 * <tr>
 * <td valign="top">ReleaseOut</td>
 * <td>occurs when the mouse button is clicked and the mouse cursor is dragged
 * into the active area of the menu item.</td>
 * </tr>
 * </table>
 *
 * <p>
 * In addition to responding to mouse events, buttons also respond to keys being
 * pressed on the keyboard. Keyboard events are defined by the character key
 * being pressed, e.g. "t", "T", "$", etc. The event code for a key is generated
 * using the <b>codeForKey</b> method:
 * </p>
 *
 * <pre>
 * int eventCode = ButtonEvent.codeForKey('J');
 * </pre>
 *
 * <p>
 * For control keys the codes are defined using the following set of constants:
 * </p>
 *
 * <table>
 * <tr>
 * <td>&lt;Left&gt;</td>
 * <td>Left arrow key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Right&gt;</td>
 * <td>Right arrow key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Home&gt;</td>
 * <td>Home key.</td>
 * </tr>
 * <tr>
 * <td>&lt;End&gt;</td>
 * <td>End key</td>
 * </tr>
 * <tr>
 * <td>&lt;Insert&gt;</td>
 * <td>Insert key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Delete&gt;</td>
 * <td>Delete key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Backspace&gt;</td>
 * <td>Backspace key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Enter&gt;</td>
 * <td>Enter (return) key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Up&gt;</td>
 * <td>Up arrow key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Down&gt;</td>
 * <td>Down arrow key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Pageup&gt;</td>
 * <td>Page up key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Pagedown&gt;</td>
 * <td>Page down key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Tab&gt;</td>
 * <td>Tab key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Escape&gt;</td>
 * <td>Escape key.</td>
 * </tr>
 * <tr>
 * <td>&lt;Space&gt;</td>
 * <td>Space bar.</td>
 * </tr>
 * </table>
 *
 * <p>
 * A button can respond in the same way for multiple events by creating a
 * compound event code created by performing a bit-wise Or of the individual
 * codes:
 * </p>
 *
 * <pre>
 * int eventCode = ButtonEvent.RollOver | ButtonEvent.Press;
 * int eventCode = ButtonEvent.Enter | ButtonEvent.PageUp;
 * </pre>
 *
 * <p>
 * While multiple mouse events can be defined for a button only one keyboard
 * event can be defined.
 * </p>
 *
 * @see DefineButton2
 */
//TODO(class)
public final class ButtonEventHandler implements SWFEncodeable {

    /** Number of bits to shift key code for encoding with event flags. */
    private static final int KEY_OFFSET = 9;
    /** Bit mask for key field. */
    private static final int KEY_MASK = 0x0E00;
    /** Bit mask for key field. */
    private static final int EVENT_MASK = 0x01FF;

    /** Format string used in toString() method. */
    private static final String FORMAT = "ButtonEventHandler: { event=%s;"
    		+ " actions=%s }";

    /** The compound code that represents the events the button responds to. */
    private int event;
    /** The keyboard short-cut used to click the button. */
    private int key;
    /** The set of actions executed when the button event occurs. */
    private List<Action> actions;

    /** The length of the object when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a ButtonEventHandler object using values encoded
     * in the Flash binary format.
     *
     * @param size the length, in bytes, of the encoded event handler.
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
    public ButtonEventHandler(final int size, final SWFDecoder coder,
            final Context context) throws IOException {
        final int eventKey = coder.readUnsignedShort();
        event = eventKey & EVENT_MASK;
        key = eventKey & KEY_MASK;
        length = size;

        actions = new ArrayList<Action>();

        final SWFFactory<Action> decoder = context.getRegistry()
                .getActionDecoder();

        if (decoder == null) {
            if (length != 0) {
                actions.add(new ActionData(coder.readBytes(new byte[length])));
            }
        } else {
            coder.mark();
            while (coder.bytesRead() < length) {
                actions.add(decoder.getObject(coder, context));
            }
            coder.unmark();
        }
    }

    /**
     * Creates an uninitialised ButtonEvent object.
     */
    public ButtonEventHandler() {
        actions = new ArrayList<Action>();
    }

    /**
     * Creates and initialises a ButtonEventHandler object using the values
     * copied from another ButtonEventHandler object.
     *
     * @param object
     *            a ButtonEventHandler object from which the values will be
     *            copied.
     */
    public ButtonEventHandler(final ButtonEventHandler object) {
        event = object.event;
        actions = new ArrayList<Action>(object.actions);
    }

    /**
     * Add an action to the end of the actions array.
     *
     * @param anAction
     *            an object derived from the base class Action. Must not be
     *            null.
     * @return this object.
     */
    public ButtonEventHandler add(final Action anAction) {
        if (anAction == null) {
            throw new IllegalArgumentException();
        }
        actions.add(anAction);
        return this;
    }

    /**
     * Get the set of events that this handler responds to.
     *
     * @return the set of handled events.
     */
    public Set<ButtonEvent> getEvent() {
        final Set<ButtonEvent> set = EnumSet.allOf(ButtonEvent.class);

        for (final Iterator<ButtonEvent> iter = set.iterator();
                iter.hasNext();) {
            if ((event & iter.next().getValue()) == 0) {
                iter.remove();
            }
        }
        return set;
    }

    /**
     * Sets the event code that this ButtonEvent defines actions for.
     *
     * @param set
     *            the set of events.
     * @return this object.
     */
    public ButtonEventHandler setEvent(final Set<ButtonEvent> set) {
        for (final ButtonEvent buttonEvent : set) {
            event |= buttonEvent.getValue();
        }
        return this;
    }

    /**
     * Get the array of actions that are executed by the button in response
     * to specified event(s).
     *
     * @return the actions executed by this handler.
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Get the keyboard shortcut that triggers this handler.
     *
     * @return the keyboard shortcut that causes the handler actions to be
     * executed.
     */
    public char getKey() {
        return (char) (key >>> KEY_OFFSET);
    }

    /**
     * Set the keyboard shortcut that triggers this handler.
     *
     * @param buttonKey the keyboard shortcut that causes the handler
     * actions to be executed.
     * @return this object.
     */
    public ButtonEventHandler setKey(final ButtonKey buttonKey) {
        key = buttonKey.getChar() << KEY_OFFSET;
        return this;
    }

    /**
     * Set the keyboard shortcut that triggers this handler.
     *
     * @param character the keyboard character that causes the handler
     * actions to be executed.
     * @return this object.
     */
    public ButtonEventHandler setKey(final char character) {
        key = character << KEY_OFFSET;
        return this;
    }

    /**
     * Sets the array of actions that are executed by the button in response to
     * specified event(s).
     *
     * @param anArray
     *            the array of action objects that will be executed when the
     *            specified event(s) occur. The array may be empty but must not
     *            be null.
     * @return this object.
     */
    public ButtonEventHandler setActions(final List<Action> anArray) {
        if (anArray == null) {
            throw new IllegalArgumentException();
        }
        actions = anArray;
        return this;
    }

    /** {@inheritDoc} */
    public ButtonEventHandler copy() {
        return new ButtonEventHandler(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, event, actions);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 2;

        for (final Action action : actions) {
            length += action.prepareToEncode(context);
        }

        int len = length;
        if (context.contains(Context.LAST)) {
            length = -2;
        }

        return len;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        coder.writeShort(length + 2);
        coder.writeShort(event | key);

        for (final Action action : actions) {
            action.encode(coder, context);
        }
    }
}
