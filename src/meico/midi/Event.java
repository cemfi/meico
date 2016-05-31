package meico.midi;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by aberndt on 08.02.2016.
 */

@Deprecated
public class Event {

    // use these constants for the event type
    public static final short NOTE_OFF                  = 128;
    public static final short NOTE_ON                   = 144;
    public static final short POLY_AFTERTOUCH           = 160;
    public static final short CONTROL_CHANGE            = 176;
    public static final short PROGRAM_CHANGE            = 192;
    public static final short CHANNEL_AFTERTOUCH        = 208;
    public static final short PITCH_BEND                = 224;
    public static final short SYSEX_START               = 240;
    public static final short MIDI_TIME_CODE            = 241;
    public static final short SONG_POSITION_POINTER     = 242;
    public static final short SONG_SELECT               = 243;
    public static final short UNDEF1                    = 244;
    public static final short UNDEF2                    = 245;
    public static final short TUNE_REQUEST              = 246;
    public static final short SYSEX_END                 = 247;
    public static final short TIMING_CLOCK              = 248;
    public static final short UNDEF3                    = 249;
    public static final short START                     = 250;
    public static final short CONTINUE                  = 251;
    public static final short STOP                      = 252;
    public static final short UNDEF4                    = 253;
    public static final short ACTIVE_SENSING            = 254;
    public static final short SYSTEM_RESET              = 255;
    public static final short META_EVENT                = 255;

    // use these constants to set the controller number  of a CONTROL_CHANGE event
    public static final short CC_Bank_Select                = 0;
    public static final short CC_Modulation_Wheel           = 1;
    public static final short CC_Breath_Ctrl                = 2;
    public static final short CC_Undefined_Ctrl_1           = 3;
    public static final short CC_Foot_Ctrl                  = 4;
    public static final short CC_Portamento_Time            = 5;
    public static final short CC_Data_Entry                 = 6;
    public static final short CC_Channel_Volume             = 7;
    public static final short CC_Balance                    = 8;
    public static final short CC_Undefined_Ctrl_2           = 9;
    public static final short CC_Pan                        = 10;
    public static final short CC_Expression_Ctrl            = 11;
    public static final short CC_Effect_Ctrl_1              = 12;
    public static final short CC_Effect_Ctrl_2              = 13;
    public static final short CC_Undefined_Ctrl_3           = 14;
    public static final short CC_Undefined_Ctrl_4           = 15;
    public static final short CC_General_Purpose_Ctrl_1     = 16;
    public static final short CC_General_Purpose_Ctrl_2     = 17;
    public static final short CC_General_Purpose_Ctrl_3     = 18;
    public static final short CC_General_Purpose_Ctrl_4     = 19;
    public static final short CC_Undefined_Ctrl_5           = 20;
    public static final short CC_Undefined_Ctrl_6           = 21;
    public static final short CC_Undefined_Ctrl_7           = 22;
    public static final short CC_Undefined_Ctrl_8           = 23;
    public static final short CC_Undefined_Ctrl_9           = 24;
    public static final short CC_Undefined_Ctrl_10          = 25;
    public static final short CC_Undefined_Ctrl_11          = 26;
    public static final short CC_Undefined_Ctrl_12          = 27;
    public static final short CC_Undefined_Ctrl_13          = 28;
    public static final short CC_Undefined_Ctrl_14          = 29;
    public static final short CC_Undefined_Ctrl_15          = 30;
    public static final short CC_Undefined_Ctrl_16          = 31;
    public static final short CC_Bank_Select_14b            = 32;
    public static final short CC_Modulation_Wheel_14b       = 33;
    public static final short CC_Breath_Ctrl_14b            = 34;
    public static final short CC_Undefined_Ctrl_1_14b       = 35;
    public static final short CC_Foot_Ctrl_14b              = 36;
    public static final short CC_Portamento_Time_14b        = 37;
    public static final short CC_Data_Entry_14b             = 38;
    public static final short CC_Channel_Volume_14b         = 39;
    public static final short CC_Balance_14b                = 40;
    public static final short CC_Undefined_Ctrl_2_14b       = 41;
    public static final short CC_Pan_14b                    = 42;
    public static final short CC_Expression_Ctrl_14b        = 43;
    public static final short CC_Effect_Ctrl_1_14b          = 44;
    public static final short CC_Effect_Ctrl_2_14b          = 45;
    public static final short CC_Undefined_Ctrl_3_14b       = 46;
    public static final short CC_Undefined_Ctrl_4_14b       = 47;
    public static final short CC_General_Purpose_Ctrl_1_14b = 48;
    public static final short CC_General_Purpose_Ctrl_2_14b = 49;
    public static final short CC_General_Purpose_Ctrl_3_14b = 50;
    public static final short CC_General_Purpose_Ctrl_4_14b = 51;
    public static final short CC_Undefined_Ctrl_5_14b       = 52;
    public static final short CC_Undefined_Ctrl_6_14b       = 53;
    public static final short CC_Undefined_Ctrl_7_14b       = 54;
    public static final short CC_Undefined_Ctrl_8_14b       = 55;
    public static final short CC_Undefined_Ctrl_9_14b       = 56;
    public static final short CC_Undefined_Ctrl_10_14b      = 57;
    public static final short CC_Undefined_Ctrl_11_14b      = 58;
    public static final short CC_Undefined_Ctrl_12_14b      = 59;
    public static final short CC_Undefined_Ctrl_13_14b      = 60;
    public static final short CC_Undefined_Ctrl_14_14b      = 61;
    public static final short CC_Undefined_Ctrl_15_14b      = 62;
    public static final short CC_Undefined_Ctrl_16_14b      = 63;
    public static final short CC_Damper_Pedal               = 64;
    public static final short CC_Portamento_OnOff           = 65;
    public static final short CC_Sustenuto                  = 66;
    public static final short CC_Soft_Pedal                 = 67;
    public static final short CC_Legato_Footswitch          = 68;
    public static final short CC_Hold_2                     = 69;
    public static final short CC_Sound_Ctrl_1               = 70;
    public static final short CC_Sound_Ctrl_2               = 71;
    public static final short CC_Sound_Ctrl_3               = 72;
    public static final short CC_Sound_Ctrl_4               = 73;
    public static final short CC_Sound_Ctrl_5               = 74;
    public static final short CC_Sound_Ctrl_6               = 75;
    public static final short CC_Sound_Ctrl_7               = 76;
    public static final short CC_Sound_Ctrl_8               = 77;
    public static final short CC_Sound_Ctrl_9               = 78;
    public static final short CC_Sound_Ctrl_10              = 79;
    public static final short CC_General_Purpose_Ctrl_5     = 80;
    public static final short CC_General_Purpose_Ctrl_6     = 81;
    public static final short CC_General_Purpose_Ctrl_7     = 82;
    public static final short CC_General_Purpose_Ctrl_8     = 83;
    public static final short CC_Portamento_Ctrl            = 84;
    public static final short CC_Undefined_Ctrl_17          = 85;
    public static final short CC_Undefined_Ctrl_18          = 86;
    public static final short CC_Undefined_Ctrl_19          = 87;
    public static final short CC_Undefined_Ctrl_20          = 88;
    public static final short CC_Undefined_Ctrl_21          = 89;
    public static final short CC_Undefined_Ctrl_22          = 90;
    public static final short CC_Reverb_Send_Level          = 91;
    public static final short CC_Effects_2_Depth            = 92;
    public static final short CC_Chorus_Send_Level          = 93;
    public static final short CC_Effects_4_Depth            = 94;
    public static final short CC_Effects_5_Depth            = 95;
    public static final short CC_Data_Entry_plus_1          = 96;
    public static final short CC_Data_Entry_minus_1         = 97;
    public static final short CC_Nonregistered_Param_Num_LSB= 98;
    public static final short CC_Nonregistered_Param_Num_MSB= 99;
    public static final short CC_Registered_Param_Num_LSB   = 100;
    public static final short CC_Registered_Param_Num_MSB   = 101;
    public static final short CC_Undefined_Ctrl_23          = 102;
    public static final short CC_Undefined_Ctrl_24          = 103;
    public static final short CC_Undefined_Ctrl_25          = 104;
    public static final short CC_Undefined_Ctrl_26          = 105;
    public static final short CC_Undefined_Ctrl_27          = 106;
    public static final short CC_Undefined_Ctrl_28          = 107;
    public static final short CC_Undefined_Ctrl_29          = 108;
    public static final short CC_Undefined_Ctrl_30          = 109;
    public static final short CC_Undefined_Ctrl_31          = 110;
    public static final short CC_Undefined_Ctrl_32          = 111;
    public static final short CC_Undefined_Ctrl_33          = 112;
    public static final short CC_Undefined_Ctrl_34          = 113;
    public static final short CC_Undefined_Ctrl_35          = 114;
    public static final short CC_Undefined_Ctrl_36          = 115;
    public static final short CC_Undefined_Ctrl_37          = 116;
    public static final short CC_Undefined_Ctrl_38          = 117;
    public static final short CC_Undefined_Ctrl_39          = 118;
    public static final short CC_Undefined_Ctrl_40          = 119;
    public static final short CC_All_Sound_Off              = 120;
    public static final short CC_Reset_All_Controllers      = 121;
    public static final short CC_Local_Control_OnOff        = 122;
    public static final short CC_All_Notes_Off              = 123;
    public static final short CC_Omni_Mode_Off              = 124;
    public static final short CC_Omni_Mode_On               = 125;
    public static final short CC_Poly_Mode_OnOff            = 126;
    public static final short CC_Poly_Mode_On               = 127;

    // if the event type is meta event, use these constants to indicate the metaevent type (??????????????? wohin damit ??????????????????)
    public static final short META_Sequence_Number          = 0x00;
    public static final short META_Text_Event               = 0x01;
    public static final short META_Copyright_Notice         = 0x02;
    public static final short META_Track_Name               = 0x03;
    public static final short META_Sequence_Name            = 0x03;
    public static final short META_Instrument_Name          = 0x04;
    public static final short META_Lyric                    = 0x05;
    public static final short META_Marker                   = 0x06;
    public static final short META_Cue_Point                = 0x07;
    public static final short META_Midi_Channel_Prefix      = 0x20;
    public static final short META_End_of_Track             = 0x2F;
    public static final short META_Set_Tempo                = 0x51;
    public static final short META_SMTPE_Offset             = 0x54;
    public static final short META_Time_Signature           = 0x58;
    public static final short META_Key_Signature            = 0x59;
    public static final short META_Sequence_specific_Meta_event = 0x7F;

    private BigInteger date;
    private short type;
    private short channel;
    private List<Short> data;

    /**
     * constructor used for channel voice and channel mode messages
     * @param type This conctructor accepts only type values 128, 144, 160, 176, 192, 208, and 224
     * @param date
     * @param channel
     */
    public Event(short type, BigInteger date, short channel) {
        this.channelVoiceModeConstructorSatus(type, date, channel);
    }

    /**
     * this constructor includes the two data bytes of the midi message
     * @param type
     * @param date
     * @param channel
     * @param firstDataByte e.g. the pitch of a note event and the controller number of a control change event
     * @param secondDataByte e.g. the velocity of a note event and the controller value of a control change event
     */
    public Event(short type, BigInteger date, short channel, short firstDataByte, short secondDataByte) {
        this.channelVoiceModeConstructorSatus(type, date, channel);
        this.setFirstDataByte(firstDataByte);
        this.setSecondDataByte(secondDataByte);
    }

    /**
     * this constructor includes the first data byte of the midi message
     * @param type
     * @param date
     * @param channel
     * @param firstDataByte e.g. the pitch of a note event and the controller number of a control change event
     */
    public Event(short type, BigInteger date, short channel, short firstDataByte) {
        this.channelVoiceModeConstructorSatus(type, date, channel);
        this.setFirstDataByte(firstDataByte);
    }

    /**
     * just a helper to avoid writing multiple copies of the same procedure in the constructors
     * @param type
     * @param date
     * @param channel
     */
    private void channelVoiceModeConstructorSatus(short type, BigInteger date, short channel) {
        if ((type != 128) && (type != 144) && (type != 160) && (type != 176) && (type != 192) && (type != 208) && (type != 224))
            throw new IllegalArgumentException("Constructor Event(int type, long date, int channel) accepts only the following type values:\nNOTE_OFF (128), NOTE_ON (144), POLY_PRESSURE (160), CONTROL_CHANGE (176), PROGRAM_CHANGE (192), CHANNEL_PRESSURE (208), PITCH_BEND (224).");

        this.type = type;
        this.date = date;
        this.channel = (channel < 16) ? channel : 15;   // channel values greater than 15 are cut
    }

        /**
         * constructor used for system common and system realtime messages (valuse in [240, 255])
         * @param type
         * @param date
         */
    public Event(short type, BigInteger date) {
        if ((type < 240) || (type > 255))
            throw new IllegalArgumentException("Constructor Event(Byte type, long date) accepts only the following type values:\nSYSEX_START (240), MIDI_TIME_CODE (241), SONG_POSITION_POINTER (242), SONG_SELECT (243), UNDEF1 (244), UNDEF2 (245), TUNE_REQUEST (246), SYSEX_END (247), TIMING_CLOCK (248), UNDEF3 (249), START (250), CONTINUE (251), STOP (252), UNDEF4 (253), ACTIVE_SENSING (254), SYSTEM_RESET (255).");

        this.type = type;
        this.channel = 0;
        this.date = date;
    }

    /**
     * This setter should be used in conjunction with the Event(short type, BigInteger date) constructor, just like in the following way:
     *      Event meta = new Event(Event.META_EVENT, date);
     *      setMetaEventType(Event.META_Sequence_Number);
     */
    public void setMetaEventType(short metaEvType){
        if (this.type != META_EVENT)
            throw new IllegalArgumentException("Illegal call of setMetaEventType() for a non-META_EVENT.");

        switch (metaEvType) {                                       // create the data byte array
            case META_Sequence_Number:
                this.setData(new short[]{metaEvType, 0x02, 0, 0});  // 00 02 ss ss (ss ss is the 16 bit sequence number)
                break;
            case META_Text_Event:
            case META_Copyright_Notice:
            case META_Sequence_Name:                                // equals META_Track_Name
            case META_Instrument_Name:
            case META_Lyric:
            case META_Marker:
            case META_Cue_Point:
                this.setData(new short[]{metaEvType});              // user has to specify the length byte and succeeding ascii bytes
                break;
            case META_Midi_Channel_Prefix:
                this.setData(new short[]{metaEvType, 0x01, 0});     // 20 01 cc (cc is the midi channel (00-0F) that all succeeding meta and sysex events are associated with), default is 0
                break;
            case META_End_of_Track:
                this.setData(new short[]{metaEvType, 0x00});        // the End of TrackOld event has no optional data byte, it's all fixed
                break;
            case META_Set_Tempo:
                this.setData(new short[]{metaEvType, 0x03, 0x50, 0x00, 0x00});  // 51 03 tt tt tt (tt tt tt sets the tempo in microseconds per quarter note, 50 00 00 is 120 bpm), default is 120 bpm
                break;
            case META_SMTPE_Offset:
                this.setData(new short[]{metaEvType, 0x05, 0, 0, 0, 0, 0});     // 54 05 hh mm ss fr ff (hh=hours, mm =minutes, ss=seconds, fr=frames, ff=fractional frame), default is no offset
                break;
            case META_Time_Signature:
                this.setData(new short[]{metaEvType, 0x04, 4, 2, 24, 8});       // 58 04 nn dd cc bb (nn=numerator, dd=denominator expressed as a power o 2 (4 corresponds to 2^2, hence, 2), cc=midi clocks per metronome tick, bb=number of 1/32 notes per 24 midi clocks), default is 4/4
                break;
            case META_Key_Signature:
                this.setData(new short[]{metaEvType, 0x02, 0, 0});  // 59 02 sf mi (sf=number of sharps/flats [-7, 7], mi=major (0) or minor (1)), default is C major
                break;
            case META_Sequence_specific_Meta_event:
                this.setData(new short[]{metaEvType});              // user has to specify length, id and data byte
                break;
            default:
                this.setFirstDataByte(metaEvType);
        }
    }

    /**
     * A setter to edit the event type. When setting a system common or system realtime message, the channel is set to 0 as these event types have no channel! When setting a channel voice or channel mode message, the channel is left unchanged.
     * @param type
     */
    public void setType(short type) {
        this.type = type;
        if ((type >= 240) || (type <= 255)) {   // When setting a system common or system realtime message
            this.channel = 0;                   // the channel is set to 0 as these event types have no channel!
        }
    }

    /**
     * A setter to edit the channel of the event. If the event holds a system common or system realtime message, the channel remains unchanged as these message types have no channel attribute.
     * @param channel
     */
    public void setChannel(short channel) {
        if ((type >= 240) || (type <= 255)) {               // if the event type is a system common or system realtime message
            return;                                         // these messages have no channel attribute, hence, the channel has to be left 0
        }
        this.channel = (channel < 16) ? channel : 15;       // channel values greater than 15 are cut
    }

    /**
     * A setter to edit the date of the event.
     * @param date
     */
    public void setDate(BigInteger date) {
        this.date = date;
    }

    /**
     * A getter that returns the event type.
     * @return
     */
    public short getType() {
        return this.type;
    }

    /**
     * A getter that returns the channel of the event. If the event holds a system common or system realtime message, the return value is 0 by default.
     * @return
     */
    public short getChannel() {
        return this.channel;
    }

    /**
     * A getter that returns the event date.
     * @return
     */
    public BigInteger getDate() {
        return this.date;
    }

    /**
     * input the data bytes via an array
     * @param data
     */
    public void setData(short[] data) {
        this.data.clear();
        for (int i = 0; i < data.length; ++i)
            this.data.add(data[i]);
    }

    /**
     * this getter returns the complete list of data bytes as an array
     * @return
     */
    public short[] getData() {
        short[] data = new short[this.data.size()];
        for (int i = 0; i < this.data.size(); ++i)
            data[i] = this.data.get(i);
        return data;
    }

    /**
     * a setter method to edit the value of the first data byte; if there is no first data byte, yet, then one is created
     * @param value
     */
    public void setFirstDataByte(short value) {
        try {
            data.set(0, value);                     // set the pitch data byte (1st data byte)
        } catch (IndexOutOfBoundsException e) {     // if there is no 1st data byte, yet
            data.add(value);                        // add it to the list
        }
    }
    /**
     * a setter method to edit the pitch value (first data byte) of the event; if there is no first data byte, yet, then one is created
     * @param pitch
     */
    public void setPitch(short pitch) {
        this.setFirstDataByte(pitch);
    }

    /**
     * a setter method to edit the controller number of a control change message
     * @param ctrl
     */
    public void setControllerNumber(short ctrl) {
        this.setFirstDataByte(ctrl);
    }


    /**
     * a getter that returns the 1st data byte or -1 if there are no data bytes
     * @return
     */
    public short getFirstDataByte() {
        try {
            return data.get(0);
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    /**
     * same as getFirstDataByte()
     * @return
     */
    public short getPitch() {
        return this.getFirstDataByte();
    }

    /**
     * a setter method to edit the value of the second data byte; if there is no second data byte, yet, then create it
     * @param value
     */
    public void setSecondDataByte(short value) {
        try {
            data.set(1, value);                     // set the data byte
        } catch (IndexOutOfBoundsException e) {     // if no data bytes present so far
            if (data.size() == 0)                   // if there is even no 1st data byte
                data.add((short) 100);              // create it with a default value
            data.add(value);                        // add the data byte
        }
    }
    /**
     * a setter method to edit the velocity value (2nd data byte) of the event; if there is no 2nd data byte, yet, it is created
     * @param velocity
     */
    public void setVelocity(short velocity) {
        this.setSecondDataByte(velocity);
    }

    /**
     * a setter to edit the value of a control change event
     * @param value
     */
    public void setControllerValue(short value) {
        this.setSecondDataByte(value);
    }

    /**
     * a getter that returns the 2nd data byte or -1 if there is no 2nd data byte
     * @return
     */
    public short getSecondDataByte() {
        try {
            return data.get(1);
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    /**
     * same as getSecondDataByte()
     * @return
     */
    public short getVelocity() {
        return this.getSecondDataByte();
    }

    /**
     * this method outputs the status byte of the midi message
     * @return
     */
    private byte generateStatusByte() {
        return (byte) (this.type + this.channel);    // TODO: Is this correct? The byte data type goes from -128 to 127. The values I have here go from 0 to 255. I may need to output something different here.
    }
}
